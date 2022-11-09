package com.inzisoft.ibks.data.repository

import com.inzisoft.ibks.data.remote.model.ApiResult
import com.inzisoft.ibks.data.remote.model.LoginResponse
import kotlinx.coroutines.flow.Flow

interface LoginRepository {

    // 로그인
    suspend fun requestLogin(deviceId: String, id: String, password: String): Flow<ApiResult<LoginResponse>>

    // 기기등록
    suspend fun registerDevice(deviceId: String, id: String): Flow<ApiResult<Unit>>

}