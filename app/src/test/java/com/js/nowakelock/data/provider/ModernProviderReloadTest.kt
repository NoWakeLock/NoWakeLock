package com.js.nowakelock.data.provider

import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.content.pm.ProviderInfo
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import androidx.test.core.app.ApplicationProvider
import com.js.nowakelock.data.counter.WakelockRegistry
import com.js.nowakelock.data.db.InfoDatabase
import com.js.nowakelock.data.db.Type
import com.js.nowakelock.xposedhook.model.RuntimeTransfer
import com.js.nowakelock.xposedhook.model.XpRecord
import com.js.nowakelock.xposedhook.modern.ModernSettingsProviderHook
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowContentResolver
import org.robolectric.shadows.ShadowProcess
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], manifest = Config.NONE)
class ModernProviderReloadTest {
    private lateinit var context: Context
    private val epoch = 1_700_000_000_000L
    private val registry = WakelockRegistry.getInstance()
    private val ingress = ModernSettingsProviderHook::class.java.getDeclaredMethod("call",
        Context::class.java, String::class.java, Bundle::class.java).apply { isAccessible = true }

    @Before fun setup() {
        context = ApplicationProvider.getApplicationContext()
        ShadowProcess.setPid(23456)
        RuntimeTransfer.enableModern()
        RuntimeTransfer.retired = false
        RuntimeTransfer.state().remove("providerState")
        RuntimeTransfer.get<AtomicBoolean>("providerCreated").set(false)
        registry.clearAll()
        assertTrue(XpRecord.stopForReload())
    }

    @After fun cleanup() {
        assertTrue(XpRecord.stopForReload())
        InfoDatabase.closeForReload()
        forgetProvider()
        registry.clearAll()
        context.deleteDatabase("info_db")
        RuntimeTransfer.retired = false
    }

    private fun forgetProvider() {
        XProvider::class.java.getDeclaredField("instance").apply { isAccessible = true; set(null, null) }
    }

    private fun args(id: String, time: Long, end: Boolean = false) = Bundle().apply {
        putString("name", "reload-lock"); putString("packageName", "test.app")
        putString("type", Type.Wakelock.value); putString("instanceId", id)
        putInt("userId", 10)
        putLong(if (end) "endTime" else "startTime", epoch + time)
    }

    private fun incoming(method: String, args: Bundle): Bundle? =
        ingress.invoke(ModernSettingsProviderHook, context, method, args) as? Bundle

    private fun awaitDuration(expected: Long) = runBlocking {
        val deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5)
        var actual: Long? = null
        do {
            actual = InfoDatabase.getInstance(context).infoDao().loadInfo("reload-lock", Type.Wakelock, 10)?.countTime
            if (actual == expected) break
            Thread.sleep(10)
        } while (System.nanoTime() < deadline)
        assertEquals(expected, actual)
    }

    @Test fun `overlapping holds and provider ingress survive reopen without clearing rows`() = runBlocking(Dispatchers.IO) {
        val old = XProvider.getInstance(context)
        old.getMethod("NewEvent", args("one", 0))
        old.getMethod("NewEvent", args("two", 100))
        // Exercise the health query too: it must use the owned Room executor.
        assertTrue(old.getMethod("CheckHookEffectiveness", Bundle().apply { putString("type", "Wakelock") })!!.getBoolean("hasData"))
        assertTrue(XProvider.retireForReload())
        assertFalse(Thread.getAllStackTraces().keys.any { it.isAlive && it.name == "NWL-database" })
        assertNotNull(incoming("EndEvent", args("one", 150, true)))
        val state = RuntimeTransfer.state()
        forgetProvider()
        registry.clearAll()
        RuntimeTransfer.adopt(state)
        XProvider.getInstance(context)
        assertNotNull(incoming("EndEvent", args("two", 200, true)))
        XpRecord.resumeAfterReload()
        awaitDuration(200)
        assertTrue(XpRecord.stopForReload())
        val db = InfoDatabase.getInstance(context)
        assertEquals(2, db.infoDao().loadInfo("reload-lock", Type.Wakelock, 10)!!.count)
        assertNull(db.infoDao().loadInfo("reload-lock", Type.Wakelock, 0))
        assertEquals(2, db.infoEventDao().loadAllEvents().size)
        assertEquals(0L, registry.getOngoingDuration("reload-lock", "test.app", Type.Wakelock, 10, epoch + 500))
    }

    @Test fun `first same-process attachment does not requeue acquire behind a later release`() {
        val entered = CountDownLatch(1)
        val proceed = CountDownLatch(1)
        val first = AtomicBoolean(true)
        val bridge = object : ContentProvider() {
            override fun onCreate() = true
            override fun call(method: String, arg: String?, extras: Bundle?): Bundle? {
                if (arg == "NewEvent" && first.compareAndSet(true, false)) {
                    entered.countDown()
                    check(proceed.await(5, TimeUnit.SECONDS))
                }
                return incoming(requireNotNull(arg), requireNotNull(extras))
            }
            override fun query(uri: Uri, p: Array<out String>?, s: String?, a: Array<out String>?, o: String?): Cursor? = null
            override fun getType(uri: Uri): String? = null
            override fun insert(uri: Uri, values: ContentValues?): Uri? = null
            override fun delete(uri: Uri, s: String?, a: Array<out String>?) = 0
            override fun update(uri: Uri, v: ContentValues?, s: String?, a: Array<out String>?) = 0
        }
        bridge.attachInfo(context, ProviderInfo().apply { authority = "settings" })
        ShadowContentResolver.registerProviderInternal("settings", bridge)
        // Force the transport path, as on the first provider access after boot.
        XpRecord::class.java.getDeclaredField("localProvider").apply { isAccessible = true; set(null, null) }
        XpRecord.newEvent("reload-lock", "test.app", Type.Wakelock, context, 10, epoch, "one")
        XpRecord.resumeAfterReload()
        try {
            assertTrue(entered.await(5, TimeUnit.SECONDS))
            XpRecord.endEvent("reload-lock", "test.app", Type.Wakelock, context, 10, epoch, epoch + 200, "one")
        } finally { proceed.countDown() }
        awaitDuration(200)
    }
}
