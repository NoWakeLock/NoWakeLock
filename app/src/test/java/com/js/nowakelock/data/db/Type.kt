package com.js.nowakelock.data.db

/**
 * Mock of Type enum for testing purposes
 * This is a simplified version of the actual Type enum used in the app
 */
enum class Type(val value: String) {
    Wakelock("Wakelock"),
    Alarm("Alarm"),
    Service("Service");
    
    override fun toString(): String {
        return value
    }
} 