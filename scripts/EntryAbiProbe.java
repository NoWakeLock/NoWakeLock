import android.content.SharedPreferences;
import dalvik.system.PathClassLoader;
import java.lang.reflect.*;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

/** Standalone ART fixture. It never attaches to a live framework or installs system hooks.
 * Args: legacy|102 framework.dex module.apk
 * The fixture acts as a minimal framework, including its reserved attachment operation.
 */
public final class EntryAbiProbe {
    static final String PREFIX = "io.github.libxposed.api.";
    static final String ENTRY = "com.js.nowakelock.xposedhook.entry.UniversalXposedEntry";
    static final Set<Object> listeners = new HashSet<>();
    static ClassLoader framework;
    static Object api;
    static Class<?> lifecycle;
    static String apk;

    static Object proxy(Class<?> type, Map<String, Object> values) {
        return Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, (p, m, a) -> {
            if (m.getName().equals("hashCode")) return System.identityHashCode(p);
            if (m.getName().equals("equals")) return p == a[0];
            if (values.containsKey(m.getName())) return values.get(m.getName());
            if (m.getReturnType() == boolean.class) return false;
            if (m.getReturnType() == int.class) return 0;
            if (m.getReturnType() == long.class) return 0L;
            return null;
        });
    }

    static Object parameter(String name) throws Exception {
        return proxy(framework.loadClass(PREFIX + "XposedModuleInterface$" + name),
                Map.of("getProcessName", "nwl.abi.probe", "getPackageName", "nwl.abi.probe"));
    }

    static Object invoke(Object entry, String callback, String param, Object value) throws Exception {
        return lifecycle.getMethod(callback, framework.loadClass(PREFIX + "XposedModuleInterface$" + param)).invoke(entry, value);
    }

    static Object modernEntry() throws Exception {
        var loader = new PathClassLoader(apk, framework);
        var instance = loader.loadClass(ENTRY).getConstructor().newInstance();
        framework.loadClass(PREFIX + "XposedInterfaceWrapper").getMethod("attachFramework",
                framework.loadClass(PREFIX + "XposedInterface"), Runnable.class).invoke(instance, api, (Runnable) () -> {});
        return instance;
    }

    static Object handover(Object old, List<WeakReference<ClassLoader>> retired) throws Exception {
        var state = new AtomicReference<Object>();
        Class<?> outgoing = framework.loadClass(PREFIX + "XposedModuleInterface$HotReloadingParam");
        Object param = Proxy.newProxyInstance(framework, new Class<?>[]{outgoing}, (p, m, a) -> {
            if (m.getName().equals("setSavedInstanceState")) state.set(a[0]);
            return null;
        });
        if (!Boolean.TRUE.equals(invoke(old, "onHotReloading", "HotReloadingParam", param)))
            throw new AssertionError("Retirement refused in isolated fixture");
        if (!listeners.isEmpty()) throw new AssertionError("Old listener retained");
        if (workers() != 0) throw new AssertionError("Old statistics worker retained");
        retired.add(new WeakReference<>(old.getClass().getClassLoader()));
        Object next = modernEntry();
        Object incoming = proxy(framework.loadClass(PREFIX + "XposedModuleInterface$HotReloadedParam"),
                Map.of("getProcessName", "nwl.abi.probe", "getSavedInstanceState", state.get(), "getOldHookHandles", Collections.emptyList()));
        invoke(next, "onHotReloaded", "HotReloadedParam", incoming);
        if (listeners.size() != 1) throw new AssertionError("Expected exactly one new listener: " + listeners.size());
        if (workers() != 1) throw new AssertionError("Expected exactly one new worker: " + workers());
        return next;
    }

    static long workers() {
        return Thread.getAllStackTraces().keySet().stream().filter(t -> t.isAlive() && t.getName().equals("NWL-statistics")).count();
    }

    public static void main(String[] args) throws Exception {
        framework = new PathClassLoader(args[1], ClassLoader.getSystemClassLoader());
        apk = args[2];
        lifecycle = framework.loadClass(PREFIX + "XposedModuleInterface");
        var apiType = framework.loadClass(PREFIX + "XposedInterface");
        var values = new HashMap<String, Object>();
        values.put("getFrameworkName", "LSPosed");
        values.put("getFrameworkVersion", "1.11.0");
        Object prefs = Proxy.newProxyInstance(SharedPreferences.class.getClassLoader(), new Class<?>[]{SharedPreferences.class}, (p, m, a) -> {
            switch (m.getName()) {
                case "getAll": return new HashMap<>(Map.of("__nwl_revision", 1L, "debug", false));
                case "registerOnSharedPreferenceChangeListener": listeners.add(a[0]); return null;
                case "unregisterOnSharedPreferenceChangeListener": listeners.remove(a[0]); return null;
                default: return null;
            }
        });
        values.put("getRemotePreferences", prefs);
        if (args[0].equals("102")) values.put("getFrameworkProperties", apiType.getField("PROP_CAP_REMOTE").getLong(null));
        api = proxy(apiType, values);
        if (args[0].equals("legacy")) {
            try {
                framework.loadClass(PREFIX + "XposedModuleInterface$HotReloadingParam");
                throw new AssertionError("Fixture is not an old ABI");
            } catch (ClassNotFoundException expected) {}
            var loader = new PathClassLoader(apk, framework);
            Class<?> loadedParam = framework.loadClass(PREFIX + "XposedModuleInterface$ModuleLoadedParam");
            Object entry = loader.loadClass(ENTRY).getConstructor(apiType, loadedParam).newInstance(api, parameter("ModuleLoadedParam"));
            invoke(entry, "onPackageLoaded", "PackageLoadedParam", parameter("PackageLoadedParam"));
            var delegate = entry.getClass().getDeclaredField("delegate");
            delegate.setAccessible(true);
            if (!delegate.get(entry).getClass().getName().endsWith("OldModernEntryAdapter")) throw new AssertionError("Wrong legacy dispatch");
            System.out.println("PASS legacy constructor and package callback on ART without API102 callback types");
        } else {
            Object entry = modernEntry();
            invoke(entry, "onModuleLoaded", "ModuleLoadedParam", parameter("ModuleLoadedParam"));
            var retired = new ArrayList<WeakReference<ClassLoader>>();
            entry = handover(entry, retired);
            entry = handover(entry, retired);
            for (int i = 0; i < 20 && retired.stream().anyMatch(r -> r.get() != null); i++) {
                System.gc(); System.runFinalization(); Thread.sleep(50);
            }
            long retained = retired.stream().filter(r -> r.get() != null).count();
            if (retained != 0) throw new AssertionError("Old module classloaders retained: " + retained);
            System.out.println("PASS API102 A-B-C lifecycle, neutral state, one listener/worker, retired classloaders collected (isolated fixture)");
        }
    }
}
