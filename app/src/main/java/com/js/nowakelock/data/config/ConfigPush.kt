package com.js.nowakelock.data.config

import android.content.Context
import android.os.Binder
import android.os.Bundle
import android.os.Parcel
import com.js.nowakelock.BuildConfig
import com.js.nowakelock.data.provider.getURI
import com.js.nowakelock.xposedhook.model.XpNSP
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay

/** Application-to-system protocol, deliberately independent of framework-private Binder APIs. */
object ConfigPush {
    const val METHOD = "ApplyConfigSnapshot"
    const val MAX_BYTES = 192 * 1024
    fun encode(snapshot: ConfigSnapshot): Bundle {
        val values = Bundle()
        snapshot.values.forEach { (key, value) ->
            when (value) {
                is Boolean -> values.putBoolean(key, value)
                is Long -> values.putLong(key, value)
                is Set<*> -> values.putStringArrayList(key, ArrayList(value.map { it as String }))
                else -> error("Unsupported configuration value")
            }
        }
        return Bundle().apply {
            putInt("protocol", 1)
            putLong("revision", snapshot.revision)
            putBundle("values", values)
            checkSize(this)
        }
    }
    private fun checkSize(bundle: Bundle) {
        val parcel = Parcel.obtain()
        try {
            parcel.writeBundle(bundle)
            require(parcel.dataSize() <= MAX_BYTES) { "Configuration exceeds 192 KiB" }
        } finally { parcel.recycle() }
    }
    @Suppress("DEPRECATION")
    fun decode(bundle: Bundle): Map<String, Any> {
        checkSize(bundle)
        require(bundle.getInt("protocol") == 1) { "Unsupported protocol" }
        val revision = bundle.getLong("revision")
        require(revision > 0)
        val values = requireNotNull(bundle.getBundle("values"))
        require(values.size() <= 10000)
        return values.keySet().associateWith { key ->
            require(key.length <= 4096 && XposedConfigKeys.isOwned(key) &&
                key != XposedConfigKeys.REVISION && key != XposedConfigKeys.OWNED_KEYS)
            when (val v = values.get(key)) {
                is Boolean -> { require(key == XposedConfigKeys.DEBUG || key.endsWith("_flag") || key.endsWith("_flag_lock")); v }
                is Long -> { require(key.endsWith("_aTI") && v >= 0); v }
                is ArrayList<*> -> {
                    require(key.endsWith("_rE") && v.all { it is String && it.length <= 4096 })
                    v.map { it as String }.toSet().also { set -> set.forEach { Regex(it) } }
                }
                else -> error("Invalid rule type")
            }
        } + (XposedConfigKeys.REVISION to revision)
    }
    fun receive(context: Context, bundle: Bundle): Bundle {
        // A system-wide policy is owned by the primary-user module application.
        val uid = Binder.getCallingUid()
        val owner = context.packageManager.getApplicationInfo(BuildConfig.APPLICATION_ID, 0).uid
        require(uid == owner && uid / 100000 == 0) { "Unauthorized configuration publisher" }
        val applied = XpNSP.getInstance().acceptPushed(decode(bundle))
        return Bundle().apply { putLong("appliedRevision", applied); putInt("pid", android.os.Process.myPid()) }
    }
    fun observed(context: Context): Long = context.contentResolver.call(getURI(), "NoWakelock",
        "CheckConfigBackendStatus", Bundle())?.getLong("observedRevision") ?: 0
    fun send(context: Context, snapshot: ConfigSnapshot): Boolean {
        val result = context.contentResolver.call(getURI(), "NoWakelock", METHOD, encode(snapshot))
        return result?.getLong("appliedRevision") == snapshot.revision
    }
    suspend fun sendWithRetry(context: Context, snapshot: ConfigSnapshot): Boolean {
        repeat(3) { attempt ->
            try {
                if (send(context, snapshot)) return true
            } catch (e: CancellationException) { throw e }
            catch (_: Exception) { /* A starting Provider may not be available yet. */ }
            if (attempt < 2) delay(if (attempt == 0) 100L else 300L)
        }
        return false
    }
}
