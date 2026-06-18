package com.template.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.template.app.core.utils.AppEventManager
import com.template.app.core.utils.Resource
import com.template.app.domain.model.VelaMediaState
import com.template.app.domain.repository.VelaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MediaViewModel @Inject constructor(
    private val velaRepository: VelaRepository,
    private val appEventManager: AppEventManager // Added
) : ViewModel() {

    fun refreshMedia() {
        viewModelScope.launch {
            velaRepository.getNowPlaying()
        }
    }

    val mediaState: StateFlow<VelaMediaState?> = velaRepository.observeMedia()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun togglePlayPause() {
        viewModelScope.launch {
            val result = velaRepository.togglePlayPause()
            if (result is Resource.Error) {
                appEventManager.showActionErrorSnackbar(result.message)
            }
            refreshMedia()
        }
    }

    fun playNext() {
        viewModelScope.launch {
            val result = velaRepository.mediaNext()
            if (result is Resource.Error) {
                appEventManager.showActionErrorSnackbar(result.message)
            }
            refreshMedia()
        }
    }

    fun playPrevious() {
        viewModelScope.launch {
            val result = velaRepository.mediaPrevious()
            if (result is Resource.Error) {
                appEventManager.showActionErrorSnackbar(result.message)
            }
            refreshMedia()
        }
    }

    fun seekTo(seconds: Int) {
        viewModelScope.launch {
            val result = velaRepository.mediaSeek(seconds)
            if (result is Resource.Error) {
                appEventManager.showActionErrorSnackbar(result.message)
            }
            refreshMedia()
        }
    }
}