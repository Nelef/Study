package com.inzisoft.ibks.data.repository

import com.inzisoft.ibks.data.remote.model.ApiResult
import com.inzisoft.ibks.data.remote.model.LoginResponse
import com.inzisoft.ibks.data.remote.model.ModifyPwdResponse
import kotlinx.coroutines.flow.Flow

interface LoginRepository {

    // 로그인 (id = sabun [사번])
    suspend fun requestLogin(id: String, pwd: String, deviceId: String): Flow<ApiResult<LoginResponse>>

    // 기기등록
    suspend fun registerDevice(deviceId: String, id: String): Flow<ApiResult<Unit>>

    // 비밀번호 변경 (id = sabun [사번])
    suspend fun modifyPwd(id: String, pwd: String, newPwd: String): Flow<ApiResult<ModifyPwdResponse>>

    // 로그아웃
    suspend fun signOut(id: String): Flow<ApiResult<Unit>>
}