package com.js.nowakelock.xposedhook.modern

import com.js.nowakelock.xposedhook.HookInstallRegistry
import io.github.libxposed.api.XposedInterface
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*

class ModernHookSupportTest {
    class Target { fun call(): Int = 1 }
    private val method = Target::class.java.getDeclaredMethod("call")
    @Before fun reset() { HookInstallRegistry.clearForTest(); ModernHookSupport.abandonReload() }

    @Test fun `matching handle is replaced through official handle and not registered twice`() {
        val api = mock<XposedInterface>()
        val old = mock<XposedInterface.HookHandle>()
        whenever(old.executable).thenReturn(method)
        ModernHookSupport.beginReload(listOf(old))
        ModernHookSupport.hookMethod(api, method) { 99 }
        ModernHookSupport.finishReload()
        val callback = argumentCaptor<XposedInterface.Hooker>()
        verify(old).replaceHook(callback.capture())
        verify(api, never()).hook(any())
        verify(old, never()).unhook()
        assertEquals(99, callback.firstValue.intercept(mock()))
    }

    @Test fun `missing required discovery preserves unmatched old hook and fails reload`() {
        val old = mock<XposedInterface.HookHandle>()
        whenever(old.executable).thenReturn(method)
        ModernHookSupport.beginReload(listOf(old))
        assertThrows(IllegalStateException::class.java) { ModernHookSupport.finishReload() }
        verify(old, never()).unhook()
    }

    @Test fun `obsolete hook removal requires explicit intent`() {
        val old = mock<XposedInterface.HookHandle>()
        whenever(old.executable).thenReturn(method)
        ModernHookSupport.beginReload(listOf(old))
        ModernHookSupport.finishReload(setOf(method.toGenericString()))
        verify(old).unhook()
    }

    private fun installed(hooker: XposedInterface.Hooker): XposedInterface.Hooker {
        val api = mock<XposedInterface>()
        val builder = mock<XposedInterface.HookBuilder>()
        whenever(api.hook(method)).thenReturn(builder)
        whenever(builder.setId(any())).thenReturn(builder)
        ModernHookSupport.hookMethod(api, method, hooker)
        val captor = argumentCaptor<XposedInterface.Hooker>()
        verify(builder).intercept(captor.capture())
        return captor.firstValue
    }

    @Test fun `pass through returns original value and proceeds exactly once`() {
        val hook = installed { it.proceed() }
        val chain = mock<XposedInterface.Chain>()
        whenever(chain.proceed()).thenReturn(42)
        assertEquals(42, hook.intercept(chain))
        verify(chain, times(1)).proceed()
    }

    @Test fun `blocked primitive result skips original method`() {
        val hook = installed { ModernHookSupport.defaultReturn(it) }
        val chain = mock<XposedInterface.Chain>()
        whenever(chain.executable).thenReturn(method)
        assertEquals(0, hook.intercept(chain))
        verify(chain, never()).proceed()
    }

    @Test fun `original exception propagates without retrying original`() {
        val hook = installed { it.proceed() }
        val chain = mock<XposedInterface.Chain>()
        val failure = IllegalStateException("original")
        whenever(chain.proceed()).thenThrow(failure)
        assertSame(failure, assertThrows(IllegalStateException::class.java) { hook.intercept(chain) })
        verify(chain, times(1)).proceed()
    }
}
