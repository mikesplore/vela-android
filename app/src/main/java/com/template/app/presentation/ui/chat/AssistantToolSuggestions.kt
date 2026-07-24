package com.template.app.presentation.ui.chat

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Monitor
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Screenshot
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.ui.graphics.vector.ImageVector

data class AssistantSuggestion(
    val toolName: String,
    val label: String,
    val icon: ImageVector
)

/**
 * Client-side map of assistant tool ids → suggestion chip copy/icons.
 * Unknown tools are skipped when picking random suggestions.
 */
object AssistantToolSuggestions {

    private val catalog: Map<String, Pair<String, ImageVector>> = mapOf(
        "get_snapshot" to ("What's my system snapshot?" to Icons.Default.Memory),
        "get_cpu_usage" to ("What's my CPU usage?" to Icons.Default.Memory),
        "get_ram_usage" to ("How much RAM is free?" to Icons.Default.Memory),
        "get_memory_usage" to ("How much memory is in use?" to Icons.Default.Memory),
        "get_disk_usage" to ("Show disk usage" to Icons.Default.Storage),
        "get_battery" to ("What's my battery at?" to Icons.Default.BatteryFull),
        "get_uptime" to ("How long has this host been up?" to Icons.Default.Memory),
        "list_processes" to ("List top processes" to Icons.Default.Terminal),
        "get_active_window" to ("What's the active window?" to Icons.Default.Monitor),
        "display_screenshot" to ("Take a screenshot" to Icons.Default.Screenshot),
        "lock_screen" to ("Lock the screen" to Icons.Default.Lock),
        "get_brightness" to ("What's the screen brightness?" to Icons.Default.Monitor),
        "get_volume" to ("What's the current volume?" to Icons.AutoMirrored.Filled.VolumeUp),
        "set_mute" to ("Mute the audio" to Icons.AutoMirrored.Filled.VolumeUp),
        "get_network_info" to ("Show network info" to Icons.Default.Wifi),
        "get_wifi_status" to ("What's my Wi‑Fi status?" to Icons.Default.Wifi),
        "list_files" to ("List files in home" to Icons.Default.Folder),
        "read_clipboard" to ("What's on the clipboard?" to Icons.Default.ContentPaste),
        "get_now_playing" to ("What's playing right now?" to Icons.Default.PlayCircle),
        "send_notification" to ("Send a desktop notification" to Icons.Default.Notifications),
        "get_docker_info" to ("Is Docker running?" to Icons.Default.Storage),
        "list_docker_containers" to ("List Docker containers" to Icons.Default.Storage),
        "get_docker_containers" to ("Show Docker containers" to Icons.Default.Storage),
        "power_sleep" to ("Put the host to sleep" to Icons.Default.PowerSettingsNew),
        "shutdown" to ("Shutdown the host" to Icons.Default.PowerSettingsNew),
        "restart" to ("Restart the host" to Icons.Default.PowerSettingsNew),
        "list_services" to ("List system services" to Icons.Default.Terminal),
        "check_updates" to ("Check for system updates" to Icons.Default.Terminal),
        "get_temperatures" to ("Show temperatures" to Icons.Default.Memory),
    )

    fun pickRandom(availableTools: List<String>, count: Int = 4): List<AssistantSuggestion> {
        val mapped = availableTools.mapNotNull { tool ->
            catalog[tool]?.let { (label, icon) ->
                AssistantSuggestion(toolName = tool, label = label, icon = icon)
            }
        }
        if (mapped.isEmpty()) return emptyList()
        return mapped.shuffled().take(count)
    }
}
