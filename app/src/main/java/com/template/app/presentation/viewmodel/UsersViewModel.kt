package com.template.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.template.app.core.utils.Resource
import com.template.app.domain.model.User
import com.template.app.domain.repository.UserRepository
import com.template.app.domain.usecase.CreateUserUseCase
import com.template.app.domain.usecase.DeleteUserUseCase
import com.template.app.domain.usecase.GetUsersUseCase
import com.template.app.domain.usecase.UpdateUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

// ──── UI State sealed class ──────────────────────────────────────────────────
sealed class UsersUiState {
    data object Idle : UsersUiState()
    data object Loading : UsersUiState()
    data class Success(val users: List<User>) : UsersUiState()
    data class Error(val message: String) : UsersUiState()
}

// ──── One-shot events (snackbars, navigation, etc.) ──────────────────────────
sealed class UsersEvent {
    data class ShowSnackbar(val message: String) : UsersEvent()
    data class NavigateToDetail(val userId: String) : UsersEvent()
}

@HiltViewModel
class UsersViewModel @Inject constructor(
    private val getUsersUseCase: GetUsersUseCase,
    private val createUserUseCase: CreateUserUseCase,
    private val updateUserUseCase: UpdateUserUseCase,
    private val deleteUserUseCase: DeleteUserUseCase,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UsersUiState>(UsersUiState.Idle)
    val uiState: StateFlow<UsersUiState> = _uiState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    // SharedFlow for one-shot events (won't replay on recomposition)
    private val _events = MutableSharedFlow<UsersEvent>()
    val events: SharedFlow<UsersEvent> = _events.asSharedFlow()

    // Observe live DB updates reactively
    val cachedUsers: StateFlow<List<User>> = userRepository
        .observeUsers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        loadUsers()
    }

    fun loadUsers() {
        viewModelScope.launch {
            _uiState.value = UsersUiState.Loading
            fetchUsersInternal()
        }
    }

    fun refreshUsers() {
        viewModelScope.launch {
            _isRefreshing.value = true
            fetchUsersInternal()
            _isRefreshing.value = false
        }
    }

    private suspend fun fetchUsersInternal() {
        when (val result = getUsersUseCase()) {
            is Resource.Success -> _uiState.value = UsersUiState.Success(result.data)
            is Resource.Error -> _uiState.value = UsersUiState.Error(result.message)
            is Resource.Loading -> _uiState.value = UsersUiState.Loading
        }
    }

    fun onUserClicked(userId: String) {
        viewModelScope.launch {
            _events.emit(UsersEvent.NavigateToDetail(userId))
        }
    }

    fun addUser(displayName: String, email: String) {
        viewModelScope.launch {
            _uiState.value = UsersUiState.Loading
            when (val result = createUserUseCase(displayName, email)) {
                is Resource.Success -> {
                    _events.emit(UsersEvent.ShowSnackbar("User added successfully"))
                    loadUsers() // Refresh list
                }
                is Resource.Error -> {
                    _uiState.value = UsersUiState.Error(result.message)
                    _events.emit(UsersEvent.ShowSnackbar(result.message))
                }
                else -> Unit
            }
        }
    }

    fun editUser(id: String, displayName: String, email: String) {
        viewModelScope.launch {
            _uiState.value = UsersUiState.Loading
            when (val result = updateUserUseCase(id, displayName, email)) {
                is Resource.Success -> {
                    _events.emit(UsersEvent.ShowSnackbar("User updated successfully"))
                    loadUsers()
                }
                is Resource.Error -> {
                    _uiState.value = UsersUiState.Error(result.message)
                    _events.emit(UsersEvent.ShowSnackbar(result.message))
                }
                else -> Unit
            }
        }
    }

    fun deleteUser(id: String) {
        viewModelScope.launch {
            _uiState.value = UsersUiState.Loading
            when (val result = deleteUserUseCase(id)) {
                is Resource.Success -> {
                    _events.emit(UsersEvent.ShowSnackbar("User deleted successfully"))
                    loadUsers()
                }
                is Resource.Error -> {
                    _uiState.value = UsersUiState.Error(result.message)
                    _events.emit(UsersEvent.ShowSnackbar(result.message))
                }
                else -> Unit
            }
        }
    }
}
