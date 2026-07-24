package com.template.app.presentation.ui.capabilities

import com.template.app.domain.model.HostCapabilities
import com.template.app.domain.model.ModuleKeys
import com.template.app.presentation.ui.Routes

/**
 * Maps UI routes to capability module keys.
 * Settings is never gated. Dashboard stays as shell even without monitoring
 * (shown disabled-with-reason in bottom bar only if needed to avoid empty bar).
 */
object ModuleNavGate {

    fun moduleForRoute(route: String): String? = when (route) {
        Routes.DASHBOARD -> ModuleKeys.MONITORING
        Routes.MONITOR -> ModuleKeys.MONITORING
        Routes.CHAT -> ModuleKeys.ASSISTANT
        Routes.MEDIA -> ModuleKeys.MEDIA
        Routes.DISPLAY -> ModuleKeys.DISPLAY
        Routes.AUDIO -> ModuleKeys.AUDIO
        Routes.NETWORK, Routes.NETWORK_LOGS -> ModuleKeys.NETWORK
        Routes.FILES -> ModuleKeys.FILESYSTEM
        Routes.PROCESSES -> ModuleKeys.PROCESSES
        Routes.SCHEDULER -> ModuleKeys.SCHEDULER
        Routes.MAINTENANCE -> ModuleKeys.MAINTENANCE
        Routes.POWER -> ModuleKeys.POWER
        Routes.CLIPBOARD -> null // not a probed module key on current server
        Routes.INPUT_CONTROL -> ModuleKeys.INPUT_CONTROL
        Routes.NOTIFICATIONS -> ModuleKeys.NOTIFICATIONS
        Routes.SECURITY -> ModuleKeys.SECURITY
        Routes.DOCKER -> ModuleKeys.DOCKER
        Routes.PUSH -> ModuleKeys.PUSH
        Routes.SETTINGS -> null
        else -> null
    }

    fun isRouteAvailable(route: String, caps: HostCapabilities?): Boolean {
        val module = moduleForRoute(route) ?: return true
        // Until capabilities are loaded, hide gated routes (avoid hitting dead endpoints).
        if (caps == null || !caps.isLoaded) return route == Routes.SETTINGS || route == Routes.DASHBOARD
        if (module == ModuleKeys.MONITORING) {
            return caps.isModuleAvailable(ModuleKeys.MONITORING) ||
                caps.isModuleAvailable(ModuleKeys.SYSTEM_INFO)
        }
        return caps.isModuleAvailable(module)
    }

    fun unavailableReason(route: String, caps: HostCapabilities?): String? {
        val module = moduleForRoute(route) ?: return null
        if (caps == null || !caps.isLoaded) return "Capabilities not loaded yet"
        return caps.moduleReason(module) ?: "Not available on this host"
    }
}
