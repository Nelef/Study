package com.inzisoft.ibks.data.remote.model

sealed class ApiResult<out T> {
    class Loading<T> : ApiResult<T>()
    data class Success<T>(val data: T, val headers: Map<String, List<String>>? = null) : ApiResult<T>()
    data class Error<T>(val code: Int = 0, val message: String = "", val exception: Throwable? = null) : ApiResult<T>()
}