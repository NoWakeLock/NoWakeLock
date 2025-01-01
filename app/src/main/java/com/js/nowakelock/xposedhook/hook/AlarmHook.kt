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
            XposedHelpers.findAndHookMethod("com.android.server.alarm.AlarmManagerService",
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

            XposedHelpers.findAndHookMethod("com.android.server.AlarmManagerService",
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
            XposedHelpers.findAndHookMethod("com.android.server.AlarmManagerService",
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
            var alarmName: String
            var packageName: String
            var uid: Int

            for (i in 0 until triggerList.size) {

                try {
                    val tmp = triggerList[i]
                    val tmp2 = tmp.javaClass.getDeclaredField("statsTag").get(tmp) as String
                    alarmName = tmp2.replace(Regex("\\*.*\\*:"), "")
                    packageName = tmp.javaClass.getDeclaredField("packageName").get(tmp) as String
                    uid = tmp.javaClass.getDeclaredField("uid").get(tmp) as Int
                } catch (e: Exception) {
                    XpUtil.log(" alarm: hookAlarmsLocked err:$e")
                    continue
                }

                val userId = getUserId(uid)

//                XpUtil.log("$packageName alarm: $alarmName uid:$uid userid:$userId")

                val now = SystemClock.elapsedRealtime() //current time
                val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager

                // block or not
                val block =
                    block(alarmName, packageName, userId, lastAllowTime[alarmName] ?: 0, now, booted and !pm.isInteractive)

                if (block) {//block alarm
                    triggerList.removeAt(i)

                    XpUtil.log("$packageName alarm: $alarmName block $booted ${pm.isInteractive}")
                    //update blockCount
                    XpRecord.upBlockCount(alarmName, packageName, type, context, userId)

                } else {//allow alarm
                    lastAllowTime[alarmName] = now
                    XpRecord.upCount(alarmName, packageName, type, context, userId)//update count
                }
            }
        }

        private fun block(
            name: String, packageName: String, userId: Int, lastActive: Long, now: Long, isLocked: Boolean
        ): Boolean {
            val xpNSP = XpNSP.getInstance()
            return xpNSP.flag(name, packageName, type, userId)
                    || isLocked && xpNSP.flagLock(name, packageName, type, userId)
                    || xpNSP.aTI(now, lastActive, name, packageName, type, userId)
                    || xpNSP.rE(name, packageName, type, userId)
        }
    }
}