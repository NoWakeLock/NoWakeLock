package com.js.nowakelock.xposedhook.hook

import android.app.AndroidAppHelper
import android.content.Context
import android.os.Build
import android.os.PowerManager
import android.os.SystemClock
import com.js.nowakelock.base.getUserId
import com.js.nowakelock.data.db.Type
import com.js.nowakelock.xposedhook.XpUtil
import com.js.nowakelock.xposedhook.model.XpNSP
import com.js.nowakelock.xposedhook.model.XpRecord
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import java.util.*


class AlarmHook {
    companion object {

        private val type = Type.Alarm
        var booted = false

        @Volatile
        private var lastAllowTime = HashMap<String, Long>()//last allow time

        fun hookAlarm(lpparam: XC_LoadPackage.LoadPackageParam) {

            when (Build.VERSION.SDK_INT) {
                //Try for alarm hooks for API levels >= 31 (S)
                in Build.VERSION_CODES.S..40 -> alarmHook31to32(lpparam)
                //Try for alarm hooks for API levels 29-30 (Q R)
                in Build.VERSION_CODES.Q..Build.VERSION_CODES.R -> alarmHook29to30(lpparam)
                //Try for alarm hooks for API levels < 29 > 24.(N ~ P)
                in Build.VERSION_CODES.N..Build.VERSION_CODES.P -> alarmHook24to28(lpparam)
            }
        }

        /*
        * https://cs.android.com/android/platform/superproject/+/master:frameworks/base/apex/jobscheduler/service/java/com/android/server/alarm/AlarmManagerService.java;l=171;bpv=0;bpt=1?hl=zh-cn
        * */
        private fun alarmHook31to32(lpparam: XC_LoadPackage.LoadPackageParam) {
            XposedHelpers.findAndHookMethod(
                "com.android.server.alarm.AlarmManagerService",
                lpparam.classLoader,
                "triggerAlarmsLocked",
                ArrayList::class.java, Long::class.javaPrimitiveType,
                object : XC_MethodHook() {
                    @Throws(Throwable::class)
                    override fun afterHookedMethod(param: MethodHookParam) {
                        val triggerList = param.args[0] as ArrayList<*>
                        val context: Context =
                            AndroidAppHelper.currentApplication().applicationContext
                        hookAlarmsLocked(
//                            param,
                            triggerList, context
                        )
                    }
                })
        }

        private fun alarmHook29to30(lpparam: XC_LoadPackage.LoadPackageParam) {

            XposedHelpers.findAndHookMethod(
                "com.android.server.AlarmManagerService",
                lpparam.classLoader,
                "triggerAlarmsLocked",
                ArrayList::class.java, Long::class.javaPrimitiveType,
                object : XC_MethodHook() {
                    @Throws(Throwable::class)
                    override fun afterHookedMethod(param: MethodHookParam) {
                        val triggerList = param.args[0] as ArrayList<*>
                        val context: Context =
                            AndroidAppHelper.currentApplication().applicationContext
                        hookAlarmsLocked(
//                            param,
                            triggerList, context
                        )
                    }
                })
        }

        private fun alarmHook24to28(lpparam: XC_LoadPackage.LoadPackageParam) {
            XposedHelpers.findAndHookMethod(
                "com.android.server.AlarmManagerService",
                lpparam.classLoader,
                "triggerAlarmsLocked",
                ArrayList::class.java, Long::class.javaPrimitiveType, Long::class.javaPrimitiveType,
                object : XC_MethodHook() {
                    @Throws(Throwable::class)
                    override fun afterHookedMethod(param: MethodHookParam) {
                        val triggerList = param.args[0] as ArrayList<*>
//                        val nowELAPSED = param.args[1] as Long
//                        val nowRTC = param.args[2] as Long
//                            log("Alarm N ${triggerList.size} $nowELAPSED $nowRTC")
                        val context: Context =
                            AndroidAppHelper.currentApplication().applicationContext
                        hookAlarmsLocked(
//                            param,
                            triggerList, context
                        )
                    }
                })
        }

        // handle alarm
        private fun hookAlarmsLocked(
            triggerList: ArrayList<*>,
            context: Context
        ) {
            // Process alarms in reverse order to safely remove items
            for (i in triggerList.size - 1 downTo 0) {
                val alarmInfo = extractAlarmInfo(triggerList[i]) ?: continue
                val (alarmName, packageName, uid) = alarmInfo
                
                val userId = getUserId(uid)
//                XpUtil.log("$packageName alarm: $alarmName uid:$uid userid:$userId")

                val now = SystemClock.elapsedRealtime()
                val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
                val isScreenOff = booted and !pm.isInteractive
                
                // Determine if alarm should be blocked
                val shouldBlock = block(
                    alarmName,
                    packageName,
                    userId,
                    lastAllowTime[alarmName] ?: 0,
                    now,
                    isScreenOff
                )

                if (shouldBlock) {
                    // Remove alarm from trigger list
                    triggerList.removeAt(i)
                    XpUtil.log("$packageName alarm: $alarmName block $booted ${pm.isInteractive}")
                    
                    // Record blocked event
                    XpRecord.blockEvent(
                        alarmName, packageName, type,
                        context, userId
                    )
                } else {
                    // Update last allowed time and record event
                    lastAllowTime[alarmName] = now
                    XpRecord.newEvent(
                        alarmName, packageName, type,
                        context, userId
                    )
                }
            }
        }
        
        private fun extractAlarmInfo(alarm: Any?): Triple<String, String, Int>? {
            return try {
                val statsTag = alarm?.javaClass?.getDeclaredField("statsTag")?.get(alarm) as String
                val alarmName = statsTag.replace(Regex("\\*.*\\*:"), "")
                val packageName = alarm.javaClass.getDeclaredField("packageName").get(alarm) as String
                val uid = alarm.javaClass.getDeclaredField("uid").get(alarm) as Int
                
                Triple(alarmName, packageName, uid)
            } catch (e: Exception) {
                XpUtil.log(" alarm: hookAlarmsLocked err:$e")
                null
            }
        }

        private fun block(
            name: String,
            packageName: String,
            userId: Int,
            lastActive: Long,
            now: Long,
            isLocked: Boolean
        ): Boolean {
            val xpNSP = XpNSP.getInstance()
            return xpNSP.flag(name, packageName, type, userId)
                    || isLocked && xpNSP.flagLock(name, packageName, type, userId)
                    || xpNSP.aTI(now, lastActive, name, packageName, type, userId)
                    || xpNSP.rE(name, packageName, type, userId)
        }
    }
}