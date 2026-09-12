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
import com.js.nowakelock.base.getCPResult
import com.js.nowakelock.data.provider.ProviderMethod
import com.js.nowakelock.BuildConfig
import android.os.Bundle

/**
 * Implementation of the ModuleCheckRepository interface
 */
class ModuleCheckRepositoryImpl(
    private val context: Context,
    private val userPreferencesRepository: UserPreferencesRepository
) : ModuleCheckRepository {
    
    private val moduleCheckManager = ModuleCheckManager(context, userPreferencesRepository)
    override suspend fun reloadCode(): CodeReloadReport = withContext(Dispatchers.IO) {
        val before = try { getCPResult(context, ProviderMethod.CheckHookActive.value, Bundle()) } catch (_: Exception) { null }
        val retryFailure = before == null || before.getString("providerReloadFailure") != null || before.getString("systemReloadFailure") != null
        val report = XposedRemotePreferencesManagers.create().reloadCode(retryFailure)
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
