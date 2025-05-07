package com.js.nowakelock.data.repository.daDetail

import android.content.Context
import android.util.Log
import com.js.nowakelock.data.db.Type
import com.js.nowakelock.data.model.DAInfoEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
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

    // Internal data class for JSON parsing with multilanguage support
    // GUARDED - ASK BEFORE MODIFYING
    @Serializable
    private data class DAInfoJsonModel(
        val id: String,
        val name: String,
        val type: String, // Will be converted to Type enum
        @SerialName("package") val package_name: String?,  // Using original JSON naming convention
        val pattern: String? = null,
        val tags: List<String> = emptyList(),
        @SerialName("safe_to_block") val safe_to_block: String,
        val warning: String? = null,
        val description: Map<String, String>,  // Multilanguage support
        val recommendation: Map<String, String>? = null  // Multilanguage support
    ) {
        // Convert JSON model to public DAInfoEntry
        fun toDAInfoEntry(): DAInfoEntry {
            return DAInfoEntry(
                id = id,
                name = name,
                type = Type.fromString(type.uppercase()),
                packageName = package_name,
                safeToBlock = safe_to_block,
                description = description["en"] ?: "",
                recommendation = recommendation?.get("en"),
                warning = warning,
                pattern = pattern,
                tags = tags
            )
        }
    }

    // Nested ReleaseNote class for better type handling
    @Serializable
    private data class ReleaseNote(
        val version: Int,
        val date: String,
        val notes: Map<String, String>
    )

    // Represents the entire JSON structure
    @Serializable
    private data class DAInfoJson(
        val version: Int,
        @SerialName("update_date") val update_date: String,
        @SerialName("release_notes") val release_notes: List<ReleaseNote>,
        val items: List<DAInfoJsonModel>
    )

    // Configure JSON parser with lenient settings
    private val json = Json { 
        ignoreUnknownKeys = true 
        isLenient = true
        coerceInputValues = true
    }

    // Lazy-loaded data to avoid parsing JSON on every request
    // Now holds the entire DAInfoJson structure
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
     * @return DAInfoJson object containing all the data
     */
    private fun loadInfoData(): DAInfoJson? {
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

            // Parse JSON structure using kotlinx.serialization
            return json.decodeFromString<DAInfoJson>(jsonString)

        } catch (e: FileNotFoundException) {
            Log.e(TAG, "JSON file not found: ${e.message}")
        } catch (e: IOException) {
            Log.e(TAG, "Error reading JSON file: ${e.message}")
        } catch (e: SerializationException) {
            Log.e(TAG, "Error parsing JSON: ${e.message}")
            e.printStackTrace() // Print the full stacktrace for serialization errors
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error loading info data: ${e.message}")
            e.printStackTrace()
        }

        return null
    }

    /**
     * Find the best matching entry based on ID and optional package name
     * @param name The ID to match
     * @param packageName Optional package name for more precise matching
     * @return The best matching DAInfoEntry or null if none found
     */
    private fun findBestMatch(name: String, packageName: String?): DAInfoEntry? {
        // Return null if infoData is null
        val jsonData = infoData ?: return null
        
        // Try exact match (ID + packageName)
        if (packageName != null) {
            val exactMatch = jsonData.items.find {
                it.name.equals(name, ignoreCase = true) &&
                        it.package_name.equals(packageName, ignoreCase = true)
            }

            if (exactMatch != null) {
                return exactMatch.toDAInfoEntry()
            }
        }

        // Try ID match only
        val idMatch = jsonData.items.find { it.name.equals(name, ignoreCase = true) }
        if (idMatch != null) {
            return idMatch.toDAInfoEntry()
        }

        // Try pattern match (for future implementation)
        val patternMatch = jsonData.items.find { entry ->
            entry.pattern?.let { pattern ->
                name.matches(Regex(pattern))
            } ?: false
        }

        return patternMatch?.toDAInfoEntry()
    }
}
