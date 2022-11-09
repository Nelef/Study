package com.inzisoft.ibks.data.remote

import com.google.gson.Gson
import com.inzisoft.ibks.ExceptionCode
import com.inzisoft.ibks.data.remote.converter.CryptoService
import com.inzisoft.ibks.data.remote.model.ApiResponse
import com.inzisoft.ibks.data.remote.model.ApiResult
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import java.net.ConnectException
import javax.inject.Inject

@ViewModelScoped
open class BaseRemoteDataSourceImpl @Inject constructor(private val cryptoService: CryptoService): BaseRemoteDataSource {

    @Suppress("UNCHECKED_CAST")
    override suspend fun <T> apiCall(request: suspend () -> Response<ApiResponse<T>>): Flow<ApiResult<T>> =
        flow {
            emit(ApiResult.Loading())

            val response = request()
            emit(
                if (response.isSuccessful) {
                    val body = response.body() as? ApiResponse<*> ?: throw NullPointerException(
                        "Body is empty."
                    )

                    if (body.status == 200) {
                        val headers = response.headers().toMultimap()
//                            val data =
//                                body.data as? T ?: throw NullPointerException("Data is empty")
                        val data = body.data as T
                        ApiResult.Success(data, headers)
                    } else {
                        ApiResult.Error(body.status, body.message)
                    }
                } else {
                    val errorBody = response.errorBody()

                    if (errorBody == null) {
                        ApiResult.Error(response.code(), response.message())
                    } else {
                        val body =
                            Gson().fromJson(errorBody.charStream(), ApiResponse::class.java)
                        ApiResult.Error(body.status, body.message)
                    }
                }
            )
        }.flowOn(Dispatchers.IO)
            .catch { e ->
                emit(handleException(e))
            }

    override suspend fun download(needDecrypt: Boolean, request: suspend () -> Response<ResponseBody>): Flow<ApiResult<ByteArray>> =
        flow {
            emit(ApiResult.Loading())

            val response = request()
            emit(
                if (response.isSuccessful) {
                    val body = response.body() ?: throw NullPointerException("Body is empty.")
                    val headers = response.headers().toMultimap()

                    if (needDecrypt) {
                        ApiResult.Success(cryptoService.decrypt(body.bytes()), headers)
                    } else {
                        ApiResult.Success(body.bytes(), headers)
                    }
                } else {
                    ApiResult.Error(response.code(), response.message())
                }
            )

        }
            .flowOn(Dispatchers.IO)
            .catch { e ->
                emit(handleException(e))
            }

    private fun <T> handleException(e: Throwable): ApiResult.Error<T> {
        return if (e is ConnectException) {
            ApiResult.Error(ExceptionCode.EXCEPTION_CODE_INTERNAL_CONNECTION, e.message ?: "connection fail", exception = e)
        } else {
            ApiResult.Error(ExceptionCode.EXCEPTION_CODE_INTERNAL_UNKNOWN, e.message ?: "unknown", exception = e)
        }
    }

    override fun toMultipartFile(
        name: String,
        fileName: String,
        data: ByteArray,
        contentType: String,
        isEncrypt: Boolean
    ): MultipartBody.Part {
        val requestBody = RequestBody.create(
            MediaType.parse(contentType),
            if (isEncrypt) cryptoService.encrypt(data) else data
        )
        return MultipartBody.Part.createFormData(name, fileName, requestBody)
    }

    override fun toMultipartJson(name: String, data: String, isEncrypt: Boolean): MultipartBody.Part {
        val requestBody = RequestBody.create(
            MediaType.parse("application/json"),
            if (isEncrypt) cryptoService.encrypt(data.toByteArray()) else data.toByteArray()
        )
        return MultipartBody.Part.createFormData(name, null, requestBody)
    }

}