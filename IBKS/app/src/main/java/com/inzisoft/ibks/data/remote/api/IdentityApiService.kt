package com.inzisoft.ibks.data.remote.api

import com.inzisoft.ibks.BuildConfig
import com.inzisoft.ibks.data.remote.model.ApiResponse
import com.inzisoft.ibks.data.remote.model.AuthRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface IdentityApiService {
    /**
     * 신분증 진위확인(주민등록증/운전면허증)
     */
    @POST("/${BuildConfig.MAIN_API}/identify/id")
    suspend fun verifyIdcard(@Body authRequest: AuthRequest): Response<ApiResponse<Unit>>
}