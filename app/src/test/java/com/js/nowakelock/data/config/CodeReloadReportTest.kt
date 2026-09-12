package com.js.nowakelock.data.config

import org.junit.Assert.*
import org.junit.Test

class CodeReloadReportTest {
    private val system = ExecutingTarget("build-B", 100, true)
    private val provider = ExecutingTarget("build-B", 200, true)
    private val report = CodeReloadReport(listOf(
        CodeReloadTarget("system_server", 100, 1000, "SUCCEEDED", "UP_TO_DATE"),
        CodeReloadTarget("provider", 200, 1000, "SUCCEEDED", "UP_TO_DATE")))

    @Test fun `both separate targets must execute the new build with completed lifecycle`() {
        assertTrue(report.confirm("build-B", provider, system).verified)
        assertFalse(report.confirm("build-B", provider.copy(buildId = "build-A"), system).verified)
        assertFalse(report.confirm("build-B", provider, system.copy(ready = false)).verified)
        assertFalse(report.confirm("build-B", provider, system.copy(failure = "partial replacement")).verified)
        assertFalse(report.confirm("build-B", provider.copy(pid = 999), system).verified)
    }

    @Test fun `framework current or successful request alone cannot conceal partial failure`() {
        val partial = report.copy(targets = report.targets.map { it.copy(result = "ALREADY_CURRENT") })
        assertFalse(partial.confirm("build-B", provider, system.copy(ready = false, failure = "missing hook")).verified)
        assertFalse(report.copy(targets = report.targets.map { it.copy(result = "REFUSED") }).confirm("build-B", provider, system).verified)
        assertFalse(report.copy(targets = report.targets.map { it.copy(state = "STALE") }).confirm("build-B", provider, system).verified)
    }

    @Test fun `same process provider and system are one required target`() {
        assertTrue(report.copy(targets = report.targets.take(1)).confirm("build-B", system, system).verified)
        assertFalse(CodeReloadReport().confirm("build-B", system, system).verified)
    }
}
