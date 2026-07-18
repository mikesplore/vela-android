package com.template.app.presentation.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.template.app.presentation.ui.Routes

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    rootNavController: NavHostController,
    onLogout: () -> Unit,
    onAddDevice: () -> Unit = {}
) {
    val navController = rememberNavController()
    val sheetState = rememberModalBottomSheetState()
    var showSheet by remember { mutableStateOf(false) }
    val colorScheme = MaterialTheme.colorScheme

    val items = listOf(
        NavigationItem("Dashboard", Routes.DASHBOARD, Icons.Default.Dashboard),
        NavigationItem("Assistant", Routes.CHAT, Icons.Default.SmartToy),
        NavigationItem("Monitor", Routes.MONITOR, Icons.Default.Speed),
        NavigationItem("Media", Routes.MEDIA, Icons.Default.PlayCircle)
    )

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            NavigationBar(
                containerColor = colorScheme.background,
                contentColor = colorScheme.onBackground
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                items.forEach { item ->
                    val isChat = item.route == Routes.CHAT
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.title) },
                        label = { Text(item.title, fontSize = 10.sp) },
                        selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                        onClick = {
                            if (isChat) {
                                // Navigate via rootNavController to take over the whole screen
                                rootNavController.navigate(item.route)
                            } else {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = colorScheme.primary,
                            unselectedIconColor = colorScheme.onSurfaceVariant,
                            selectedTextColor = colorScheme.primary,
                            unselectedTextColor = colorScheme.onSurfaceVariant,
                            indicatorColor = Color.Transparent
                        )
                    )
                }

                NavigationBarItem(
                    icon = { Icon(Icons.Default.MoreHoriz, contentDescription = "More") },
                    label = { Text("More", fontSize = 10.sp) },
                    selected = false,
                    onClick = { showSheet = true },
                    colors = NavigationBarItemDefaults.colors(
                        unselectedIconColor = colorScheme.onSurfaceVariant,
                        unselectedTextColor = colorScheme.onSurfaceVariant
                    )
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.DASHBOARD,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Routes.DASHBOARD) {
                DashboardScreen(onAddDevice = onAddDevice)
            }
            composable(Routes.DISPLAY) { DisplayScreen() }
            composable(Routes.MONITOR) { MonitorScreen() }
            composable(Routes.MEDIA) { MediaScreen() }

            composable(Routes.FILES) { FilesScreen() }
            composable(Routes.PROCESSES) { ProcessesScreen(onBack = { showSheet = true }) }
            composable(Routes.SECURITY) { SecurityScreen() }
            composable(Routes.SCHEDULER) { SchedulerScreen() }
            composable(Routes.MAINTENANCE) { MaintenanceScreen(onBack = {navController.popBackStack()}) }
            composable(Routes.NETWORK) { NetworkScreen() }
            composable(Routes.AUDIO){ AudioScreen() }
            composable(Routes.POWER) {
                PowerScreen(onBack = { navController.popBackStack() })
            }
            composable(Routes.NETWORK_LOGS) { NetworkLogsScreen() }
            composable(Routes.CLIPBOARD) { ClipboardScreen() }
            composable(Routes.INPUT_CONTROL) { InputControlScreen() }
            composable(Routes.NOTIFICATIONS) { NotificationsScreen() }
            composable(Routes.SETTINGS) {
                SettingsScreen(
                    onCredentialsCleared = { onLogout() },
                    onAddDevice = onAddDevice
                )
            }
        }

        if (showSheet) {
            ModalBottomSheet(
                onDismissRequest = { showSheet = false },
                sheetState = sheetState,
                containerColor = colorScheme.surface,
                dragHandle = { BottomSheetDefaults.DragHandle(color = colorScheme.outline) }
            ) {
                MoreMenuGrid(
                    onNavigate = { route ->
                        showSheet = false
                        navController.navigate(route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    }
}


@Composable
fun MoreMenuGrid(onNavigate: (String) -> Unit) {
    val colorScheme = MaterialTheme.colorScheme
    val moreItems = listOf(
        NavigationItem("Display", Routes.DISPLAY, Icons.Default.Monitor),
        NavigationItem("Files", Routes.FILES, Icons.Default.Folder),
        NavigationItem("Processes", Routes.PROCESSES, Icons.Default.Memory),
        NavigationItem("Audio", Routes.AUDIO, Icons.AutoMirrored.Filled.VolumeUp),
        //NavigationItem("Security", Routes.SECURITY, Icons.Default.Security),
        NavigationItem("Scheduler", Routes.SCHEDULER, Icons.Default.Schedule),
        NavigationItem("Network", Routes.NETWORK, Icons.Default.NetworkCheck),
        //NavigationItem("Maintenance", Routes.MAINTENANCE, Icons.Default.Build),
        NavigationItem("Power", Routes.POWER, Icons.Default.PowerSettingsNew),
        NavigationItem("Clipboard", Routes.CLIPBOARD, Icons.Default.ContentPaste),
        //NavigationItem("Input", Routes.INPUT_CONTROL, Icons.Default.Keyboard),
        NavigationItem("Alerts", Routes.NOTIFICATIONS, Icons.Default.Notifications),
        NavigationItem("Settings", Routes.SETTINGS, Icons.Default.Settings),
        NavigationItem("Network Logs", Routes.NETWORK_LOGS, Icons.AutoMirrored.Filled.List)
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 32.dp, start = 16.dp, end = 16.dp)
    ) {
        Text(
            text = "SYSTEM TOOLS",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 16.dp, start = 8.dp)
        )
        
        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(moreItems) { item ->
                MoreMenuItem(item = item, onClick = { onNavigate(item.route) })
            }
        }
    }
}

@Composable
fun MoreMenuItem(item: NavigationItem, onClick: () -> Unit) {
    val colorScheme = MaterialTheme.colorScheme
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(colorScheme.surfaceVariant, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.title,
                tint = colorScheme.onSurface,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = item.title,
            fontSize = 10.sp,
            color = colorScheme.onSurface,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

data class NavigationItem(val title: String, val route: String, val icon: ImageVector)
