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
            
            // Hook all 'call' methods since signatures differ across Android versions
            val methods = clsSet.declaredMethods.filter { it.name == "call" }
            for (mCall in methods) {
                XposedBridge.hookMethod(mCall, object : XC_MethodHook() {
                    @Throws(Throwable::class)
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        makeCall(param)
                    }
                })
            }
        }

        private fun makeCall(param: XC_MethodHook.MethodHookParam) {
            try {
                var method: String? = null
                var arg: String? = null
                var extras: Bundle? = null

                // Handle different call signatures in ContentProvider
                when (param.args.size) {
                    3 -> {
                        // call(String method, String arg, Bundle extras)
                        method = param.args[0] as String?
                        arg = param.args[1] as String?
                        extras = param.args[2] as Bundle?
                    }
                    4 -> {
                        // call(String authority, String method, String arg, Bundle extras)
                        method = param.args[1] as String?
                        arg = param.args[2] as String?
                        extras = param.args[3] as Bundle?
                    }
                    5 -> {
                        // call(String callingPkg, String authority, String method, String arg, Bundle extras)
                        method = param.args[2] as String?
                        arg = param.args[3] as String?
                        extras = param.args[4] as Bundle?
                    }
                }

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