package com.inzisoft.ibks.data.remote

import com.inzisoft.ibks.BuildConfig
import com.inzisoft.ibks.data.remote.model.ApiResponse
import com.inzisoft.ibks.data.remote.model.ApiResult
import kotlinx.coroutines.flow.Flow
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Response

interface BaseRemoteDataSource {

    suspend fun <T> apiCall(request: suspend () -> Response<ApiResponse<T>>): Flow<ApiResult<T>>

    suspend fun download(needDecrypt: Boolean = BuildConfig.ENCRYPT_API, request: suspend () -> Response<ResponseBody>): Flow<ApiResult<ByteArray>>

    fun toMultipartFile(
        name: String,
        fileName: String,
        data: ByteArray,
        contentType: String,
        isEncrypt: Boolean = BuildConfig.ENCRYPT_API
    ): MultipartBody.Part

    fun toMultipartJson(
        name: String,
        data: String,
        isEncrypt: Boolean = BuildConfig.ENCRYPT_API
    ): MultipartBody.Part

}