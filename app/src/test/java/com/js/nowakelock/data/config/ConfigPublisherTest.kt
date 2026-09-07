package com.js.nowakelock.data.config

import android.content.Context
import android.content.SharedPreferences
import androidx.test.core.app.ApplicationProvider
import com.google.gson.Gson
import com.js.nowakelock.BasicApp
import com.js.nowakelock.data.db.Type
import com.js.nowakelock.data.db.dao.AppDaDao
import com.js.nowakelock.data.db.dao.DADao
import com.js.nowakelock.data.db.entity.St
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import kotlinx.coroutines.async
import kotlinx.coroutines.Dispatchers
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ConfigPublisherTest {
    @Before
    fun setUp() {
        BasicApp.context = ApplicationProvider.getApplicationContext<Context>()
        BasicApp.gson = Gson()
    }

    @Test
    fun `publishAll records backend write failures in status`() = runTest {
        val daDao = mock<DADao>()
        val appDaDao = mock<AppDaDao>()
        whenever(daDao.loadAllSts()).thenReturn(
            listOf(
                St(
                    name = "JobScheduler",
                    type = Type.Service,
                    packageName = "android",
                    fullBlock = true,
                    screenOffBlock = false,
                    timeWindowMs = 3000,
                    userId = 0
                )
            )
        )
        whenever(appDaDao.loadAllAppSts()).thenReturn(emptyList())

        val remotePreferences = failingSharedPreferences()
        val publisher = ConfigPublisher(
            daDao = daDao,
            appDaDao = appDaDao,
            remotePreferencesManager = FakeRemotePreferencesManager(remotePreferences)
        )

        assertFalse(publisher.publishAll())
        assertEquals("Failed to publish config to remote", publisher.backendStatus().lastError)
    }

    @Test
    fun `queued stale item rereads latest data after publication lock`() = runTest {
        val da = mock<DADao>()
        val apps = mock<AppDaDao>()
        val backend = mock<XposedConfigBackend>()
        whenever(backend.name).thenReturn("legacy")
        whenever(backend.isReadable).thenReturn(true)
        whenever(apps.loadAllAppSts()).thenReturn(emptyList())
        val old = St(name = "probe", type = Type.Wakelock, packageName = "example", fullBlock = false)
        val current = java.util.concurrent.atomic.AtomicReference(old)
        whenever(da.loadAllSts()).thenAnswer { listOf(current.get()) }
        val started = CountDownLatch(1)
        val release = CountDownLatch(1)
        val snapshots = java.util.Collections.synchronizedList(mutableListOf<ConfigSnapshot>())
        whenever(backend.publish(any())).thenAnswer {
            snapshots.add(it.getArgument(0))
            if (snapshots.size == 1) {
                started.countDown()
                check(release.await(5, TimeUnit.SECONDS))
            }
            true
        }
        val publisher = ConfigPublisher(da, apps, FakeRemotePreferencesManager(null),
            legacyBackend = backend)
        val first = async(Dispatchers.IO) { publisher.publishAll() }
        try {
            assertTrue(started.await(5, TimeUnit.SECONDS))
            current.set(old.copy(fullBlock = true))
            val second = async(Dispatchers.IO) { publisher.publishSt(old) }
            release.countDown()
            assertTrue(first.await())
            assertTrue(second.await())
        } finally { release.countDown() }
        assertEquals(false, snapshots[0].values[XposedConfigKeys.flag(old)])
        assertEquals(true, snapshots[1].values[XposedConfigKeys.flag(old)])
        assertTrue(snapshots[1].revision > snapshots[0].revision)
    }

    @Test
    fun `configuration saved offline can be republished when backend returns`() = runTest {
        val da = mock<DADao>()
        val apps = mock<AppDaDao>()
        whenever(da.loadAllSts()).thenReturn(emptyList())
        whenever(apps.loadAllAppSts()).thenReturn(emptyList())
        val unavailableLegacy = mock<XposedConfigBackend>()
        val state = ConfigLocalState(BasicApp.context)
        val offline = ConfigPublisher(da, apps, FakeRemotePreferencesManager(null), state, unavailableLegacy)
        assertFalse(offline.publishDebug(true))
        assertTrue(ConfigLocalState(BasicApp.context).debug())
        val prefs = BasicApp.context.getSharedPreferences("reconnected", 0)
        val online = ConfigPublisher(da, apps, FakeRemotePreferencesManager(prefs), state, unavailableLegacy)
        assertTrue(online.publishAll())
        assertTrue(prefs.getBoolean(XposedConfigKeys.DEBUG, false))
        assertEquals(state.publishedRevision(), prefs.getLong(XposedConfigKeys.REVISION, 0))
    }

    private fun failingSharedPreferences(): SharedPreferences {
        val preferences = mock<SharedPreferences>()
        val editor = mock<SharedPreferences.Editor>()
        whenever(preferences.edit()).thenReturn(editor)
        whenever(preferences.all).thenReturn(emptyMap<String, Any>())
        whenever(editor.putBoolean(any(), any())).thenReturn(editor)
        whenever(editor.putLong(any(), any())).thenReturn(editor)
        whenever(editor.putStringSet(any(), any())).thenReturn(editor)
        whenever(editor.commit()).thenReturn(false)
        return preferences
    }

    private class FakeRemotePreferencesManager(
        private val preferences: SharedPreferences?
    ) : XposedRemotePreferencesManager {
        override fun register(onRemoteAvailable: () -> Unit) = Unit
        override fun getRemotePreferences(): SharedPreferences? = preferences
        override fun status(legacyReadable: Boolean): ConfigBackendStatus {
            val remoteReadable = preferences != null
            return ConfigBackendStatus(
                legacyReadable = legacyReadable,
                remoteReadable = remoteReadable,
                activeBackend = ConfigBackendStatus.activeBackendName(legacyReadable, remoteReadable)
            )
        }
    }
}
