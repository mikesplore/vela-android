package com.template.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.template.app.domain.usecase.GetSettingsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class BiometricsGateViewModel @Inject constructor(
    getSettingsUseCase: GetSettingsUseCase
) : ViewModel() {
    val biometricsEnabled: StateFlow<Boolean> = getSettingsUseCase()
        .map { it.biometricsEnabled }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)
}
