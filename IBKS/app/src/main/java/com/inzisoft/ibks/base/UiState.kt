package com.inzisoft.ibks.base

sealed class UiState<out T> {
    object None : UiState<Nothing>()
    object Finish : UiState<Nothing>()
    data class Loading(val message: String? = null) : UiState<Nothing>()
    data class Success<T : Any>(val data: T) : UiState<T>()
    data class Error(val message: String = "", val exception: Exception? = null, val code: Int = 0) : UiState<Nothing>()
}