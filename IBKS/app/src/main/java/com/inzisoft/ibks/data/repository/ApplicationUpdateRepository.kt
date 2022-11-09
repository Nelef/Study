package com.inzisoft.ibks.data.repository

import com.inzisoft.ibks.data.remote.model.ApiResult
import com.inzisoft.ibks.data.remote.model.ApplicationVersionResponse
import kotlinx.coroutines.flow.Flow

interface ApplicationUpdateRepository {
    // check latest version
    suspend fun checkVersion(): Flow<ApiResult<ApplicationVersionResponse>>
}