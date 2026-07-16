package com.template.app.presentation.ui.screens.onboarding

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.template.app.presentation.ui.components.QrScannerView
import com.template.app.presentation.viewmodel.OnboardingViewModel

private enum class PairMode { SCAN, MANUAL }

@Composable
fun OnboardingStepSettings(
    baseUrl: String,
    pairingCode: String,
    pairingPin: String,
    showPassword: Boolean,
    testState: OnboardingViewModel.TestResult,
    onUrlChange: (String) -> Unit,
    onCodeChange: (String) -> Unit,
    onPinChange: (String) -> Unit,
    onTogglePassword: () -> Unit,
    onPerformPairing: () -> Unit,
    onSkipOnboarding: () -> Unit,
    onContinue: () -> Unit,
    onQrScanned: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var mode by remember { mutableStateOf(PairMode.SCAN) }
    var showScanner by remember { mutableStateOf(false) }

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasCameraPermission = granted
            if (granted) showScanner = true
        }
    )

    LaunchedEffect(testState) {
        if (testState is OnboardingViewModel.TestResult.Success) onContinue()
    }

    if (showScanner && hasCameraPermission) {
        ScannerScreen(
            onCodeScanned = { code ->
                showScanner = false
                onQrScanned(code)
            },
            onClose = { showScanner = false }
        )
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(8.dp))

        Text(
            text = "Pair Device",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(Modifier.height(24.dp))

        // Segmented switch — makes the two modes mutually exclusive
        ModeSwitch(
            mode = mode,
            onModeChange = { mode = it }
        )

        Spacer(Modifier.height(32.dp))

        AnimatedContent(
            targetState = mode,
            transitionSpec = {
                (fadeIn() + slideInHorizontally { if (targetState == PairMode.MANUAL) it / 4 else -it / 4 })
                    .togetherWith(fadeOut())
            },
            label = "pair_mode"
        ) { current ->
            when (current) {
                PairMode.SCAN -> ScanModeContent(
                    hasCameraPermission = hasCameraPermission,
                    onScanClick = {
                        if (hasCameraPermission) showScanner = true
                        else launcher.launch(Manifest.permission.CAMERA)
                    }
                )
                PairMode.MANUAL -> ManualModeContent(
                    baseUrl = baseUrl,
                    pairingCode = pairingCode,
                    pairingPin = pairingPin,
                    showPassword = showPassword,
                    testState = testState,
                    onUrlChange = onUrlChange,
                    onCodeChange = onCodeChange,
                    onPinChange = onPinChange,
                    onTogglePassword = onTogglePassword,
                    onPerformPairing = onPerformPairing
                )
            }
        }

        Spacer(Modifier.height(48.dp))
    }
}

@Composable
private fun ModeSwitch(mode: PairMode, onModeChange: (PairMode) -> Unit) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(4.dp)) {
            SegmentButton(
                label = "Scan QR",
                selected = mode == PairMode.SCAN,
                modifier = Modifier.weight(1f),
                onClick = { onModeChange(PairMode.SCAN) }
            )
            SegmentButton(
                label = "Manual",
                selected = mode == PairMode.MANUAL,
                modifier = Modifier.weight(1f),
                onClick = { onModeChange(PairMode.MANUAL) }
            )
        }
    }
}

@Composable
private fun SegmentButton(
    label: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val bg = if (selected) MaterialTheme.colorScheme.surface else Color.Transparent
    val fg = if (selected) MaterialTheme.colorScheme.onSurface
    else MaterialTheme.colorScheme.onSurfaceVariant

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = bg,
        shadowElevation = if (selected) 1.dp else 0.dp,
        modifier = modifier.height(40.dp)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Text(label, fontWeight = FontWeight.SemiBold, color = fg, fontSize = 14.sp)
        }
    }
}

@Composable
private fun ScanModeContent(
    hasCameraPermission: Boolean,
    onScanClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Scan the QR code from your Vela dashboard to link this device instantly.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        Surface(
            onClick = onScanClick,
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Icon(
                    Icons.Default.QrCodeScanner,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    if (hasCameraPermission) "Tap to scan" else "Tap to allow camera & scan",
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun ManualModeContent(
    baseUrl: String,
    pairingCode: String,
    pairingPin: String,
    showPassword: Boolean,
    testState: OnboardingViewModel.TestResult,
    onUrlChange: (String) -> Unit,
    onCodeChange: (String) -> Unit,
    onPinChange: (String) -> Unit,
    onTogglePassword: () -> Unit,
    onPerformPairing: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Enter your relay details manually.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = baseUrl,
            onValueChange = onUrlChange,
            label = { Text("Relay Base URL") },
            placeholder = { Text("https://your-vela-instance.com") },
            leadingIcon = { Icon(Icons.Default.Dns, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri, imeAction = ImeAction.Next)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = pairingCode,
                onValueChange = onCodeChange,
                label = { Text("Code") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next)
            )

            OutlinedTextField(
                value = pairingPin,
                onValueChange = onPinChange,
                label = { Text("PIN") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp),
                singleLine = true,
                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = onTogglePassword) {
                        Icon(
                            imageVector = if (showPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = "Toggle password"
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword, imeAction = ImeAction.Done)
            )
        }

        // Inline status feedback, only rendered here (not competing with scan UI)
        AnimatedContent(targetState = testState, label = "manual_status") { state ->
            when (state) {
                is OnboardingViewModel.TestResult.Testing -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        Spacer(Modifier.width(10.dp))
                        Text("Connecting…", style = MaterialTheme.typography.bodySmall)
                    }
                }
                is OnboardingViewModel.TestResult.Error -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.ErrorOutline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            state.message,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                else -> Spacer(Modifier.height(0.dp))
            }
        }

        Button(
            onClick = onPerformPairing,
            enabled = testState !is OnboardingViewModel.TestResult.Testing,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(Icons.Default.Link, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Pair", fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun ScannerScreen(
    onCodeScanned: (String) -> Unit,
    onClose: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        QrScannerView(onCodeScanned = onCodeScanned, modifier = Modifier.fillMaxSize())

        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(260.dp)
                .border(1.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
        )

        IconButton(
            onClick = onClose,
            modifier = Modifier
                .padding(top = 48.dp, end = 24.dp)
                .align(Alignment.TopEnd)
                .background(Color.Black.copy(alpha = 0.6f), CircleShape)
        ) {
            Icon(Icons.Default.Close, contentDescription = "Close Scanner", tint = Color.White)
        }

        Surface(
            color = Color.Black.copy(alpha = 0.7f),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 60.dp)
        ) {
            Text(
                text = "Center the QR code in the frame",
                color = Color.White,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}