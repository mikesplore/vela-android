package com.template.app.domain.model

data class HostCapabilities(
    val checkedAt: String? = null,
    val modules: Map<String, ModuleCapability> = emptyMap(),
    val availableAssistantTools: List<String> = emptyList(),
    val fetchedAtMillis: Long = 0L
) {
    fun isModuleAvailable(key: String): Boolean =
        modules[key]?.available == true

    fun moduleReason(key: String): String? =
        modules[key]?.reason

    /** True once we have successfully persisted a capabilities snapshot. */
    val isLoaded: Boolean get() = fetchedAtMillis > 0L
}

data class ModuleCapability(
    val key: String,
    val available: Boolean,
    val configEnabled: Boolean,
    val reason: String? = null,
    val missingCommands: List<String> = emptyList()
)

/** Known module keys from the Vela capabilities probe. */
object ModuleKeys {
    const val FILESYSTEM = "filesystem"
    const val AUDIO = "audio"
    const val DISPLAY = "display"
    const val INPUT_CONTROL = "input_control"
    const val MEDIA = "media"
    const val NETWORK = "network"
    const val NOTIFICATIONS = "notifications"
    const val POWER = "power"
    const val SECURITY = "security"
    const val SYSTEM_INFO = "system_info"
    const val MAINTENANCE = "maintenance"
    const val MONITORING = "monitoring"
    const val PROCESSES = "processes"
    const val SCHEDULER = "scheduler"
    const val DOCKER = "docker"
    const val ALERTS = "alerts"
    const val PUSH = "push"
    const val SPOTIFY = "spotify"
    const val ASSISTANT = "assistant"
    const val CLIPBOARD = "clipboard"
}
