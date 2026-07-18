package com.template.app

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.template.app.core.utils.UiEvent
import com.template.app.domain.model.AppThemeMode
import com.template.app.presentation.ui.AppNavHost
import com.template.app.presentation.ui.Routes
import com.template.app.presentation.ui.components.DeviceSwitcherSheet
import com.template.app.presentation.ui.components.LoadingOverlay
import com.template.app.presentation.ui.theme.AppTheme
import com.template.app.presentation.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    @RequiresApi(Build.VERSION_CODES.O)
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val startDestination by viewModel.startDestination.collectAsStateWithLifecycle()
            val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
            val health by viewModel.health.collectAsStateWithLifecycle()
            val activeDevice by viewModel.activeDevice.collectAsStateWithLifecycle()
            val pairedDevices by viewModel.pairedDevices.collectAsStateWithLifecycle()
            val isLoading by viewModel.appEventManager.isLoading.collectAsStateWithLifecycle()
            val snackbarHostState = remember { SnackbarHostState() }
            var showDeviceSwitcher by remember { mutableStateOf(false) }

            // Central NavController for root navigation (WhatsApp style)
            val rootNavController = rememberNavController()
            val navBackStackEntry by rootNavController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route
            val showDeviceControl = currentRoute != Routes.ONBOARDING &&
                currentRoute != Routes.ADD_DEVICE &&
                pairedDevices.isNotEmpty()

            LaunchedEffect(Unit) {
                viewModel.appEventManager.events.collectLatest { event ->
                    when (event) {
                        is UiEvent.ShowNetworkErrorSnackbar -> {
                            Log.e("MainActivity", "Error: ${event.message}")
                        }
                        is UiEvent.ShowActionSuccessSnackbar -> {
                            snackbarHostState.showSnackbar(event.message)
                        }
                        is UiEvent.ShowActionErrorSnackbar -> {
                            snackbarHostState.showSnackbar(event.message)
                        }
                    }
                }
            }

            val isDarkTheme = when (themeMode) {
                AppThemeMode.LIGHT -> false
                AppThemeMode.DARK -> true
                AppThemeMode.SYSTEM -> isSystemInDarkTheme()
            }

            AppTheme(darkTheme = isDarkTheme) {
                Box(modifier = Modifier.fillMaxSize()) {
                    if (showDeviceSwitcher) {
                        DeviceSwitcherSheet(
                            devices = pairedDevices,
                            onDismiss = { showDeviceSwitcher = false },
                            onSwitch = { viewModel.switchDevice(it) },
                            onAddDevice = {
                                rootNavController.navigate(Routes.ADD_DEVICE)
                            }
                        )
                    }

                    Scaffold(
                        topBar = {
                            VelaTopBar(
                                title = if (currentRoute == Routes.CHAT) "Assistant" else "Vela",
                                showBack = currentRoute == Routes.CHAT || currentRoute == Routes.ADD_DEVICE,
                                onBack = { rootNavController.popBackStack() },
                                trailing = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        if (showDeviceControl) {
                                            DeviceSwitcherChip(
                                                label = activeDevice?.displayName ?: "Device",
                                                onClick = { showDeviceSwitcher = true }
                                            )
                                        }
                                        ConnectionStatusIndicator(isConnected = health != null)
                                    }
                                }
                            )
                        },
                        snackbarHost = { SnackbarHost(snackbarHostState) },
                    ) { innerPadding ->
                        Surface(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            startDestination?.let { destination ->
                                AppNavHost(
                                    navController = rootNavController,
                                    startDestination = destination
                                )
                            }
                        }
                    }

                    AnimatedVisibility(
                        visible = isLoading,
                        enter = androidx.compose.animation.fadeIn(
                            animationSpec = androidx.compose.animation.core.tween(durationMillis = 400)
                        ),
                        exit = androidx.compose.animation.fadeOut(
                            animationSpec = androidx.compose.animation.core.tween(durationMillis = 400)
                        )
                    ) {
                        LoadingOverlay(isLoading = true)
                    }
                }
            }
        }
    }
}

@Composable
private fun DeviceSwitcherChip(
    label: String,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp, horizontal = 2.dp)
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.widthIn(max = 96.dp)
        )
        Icon(
            Icons.Default.ExpandMore,
            contentDescription = "Switch device",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
fun ConnectionStatusIndicator(isConnected: Boolean) {
    Box(
        modifier = Modifier
            .size(8.dp)
            .background(
                color = if (isConnected) Color(0xFF4CAF50) else Color(0xFFF44336),
                shape = CircleShape
            )
    )
}

@Composable
fun VelaTopBar(
    title: String,
    showBack: Boolean,
    onBack: () -> Unit,
    trailing: @Composable (() -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (showBack) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(Modifier.width(8.dp))
            }
            Text(
                text = title,
                fontSize = MaterialTheme.typography.titleLarge.fontSize,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
            trailing?.invoke()
        }
    }
}
