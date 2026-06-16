package com.template.app.presentation.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun PlaceholderScreen(name: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = name)
    }
}

@Composable
fun DisplayScreen() = PlaceholderScreen("Display Screen")

@Composable
fun AudioScreen() = PlaceholderScreen("Audio Screen")

@Composable
fun NetworkScreen() = PlaceholderScreen("Network Screen")

@Composable
fun FilesScreen() = PlaceholderScreen("Files Screen")

@Composable
fun SecurityScreen() = PlaceholderScreen("Security Screen")

@Composable
fun SchedulerScreen() = PlaceholderScreen("Scheduler Screen")

@Composable
fun MaintenanceScreen() = PlaceholderScreen("Maintenance Screen")

@Composable
fun InputControlScreen() = PlaceholderScreen("Input Control Screen")

@Composable
fun NotificationsScreen() = PlaceholderScreen("Notifications Screen")

@Composable
fun SettingsScreen() = PlaceholderScreen("Settings Screen")
