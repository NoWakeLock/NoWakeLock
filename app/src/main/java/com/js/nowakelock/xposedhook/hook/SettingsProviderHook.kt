package com.js.nowakelock.xposedhook.hook

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import com.js.nowakelock.xposedhook.XpUtil
import com.js.nowakelock.data.provider.XProvider
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.callbacks.XC_LoadPackage
import java.lang.reflect.Method


class SettingsProviderHook {
    companion object {

        @SuppressLint("PrivateApi")
        fun hook(lpparam: XC_LoadPackage.LoadPackageParam) {

            // https://android.googlesource.com/platform/frameworks/base/+/master/packages/SettingsProvider/src/com/android/providers/settings/SettingsProvider.java
            val clsSet = Class.forName(
                "com.android.providers.settings.SettingsProvider",
                false,
                lpparam.classLoader
            )
            // Bundle call(String method, String arg, Bundle extras)
            val mCall: Method = clsSet.getMethod(
                "call",
                String::class.java,
                String::class.java,
                Bundle::class.java
            )

            XposedBridge.hookMethod(mCall, object : XC_MethodHook() {
                @Throws(Throwable::class)
                override fun beforeHookedMethod(param: MethodHookParam) {
                    makeCall(param)
                }
            })
        }

        private fun makeCall(param: XC_MethodHook.MethodHookParam) {
            try {
                val method = param.args[0] as String?
                val arg = param.args[1] as String?
                val extras = param.args[2] as Bundle?

                if ("NoWakelock" == method) { // if call form NoWakelock
                    try {
                        val mGetContext = param.thisObject.javaClass.getMethod("getContext")
                        val context: Context =
                            mGetContext.invoke(param.thisObject) as Context

//                        XpUtil.log("$method,$arg,$extras")

                        param.result = call(context, arg, extras) // call XProvider
                    } catch (ex: IllegalArgumentException) {
                        XpUtil.log("Error: " + ex.message)
                        param.throwable = ex
                    } catch (ex: Throwable) {
                        XpUtil.log(Log.getStackTraceString(ex))
                        param.result = null
                    }
                }
            } catch (ex: Throwable) {
                XpUtil.log(Log.getStackTraceString(ex))
            }
        }

        private fun call(context: Context?, method: String?, extras: Bundle?): Bundle? {
            return if (context == null || extras == null || method == null) {
                XpUtil.log("null")
                null
            } else {
                XProvider.getInstance(context).getMethod(method, extras)
            }
        }
    }
}