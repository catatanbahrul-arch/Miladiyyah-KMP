package id.wahidiyah.miladiyyah.core.utils

sealed class UiState<out T> {
    object Loading : UiState<Nothing>()
    object Empty : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String, val isOffline: Boolean = false) : UiState<Nothing>()
}
