package com.template.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    private val velaRepository: VelaRepository
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
            velaRepository.togglePlayPause()
            refreshMedia()
        }
    }

    fun playNext() {
        viewModelScope.launch {
            velaRepository.mediaNext()
            refreshMedia()
        }
    }

    fun playPrevious() {
        viewModelScope.launch {
            velaRepository.mediaPrevious()
            refreshMedia()
        }
    }

    fun seekTo(seconds: Int) {
        viewModelScope.launch {
            velaRepository.mediaSeek(seconds)
            refreshMedia()
        }
    }
}
