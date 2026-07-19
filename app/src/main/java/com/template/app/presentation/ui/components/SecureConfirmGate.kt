package com.template.app.presentation.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.template.app.presentation.viewmodel.BiometricsGateViewModel

/**
 * When biometrics are enabled, prompts biometric auth and calls [onConfirm] on success.
 * Otherwise shows [VelaConfirmationSheet]. Failure / dismiss aborts with no side effects.
 */
@Composable
fun SecureConfirmGate(
    visible: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    title: String,
    message: String,
    details: List<String> = emptyList(),
    confirmText: String = "Confirm",
    dismissText: String = "Cancel",
    icon: ImageVector? = null,
    isDanger: Boolean = false,
    biometricTitle: String = "Confirm action",
    biometricSubtitle: String? = null,
    viewModel: BiometricsGateViewModel = hiltViewModel()
) {
    if (!visible) return

    val biometricsEnabled by viewModel.biometricsEnabled.collectAsStateWithLifecycle()
    val biometricAvailable = rememberBiometricAvailable()
    val biometricAuth = rememberBiometricAuth()
    var showSheet by remember { mutableStateOf(false) }
    var prompted by remember { mutableStateOf(false) }

    val useBiometric = biometricsEnabled && biometricAvailable && biometricAuth != null

    LaunchedEffect(visible, useBiometric) {
        if (!visible) {
            prompted = false
            showSheet = false
            return@LaunchedEffect
        }
        if (useBiometric && !prompted) {
            prompted = true
            biometricAuth!!.authenticate(
                title = biometricTitle,
                subtitle = biometricSubtitle ?: message,
                onSuccess = {
                    onConfirm()
                    onDismiss()
                },
                onErrorOrCancel = { onDismiss() }
            )
        } else if (!useBiometric) {
            showSheet = true
        }
    }

    if (showSheet) {
        VelaConfirmationSheet(
            onDismiss = onDismiss,
            onConfirm = onConfirm,
            title = title,
            message = message,
            details = details,
            confirmText = confirmText,
            dismissText = dismissText,
            icon = icon,
            isDanger = isDanger
        )
    }
}
