package com.js.nowakelock.data.counter

import org.junit.runner.RunWith
import org.junit.runners.Suite

/**
 * Test suite for wakelock counter-related tests
 * Controls test execution order to ensure problematic tests run independently
 */
@RunWith(Suite::class)
@Suite.SuiteClasses(
    WakelockCounterTest::class,
    WakelockRegistryBasicTest::class,
    WakelockRegistryProblemTest::class
)
class WakelockTests 