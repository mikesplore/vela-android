package com.template.app.presentation.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.template.app.domain.model.AppThemeMode
import com.template.app.presentation.ui.components.SectionHeader
import com.template.app.presentation.viewmodel.SettingsViewModel
import com.template.app.presentation.viewmodel.TestResult

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onCredentialsCleared: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(20.dp)
    ) {
        // --- CONNECTION SECTION ---
        SectionHeader(title = "CONNECTION")
        
        SettingsField(
            label = "Base URL",
            value = state.baseUrl,
            onValueChange = { viewModel.updateBaseUrl(it) },
            placeholder = "http://192.168.1.100:8000",
            icon = Icons.Default.Dns
        )

        SettingsField(
            label = "API Key",
            value = state.apiToken,
            onValueChange = { viewModel.updateApiToken(it) },
            placeholder = "••••••••••••",
            icon = Icons.Default.Key,
            isPassword = true
        )

        if (state.isConnected) {
            StatusPill(
                text = "Connected · Uptime ${state.uptime}",
                icon = Icons.Default.CheckCircle,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        } else if (state.testResult is TestResult.Error) {
            StatusPill(
                text = (state.testResult as TestResult.Error).message,
                icon = Icons.Default.Error,
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = { viewModel.testConnection() },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                contentPadding = PaddingValues(12.dp)
            ) {
                if (state.isTesting) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Default.Link, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Test", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                }
            }

            Button(
                onClick = { viewModel.saveSettings() },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                contentPadding = PaddingValues(12.dp)
            ) {
                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Save", fontSize = 13.sp, fontWeight = FontWeight.Medium)
            }
        }

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 22.dp),
            thickness = 0.5.dp,
            color = MaterialTheme.colorScheme.outlineVariant
        )

        // --- APPEARANCE SECTION ---
        SectionHeader(title = "APPEARANCE")
        Spacer(modifier = Modifier.height(14.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ThemeCard(
                label = "Dark",
                icon = Icons.Default.DarkMode,
                isActive = state.themeMode == AppThemeMode.DARK,
                onClick = { viewModel.updateTheme(AppThemeMode.DARK) },
                modifier = Modifier.weight(1f)
            )
            ThemeCard(
                label = "Light",
                icon = Icons.Default.LightMode,
                isActive = state.themeMode == AppThemeMode.LIGHT,
                onClick = { viewModel.updateTheme(AppThemeMode.LIGHT) },
                modifier = Modifier.weight(1f)
            )
            ThemeCard(
                label = "System",
                icon = Icons.Default.SettingsSuggest,
                isActive = state.themeMode == AppThemeMode.SYSTEM,
                onClick = { viewModel.updateTheme(AppThemeMode.SYSTEM) },
                modifier = Modifier.weight(1f)
            )
        }

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 22.dp),
            thickness = 0.5.dp,
            color = MaterialTheme.colorScheme.outlineVariant
        )

        // --- ABOUT SECTION ---
        SectionHeader(title = "ABOUT")
        AboutRow(label = "App version", value = "1.4.2")
        AboutRow(label = "Agent version", value = state.agentVersion)

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 22.dp),
            thickness = 0.5.dp,
            color = MaterialTheme.colorScheme.outlineVariant
        )

        // --- CLEAR CREDENTIALS ---
        Surface(
            onClick = { viewModel.clearCredentials(onCredentialsCleared) },
            color = MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
            shape = RoundedCornerShape(10.dp),
            border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.25f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(13.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Clear saved credentials",
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun SettingsField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    icon: ImageVector,
    isPassword: Boolean = false
) {
    Column(modifier = Modifier.padding(bottom = 14.dp)) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier.padding(bottom = 6.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, fontSize = 13.sp) },
            leadingIcon = { Icon(icon, contentDescription = null, modifier = Modifier.size(14.dp)) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            singleLine = true,
            visualTransformation = if (isPassword) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
            keyboardOptions = KeyboardOptions(keyboardType = if (isPassword) KeyboardType.Password else KeyboardType.Text),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
                focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            )
        )
    }
}

@Composable
fun StatusPill(
    text: String,
    icon: ImageVector,
    containerColor: Color,
    contentColor: Color
) {
    Surface(
        color = containerColor.copy(alpha = 0.1f),
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, containerColor.copy(alpha = 0.25f)),
        modifier = Modifier.padding(top = 12.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(13.dp), tint = contentColor)
            Text(text = text, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = contentColor)
        }
    }
}

@Composable
fun ThemeCard(
    label: String,
    icon: ImageVector,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.08f)
    val bgColor = if (isActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(0.5.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(14.dp, 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .clip(RoundedCornerShape(7.dp))
                .background(if (label == "Dark") Color(0xFF1A1A1A) else if (label == "Light") Color(0xFFF2F2F2) else Color.Gray),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = if (label == "Dark") Color.White.copy(alpha = 0.5f) else Color.Black.copy(alpha = 0.4f),
                modifier = Modifier.size(16.dp)
            )
        }
        Text(
            text = label,
            fontSize = 12.sp,
            color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun AboutRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
        Text(value, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Medium)
    }
    HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
}
