package com.inzisoft.ibks.data.repository

import com.inzisoft.ibks.data.remote.BaseRemoteDataSource
import com.inzisoft.ibks.data.remote.api.ApkApiService
import com.inzisoft.ibks.data.remote.model.ApiResult
import com.inzisoft.ibks.data.remote.model.ApplicationVersionResponse
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ApplicationUpdateRepositoryImpl @Inject constructor(
    private val apkApiService: ApkApiService,
    private val remoteDataSource: BaseRemoteDataSource
) : ApplicationUpdateRepository {

    override suspend fun checkVersion(): Flow<ApiResult<ApplicationVersionResponse>> {
        return remoteDataSource.apiCall {
            apkApiService.version()
        }
    }
}