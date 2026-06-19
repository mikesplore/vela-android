package com.template.app.presentation.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.template.app.presentation.ui.components.DataRow
import com.template.app.presentation.ui.components.SectionHeader
import com.template.app.presentation.viewmodel.DisplayViewModel
import com.template.app.presentation.ui.theme.DarkWarning
import com.template.app.presentation.ui.theme.LightWarning

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DisplayScreen(
    viewModel: DisplayViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val colorScheme = MaterialTheme.colorScheme
    val isDark = isSystemInDarkTheme()
    val warningColor = if (isDark) DarkWarning else LightWarning

    LaunchedEffect(Unit) {
        viewModel.takeScreenshot()
    }

    // Mapping Kelvin (1000-10000) to Percentage (0-100)
    // 0% = 10000K (Cool), 100% = 1000K (Warm)
    val nightLightPercent = ((10000 - state.nightLightTemperature).toFloat() / 90f).toInt().coerceIn(0, 100)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Preview Zone
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 10f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(colorScheme.surfaceVariant)
                    .border(0.5.dp, colorScheme.outlineVariant.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (state.screenshot != null) {
                    Image(
                        bitmap = state.screenshot!!.asImageBitmap(),
                        contentDescription = "Live preview",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.FillWidth
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                    )
                }
                
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp),
                    color = Color.Black.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        "Current preview",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 10.sp,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Brightness
            SectionHeader("Brightness")
            Spacer(modifier = Modifier.height(10.dp))
            BrightnessSlider(
                value = state.brightness,
                onValueChange = { viewModel.setBrightness(it) }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 22.dp), color = colorScheme.outlineVariant.copy(alpha = 0.3f))

            // Resolution
            SectionHeader("Resolution")
            Spacer(modifier = Modifier.height(10.dp))
            DataRow("Current", state.resolution?.let { "${it.width}×${it.height} @ ${it.refresh} Hz" } ?: "Unknown")
            DataRow("Output", state.resolution?.output ?: "Unknown")


            HorizontalDivider(modifier = Modifier.padding(vertical = 22.dp), color = colorScheme.outlineVariant.copy(alpha = 0.3f))

            // Night Light
            SectionHeader("Night light")
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.NightsStay, contentDescription = null, tint = colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(10.dp))
                    Text("Night light", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }
                Switch(
                    checked = state.isNightLightEnabled,
                    onCheckedChange = { viewModel.setNightLight(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = colorScheme.onPrimary,
                        checkedTrackColor = colorScheme.primary,
                        uncheckedThumbColor = colorScheme.onPrimary,
                        uncheckedTrackColor = colorScheme.surfaceVariant
                    )
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            BrightnessSlider(
                value = nightLightPercent,
                onValueChange = { percent ->
                    // Convert back to Kelvin for the API: 100% -> 1000K, 0% -> 10000K
                    val kelvin = 10000 - (percent * 90)
                    viewModel.setNightLight(state.isNightLightEnabled, kelvin)
                },
                icon = Icons.Default.Thermostat,
                activeColor = warningColor
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 22.dp), color = colorScheme.outlineVariant.copy(alpha = 0.3f))

            // Power & Lock
            SectionHeader("Power & lock")
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                PowerButton(
                    label = "Turn off",
                    icon = Icons.Default.PowerSettingsNew,
                    color = colorScheme.error,
                    onClick = { viewModel.monitorOff() },
                    modifier = Modifier.weight(1f)
                )
                PowerButton(
                    label = "Turn on",
                    icon = Icons.Default.Lightbulb,
                    color = colorScheme.primary,
                    onClick = { viewModel.monitorOn() },
                    modifier = Modifier.weight(1f)
                )
                PowerButton(
                    label = "Lock",
                    icon = Icons.Default.Lock,
                    color = colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    onClick = { viewModel.lockScreen() },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}


@Composable
private fun BrightnessSlider(
    value: Int,
    onValueChange: (Int) -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector = Icons.Default.LightMode,
    activeColor: Color = MaterialTheme.colorScheme.primary
) {
    var sliderValue by remember(value) { mutableFloatStateOf(value.toFloat()) }
    val colorScheme = MaterialTheme.colorScheme

    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = colorScheme.onSurfaceVariant.copy(alpha = 0.5f), modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(12.dp))
        Slider(
            value = sliderValue,
            onValueChange = { sliderValue = it },
            onValueChangeFinished = { onValueChange(sliderValue.toInt()) },
            valueRange = 0f..100f,
            modifier = Modifier.weight(1f),
            colors = SliderDefaults.colors(
                thumbColor = colorScheme.onPrimary,
                activeTrackColor = activeColor,
                inactiveTrackColor = colorScheme.outlineVariant.copy(alpha = 0.3f)
            )
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = "${sliderValue.toInt()}%",
            fontSize = 13.sp,
            color = colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            modifier = Modifier.width(36.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.End
        )
    }
}

@Composable
private fun RotationPill(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(9.dp))
            .background(if (isSelected) colorScheme.primary.copy(alpha = 0.18f) else colorScheme.surfaceVariant.copy(alpha = 0.2f))
            .border(
                0.5.dp,
                if (isSelected) colorScheme.primary.copy(alpha = 0.35f) else colorScheme.outlineVariant.copy(alpha = 0.3f),
                RoundedCornerShape(9.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 9.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = if (isSelected) colorScheme.primary else colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun PowerButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(11.dp))
            .background(color.copy(alpha = 0.1f))
            .border(0.5.dp, color.copy(alpha = 0.25f), RoundedCornerShape(11.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
        Spacer(Modifier.height(6.dp))
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = color)
    }
}

private fun borderStroke(width: androidx.compose.ui.unit.Dp, color: Color) = androidx.compose.foundation.BorderStroke(width, color)
