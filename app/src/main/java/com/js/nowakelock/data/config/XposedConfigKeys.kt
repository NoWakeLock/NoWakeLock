package com.js.nowakelock.data.config

import com.js.nowakelock.data.db.Type
import com.js.nowakelock.data.db.entity.AppSt
import com.js.nowakelock.data.db.entity.St

object XposedConfigKeys {
    const val GROUP_NAME = "Nowakelock"
    const val DEBUG = "debug"
    const val REVISION = "__nwl_revision"
    const val OWNED_KEYS = "__nwl_owned_keys"

    fun isOwned(key: String): Boolean = key == DEBUG || key == REVISION || key == OWNED_KEYS ||
        Regex(".*_(Wakelock|Alarm|Service)_.*_\\d+_(flag|flag_lock|aTI)").matches(key) ||
        Regex("(Wakelock|Alarm|Service)_.*_\\d+_rE").matches(key)

    fun flag(st: St): String = flag(st.name, st.packageName, st.type, st.userId)

    fun flag(name: String, packageName: String, type: Type, userId: Int): String =
        "${name}_${type}_${packageName}_${userId}_flag"

    fun flagLock(st: St): String = flagLock(st.name, st.packageName, st.type, st.userId)

    fun flagLock(name: String, packageName: String, type: Type, userId: Int): String =
        "${name}_${type}_${packageName}_${userId}_flag_lock"

    fun allowTimeInterval(st: St): String =
        allowTimeInterval(st.name, st.packageName, st.type, st.userId)

    fun allowTimeInterval(name: String, packageName: String, type: Type, userId: Int): String =
        "${name}_${type}_${packageName}_${userId}_aTI"

    fun regex(type: Type, packageName: String, userId: Int): String = "${type}_${packageName}_${userId}_rE"

    fun regexValues(appSt: AppSt): Map<Type, Set<String>> = mapOf(
        Type.Wakelock to appSt.rE_Wakelock,
        Type.Alarm to appSt.rE_Alarm,
        Type.Service to appSt.rE_Service
    )
}
