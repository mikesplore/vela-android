package com.template.app.presentation.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.FragmentActivity
import com.template.app.core.security.BiometricAuthenticator
import com.template.app.core.security.BiometricAvailability

@Composable
fun rememberBiometricAuth(): BiometricAuthenticator? {
    val context = LocalContext.current
    val activity = context as? FragmentActivity
    return remember(activity) {
        activity?.let { BiometricAuthenticator(it) }
    }
}

@Composable
fun rememberBiometricAvailable(): Boolean {
    val context = LocalContext.current
    return remember(context) { BiometricAvailability.canAuthenticate(context) }
}
