package com.template.app.presentation.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.template.app.presentation.ui.screens.onboarding.OnboardingStepGreeting
import com.template.app.presentation.ui.screens.onboarding.OnboardingStepSettings
import com.template.app.presentation.viewmodel.AddDeviceViewModel
import com.template.app.presentation.viewmodel.OnboardingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDeviceScreen(
    onDone: () -> Unit,
    onBack: () -> Unit,
    viewModel: AddDeviceViewModel = hiltViewModel()
) {
    val baseUrl by viewModel.baseUrl.collectAsStateWithLifecycle()
    val pairingCode by viewModel.pairingCode.collectAsStateWithLifecycle()
    val pairingPin by viewModel.pairingPin.collectAsStateWithLifecycle()
    val showPassword by viewModel.showPassword.collectAsStateWithLifecycle()
    val testState by viewModel.testState.collectAsStateWithLifecycle()
    val username by viewModel.username.collectAsStateWithLifecycle()
    val finished by viewModel.finished.collectAsStateWithLifecycle()

    LaunchedEffect(finished) {
        if (finished) onDone()
    }

    val showGreeting = testState is OnboardingViewModel.TestResult.Success

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (showGreeting) "Device added" else "Add device") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (showGreeting) {
                OnboardingStepGreeting(
                    username = username?.replaceFirstChar { it.uppercase() },
                    onFinish = { viewModel.finish() }
                )
            } else {
                OnboardingStepSettings(
                    baseUrl = baseUrl,
                    pairingCode = pairingCode,
                    pairingPin = pairingPin,
                    showPassword = showPassword,
                    testState = testState,
                    onUrlChange = viewModel::setBaseUrl,
                    onCodeChange = viewModel::setPairingCode,
                    onPinChange = viewModel::setPairingPin,
                    onTogglePassword = viewModel::toggleShowPassword,
                    onPerformPairing = viewModel::manualPairing,
                    onQrScanned = viewModel::onQrScanned,
                    onSkipOnboarding = {},
                    // Success auto-calls onContinue; stay on this screen so greeting can show.
                    onContinue = {}
                )
            }
        }
    }
}
