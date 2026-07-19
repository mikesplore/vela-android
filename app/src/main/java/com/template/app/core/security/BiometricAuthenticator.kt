package com.template.app.core.security

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

object BiometricAvailability {
    const val AUTHENTICATORS = BIOMETRIC_STRONG or BIOMETRIC_WEAK

    fun canAuthenticate(context: Context): Boolean {
        val manager = BiometricManager.from(context)
        return manager.canAuthenticate(AUTHENTICATORS) == BiometricManager.BIOMETRIC_SUCCESS
    }
}

/**
 * Thin wrapper around [BiometricPrompt] for FragmentActivity hosts.
 */
class BiometricAuthenticator(
    private val activity: FragmentActivity
) {
    fun authenticate(
        title: String,
        subtitle: String? = null,
        negativeButtonText: String = "Cancel",
        onSuccess: () -> Unit,
        onErrorOrCancel: () -> Unit
    ) {
        val executor = ContextCompat.getMainExecutor(activity)
        val prompt = BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    onSuccess()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    onErrorOrCancel()
                }

                override fun onAuthenticationFailed() {
                    // Keep prompt open for another attempt; terminal failures go through onAuthenticationError
                }
            }
        )

        val info = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .apply { if (!subtitle.isNullOrBlank()) setSubtitle(subtitle) }
            .setNegativeButtonText(negativeButtonText)
            .setAllowedAuthenticators(BiometricAvailability.AUTHENTICATORS)
            .build()

        prompt.authenticate(info)
    }
}
