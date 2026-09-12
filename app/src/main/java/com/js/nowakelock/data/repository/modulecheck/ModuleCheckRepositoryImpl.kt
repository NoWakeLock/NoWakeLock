package com.js.nowakelock.data.repository.modulecheck

import android.content.Context
import com.js.nowakelock.data.manager.ModuleCheckManager
import com.js.nowakelock.data.model.ModuleCheckResult
import com.js.nowakelock.data.repository.preferences.UserPreferencesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import com.js.nowakelock.data.config.CodeReloadReport
import com.js.nowakelock.data.config.ExecutingTarget
import com.js.nowakelock.data.config.XposedRemotePreferencesManagers
import com.js.nowakelock.data.config.XposedRemotePreferencesManager
import com.js.nowakelock.base.getCPResult
import com.js.nowakelock.data.provider.ProviderMethod
import com.js.nowakelock.BuildConfig
import android.os.Bundle

/**
 * Implementation of the ModuleCheckRepository interface
 */
class ModuleCheckRepositoryImpl(
    private val context: Context,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val remoteManager: XposedRemotePreferencesManager = XposedRemotePreferencesManagers.create()
) : ModuleCheckRepository {
    
    private val moduleCheckManager = ModuleCheckManager(context, userPreferencesRepository)
    override suspend fun reloadCode(): CodeReloadReport = withContext(Dispatchers.IO) {
        val before = try { getCPResult(context, ProviderMethod.CheckHookActive.value, Bundle()) } catch (_: Exception) { null }
        // Framework UP_TO_DATE may only compare versionCode. Two local release builds
        // can share that code while system_server still executes the previous APK.
        val retryCurrentTargets = listOf("provider", "system").any { prefix ->
            before?.getString("${prefix}BuildId") != BuildConfig.HOOK_BUILD_ID ||
                before.getBoolean("${prefix}CodeReady") != true ||
                before.getInt("${prefix}Pid", 0) <= 0 ||
                before.getString("${prefix}ReloadFailure") != null
        }
        val report = remoteManager.reloadCode(retryCurrentTargets)
        val state = try { getCPResult(context, ProviderMethod.CheckHookActive.value, Bundle()) } catch (_: Exception) { null }
        fun executing(prefix: String) = ExecutingTarget(state?.getString("${prefix}BuildId"),
            state?.getInt("${prefix}Pid", 0) ?: 0, state?.getBoolean("${prefix}CodeReady") == true,
            state?.getString("${prefix}ReloadFailure"))
        report.confirm(BuildConfig.HOOK_BUILD_ID, executing("provider"), executing("system"))
    }
    
    /**
     * Perform module checks and return the result as a Flow
     */
    override fun checkModuleStatus(): Flow<ModuleCheckResult> = flow {
        // Perform the actual check
        val result = moduleCheckManager.performModuleChecks()
        
        // Emit the result
        emit(result)
    }.flowOn(Dispatchers.IO)
}
