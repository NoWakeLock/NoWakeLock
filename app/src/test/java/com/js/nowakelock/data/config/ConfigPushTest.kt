package com.js.nowakelock.data.config

import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import android.content.Context
import android.content.ContentResolver
import android.os.Bundle
import kotlinx.coroutines.runBlocking
import org.mockito.kotlin.*

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], manifest = Config.NONE)
class ConfigPushTest {
    @Test fun `activation retries same revision and stops after acknowledgement`() = runBlocking {
        val context = mock<Context>()
        val resolver = mock<ContentResolver>()
        whenever(context.contentResolver).thenReturn(resolver)
        val ack = Bundle().apply { putLong("appliedRevision", 7) }
        whenever(resolver.call(any<android.net.Uri>(), eq("NoWakelock"), eq(ConfigPush.METHOD), any()))
            .thenThrow(IllegalStateException("starting")).thenReturn(ack)
        assertTrue(ConfigPush.sendWithRetry(context, ConfigSnapshot(7, mapOf("debug" to true))))
        val payload = argumentCaptor<Bundle>()
        verify(resolver, times(2)).call(any<android.net.Uri>(), eq("NoWakelock"), eq(ConfigPush.METHOD), payload.capture())
        assertTrue(payload.allValues.all { it.getLong("revision") == 7L })
    }
    @Test fun `missing acknowledgement has a finite retry budget`() = runBlocking {
        val context = mock<Context>()
        val resolver = mock<ContentResolver>()
        whenever(context.contentResolver).thenReturn(resolver)
        assertFalse(ConfigPush.sendWithRetry(context, ConfigSnapshot(7, mapOf("debug" to true))))
        verify(resolver, times(3)).call(any<android.net.Uri>(), eq("NoWakelock"), eq(ConfigPush.METHOD), any())
        Unit
    }
    @Test fun `wire snapshot is complete and rejects invalid types and excessive data`() {
        val snapshot = ConfigSnapshot(4, mapOf("debug" to true, "Wakelock_app_0_rE" to setOf("abc")))
        assertEquals(snapshot.values + (XposedConfigKeys.REVISION to 4L), ConfigPush.decode(ConfigPush.encode(snapshot)))
        assertThrows(IllegalArgumentException::class.java) {
            ConfigPush.decode(ConfigPush.encode(ConfigSnapshot(5, mapOf("debug" to 1L))))
        }
        assertThrows(IllegalArgumentException::class.java) {
            ConfigPush.encode(ConfigSnapshot(6, mapOf("Wakelock_app_0_rE" to setOf("x".repeat(200000)))))
        }
    }
}
