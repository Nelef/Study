package com.inzisoft.ibks.base

import androidx.annotation.StringRes

sealed class UiState<out T> {
    object None : UiState<Nothing>()
    data class Loading(val message: String? = null, @StringRes val strResId: Int = 0) : UiState<Nothing>()
    data class Success<T : Any>(val data: T) : UiState<T>()
    data class Error(val message: String = "", val throwable: Throwable? = null, val code: Int = 0) : UiState<Nothing>()
}