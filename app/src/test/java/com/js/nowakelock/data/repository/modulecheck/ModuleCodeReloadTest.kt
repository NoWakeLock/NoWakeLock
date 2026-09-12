package com.js.nowakelock.data.repository.modulecheck

import android.content.ContentResolver
import android.content.Context
import android.content.ContextWrapper
import android.os.Bundle
import androidx.test.core.app.ApplicationProvider
import com.js.nowakelock.BuildConfig
import com.js.nowakelock.data.config.CodeReloadReport
import com.js.nowakelock.data.config.CodeReloadTarget
import com.js.nowakelock.data.config.XposedRemotePreferencesManager
import com.js.nowakelock.data.provider.getURI
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.*
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], manifest = Config.NONE)
class ModuleCodeReloadTest {
    private fun state(build: String = BuildConfig.HOOK_BUILD_ID) = Bundle().apply {
        for (prefix in listOf("system", "provider")) {
            putString("${prefix}BuildId", build)
            putInt("${prefix}Pid", 100)
            putBoolean("${prefix}CodeReady", true)
        }
    }

    private fun check(before: Bundle?, expectedRetry: Boolean) = runBlocking {
        val resolver = mock<ContentResolver>()
        val context = object : ContextWrapper(ApplicationProvider.getApplicationContext<Context>()) {
            override fun getContentResolver() = resolver
        }
        // Framework reports UP_TO_DATE for both APKs with versionCode 87. Only our
        // executing build identity distinguishes the old code from the installed code.
        var executing = before
        whenever(resolver.call(eq(getURI()), eq("NoWakelock"), eq("CheckHookActive"), any()))
            .thenAnswer { executing }
        val manager = mock<XposedRemotePreferencesManager>()
        whenever(manager.reloadCode(any())).thenAnswer {
            val retry = it.getArgument<Boolean>(0)
            if (retry) executing = state()
            CodeReloadReport(listOf(CodeReloadTarget("system", 100, 1000,
                if (retry) "SUCCEEDED" else "ALREADY_CURRENT", "UP_TO_DATE")))
        }
        val report = ModuleCheckRepositoryImpl(context, mock(), manager).reloadCode()
        verify(manager).reloadCode(expectedRetry)
        assertTrue("Only verified executing code may confirm the update", report.verified)
    }

    @Test fun `same version code with different executing build requests reload`() {
        check(state("older-build-same-version-code"), true)
    }

    @Test fun `matching builds with incomplete provider lifecycle request retry`() {
        check(state().apply { putBoolean("providerCodeReady", false) }, true)
    }

    @Test fun `missing execution evidence requests reload`() { check(null, true) }

    @Test fun `fully verified executing build avoids unnecessary reload`() { check(state(), false) }
}
