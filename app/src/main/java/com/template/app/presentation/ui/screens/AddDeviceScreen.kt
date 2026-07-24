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
import com.template.app.presentation.ui.screens.onboarding.OnboardingStepCapabilities
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
    val finished by viewModel.finished.collectAsStateWithLifecycle()
    val capabilitiesState by viewModel.capabilitiesState.collectAsStateWithLifecycle()

    LaunchedEffect(finished) {
        if (finished) onDone()
    }

    val paired = testState is OnboardingViewModel.TestResult.Success
    val title = when {
        paired -> "Host capabilities"
        else -> "Add device"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
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
            if (paired) {
                OnboardingStepCapabilities(
                    state = capabilitiesState,
                    availableCount = (capabilitiesState as? OnboardingViewModel.CapabilitiesLoadState.Success)
                        ?.moduleCount ?: 0,
                    onRetry = viewModel::loadCapabilitiesThenFinish,
                    onContinue = { viewModel.finish() }
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
                    onContinue = {}
                )
            }
        }
    }
}
