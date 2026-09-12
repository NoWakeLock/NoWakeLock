package com.js.nowakelock.data.config

/** Application-owned status: no framework target handles escape the service manager. */
data class CodeReloadReport(
    val targets: List<CodeReloadTarget> = emptyList(),
    val error: String? = null,
    val executingBuilds: String? = null,
    val verified: Boolean = false
) {
    fun confirm(expectedBuild: String, provider: ExecutingTarget, system: ExecutingTarget): CodeReloadReport {
        val current = targets.isNotEmpty() && targets.all {
            it.state == "UP_TO_DATE" && it.result in setOf("SUCCEEDED", "ALREADY_CURRENT")
        }
        fun matches(target: ExecutingTarget) = target.ready && target.failure == null &&
            target.buildId == expectedBuild && target.pid > 0 && targets.any { it.pid == target.pid }
        return copy(executingBuilds = "Installed: $expectedBuild\nSystem: $system\nProvider: $provider",
            verified = error == null && current && matches(provider) && matches(system))
    }
    fun details(): String = listOfNotNull(error, executingBuilds,
        targets.joinToString("\n") { "${it.process} pid=${it.pid}: ${it.result} (${it.state})${it.message?.let { m -> ": $m" } ?: ""}" }
            .takeIf { it.isNotEmpty() }).joinToString("\n")
}

data class CodeReloadTarget(val process: String, val pid: Int, val uid: Int,
    val result: String, val state: String, val message: String? = null)

data class ExecutingTarget(val buildId: String?, val pid: Int, val ready: Boolean, val failure: String? = null) {
    override fun toString() = "${buildId ?: "unverified"} pid=$pid ready=$ready${failure?.let { ": $it" } ?: ""}"
}
