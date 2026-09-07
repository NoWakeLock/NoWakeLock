package com.js.nowakelock.xposedhook.entry;

/** Android/Java-only boundary so selecting an entry does not link the other hook API. */
public interface EntryDelegate {
    void moduleLoaded(String processName);
    void systemServer(ClassLoader loader);
    void packageLoaded(String packageName, ClassLoader loader);
    void packageReady(String packageName, ClassLoader loader);
}
