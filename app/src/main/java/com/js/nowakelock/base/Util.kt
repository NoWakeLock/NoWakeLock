package com.js.nowakelock.base

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.icu.text.SimpleDateFormat
import android.icu.util.TimeZone
import android.os.Bundle
import android.os.UserHandle
import android.os.UserManager
import android.view.Menu
import android.widget.Toast
import com.js.nowakelock.BasicApp
import com.js.nowakelock.R
import com.js.nowakelock.data.db.Type
import com.js.nowakelock.data.db.entity.Info
import com.js.nowakelock.data.db.entity.InfoEvent
import com.js.nowakelock.data.provider.ProviderMethod
import com.js.nowakelock.data.provider.getURI
import java.util.*
import kotlin.math.max

//Long to Time
@SuppressLint("SimpleDateFormat")
private val formatter = SimpleDateFormat("mm:ss")
fun getTime(time: Long): String {
    formatter.timeZone = TimeZone.getTimeZone("GMT+00:00")
    return formatter.format(time)
//    return (time/1000).toString()
}

// filter list
inline fun <T : Any> List<T>.appType(status: (T) -> Boolean): List<T> {
    return this.filter { status(it) }
}

fun menuGone(menu: Menu, set: Set<Int>) {
    set.forEach {
        val filterUser = menu.findItem(it)
        filterUser.isVisible = false
    }
}

// search list
inline fun <T : Any> List<T>.search(query: String, text: (T) -> String): List<T> {
    /*lowerCase and no " " */
    val q = query.lowercase(Locale.ROOT).trim { it <= ' ' }
    if (q == "") {
        return this
    }
    return this.filter {
        text(it).lowercase(Locale.ROOT).contains(q)
    }
}

// sort list
fun <T : Any> List<T>.sort(comparator: Comparator<in T>): List<T> {
    return this.sortedWith(comparator)
}

fun clipboardCopy(str: String): Boolean {
    val context = BasicApp.context
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip: ClipData = ClipData.newPlainText("", str)
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, "$str ${context.getString(R.string.clipboard)}", Toast.LENGTH_LONG)
        .show()
    return true
}

object Util {
    @JvmStatic
    fun stringToSet(value: String?): Set<String> {
        return if (value == null || value == "") {
            mutableSetOf()
        } else {
            value.split("\n")
                .filter { it.matches(Regex("[^\n ]+")) }
                .toSet()
//            value.split('\n').toSet()
        }
    }

    @JvmStatic
    fun setToString(values: Set<String>?): String {
        return if (values.isNullOrEmpty()) {
            ""
        } else {
            var tmp = ""
            values.forEach {
                tmp += "$it\n"
            }
//            tmp = tmp.substring(0, tmp.length - 1)
            tmp
        }
    }
}

fun stringToType(value: String): Type {
    return when (value) {
        "Wakelock" -> Type.Wakelock
        "Alarm" -> Type.Alarm
        "Service" -> Type.Service
        else -> Type.UnKnow
    }
}

fun typeToString(type: Type): String {
    return type.value
}

fun bundleToInfo(bundle: Bundle): Info {
    return Info(
        bundle.getString("name") ?: "",
        stringToType(bundle.getString("package") ?: ""),
        bundle.getString("packageName") ?: "",
        bundle.getInt("count"),
        bundle.getInt("blockCount"),
        bundle.getLong("countTime")
    )
}

fun infoToBundle(info: Info): Bundle {
    return Bundle().apply {
        putString("name", info.name)
        putString("package", typeToString(info.type))
        putString("packageName", info.packageName)
        putInt("count", info.count)
        putInt("blockCount", info.blockCount)
        putLong("countTime", info.countTime)
    }
}

/**
 * Call XP ContentProvider
 * @param context Context
 * @param args Bundle
 * @param method String
 * @return Bundle?
 */
fun getCPResult(context: Context, method: String, args: Bundle): Bundle? {
    // If we're not checking module status, and Xposed isn't active,
    // we bypass the ContentProvider and query the local XProvider directly (for Shizuku mode).
    if (method != ProviderMethod.CheckHookActive.value && 
        context.packageName == "com.js.nowakelock" && 
        !isModuleActive()) {
        return com.js.nowakelock.data.provider.XProvider.getInstance(context).getMethod(method, args)
    }

    val contentResolver = context.contentResolver
    return contentResolver.call(getURI(), "NoWakelock", method, args)
}

/**
 * transform seq to string
 * @param paramTimes Array<out Any>
 * @return String
 */
fun getFormattedTime(vararg paramTimes: Any): String {
    val sBuff = StringBuilder()
    var offset = 0
    if (5 == paramTimes.size) {
        offset = 2
    } else if (4 == paramTimes.size) {
        offset = 1
    }
    for (i in paramTimes.indices) {
        if (i < paramTimes.size - offset) {
            if (paramTimes[i] as Long > 0) {
                sBuff.append(String.format("%02d", paramTimes[i]))
                sBuff.append(":")
            } else {
                continue
            }
        } else {
            sBuff.append(String.format("%02d", paramTimes[i]))
            sBuff.append(":")
        }
    }
    sBuff.deleteCharAt(sBuff.lastIndexOf(":"))
    return sBuff.toString()
}

private const val PER_USER_RANGE = 100000

fun getUserId(uid: Int): Int {
    return try {
        // public static final int getUserId(int uid)
        val method =
            UserHandle::class.java.getDeclaredMethod("getUserId", Int::class.javaPrimitiveType)
        method.invoke(null, uid) as Int
    } catch (ex: Throwable) {
        return uid / PER_USER_RANGE
    }
}

/**
 * whether device has multiple active users
 * @return Boolean
 */

fun multiUser(): Boolean {
    val um = BasicApp.context.getSystemService(Context.USER_SERVICE) as UserManager
    val userList: List<UserHandle> = um.userProfiles

    if (userList.size <= 1) return false

    for (user in userList) {
        if (user.hashCode() == 0) continue
        return um.isUserRunning(user)
    }
    return false
}

/**
 * check module active
 * @return Boolean
 */
fun isModuleActive(): Boolean {
    return try {
        val args = Bundle()
        val result = getCPResult(BasicApp.context, ProviderMethod.CheckHookActive.value, args)
        val active = result?.getBoolean("active", false) ?: false

        if (active) {
            val version = result?.getString("version")
            LogUtil.d("ModuleCheck", "Module active, version: $version")
        }

        active
    } catch (e: Exception) {
        LogUtil.e("ModuleCheck", "Error checking module status: ${e.message}")
        false
    }
}

/**
 * check if Shizuku is available and has permission
 * @return Boolean
 */
fun isShizukuActive(): Boolean {
    return com.js.nowakelock.shizuku.ShizukuManager.hasPermission()
}

fun calculateTime(
    events: List<InfoEvent>
): Long {
    if (events.isEmpty() || events[0].type != Type.Wakelock) {
        return 0L
    }

    // Sort events by start time first to ensure sequential processing
    val sortedEvents = events.sortedBy { it.startTime }
    val now = System.currentTimeMillis()

    var totalDuration = 0L
    var currentStart = sortedEvents[0].startTime
    var currentEnd = sortedEvents[0].endTime ?: (currentStart + 10000L) // Default 10s for snapshot events

    for (i in 1 until sortedEvents.size) {
        val event = sortedEvents[i]
        val eventStart = event.startTime
        val eventEnd = event.endTime ?: (eventStart + 10000L) // Assume 10s duration for active snapshot events

        if (eventStart <= currentEnd) {
            // event overlap, update current end time
            currentEnd = max(currentEnd, eventEnd)
        } else {
            // event not overlap, calculate current segment duration and start new segment
            totalDuration += (currentEnd - currentStart)
            currentStart = eventStart
            currentEnd = eventEnd
        }
    }
    
    // add last segment duration
    totalDuration += (currentEnd - currentStart)
    return totalDuration
}