package com.inzisoft.ibks.data.remote.api

import com.inzisoft.ibks.BuildConfig
import com.inzisoft.ibks.data.remote.model.*
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface EdsApiService {

    /**
     * 임시저장 업로드(Web Data) API
     */
    @Multipart
    @POST("/${BuildConfig.MAIN_API}/eds/edoc/tempData")
    suspend fun tempData(@Part partList: List<MultipartBody.Part>): Response<ApiResponse<Unit>>

    @POST("/${BuildConfig.MAIN_API}/eds/edoc/grpkey")
    suspend fun generateEntryId(@Body requestEntryIdData: RequestEntryIdData): Response<ApiResponse<ResponseEntryIdData>>

    @Multipart
    @POST("/${BuildConfig.MAIN_API}/eds/edoc/result")
    suspend fun uploadResult(@Part file: MultipartBody.Part, @Part info: MultipartBody.Part): Response<ApiResponse<Unit>>

    @Multipart
    @POST("/${BuildConfig.MAIN_API}/eds/edoc/tempAttach")
    suspend fun tempAttach(@Part partList: List<MultipartBody.Part>): Response<ApiResponse<Unit>>

    @POST("/${BuildConfig.MAIN_API}/eds/edoc/tempDelete")
    suspend fun deleteTemp(@Body requestEDSBasicData: RequestEDSBasicData): Response<ApiResponse<Unit>>

    @POST("/${BuildConfig.MAIN_API}/eds/edoc/tempDownload")
    suspend fun downloadTemp(@Body requestEDSBasicData: RequestEDSBasicData): Response<ResponseBody>

    @POST("/${BuildConfig.MAIN_API}/eds/edoc/edocBatch")
    suspend fun insertEDocBatch(@Body requestEDSBatchData: RequestEDSBatchData): Response<ApiResponse<Unit>>
    // Fund pdf 다운로드
    @GET("/${BuildConfig.MAIN_API}/eds/eform/{provId}/{folderName}/{fileName}")
    suspend fun downloadEform(@Path("provId") provId: String, @Path("folderName") folderName: String, @Path("fileName") fileName: String): Response<ResponseBody>

    @GET("/${BuildConfig.MAIN_API}/eds/eform/{filePath}")
    suspend fun downloadForm(@Path(value = "filePath", encoded = true) filePath: String): Response<ResponseBody>

}