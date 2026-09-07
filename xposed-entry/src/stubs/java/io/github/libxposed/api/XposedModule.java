package io.github.libxposed.api;
/** Union of the two constructor ABIs. Never package this class. */
public abstract class XposedModule implements XposedInterface, XposedModuleInterface {
    public XposedModule() {}
    public XposedModule(XposedInterface base, ModuleLoadedParam param) {}
}
