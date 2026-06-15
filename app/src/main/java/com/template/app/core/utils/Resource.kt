package com.template.app.core.utils

/**
 * A sealed class that wraps data from any data source (network, DB, etc.)
 * and models three distinct UI states: Loading, Success, and Error.
 *
 * Usage in ViewModel:
 *   private val _uiState = MutableStateFlow<Resource<List<User>>>(Resource.Loading)
 *   val uiState = _uiState.asStateFlow()
 *
 * Usage in Composable:
 *   when (val state = uiState.collectAsStateWithLifecycle().value) {
 *       is Resource.Loading  -> LoadingIndicator()
 *       is Resource.Success  -> UserList(state.data)
 *       is Resource.Error    -> ErrorMessage(state.message)
 *   }
 */
sealed class Resource<out T> {
    data object Loading : Resource<Nothing>()
    data class Success<T>(val data: T) : Resource<T>()
    data class Error(val message: String, val throwable: Throwable? = null) : Resource<Nothing>()

    val isLoading get() = this is Loading
    val isSuccess get() = this is Success
    val isError get() = this is Error

    fun getOrNull(): T? = (this as? Success)?.data
}
