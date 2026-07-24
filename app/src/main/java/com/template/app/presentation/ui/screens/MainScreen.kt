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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.template.app.domain.model.HostCapabilities
import com.template.app.presentation.ui.Routes
import com.template.app.presentation.ui.capabilities.ModuleNavGate
import com.template.app.presentation.viewmodel.CapabilitiesViewModel

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    rootNavController: NavHostController,
    onLogout: () -> Unit,
    onAddDevice: () -> Unit = {},
    capabilitiesViewModel: CapabilitiesViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val sheetState = rememberModalBottomSheetState()
    var showSheet by remember { mutableStateOf(false) }
    val colorScheme = MaterialTheme.colorScheme

    val capabilities by capabilitiesViewModel.capabilities.collectAsStateWithLifecycle()
    val capsLoading by capabilitiesViewModel.isLoading.collectAsStateWithLifecycle()
    val capsError by capabilitiesViewModel.error.collectAsStateWithLifecycle()

    val allTabItems = listOf(
        NavigationItem("Dashboard", Routes.DASHBOARD, Icons.Default.Dashboard),
        NavigationItem("Assistant", Routes.CHAT, Icons.Default.SmartToy),
        NavigationItem("Monitor", Routes.MONITOR, Icons.Default.Speed),
        NavigationItem("Media", Routes.MEDIA, Icons.Default.PlayCircle)
    )

    val tabItems = remember(capabilities) {
        allTabItems.filter { ModuleNavGate.isRouteAvailable(it.route, capabilities) }
            .ifEmpty {
                // Keep a shell so the bar never collapses oddly
                listOf(NavigationItem("Dashboard", Routes.DASHBOARD, Icons.Default.Dashboard))
            }
    }

    // If current route became unavailable after device switch, bounce home
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    LaunchedEffect(capabilities, navBackStackEntry?.destination?.route) {
        val route = navBackStackEntry?.destination?.route ?: return@LaunchedEffect
        if (!ModuleNavGate.isRouteAvailable(route, capabilities) && route != Routes.SETTINGS) {
            navController.navigate(Routes.DASHBOARD) {
                popUpTo(navController.graph.findStartDestination().id) { inclusive = false }
                launchSingleTop = true
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            bottomBar = {
                NavigationBar(
                    containerColor = colorScheme.background,
                    contentColor = colorScheme.onBackground
                ) {
                    val currentDestination = navBackStackEntry?.destination

                    tabItems.forEach { item ->
                        val isChat = item.route == Routes.CHAT
                        NavigationBarItem(
                            icon = { Icon(item.icon, contentDescription = item.title) },
                            label = { Text(item.title, fontSize = 10.sp) },
                            selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                            onClick = {
                                if (isChat) {
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
                composable(Routes.DASHBOARD) { DashboardScreen() }
                composable(Routes.DISPLAY) { DisplayScreen() }
                composable(Routes.MONITOR) { MonitorScreen() }
                composable(Routes.MEDIA) { MediaScreen() }

                composable(Routes.FILES) { FilesScreen() }
                composable(Routes.PROCESSES) { ProcessesScreen(onBack = { showSheet = true }) }
                composable(Routes.SECURITY) { SecurityScreen() }
                composable(Routes.SCHEDULER) { SchedulerScreen() }
                composable(Routes.MAINTENANCE) { MaintenanceScreen(onBack = { navController.popBackStack() }) }
                composable(Routes.NETWORK) { NetworkScreen() }
                composable(Routes.AUDIO) { AudioScreen() }
                composable(Routes.POWER) {
                    PowerScreen(onBack = { navController.popBackStack() })
                }
                composable(Routes.NETWORK_LOGS) { NetworkLogsScreen() }
                composable(Routes.CLIPBOARD) { ClipboardScreen() }
                composable(Routes.INPUT_CONTROL) { InputControlScreen() }
                composable(Routes.NOTIFICATIONS) { AlertsScreen() }
                composable(Routes.DOCKER) {
                    DockerScreen()
                }
                composable(Routes.SETTINGS) {
                    SettingsScreen(
                        onCredentialsCleared = { onLogout() },
                        onAddDevice = onAddDevice,
                        onRefreshCapabilities = { capabilitiesViewModel.refreshFromSettings() },
                        capabilities = capabilities,
                        capabilitiesLoading = capsLoading
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
                        capabilities = capabilities,
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

        // Sleek blocking overlay only when this device has no cached capabilities yet
        if (capsLoading && (capabilities == null || capabilities?.isLoaded != true)) {
            CapabilitiesLoadingOverlay(
                error = capsError,
                onRetry = { capabilitiesViewModel.ensureCapabilitiesLoaded() }
            )
        }
    }
}

@Composable
private fun CapabilitiesLoadingOverlay(error: String?, onRetry: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background.copy(alpha = 0.92f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Loading host capabilities…",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            if (!error.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = onRetry) { Text("Retry") }
            }
        }
    }
}

@Composable
fun MoreMenuGrid(
    capabilities: HostCapabilities?,
    onNavigate: (String) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val moreItems = listOf(
        NavigationItem("Display", Routes.DISPLAY, Icons.Default.Monitor),
        NavigationItem("Files", Routes.FILES, Icons.Default.Folder),
        NavigationItem("Processes", Routes.PROCESSES, Icons.Default.Memory),
        NavigationItem("Audio", Routes.AUDIO, Icons.AutoMirrored.Filled.VolumeUp),
        NavigationItem("Scheduler", Routes.SCHEDULER, Icons.Default.Schedule),
        NavigationItem("Network", Routes.NETWORK, Icons.Default.NetworkCheck),
        NavigationItem("Maintenance", Routes.MAINTENANCE, Icons.Default.Build),
        NavigationItem("Power", Routes.POWER, Icons.Default.PowerSettingsNew),
        NavigationItem("Clipboard", Routes.CLIPBOARD, Icons.Default.ContentPaste),
        NavigationItem("Docker", Routes.DOCKER, Icons.Default.Storage),
        NavigationItem("Alerts", Routes.NOTIFICATIONS, Icons.Default.Notifications),
        NavigationItem("Settings", Routes.SETTINGS, Icons.Default.Settings),
        NavigationItem("Network Logs", Routes.NETWORK_LOGS, Icons.AutoMirrored.Filled.List)
    )

    val visible = moreItems.mapNotNull { item ->
        val available = ModuleNavGate.isRouteAvailable(item.route, capabilities)
        when {
            available -> item to null
            item.route == Routes.SETTINGS -> item to null
            // Show disabled tiles only when hiding would leave a sparse weird grid
            // (we prefer hide — so skip unavailable)
            else -> null
        }
    }

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
            items(visible) { (item, _) ->
                MoreMenuItem(item = item, onClick = { onNavigate(item.route) })
            }
        }
    }
}

@Composable
fun MoreMenuItem(item: NavigationItem, onClick: () -> Unit, enabled: Boolean = true) {
    val colorScheme = MaterialTheme.colorScheme
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .alpha(if (enabled) 1f else 0.4f)
            .clickable(enabled = enabled, onClick = onClick)
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
