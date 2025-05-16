package com.js.nowakelock.data.repository.modulecheck

import com.js.nowakelock.data.model.ModuleCheckResult
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for module check functionality
 */
interface ModuleCheckRepository {
    
    /**
     * Perform module checks and return the result as a Flow
     */
    fun checkModuleStatus(): Flow<ModuleCheckResult>
} 