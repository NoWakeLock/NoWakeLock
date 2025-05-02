package com.js.nowakelock.data.counter

import android.util.Log
import java.lang.reflect.Field
import java.lang.reflect.Modifier

/**
 * Utility class for testing WakelockCounter and WakelockRegistry
 */
object TestUtils {
    
    /**
     * Resets the WakelockRegistry singleton instance using reflection
     * This allows tests to start with a clean registry state
     */
    fun resetWakelockRegistry() {
        try {
            val instanceField = WakelockRegistry::class.java.getDeclaredField("instance")
            instanceField.isAccessible = true
            
            // Remove final modifier if needed
            try {
                val modifiersField = Field::class.java.getDeclaredField("modifiers")
                modifiersField.isAccessible = true
                modifiersField.setInt(instanceField, instanceField.modifiers and Modifier.FINAL.inv())
            } catch (e: Exception) {
                // Some JVMs don't allow modifying the modifiers field
                // In that case, we'll just try to set the field directly
            }
            
            // Set instance to null
            instanceField.set(null, null)
            
            // Also clear any internal state in counters map if possible
            val existingInstance = WakelockRegistry.getInstance()
            
            try {
                val countersField = WakelockRegistry::class.java.getDeclaredField("counters")
                countersField.isAccessible = true
                val countersMap = countersField.get(existingInstance)
                val clearMethod = countersMap.javaClass.getMethod("clear")
                clearMethod.invoke(countersMap)
            } catch (e: Exception) {
                // If we can't clear the counters map directly, 
                // at least call the clearAll method
                existingInstance.clearAll()
            }
        } catch (e: Exception) {
            println("Failed to reset WakelockRegistry singleton: ${e.message}")
            e.printStackTrace()
        }
    }
    
    /**
     * Mock class for android.util.Log to use in unit tests
     * Redirects log calls to standard output for verification
     */
    class MockLog {
        companion object {
            var lastLogTag: String? = null
            var lastLogMessage: String? = null
            
            fun e(tag: String, message: String): Int {
                lastLogTag = tag
                lastLogMessage = message
                println("LOG ERROR: $tag - $message")
                return 0
            }
            
            fun reset() {
                lastLogTag = null
                lastLogMessage = null
            }
        }
    }
} 