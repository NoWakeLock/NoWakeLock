package com.js.nowakelock.data.repository.modulecheck

import android.content.Context
import com.js.nowakelock.data.manager.ModuleCheckManager
import com.js.nowakelock.data.model.ModuleCheckResult
import com.js.nowakelock.data.repository.preferences.UserPreferencesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

/**
 * Implementation of the ModuleCheckRepository interface
 */
class ModuleCheckRepositoryImpl(
    private val context: Context,
    private val userPreferencesRepository: UserPreferencesRepository
) : ModuleCheckRepository {
    
    private val moduleCheckManager = ModuleCheckManager(context, userPreferencesRepository)
    
    /**
     * Perform module checks and return the result as a Flow
     */
    override fun checkModuleStatus(): Flow<ModuleCheckResult> = flow {
        // Return initial empty result
        emit(ModuleCheckResult.createEmpty())
        
        // Perform the actual check
        val result = moduleCheckManager.performModuleChecks()
        
        // Emit the result
        emit(result)
    }.flowOn(Dispatchers.IO)
} 