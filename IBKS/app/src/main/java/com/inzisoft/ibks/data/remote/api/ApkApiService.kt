package com.inzisoft.ibks.data.remote.api

import com.inzisoft.ibks.BuildConfig
import com.inzisoft.ibks.data.remote.model.ApiResponse
import com.inzisoft.ibks.data.remote.model.ApplicationVersionResponse
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface ApkApiService {
    /**
     * App Version
     */
    @GET("/${BuildConfig.MAIN_API}/apk/version")
    suspend fun version(): Response<ApiResponse<ApplicationVersionResponse>>


    /**
     * Apk Download
     */
    @GET("/${BuildConfig.MAIN_API}/apk/download")
    @Headers("Content-Type: application/octet-stream", "Accept: application/octet-stream")
    suspend fun download(@Query("version") version: String, @Query("name") fileName: String): Response<ResponseBody>
}