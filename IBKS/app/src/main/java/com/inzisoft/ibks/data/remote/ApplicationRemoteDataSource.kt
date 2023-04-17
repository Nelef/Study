package com.inzisoft.ibks.data.remote

import com.inzisoft.ibks.data.remote.api.ApplicationService
import com.inzisoft.ibks.data.remote.model.*
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ApplicationRemoteDataSource @Inject constructor(
    private val applicationService: ApplicationService,
    private val remoteDataSource: BaseRemoteDataSource
) {
    suspend fun issueOTP(
        cellphone: String
    ): Flow<ApiResult<Unit>> {
        return remoteDataSource.apiCall {
            applicationService.issueOTP(RequestIssueOtp(cellphone))
        }
    }

    suspend fun confirmOTP(
        cellphone: String,
        otp: String
    ): Flow<ApiResult<Unit>> {
        return remoteDataSource.apiCall {
            applicationService.confirmOTP(RequestConfirmOtp(cellphone, otp))
        }
    }

}