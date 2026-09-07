package com.js.nowakelock.data.config

import com.js.nowakelock.data.db.entity.AppSt
import com.js.nowakelock.data.db.entity.St

object ConfigSnapshotWriter {
    fun snapshot(sts: List<St>, appSts: List<AppSt>, debug: Boolean, revision: Long): ConfigSnapshot {
        val values = linkedMapOf<String, Any>(XposedConfigKeys.DEBUG to debug)
        sts.forEach { st ->
            values[XposedConfigKeys.flag(st)] = st.fullBlock
            values[XposedConfigKeys.flagLock(st)] = st.screenOffBlock ?: false
            values[XposedConfigKeys.allowTimeInterval(st)] = st.timeWindowMs
        }
        appSts.forEach { app ->
            XposedConfigKeys.regexValues(app).forEach { (type, regex) ->
                values[XposedConfigKeys.regex(type, app.packageName, app.userId)] = regex.toSet()
            }
        }
        return ConfigSnapshot(revision, values.toMap())
    }
    fun writeSt(backend: XposedConfigBackend, st: St): Boolean {
        return listOf(
            backend.putBoolean(XposedConfigKeys.flag(st), st.fullBlock),
            backend.putBoolean(XposedConfigKeys.flagLock(st), st.screenOffBlock ?: false),
            backend.putLong(XposedConfigKeys.allowTimeInterval(st), st.timeWindowMs)
        ).all { it }
    }

    fun writeAppSt(backend: XposedConfigBackend, appSt: AppSt): Boolean {
        return XposedConfigKeys.regexValues(appSt).map { (type, values) ->
            backend.putStringSet(
                XposedConfigKeys.regex(type, appSt.packageName, appSt.userId),
                values
            )
        }.all { it }
    }

    fun writeAll(backend: XposedConfigBackend, sts: List<St>, appSts: List<AppSt>): Boolean {
        val ruleResults = sts.map { writeSt(backend, it) }
        val appResults = appSts.map { writeAppSt(backend, it) }
        return (ruleResults + appResults).all { it }
    }
}
