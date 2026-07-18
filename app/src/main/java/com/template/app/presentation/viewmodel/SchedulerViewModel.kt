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
    val cronExpression: String = ""
)

@HiltViewModel
class SchedulerViewModel @Inject constructor(
    private val velaRepository: SchedulesRepository,
    private val appEventManager: AppEventManager
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

    fun updateCronExpression(value: String) {
        _state.update { it.copy(cronExpression = value) }
    }

    fun resetForm() {
        _state.update {
            it.copy(
                command = "",
                runAt = "",
                isRecurring = false,
                cronExpression = "",
                error = null
            )
        }
    }

    fun createTask(onSuccess: () -> Unit = {}) {
        val currentState = _state.value
        if (currentState.command.isBlank() || currentState.runAt.isBlank()) return
        if (currentState.isRecurring && currentState.cronExpression.isBlank()) {
            appEventManager.showActionErrorSnackbar("Enter a cron expression for recurring tasks")
            return
        }

        val (command, args) = parseCommandLine(currentState.command)
        if (command.isBlank()) {
            appEventManager.showActionErrorSnackbar("Command is required")
            return
        }

        viewModelScope.launch {
            appEventManager.setLoading(true)
            _state.update { it.copy(isCreating = true) }
            val result = velaRepository.createScheduledTask(
                command = command,
                args = args,
                runAt = currentState.runAt,
                recurring = if (currentState.isRecurring) currentState.cronExpression.trim() else null
            )

            when (result) {
                is Resource.Success -> {
                    _state.update {
                        it.copy(
                            isCreating = false,
                            command = "",
                            runAt = "",
                            isRecurring = false,
                            cronExpression = "",
                            error = null
                        )
                    }
                    appEventManager.showActionSuccessSnackbar("Task scheduled successfully")
                    onSuccess()
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

    /**
     * DatePicker gives UTC midnight for the selected calendar day; combine with local hour/minute
     * and emit ISO-8601 with zone offset (e.g. 2026-07-18T22:00:00+03:00).
     */
    fun formatToIsoTimestamp(dateMillis: Long, hour: Int, minute: Int): String {
        val utcCal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            timeInMillis = dateMillis
        }
        val localTz = TimeZone.getDefault()
        val cal = Calendar.getInstance(localTz).apply {
            set(Calendar.YEAR, utcCal.get(Calendar.YEAR))
            set(Calendar.MONTH, utcCal.get(Calendar.MONTH))
            set(Calendar.DAY_OF_MONTH, utcCal.get(Calendar.DAY_OF_MONTH))
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US)
        sdf.timeZone = localTz
        return sdf.format(cal.time)
    }

    private fun parseCommandLine(input: String): Pair<String, List<String>> {
        val parts = input.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
        if (parts.isEmpty()) return "" to emptyList()
        return parts.first() to parts.drop(1)
    }
}
