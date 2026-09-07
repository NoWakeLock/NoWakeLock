package com.js.nowakelock.data.config

import com.js.nowakelock.base.SPTools

class LegacySharedPreferencesBackend : XposedConfigBackend {
    override val name: String = ConfigBackendStatus.BACKEND_LEGACY
    override val isReadable: Boolean
        get() = SPTools.isAvailable()

    override fun publish(snapshot: ConfigSnapshot): Boolean = SPTools.publish(snapshot)

    override fun putBoolean(key: String, value: Boolean): Boolean {
        return SPTools.setBoolean(key, value)
    }

    override fun putLong(key: String, value: Long): Boolean {
        return SPTools.setLong(key, value)
    }

    override fun putStringSet(key: String, value: Set<String>): Boolean {
        return SPTools.setSet(key, value)
    }
}
