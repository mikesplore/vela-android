package com.template.app.presentation.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.Bluetooth
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material.icons.rounded.WifiOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.template.app.domain.model.*
import com.template.app.presentation.viewmodel.NetworkViewModel

@Composable
fun NetworkScreen(
    viewModel: NetworkViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val cs = MaterialTheme.colorScheme

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(cs.background),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // IP & Location Section
        item {
            NetworkSection(label = "IP & LOCATION") {
                InfoRow(label = "Local IP", value = state.networkInfo?.localIp ?: "---")
                InfoRow(label = "Public IP", value = state.networkInfo?.publicIp ?: "---")
                val location = state.networkInfo?.location
                val locationStr = if (location != null) "${location.city}, ${location.country}" else "---"
                InfoRow(label = "Location", value = locationStr)
            }
        }

        item { SectionDivider() }

        // Wi-Fi Section
        item {
            NetworkSection(label = "WI-FI") {
                val wifiEnabled = state.wifiStatus?.isEnabled ?: true
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (wifiEnabled) Icons.Rounded.Wifi else Icons.Rounded.WifiOff,
                            contentDescription = null,
                            tint = cs.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = "Wi-Fi enabled",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = cs.onSurface.copy(alpha = 0.85f)
                        )
                    }
                    Switch(
                        checked = wifiEnabled,
                        onCheckedChange = { viewModel.toggleWifi(it) },
                        enabled = !state.isWifiToggling
                    )
                }

                if (wifiEnabled) {
                    state.wifiStatus?.let { status ->
                        if (status.connected && status.ssid != null) {
                            WifiItem(
                                ssid = status.ssid!!,
                                signal = status.signal ?: 0,
                                isConnected = true,
                                onClick = {}
                            )
                        }
                        status.availableNetworks.forEach { net ->
                            if (net.ssid != status.ssid) {
                                WifiItem(
                                    ssid = net.ssid,
                                    signal = net.signal,
                                    isConnected = false,
                                    onClick = {}
                                )
                            }
                        }
                    }
                }

                if (state.wifiStatus?.connected == true) {
                    Spacer(Modifier.height(14.dp))
                    Button(
                        onClick = { viewModel.disconnectWifi() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = cs.error.copy(alpha = 0.1f),
                            contentColor = cs.error
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Disconnect", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item { SectionDivider() }

        // Ping Section
        item {
            NetworkSection(label = "PING") {
                var host by remember { mutableStateOf("") }
                var count by remember { mutableStateOf("4") }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = host,
                        onValueChange = { host = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("8.8.8.8", fontSize = 13.sp, color = cs.onSurfaceVariant.copy(alpha = 0.3f)) },
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = cs.surfaceVariant.copy(alpha = 0.1f),
                            unfocusedBorderColor = cs.outlineVariant.copy(alpha = 0.2f)
                        )
                    )
                    OutlinedTextField(
                        value = count,
                        onValueChange = { count = it },
                        modifier = Modifier.width(64.dp),
                        placeholder = { Text("4", fontSize = 13.sp, color = cs.onSurfaceVariant.copy(alpha = 0.3f)) },
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = cs.surfaceVariant.copy(alpha = 0.1f),
                            unfocusedBorderColor = cs.outlineVariant.copy(alpha = 0.2f)
                        )
                    )
                    Button(
                        onClick = { viewModel.pingHost(host, count.toIntOrNull() ?: 4) },
                        enabled = !state.isPinging,
                        modifier = Modifier.height(56.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = cs.primary.copy(alpha = 0.15f),
                            contentColor = cs.primary
                        )
                    ) {
                        if (state.isPinging) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        } else {
                            Text("Ping", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    PingResultBox(
                        label = "Loss",
                        value = "${state.pingResult?.lossPercent ?: 0.0}%",
                        isGood = (state.pingResult?.lossPercent ?: 0.0) == 0.0,
                        modifier = Modifier.weight(1f)
                    )
                    PingResultBox(
                        label = "Avg RTT",
                        value = "${state.pingResult?.avgRttMs ?: 0.0} ms",
                        modifier = Modifier.weight(1f)
                    )
                    PingResultBox(
                        label = "Packets",
                        value = "${state.pingResult?.received ?: 0}/${state.pingResult?.transmitted ?: 0}",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        item { SectionDivider() }

        // Speed Test Section
        item {
            NetworkSection(label = "SPEED TEST") {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = cs.surfaceVariant.copy(alpha = 0.2f),
                    border = BorderStroke(0.5.dp, cs.outlineVariant.copy(alpha = 0.2f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            SpeedStat(
                                label = "Download",
                                value = state.speedTest?.downloadMbps?.toString() ?: "0.0",
                                unit = "Mbps",
                                isPrimary = true,
                                modifier = Modifier.weight(1f)
                            )
                            SpeedStat(
                                label = "Upload",
                                value = state.speedTest?.uploadMbps?.toString() ?: "0.0",
                                unit = "Mbps",
                                modifier = Modifier.weight(1f)
                            )
                            SpeedStat(
                                label = "Ping",
                                value = state.speedTest?.pingMs?.toString() ?: "0.0",
                                unit = "ms",
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Spacer(Modifier.height(14.dp))
                        Button(
                            onClick = { viewModel.runSpeedTest() },
                            enabled = !state.isSpeedTesting,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = cs.primary.copy(alpha = 0.15f),
                                contentColor = cs.primary
                            )
                        ) {
                            if (state.isSpeedTesting) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                            } else {
                                Text("Run speed test", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        item { SectionDivider() }

        // Bluetooth Section
        item {
            NetworkSection(label = "BLUETOOTH") {
                if (state.isBluetoothLoading && state.bluetoothDevices.isEmpty()) {
                    Box(Modifier.fillMaxWidth().padding(20.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    }
                }
                state.bluetoothDevices.forEach { device ->
                    BluetoothItem(
                        device = device,
                        onAction = { viewModel.toggleBluetoothPairing(device) }
                    )
                }
            }
        }
    }
}

@Composable
private fun NetworkSection(
    label: String,
    content: @Composable ColumnScope.() -> Unit
) {
    val cs = MaterialTheme.colorScheme
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 20.dp)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = cs.onSurfaceVariant.copy(alpha = 0.5f),
            letterSpacing = 0.8.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 14.dp)
        )
        content()
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    val cs = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 9.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 13.sp, color = cs.onSurfaceVariant.copy(alpha = 0.6f))
        Text(text = value, fontSize = 13.sp, color = cs.onSurface.copy(alpha = 0.82f), fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun WifiItem(
    ssid: String,
    signal: Int,
    isConnected: Boolean,
    onClick: () -> Unit
) {
    val cs = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 11.dp)
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(cs.surfaceVariant.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Wifi,
                contentDescription = null,
                tint = if (isConnected) cs.primary else cs.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(14.dp)
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = ssid,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = if (isConnected) cs.primary else cs.onSurface.copy(alpha = 0.85f)
            )
            if (isConnected) {
                Text(
                    text = "Connected",
                    fontSize = 11.sp,
                    color = cs.onSurfaceVariant.copy(alpha = 0.4f)
                )
            }
        }
        Text(
            text = "$signal%",
            fontSize = 12.sp,
            color = cs.onSurfaceVariant.copy(alpha = 0.4f),
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
private fun PingResultBox(
    label: String,
    value: String,
    isGood: Boolean = false,
    modifier: Modifier = Modifier
) {
    val cs = MaterialTheme.colorScheme
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = cs.surfaceVariant.copy(alpha = 0.15f),
        border = BorderStroke(0.5.dp, cs.outlineVariant.copy(alpha = 0.15f))
    ) {
        Column(modifier = Modifier.padding(10.dp, 12.dp)) {
            Text(
                text = label.uppercase(),
                fontSize = 9.sp,
                color = cs.onSurfaceVariant.copy(alpha = 0.5f),
                letterSpacing = 0.4.sp
            )
            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = if (isGood) Color(0xFF6FCB72) else cs.onSurface.copy(alpha = 0.88f)
            )
        }
    }
}

@Composable
private fun SpeedStat(
    label: String,
    value: String,
    unit: String,
    isPrimary: Boolean = false,
    modifier: Modifier = Modifier
) {
    val cs = MaterialTheme.colorScheme
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = if (isPrimary) cs.primary else cs.onSurface.copy(alpha = 0.9f)
        )
        Text(
            text = unit,
            fontSize = 9.sp,
            color = cs.onSurfaceVariant.copy(alpha = 0.4f)
        )
        Text(
            text = label,
            fontSize = 10.sp,
            color = cs.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
private fun BluetoothItem(
    device: VelaBluetoothDevice,
    onAction: () -> Unit
) {
    val cs = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(cs.surfaceVariant.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Bluetooth,
                contentDescription = null,
                tint = cs.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(14.dp)
            )
        }
        Spacer(Modifier.width(12.dp))
        Text(
            text = device.name,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = cs.onSurface.copy(alpha = 0.85f),
            modifier = Modifier.weight(1f)
        )
        Text(
            text = if (device.isPaired) "Unpair" else "Pair",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = if (device.isPaired) cs.error else cs.primary,
            modifier = Modifier.clickable(onClick = onAction)
        )
    }
}

@Composable
private fun SectionDivider() {
    val cs = MaterialTheme.colorScheme
    HorizontalDivider(
        modifier = Modifier.fillMaxWidth(),
        thickness = 0.5.dp,
        color = cs.outlineVariant.copy(alpha = 0.15f)
    )
}
