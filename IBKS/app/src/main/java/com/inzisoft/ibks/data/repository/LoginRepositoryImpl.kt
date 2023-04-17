package com.inzisoft.ibks.data.repository

import com.inzisoft.ibks.data.remote.BaseRemoteDataSource
import com.inzisoft.ibks.data.remote.api.AuthApiService
import com.inzisoft.ibks.data.remote.api.UserApiService
import com.inzisoft.ibks.data.remote.model.*
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class LoginRepositoryImpl @Inject constructor(
    private val authApiService: AuthApiService,
    private val userApiService: UserApiService,
    private val remoteDataSource: BaseRemoteDataSource
) : LoginRepository {
    // 로그인
    override suspend fun requestLogin(
        id: String,
        password: String,
        deviceId: String
    ): Flow<ApiResult<LoginResponse>> {
        return remoteDataSource.apiCall {
            val loginRequest = LoginRequest(id, password, deviceId)
            authApiService.signIn(loginRequest)
        }
    }

    override suspend fun registerDevice(deviceId: String, id: String): Flow<ApiResult<Unit>> {
        return remoteDataSource.apiCall {
            val registDeviceRequest = RegistDeviceRequest(id = id, deviceId = deviceId)
            authApiService.registDevice(registDeviceRequest)
        }
    }

    override suspend fun modifyPwd(
        id: String,
        pwd: String,
        newPwd: String
    ): Flow<ApiResult<ModifyPwdResponse>> {
        return remoteDataSource.apiCall {
            val modifyPwdRequest = ModifyPwdRequest(sabun = id, pwd = pwd, newPwd = newPwd)
            userApiService.modifyPwd(modifyPwdRequest)
        }
    }

    override suspend fun signOut(id: String): Flow<ApiResult<Unit>> {
        return remoteDataSource.apiCall {
            val signOutRequest = SignOutRequest(userid = id)
            userApiService.signOut(signOutRequest)
        }
    }
}