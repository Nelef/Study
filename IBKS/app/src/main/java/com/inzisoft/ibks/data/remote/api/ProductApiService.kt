package com.inzisoft.ibks.data.remote.api

import com.inzisoft.ibks.BuildConfig
import com.inzisoft.ibks.data.remote.model.*
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface ProductApiService {
    // Fund docs 목록 호출.
    @GET("/${BuildConfig.MAIN_API}/product/docs")
    suspend fun docs(@Query("prdCd") prdCd: String): Response<ApiResponse<List<DocsResponse>>>
}