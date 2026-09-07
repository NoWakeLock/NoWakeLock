package com.js.nowakelock.xposedhook.modern

import android.content.Context
import io.github.libxposed.api.XposedInterface
import java.lang.reflect.Executable
import java.lang.reflect.Field
import java.lang.reflect.Method
import com.js.nowakelock.xposedhook.HookInstallRegistry
import com.js.nowakelock.xposedhook.model.XpRecord

internal object ModernHookSupport {
    fun loadClass(name: String, classLoader: ClassLoader): Class<*>? {
        return try {
            Class.forName(name, false, classLoader)
        } catch (e: Throwable) {
            ModernXposedLog.error("Unable to load $name", e)
            null
        }
    }

    fun hookMethod(
        xposed: XposedInterface,
        method: Method,
        hooker: XposedInterface.Hooker
    ) {
        try {
            HookInstallRegistry.install(method) {
                method.isAccessible = true
                xposed.hook(method).setId(method.toGenericString()).intercept { chain ->
                    if (XpRecord.runtimeReportDue()) {
                        findContext(chain)?.let { XpRecord.reportRuntime(it) }
                    }
                    hooker.intercept(chain)
                }
            }
        } catch (e: Throwable) {
            ModernXposedLog.error("Hook installation failed: ${method.toGenericString()}", e)
        }
    }

    fun args(chain: XposedInterface.Chain): List<Any?> {
        return chain.args
    }

    fun defaultReturn(chain: XposedInterface.Chain): Any? {
        val returnType = (chain.executable as? Method)?.returnType ?: return null
        return defaultReturn(returnType)
    }

    fun defaultReturn(returnType: Class<*>): Any? {
        if (!returnType.isPrimitive || returnType == Void.TYPE) return null
        return when (returnType) {
            Boolean::class.javaPrimitiveType -> false
            Byte::class.javaPrimitiveType -> 0.toByte()
            Short::class.javaPrimitiveType -> 0.toShort()
            Int::class.javaPrimitiveType -> 0
            Long::class.javaPrimitiveType -> 0L
            Float::class.javaPrimitiveType -> 0f
            Double::class.javaPrimitiveType -> 0.0
            Char::class.javaPrimitiveType -> 0.toChar()
            else -> null
        }
    }

    fun findContext(chain: XposedInterface.Chain): Context? {
        contextFrom(chain.thisObject)?.let { return it }
        for (arg in args(chain)) {
            contextFrom(arg)?.let { return it }
        }
        return null
    }

    private fun contextFrom(value: Any?, depth: Int = 0): Context? {
        if (value == null || depth > MAX_CONTEXT_DEPTH) return null
        if (value is Context) return value

        tryMethod(value, "getContext")?.let { return it }
        tryField(value, "mContext")?.let { return it }
        tryField(value, "context")?.let { return it }

        for (fieldName in nestedContextFields) {
            val nested = tryRawField(value, fieldName) ?: continue
            contextFrom(nested, depth + 1)?.let { return it }
        }
        return null
    }

    private fun tryMethod(value: Any, methodName: String): Context? {
        return try {
            val method = value.javaClass.getMethod(methodName)
            method.isAccessible = true
            method.invoke(value) as? Context
        } catch (_: Throwable) {
            null
        }
    }

    private fun tryField(value: Any, fieldName: String): Context? {
        return tryRawField(value, fieldName) as? Context
    }

    private fun tryRawField(value: Any, fieldName: String): Any? {
        return try {
            findField(value.javaClass, fieldName)?.let { field ->
                field.isAccessible = true
                field.get(value)
            }
        } catch (_: Throwable) {
            null
        }
    }

    private fun findField(clazz: Class<*>, fieldName: String): Field? {
        var current: Class<*>? = clazz
        while (current != null) {
            try {
                return current.getDeclaredField(fieldName)
            } catch (_: NoSuchFieldException) {
                current = current.superclass
            }
        }
        return null
    }

    private val nestedContextFields = listOf("mAm", "mService", "mInjector", "this$0")
    private const val MAX_CONTEXT_DEPTH = 4
}
