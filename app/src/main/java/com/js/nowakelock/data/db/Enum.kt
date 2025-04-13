package com.js.nowakelock.data.db

enum class Type(var value: String) {
    Wakelock("Wakelock"), Alarm("Alarm"), Service("Service"), UnKnow("UnKnow");

    companion object {
        /**
         * Convert a string to a Type enum.
         * @param value The string to convert.
         * @return The corresponding Type enum, or UnKnow if not found.
         */
        fun fromString(value: String?): Type {
            if (value == null) return UnKnow

            return entries.find {
                it.value.equals(value, ignoreCase = true)
            } ?: UnKnow
        }
    }
}