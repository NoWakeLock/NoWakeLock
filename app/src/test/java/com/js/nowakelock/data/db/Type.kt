package com.js.nowakelock.testutil

/**
 * Legacy test-only enum.
 *
 * NOTE: Must not share the same FQCN as production `com.js.nowakelock.data.db.Type`,
 * otherwise it will shadow the real class on the unit test classpath.
 */
enum class TestType(val value: String) {
    Wakelock("Wakelock"),
    Alarm("Alarm"),
    Service("Service");

    override fun toString(): String {
        return value
    }
}
