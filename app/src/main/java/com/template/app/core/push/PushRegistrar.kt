package com.template.app.core.push

import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import com.template.app.core.utils.Resource
import com.template.app.domain.model.ModuleKeys
import com.template.app.domain.repository.CapabilitiesRepository
import com.template.app.domain.repository.PushRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Obtains the FCM token (when Firebase is configured) and registers it with the
 * active Vela host when the push module is available.
 */
@Singleton
class PushRegistrar @Inject constructor(
    private val pushRepository: PushRepository,
    private val capabilitiesRepository: CapabilitiesRepository,
    private val pushPreferences: PushPreferences
) {
    companion object {
        private const val TAG = "PushRegistrar"
    }

    fun isFirebaseAvailable(): Boolean =
        try {
            FirebaseApp.getInstance()
            true
        } catch (_: IllegalStateException) {
            false
        }

    suspend fun registerIfPossible(): Resource<Unit> {
        if (!isFirebaseAvailable()) {
            Log.d(TAG, "Firebase not configured; skipping push registration")
            return Resource.Error("Firebase is not configured on this build")
        }

        val caps = capabilitiesRepository.observeCapabilities().first()
        if (caps?.isModuleAvailable(ModuleKeys.PUSH) != true) {
            return Resource.Error("Push is not available on this host")
        }

        val token = runCatching {
            FirebaseMessaging.getInstance().token.await()
        }.getOrElse {
            Log.w(TAG, "Failed to obtain FCM token", it)
            return Resource.Error(it.message ?: "Failed to get FCM token")
        }

        return registerToken(token)
    }

    suspend fun registerToken(token: String): Resource<Unit> {
        if (token.length !in 20..4096) {
            return Resource.Error("Invalid FCM token length")
        }
        val installationId = pushPreferences.getOrCreateInstallationId()
        val result = pushRepository.registerDevice(token, installationId)
        if (result is Resource.Success) {
            pushPreferences.saveLastFcmToken(token)
        }
        return result
    }

    suspend fun unregisterIfPossible(): Resource<Unit> {
        val token = pushPreferences.getLastFcmToken() ?: return Resource.Success(Unit)
        val result = pushRepository.unregisterDevice(token)
        if (result is Resource.Success) {
            pushPreferences.clearLastFcmToken()
        }
        return result
    }
}
