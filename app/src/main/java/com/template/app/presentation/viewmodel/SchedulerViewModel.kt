package com.template.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.template.app.core.utils.AppEventManager
import com.template.app.core.utils.Resource
import com.template.app.domain.model.VelaScheduledTask
import com.template.app.domain.repository.SchedulesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject

data class SchedulerState(
    val tasks: List<VelaScheduledTask> = emptyList(),
    val isLoading: Boolean = false,
    val isCreating: Boolean = false,
    val error: String? = null,

    // Form fields
    val command: String = "",
    val runAt: String = "",
    val isRecurring: Boolean = false,
    val recurringInterval: String? = "Daily"
)

@HiltViewModel
class SchedulerViewModel @Inject constructor(
    private val velaRepository: SchedulesRepository,
    private val appEventManager: AppEventManager // Added
) : ViewModel() {

    private val _state = MutableStateFlow(SchedulerState())
    val state = _state.asStateFlow()

    init {
        observeTasks()
        refreshTasks()
    }

    private fun observeTasks() {
        velaRepository.observeScheduledTasks()
            .onEach { tasks -> _state.update { it.copy(tasks = tasks) } }
            .launchIn(viewModelScope)
    }

    fun refreshTasks() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            when (val result = velaRepository.getScheduledTasks()) {
                is Resource.Error -> {
                    _state.update { it.copy(error = result.message, isLoading = false) }
                    appEventManager.showActionErrorSnackbar(result.message)
                }

                else -> _state.update { it.copy(isLoading = false) }
            }
        }
    }

    fun updateCommand(value: String) {
        _state.update { it.copy(command = value) }
    }

    fun updateRunAt(value: String) {
        _state.update { it.copy(runAt = value) }
    }

    fun toggleRecurring(value: Boolean) {
        _state.update { it.copy(isRecurring = value) }
    }

    fun createTask() {
        val currentState = _state.value
        if (currentState.command.isBlank() || currentState.runAt.isBlank()) return

        viewModelScope.launch {
            appEventManager.setLoading(true)
            _state.update { it.copy(isCreating = true) }
            val result = velaRepository.createScheduledTask(
                command = currentState.command,
                runAt = currentState.runAt,
                recurring = if (currentState.isRecurring) currentState.recurringInterval else null
            )

            when (result) {
                is Resource.Success -> {
                    _state.update {
                        it.copy(
                            isCreating = false,
                            command = "",
                            runAt = "",
                            isRecurring = false,
                            error = null
                        )
                    }
                    appEventManager.showActionSuccessSnackbar("Task scheduled successfully")
                }

                is Resource.Error -> {
                    _state.update { it.copy(isCreating = false, error = result.message) }
                    appEventManager.showActionErrorSnackbar(result.message)
                }

                else -> {
                    _state.update { it.copy(isCreating = false) }
                }
            }
            appEventManager.setLoading(false)
        }
    }

    fun cancelTask(taskId: String) {
        viewModelScope.launch {
            appEventManager.setLoading(true)
            when (val result = velaRepository.cancelScheduledTask(taskId)) {
                is Resource.Success -> {
                    appEventManager.showActionSuccessSnackbar("Task cancelled")
                }

                is Resource.Error -> {
                    appEventManager.showActionErrorSnackbar(result.message)
                }

                else -> {}
            }
            appEventManager.setLoading(false)
        }
    }

    fun runTaskNow(taskId: String) {
        viewModelScope.launch {
            appEventManager.setLoading(true)
            when (val result = velaRepository.runTaskNow(taskId)) {
                is Resource.Success -> {
                    appEventManager.showActionSuccessSnackbar("Task execution triggered")
                }

                is Resource.Error -> {
                    appEventManager.showActionErrorSnackbar(result.message)
                }

                else -> {}
            }
            appEventManager.setLoading(false)
        }
    }

    fun formatToIsoTimestamp(dateMillis: Long, hour: Int, minute: Int): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = dateMillis
        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, minute)
        calendar.set(Calendar.SECOND, 0)
        return sdf.format(calendar.time)
    }
}