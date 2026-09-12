package io.github.libxposed.api;

/** Exact callback descriptors from fixed API100 and official API102. Never package. */
public interface XposedModuleInterface {
    interface ModuleLoadedParam { boolean isSystemServer(); String getProcessName(); }
    interface PackageLoadedParam {
        String getPackageName();
        android.content.pm.ApplicationInfo getApplicationInfo();
        boolean isFirstPackage();
        ClassLoader getDefaultClassLoader();
        ClassLoader getClassLoader();
    }
    interface PackageReadyParam extends PackageLoadedParam { ClassLoader getClassLoader(); }
    interface SystemServerLoadedParam { ClassLoader getClassLoader(); }
    interface SystemServerStartingParam { ClassLoader getClassLoader(); }
    interface HotReloadingParam { android.os.Bundle getExtras(); void setSavedInstanceState(Object state); }
    interface HotReloadedParam extends ModuleLoadedParam {
        android.os.Bundle getExtras(); Object getSavedInstanceState();
        java.util.List<?> getOldHookHandles();
    }
    default void onModuleLoaded(ModuleLoadedParam p) {}
    default void onPackageLoaded(PackageLoadedParam p) {}
    default void onPackageReady(PackageReadyParam p) {}
    default void onSystemServerLoaded(SystemServerLoadedParam p) {}
    default void onSystemServerStarting(SystemServerStartingParam p) {}
    default boolean onHotReloading(HotReloadingParam p) { return false; }
    default void onHotReloaded(HotReloadedParam p) {}
}
