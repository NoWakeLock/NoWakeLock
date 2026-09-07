package com.js.nowakelock.xposedhook

import java.lang.reflect.Executable

/** A process-local registry keyed by the actual method and its declaring ClassLoader. */
object HookInstallRegistry {
    enum class State { INSTALLING, INSTALLED, FAILED }
    data class Result(val state: State, val error: String? = null)
    private val results = linkedMapOf<Executable, Result>()

    @Synchronized
    fun install(method: Executable, action: () -> Unit): Boolean {
        if (results[method]?.state in listOf(State.INSTALLED, State.INSTALLING)) return false
        results[method] = Result(State.INSTALLING)
        try {
            action()
            results[method] = Result(State.INSTALLED)
            return true
        } catch (e: Throwable) {
            results[method] = Result(State.FAILED, e.message ?: e.javaClass.simpleName)
            throw e
        }
    }

    @Synchronized fun summary(): String = results.entries.joinToString("\n") {
        "${it.key.declaringClass.simpleName}.${it.key.name}${it.key.parameterTypes.joinToString(prefix = "(", postfix = ")") { p -> p.simpleName }}: ${it.value.state}${it.value.error?.let { e -> " ($e)" } ?: ""}"
    }
    @Synchronized internal fun clearForTest() = results.clear()
}
