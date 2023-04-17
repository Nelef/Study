package com.inzisoft.ibks.data.remote.api

import com.inzisoft.ibks.data.remote.model.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * Application service
 *
 * @constructor Create empty Application service
 */
interface ApplicationService {

//    /**
//     * 신청목록 조회
//     *
//     * @param page
//     * @param size
//     * @param data
//     * @return
//     */
//    @POST("/api/v1/appl/list")
//    suspend fun getApplicationList(
//        @Query("page") page: Int,
//        @Query("size") size: Int,
//        @Body data: RequestApplicationList
//    ): Response<ApiResponse<ResponseApplicationList>>


//    /**
//     * 대체번호(주민번호 대체) 조회(발급)
//     *
//     * @param data
//     * @return
//     */
//    @POST("/api/v1/appl/subNo")
//    suspend fun getAlternativeNumber(@Body data: RequestAlternativeNum): Response<ApiResponse<ResponseAlternativeNum>>

    /**
     * OTP 발급
     *
     * @param data
     * @return
     */
    @POST("/api/v1/appl/otpIssue")
    suspend fun issueOTP(@Body data: RequestIssueOtp): Response<ApiResponse<Unit>>

    /**
     * OTP 확인
     *
     * @param data
     * @return
     */
    @POST("/api/v1/appl/otpConfirm")
    suspend fun confirmOTP(@Body data: RequestConfirmOtp): Response<ApiResponse<Unit>>

}
