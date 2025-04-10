package com.js.nowakelock.data.repository.daDetail

import com.js.nowakelock.data.model.DAInfoEntry

/**
 * Repository for accessing detailed information about device automation items
 */
interface DAInfoRepository {
    /**
     * Get detailed information for a specific device automation item
     * @param id Identifier of the DA item
     * @param packageName Optional package name for more precise matching
     * @return Detailed information or null if not found
     */
    suspend fun getInfo(id: String, packageName: String?): DAInfoEntry?

    /**
     * Check if detailed information exists for a specific device automation item
     * @param id Identifier of the DA item
     * @param packageName Optional package name for more precise matching
     * @return True if information exists, false otherwise
     */
    suspend fun hasInfo(id: String, packageName: String?): Boolean
}