package com.inzisoft.ibks.data.repository

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import com.inzisoft.ibks.AppKeySet
import com.inzisoft.ibks.data.internal.AuthInfoDataSource
import com.inzisoft.ibks.data.internal.PreferenceDataSource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class LocalRepository @Inject constructor(
    private val preferenceDataSource: PreferenceDataSource,
    private val authInfoDataSource: AuthInfoDataSource
) {

    private val _expiredSession = mutableStateOf(false)
    val expiredSession: State<Boolean> = _expiredSession

    fun getAccessToken(): Flow<String> {
        return preferenceDataSource.getPreference(AppKeySet.ACCESS_TOKEN)
    }

    suspend fun setAccessToken(token: String) {
        preferenceDataSource.setPreference(AppKeySet.ACCESS_TOKEN, token)
    }

    suspend fun clearAccessToken() {
        setAccessToken("")

//        if (userInfoDataSource.hasUserInfo()) {
            _expiredSession.value = true
//        }
    }

    fun getAuthInfo(): Flow<AuthInfoDataSource.AuthInfo> {
        return authInfoDataSource.getAuthInfo()
    }

    suspend fun setAuthInfo(authInfo: AuthInfoDataSource.AuthInfo) {
        authInfoDataSource.setAuthInfo(authInfo)
    }

    suspend fun clearAuthInfo() {
        authInfoDataSource.clearAuthInfo()
    }

    suspend fun clear() {
        clearAuthInfo()
        clearAccessToken()
        _expiredSession.value = false
    }
}