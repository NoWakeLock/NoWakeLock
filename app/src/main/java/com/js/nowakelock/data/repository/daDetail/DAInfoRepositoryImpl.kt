package com.js.nowakelock.data.repository.daDetail

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import com.js.nowakelock.data.db.Type
import com.js.nowakelock.data.model.DAInfoEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException

/**
 * Implementation of DAInfoRepository that loads device automation information from JSON files.
 * Uses a dual-source strategy: first checks for updated JSON in internal storage,
 * falls back to the bundled version in assets if needed.
 */
class DAInfoRepositoryImpl(private val context: Context) : DAInfoRepository {
    
    companion object {
        private const val TAG = "DAInfoRepositoryImpl"
        private const val JSON_FILENAME = "da_info.json"
        private const val ASSET_PATH = JSON_FILENAME
    }
    
    // Lazy-loaded data to avoid parsing JSON on every request
    private val infoData by lazy { loadInfoData() }
    
    /**
     * Get detailed information for a specific device automation item
     * @param id Identifier of the DA item
     * @param packageName Optional package name for more precise matching
     * @return Detailed information or null if not found
     */
    override suspend fun getInfo(id: String, packageName: String?): DAInfoEntry? = withContext(Dispatchers.IO) {
        // Find the best match based on id and packageName
        findBestMatch(id, packageName)
    }

    /**
     * Check if detailed information exists for a specific device automation item
     * @param id Identifier of the DA item
     * @param packageName Optional package name for more precise matching
     * @return True if information exists, false otherwise
     */
    override suspend fun hasInfo(id: String, packageName: String?): Boolean = withContext(Dispatchers.IO) {
        findBestMatch(id, packageName) != null
    }
    
    /**
     * Loads info data from JSON file, first trying internal storage, then assets
     * @return Map of info entries by ID
     */
    private fun loadInfoData(): Map<String, DAInfoEntry> {
        try {
            // First try to load from internal storage (updated version)
            val internalFile = File(context.filesDir, JSON_FILENAME)
            
            val jsonString = if (internalFile.exists() && internalFile.canRead()) {
                // Read from internal storage if available
                internalFile.readText()
            } else {
                // Fall back to assets version
                context.assets.open(ASSET_PATH).bufferedReader().use { it.readText() }
            }
            
            // Parse JSON structure
            val gson = Gson()
            val jsonObject = gson.fromJson(jsonString, com.google.gson.JsonObject::class.java)
            
            // Extract items array
            val itemsArray = jsonObject.getAsJsonArray("items") ?: return emptyMap()
            
            // Parse items to DAInfoEntry objects
            val listType = object : TypeToken<List<DAInfoEntry>>() {}.type
            val entries = gson.fromJson<List<DAInfoEntry>>(itemsArray, listType)
            
            // Create a map for easy lookup, using id as key
            return entries.associateBy { it.id }
            
        } catch (e: FileNotFoundException) {
            Log.e(TAG, "JSON file not found: ${e.message}")
        } catch (e: IOException) {
            Log.e(TAG, "Error reading JSON file: ${e.message}")
        } catch (e: JsonSyntaxException) {
            Log.e(TAG, "Error parsing JSON: ${e.message}")
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error loading info data: ${e.message}")
        }
        
        return emptyMap()
    }
    
    /**
     * Find the best matching entry based on ID and optional package name
     * @param id The ID to match
     * @param packageName Optional package name for more precise matching
     * @return The best matching DAInfoEntry or null if none found
     */
    private fun findBestMatch(id: String, packageName: String?): DAInfoEntry? {
        // Try exact match (ID + packageName)
        if (packageName != null) {
            val exactMatch = infoData.values.find { 
                it.id.equals(id, ignoreCase = true) && 
                it.packageName.equals(packageName, ignoreCase = true) 
            }
            
            if (exactMatch != null) {
                return exactMatch
            }
        }
        
        // Try ID match only
        val idMatch = infoData.values.find { it.id.equals(id, ignoreCase = true) }
        if (idMatch != null) {
            return idMatch
        }
        
        // Try pattern match (for future implementation)
        val patternMatch = infoData.values.find { entry ->
            entry.pattern?.let { pattern ->
                id.matches(Regex(pattern))
            } ?: false
        }
        
        return patternMatch
    }
}