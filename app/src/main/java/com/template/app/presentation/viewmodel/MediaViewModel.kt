package com.template.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.template.app.core.utils.AppEventManager
import com.template.app.core.utils.Resource
import com.template.app.domain.model.VelaMediaState
import com.template.app.domain.repository.MediaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MediaViewModel @Inject constructor(
    private val mediaRepository: MediaRepository,
    private val appEventManager: AppEventManager // Added
) : ViewModel() {

    fun refreshMedia() {
        viewModelScope.launch {
            mediaRepository.getNowPlaying()
        }
    }

    val mediaState: StateFlow<VelaMediaState?> = mediaRepository.observeMedia()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun togglePlayPause() {
        viewModelScope.launch {
            if (mediaRepository.togglePlayPause() is Resource.Error) {
                appEventManager.showActionErrorSnackbar("Action failed")
            }
            refreshMedia()
        }
    }

    fun playNext() {
        viewModelScope.launch {
            if (mediaRepository.mediaNext() is Resource.Error) {
                appEventManager.showActionErrorSnackbar("Action failed")
            }
            refreshMedia()
        }
    }

    fun playPrevious() {
        viewModelScope.launch {
            if (mediaRepository.mediaPrevious() is Resource.Error) {
                appEventManager.showActionErrorSnackbar("Action failed")
            }
            refreshMedia()
        }
    }

    fun seekTo(seconds: Int) {
        viewModelScope.launch {
            if (mediaRepository.mediaSeek(seconds) is Resource.Error) {
                appEventManager.showActionErrorSnackbar("Action failed")
            }
            refreshMedia()
        }
    }
}