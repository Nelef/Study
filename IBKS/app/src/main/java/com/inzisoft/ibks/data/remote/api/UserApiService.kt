package com.inzisoft.ibks.data.remote.api

import com.inzisoft.ibks.BuildConfig
import com.inzisoft.ibks.data.remote.model.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.PUT

interface UserApiService {
    /**
     * modifyPwd
     */
    @PUT("/${BuildConfig.MAIN_API}/user/modifyPwd")
    suspend fun modifyPwd(@Body modifyPwdRequest: ModifyPwdRequest): Response<ApiResponse<ModifyPwdResponse>>

    /**
     * signOut
     */
    @POST("/${BuildConfig.MAIN_API}/user/signOut")
    suspend fun signOut(@Body signOutRequest: SignOutRequest): Response<ApiResponse<Unit>>
}