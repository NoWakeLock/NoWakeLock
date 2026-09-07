package com.js.nowakelock.data.config

/** A complete, immutable projection of the app's persisted configuration. */
data class ConfigSnapshot(val revision: Long, val values: Map<String, Any>)
