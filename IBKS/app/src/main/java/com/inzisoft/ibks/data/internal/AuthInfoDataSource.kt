package com.inzisoft.ibks.data.internal

import android.content.Context
import androidx.annotation.Keep
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject

class AuthInfoDataSource @Inject constructor(private val context: Context) {
    private val Context.authInfoDataStore: DataStore<AuthInfo> by dataStore(
        fileName = "auth_info.pb",
        serializer = AuthInfoSerializer
    )

    fun getAuthInfo(): Flow<AuthInfo> {
        return context.authInfoDataStore.data
    }

    suspend fun setAuthInfo(authInfo: AuthInfo) {
        context.authInfoDataStore.updateData {
            authInfo
        }
    }


    suspend fun clearAuthInfo() {
        context.authInfoDataStore.updateData {
            AuthInfo()
        }
    }

    object AuthInfoSerializer : Serializer<AuthInfo> {
        override val defaultValue: AuthInfo
            get() = AuthInfo()

        override fun readFrom(input: InputStream): AuthInfo {
            return Gson().fromJson(input.readBytes().decodeToString(), AuthInfo::class.java)
        }

        override fun writeTo(t: AuthInfo, output: OutputStream) {
            output.write(Gson().toJson(t).toByteArray())
        }
    }

    @Keep
    data class AuthInfo(
        val edocKey: String = "",
        val name: String = "",
        val phone: String = "",
        val firstIdNum: String = "",
        val lastIdNum: String = "",
        val birth: String = "",
        // 신분증 타입
        val authType: String = "",
        // 촬영모드 (OCR 인식/일반 촬영/직접 입력)
        val takeType: String = "",
        val isAuthComplete: Boolean = false,
        val issuedDate: String = "",
        val issuedOffice: String = "",
        val driveLicenseNo: String = ""
    )
}