package com.inzisoft.ibks.data.remote.api

import com.inzisoft.ibks.BuildConfig
import com.inzisoft.ibks.data.remote.model.ApiResponse
import com.inzisoft.ibks.data.remote.model.LoginRequest
import com.inzisoft.ibks.data.remote.model.LoginResponse
import com.inzisoft.ibks.data.remote.model.RegistDeviceRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {
    /**
     * Login API
     */
    @POST("/${BuildConfig.MAIN_API}/auth/signIn")
    suspend fun signIn(@Body loginRequest: LoginRequest): Response<ApiResponse<LoginResponse>>

    /**
     * Register Device
     */
    @POST("/${BuildConfig.MAIN_API}/auth/device/regist")
    suspend fun registDevice(@Body registRequest: RegistDeviceRequest): Response<ApiResponse<Unit>>
}