package com.template.app.presentation.viewmodel

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.template.app.core.push.PushPreferences
import com.template.app.core.push.PushRegistrar
import com.template.app.core.utils.AppEventManager
import com.template.app.core.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PushUiState(
    val firebaseReady: Boolean = false,
    val notificationPermissionGranted: Boolean = false,
    val lastTokenPreview: String? = null,
    val isBusy: Boolean = false,
    val statusMessage: String? = null
)

@HiltViewModel
class PushViewModel @Inject constructor(
    application: Application,
    private val pushRegistrar: PushRegistrar,
    private val pushPreferences: PushPreferences,
    private val appEventManager: AppEventManager
) : AndroidViewModel(application) {

    private val _state = MutableStateFlow(PushUiState())
    val state = _state.asStateFlow()

    init {
        refreshLocalState()
    }

    fun refreshLocalState() {
        viewModelScope.launch {
            val granted = hasNotificationPermission()
            val token = pushPreferences.getLastFcmToken()
            _state.update {
                it.copy(
                    firebaseReady = pushRegistrar.isFirebaseAvailable(),
                    notificationPermissionGranted = granted,
                    lastTokenPreview = token?.takeLast(12)?.let { t -> "…$t" }
                )
            }
        }
    }

    fun onPermissionResult(granted: Boolean) {
        _state.update { it.copy(notificationPermissionGranted = granted) }
        if (granted) register()
    }

    fun register() {
        viewModelScope.launch {
            _state.update { it.copy(isBusy = true, statusMessage = null) }
            when (val result = pushRegistrar.registerIfPossible()) {
                is Resource.Success -> {
                    appEventManager.showActionSuccessSnackbar("Push device registered")
                    _state.update { it.copy(statusMessage = "Registered with Vela") }
                    refreshLocalState()
                }
                is Resource.Error -> {
                    appEventManager.showActionErrorSnackbar(result.message)
                    _state.update { it.copy(statusMessage = result.message) }
                }
                else -> {}
            }
            _state.update { it.copy(isBusy = false) }
        }
    }

    fun unregister() {
        viewModelScope.launch {
            _state.update { it.copy(isBusy = true) }
            when (val result = pushRegistrar.unregisterIfPossible()) {
                is Resource.Success -> {
                    appEventManager.showActionSuccessSnackbar("Push device unregistered")
                    refreshLocalState()
                }
                is Resource.Error -> appEventManager.showActionErrorSnackbar(result.message)
                else -> {}
            }
            _state.update { it.copy(isBusy = false) }
        }
    }

    fun needsNotificationPermission(): Boolean =
        Build.VERSION.SDK_INT >= 33 && !hasNotificationPermission()

    private fun hasNotificationPermission(): Boolean {
        if (Build.VERSION.SDK_INT < 33) return true
        return ContextCompat.checkSelfPermission(
            getApplication(),
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }
}
