package com.js.nowakelock.xposedhook.entry;

import io.github.libxposed.api.XposedInterface;
import io.github.libxposed.api.XposedModule;

/** Only the selected adapter is resolved. Framework attachment belongs to the framework. */
public final class UniversalXposedEntry extends XposedModule {
    private EntryDelegate delegate;
    private boolean oldRuntime;

    public UniversalXposedEntry() { super(); }

    public UniversalXposedEntry(XposedInterface base, ModuleLoadedParam param) {
        super(base, param);
        oldRuntime = true;
        delegate = create("com.js.nowakelock.xposedhook.OldModernEntryAdapter", base);
        delegate.moduleLoaded(param.getProcessName());
    }

    @Override public void onModuleLoaded(ModuleLoadedParam param) {
        if (!oldRuntime) {
            delegate = create("com.js.nowakelock.xposedhook.ModernXposedModule", this);
            delegate.moduleLoaded(param.getProcessName());
        }
    }

    @Override public void onPackageLoaded(PackageLoadedParam param) {
        if (delegate == null) return;
        ClassLoader loader = oldRuntime ? param.getClassLoader() : param.getDefaultClassLoader();
        delegate.packageLoaded(param.getPackageName(), loader);
    }

    @Override public void onPackageReady(PackageReadyParam param) {
        if (!oldRuntime && delegate != null)
            delegate.packageReady(param.getPackageName(), param.getClassLoader());
    }

    @Override public void onSystemServerLoaded(SystemServerLoadedParam param) {
        if (oldRuntime && delegate != null) delegate.systemServer(param.getClassLoader());
    }

    @Override public void onSystemServerStarting(SystemServerStartingParam param) {
        if (!oldRuntime && delegate != null) delegate.systemServer(param.getClassLoader());
    }

    @Override public boolean onHotReloading(HotReloadingParam param) {
        return !oldRuntime && delegate != null && delegate.hotReloading(param);
    }

    @Override public void onHotReloaded(HotReloadedParam param) {
        if (oldRuntime) throw new IllegalStateException("Legacy generation cannot hot reload");
        delegate = create("com.js.nowakelock.xposedhook.ModernXposedModule", this);
        delegate.hotReloaded(param);
    }

    private static EntryDelegate create(String name, Object framework) {
        try {
            return (EntryDelegate) Class.forName(name, true, UniversalXposedEntry.class.getClassLoader())
                    .getConstructor(Object.class).newInstance(framework);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Unable to initialize " + name, e);
        }
    }
}
