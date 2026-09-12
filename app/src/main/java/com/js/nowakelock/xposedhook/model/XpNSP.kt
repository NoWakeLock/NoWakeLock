package com.js.nowakelock.xposedhook.model

import android.content.SharedPreferences
import com.js.nowakelock.data.config.ConfigBackendStatus
import com.js.nowakelock.data.config.XposedConfigKeys
import com.js.nowakelock.data.db.Type

class XpNSP(initialReader: HookConfigReader = EmptyHookConfigReader()) {
    fun decisionView(): XpNSP {
        val captured = reader.capture()
        return (captured as? ImmutableRuleReader)?.decision ?: XpNSP(captured)
    }
    @Volatile var api102SystemRuntime = false
    fun disposeReader() { (reader as? SharedPreferencesHookConfigReader)?.dispose() }
    fun resumeReader() { (reader as? SharedPreferencesHookConfigReader)?.resume() }
    fun acceptPushed(values: Map<String, *>): Long {
        check(api102SystemRuntime) { "API102 system runtime unavailable" }
        return (reader as? SharedPreferencesHookConfigReader
            ?: error("Remote reader unavailable")).accept(values)
    }

    @Volatile
    private var reader: HookConfigReader = initialReader

    companion object {

        @Volatile
        private var instance: XpNSP? = null

        fun getInstance() = instance ?: synchronized(this) {
            instance ?: XpNSP().also {
                instance = it
            }
        }

        @Synchronized fun installRemotePreferences(
            preferences: SharedPreferences,
            frameworkName: String?,
            frameworkVersion: String?
        ) {
            if (getInstance().reader is SharedPreferencesHookConfigReader) return
            val head = if (RuntimeTransfer.modern) {
                RuntimeTransfer.state().getOrPut("ruleHead") { java.util.concurrent.atomic.AtomicReference<Array<Any>>() }
            } else java.util.concurrent.atomic.AtomicReference<Array<Any>>()
            @Suppress("UNCHECKED_CAST")
            getInstance().reader = SharedPreferencesHookConfigReader(
                preferences,
                frameworkName,
                frameworkVersion,
                head as java.util.concurrent.atomic.AtomicReference<Array<Any>>
            )
        }

        fun restoreRules() {
            val frame = RuntimeTransfer.get<java.util.concurrent.atomic.AtomicReference<Array<Any>>>("ruleHead").get()
                ?: error("No rule snapshot to restore")
            installReader(ImmutableRuleReader.fromPrepared(frame, ConfigBackendStatus(
                remoteReadable = true, activeBackend = ConfigBackendStatus.BACKEND_REMOTE)))
        }

        fun installReader(reader: HookConfigReader) {
            getInstance().reader = reader
        }
    }

    fun flag(name: String, packageName: String, type: Type, userId: Int): Boolean {
//        XpUtil.log("${name}_${type}_${packageName}_${userId}_flag, flag:${getBool("${name}_${type}_${packageName}_${userId}_flag")}")
        return getBool(XposedConfigKeys.flag(name, packageName, type, userId))
    }
    fun flagLock(name: String, packageName: String, type: Type, userId: Int): Boolean {
//        XpUtil.log("${name}_${type}_${packageName}_${userId}_flag, flag:${getBool("${name}_${type}_${packageName}_${userId}_flag")}")
        return getBool(XposedConfigKeys.flagLock(name, packageName, type, userId))
    }

    fun aTI(
        now: Long, lastActive: Long,
        name: String, packageName: String, type: Type, userId: Int
    ): Boolean {

//        XpUtil.log("${name}_${type}_${packageName}_${userId}_aTI, ati:${getLong("${name}_${type}_${packageName}_${userId}_aTI")}")

        val ati = getLong(XposedConfigKeys.allowTimeInterval(name, packageName, type, userId))

        return (now - lastActive) < ati
    }

    fun rE(name: String, packageName: String, type: Type, userId: Int): Boolean {

//        XpUtil.log(
//            "${type}_${packageName}_${userId}_rE, " +
//                    "re:${getSet("${type}_${packageName}_${userId}_rE")}"
//        )

        return reader.matches(XposedConfigKeys.regex(type, packageName, userId), name)
    }

    fun getDebug(): Boolean {
        return getBool(XposedConfigKeys.DEBUG)
    }


    private fun getBool(key: String, defValue: Boolean = false): Boolean {
        return reader.getBoolean(key, defValue)
    }

    private fun getLong(key: String, defValue: Long = 0): Long {
        return reader.getLong(key, defValue)
    }

    private fun getSet(key: String): Set<String> {
        return reader.getStringSet(key)
    }

    fun reFresh() {
        reader.refresh()
    }

    fun backendStatus(): ConfigBackendStatus {
        return reader.status()
    }
}
