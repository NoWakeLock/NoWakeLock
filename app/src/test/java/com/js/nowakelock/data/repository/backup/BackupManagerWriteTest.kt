package com.js.nowakelock.data.repository.backup

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.OutputStream
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.*
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], manifest = Config.NONE)
class BackupManagerWriteTest {
    private val resolver = mock<ContentResolver>()
    private val context = mock<Context>()
    private val repo = mock<BackupRepo>()
    private val uri = Uri.parse("content://backup-test/rules.json")
    private lateinit var manager: BackupManager

    @Before fun setup() = runBlocking {
        whenever(context.contentResolver).thenReturn(resolver)
        whenever(repo.getBackup()).thenReturn(Backup())
        manager = BackupManager(context, repo)
    }

    @Test fun `missing output stream must report failure instead of successful backup`() = runBlocking {
        whenever(resolver.openOutputStream(uri)).thenReturn(null)
        val result = manager.createBackup(uri)
        assertTrue("No output stream means no backup was written", result.isFailure)
        assertTrue(result.exceptionOrNull() is IOException)
    }

    @Test fun `write failure is reported and stream is closed`() = runBlocking {
        var closed = false
        val stream = object : OutputStream() {
            override fun write(value: Int) { throw IOException("storage full") }
            override fun close() { closed = true }
        }
        whenever(resolver.openOutputStream(uri)).thenReturn(stream)
        assertTrue(manager.createBackup(uri).isFailure)
        assertTrue(closed)
    }

    @Test fun `successful backup actually writes decodable data`() = runBlocking {
        val stream = ByteArrayOutputStream()
        whenever(resolver.openOutputStream(uri)).thenReturn(stream)
        assertEquals(true, manager.createBackup(uri).getOrNull())
        assertEquals(Backup(), Json.decodeFromString<Backup>(stream.toString("UTF-8")))
    }
}
