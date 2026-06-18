package com.template.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.template.app.core.utils.AppEventManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class NetworkLogsViewModel @Inject constructor(
    appEventManager: AppEventManager
) : ViewModel() {
    val logs = appEventManager.networkLogs
}
