package com.js.nowakelock.xposedhook.modern

import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import com.js.nowakelock.xposedhook.HookInstallRegistry
import com.js.nowakelock.xposedhook.model.XpRecord
import com.js.nowakelock.xposedhook.model.RuntimeTransfer
import com.js.nowakelock.data.counter.BoundedEventQueue
import io.github.libxposed.api.XposedInterface
import java.lang.reflect.Method
import org.junit.Assert.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.*
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], manifest = Config.NONE)
class ModernServiceOverloadTest {
    private val hooks = mutableMapOf<Method, XposedInterface.Hooker>()
    private val methods = ServiceOverloadFixture::class.java.declaredMethods.sortedBy { it.parameterCount }
    private lateinit var target: ServiceOverloadFixture

    @Before fun setup() {
        assertTrue(XpRecord.stopForReload())
        HookInstallRegistry.clearForTest(); ModernHookSupport.abandonReload()
        target = ServiceOverloadFixture(ApplicationProvider.getApplicationContext<Context>())
        val api = mock<XposedInterface>()
        whenever(api.hook(any())).thenAnswer { call ->
            val method = call.getArgument<Method>(0)
            val builder = mock<XposedInterface.HookBuilder>()
            whenever(builder.setId(any())).thenReturn(builder)
            whenever(builder.intercept(any())).thenAnswer { install ->
                hooks[method] = install.getArgument(0)
                mock<XposedInterface.HookHandle>()
            }
            builder
        }
        ModernServiceHook.hookServiceClass(api, ServiceOverloadFixture::class.java)
        assertEquals(2, hooks.size)
    }

    @After fun cleanup() {
        assertTrue(XpRecord.stopForReload())
        // The fixture intentionally keeps the consumer stopped. Remove only its queued
        // synthetic events so a later real-DB fixture cannot consume them.
        val queue = BoundedEventQueue.adopt<Array<Any>>(RuntimeTransfer.get("queue"))
        while (queue.poll() != null) { }
    }

    private fun call(index: Int, intent: Intent, user: Int = 0, original: () -> Any?): Any? {
        val method = methods[index]
        val chain = mock<XposedInterface.Chain>()
        val args = mutableListOf<Any?>(null, intent, "type", 1, 10000, false, "probe.app", "feature", user)
        if (index == 1) args.add(false)
        whenever(chain.args).thenReturn(args)
        whenever(chain.thisObject).thenReturn(target)
        whenever(chain.executable).thenReturn(method)
        whenever(chain.proceed()).thenAnswer { original() }
        return hooks.getValue(method).intercept(chain)
    }
    private fun count() = XpRecord.diagnostics().getLong("recordSubmitted")
    private fun intent() = Intent().setClassName("probe.app", "probe.app.Service")

    @Test fun `nested start overloads record one application request`() {
        val request = intent(); val before = count()
        assertEquals("started", call(1, request) { call(0, request) { "started" } })
        assertEquals(1L, count() - before)
    }

    @Test fun `different users and different intents remain distinct nested requests`() {
        val request = intent(); val before = count()
        call(1, request) { call(0, request, 10) { null }; call(0, intent()) { null } }
        assertEquals(3L, count() - before)
    }

    @Test fun `original exception removes suppression for the next request`() {
        val request = intent(); val before = count()
        assertThrows(IllegalStateException::class.java) {
            call(1, request) { call(0, request) { error("system failure") } }
        }
        call(0, request) { null }
        assertEquals(2L, count() - before)
    }
}
