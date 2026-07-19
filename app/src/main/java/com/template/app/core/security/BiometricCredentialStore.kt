package com.template.app.core.security

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Stores the agent PIN used when biometrics succeed on PIN-required chat gates.
 * Never logs or exposes the PIN beyond [getPin].
 */
@Singleton
class BiometricCredentialStore @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        PREFS_NAME,
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun savePin(pin: String) {
        prefs.edit().putString(KEY_AGENT_PIN, pin).apply()
    }

    fun getPin(): String? = prefs.getString(KEY_AGENT_PIN, null)?.takeIf { it.isNotBlank() }

    fun hasPin(): Boolean = !getPin().isNullOrBlank()

    fun clear() {
        prefs.edit().remove(KEY_AGENT_PIN).apply()
    }

    private companion object {
        const val PREFS_NAME = "vela_biometric_credentials"
        const val KEY_AGENT_PIN = "agent_pin"
    }
}
