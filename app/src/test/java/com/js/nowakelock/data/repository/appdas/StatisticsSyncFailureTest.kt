package com.js.nowakelock.data.repository.appdas

import android.content.ContentResolver
import android.content.Context
import android.content.ContextWrapper
import android.os.Bundle
import androidx.test.core.app.ApplicationProvider
import com.js.nowakelock.BasicApp
import com.js.nowakelock.data.db.Type
import com.js.nowakelock.data.db.dao.AppInfoDao
import com.js.nowakelock.data.db.dao.DADao
import com.js.nowakelock.data.db.dao.InfoEventDao
import com.js.nowakelock.data.db.entity.Info
import com.js.nowakelock.data.provider.getURI
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.*
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], manifest = Config.NONE)
class StatisticsSyncFailureTest {
    private fun checkFailure(payload: Bundle, failInsert: Boolean = false) = runBlocking {
        val resolver = mock<ContentResolver>()
        val context = ApplicationProvider.getApplicationContext<Context>()
        BasicApp.context = object : ContextWrapper(context) {
            override fun getContentResolver() = resolver
        }
        whenever(resolver.call(eq(getURI()), eq("NoWakelock"), eq("LoadInfos"), any())).thenReturn(payload)
        val apps = mock<AppInfoDao>()
        whenever(apps.loadAppInfosDBFlow()).thenReturn(flowOf(emptyList()))
        val stats = mock<DADao>()
        if (failInsert) doThrow(IllegalStateException("cache write failed")).whenever(stats).insert(any<List<Info>>())
        AppDasAR(apps, stats, mock<InfoEventDao>()).syncInfos()
        verify(resolver, never()).call(eq(getURI()), eq("NoWakelock"), eq("ClearData"), any())
    }

    @Test fun `incompatible old generation payload never clears authoritative statistics`() {
        checkFailure(Bundle().apply { putSerializable("infos", "incompatible payload") })
    }

    @Test fun `local cache write failure never clears authoritative statistics`() {
        checkFailure(Bundle().apply { putSerializable("infos", arrayOf(Info("lock", Type.Wakelock, "app"))) }, true)
    }
}
