package com.inzisoft.ibks.data.internal

import android.content.Context
import android.text.TextUtils
import androidx.annotation.Keep
import androidx.compose.ui.unit.TextUnit
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import com.google.gson.Gson
import com.inzisoft.ibks.AuthType
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
            AuthInfo("")
        }
    }

    object AuthInfoSerializer: Serializer<AuthInfo> {
        override val defaultValue: AuthInfo
            get() = AuthInfo("")

        override fun readFrom(input: InputStream): AuthInfo {
            return Gson().fromJson(input.readBytes().decodeToString(), AuthInfo::class.java)
        }

        override fun writeTo(t: AuthInfo, output: OutputStream) {
            output.write(Gson().toJson(t).toByteArray())
        }
    }

    @Keep
    class AuthInfo(val edocKey: String) {
        var name = ""
        var phone = ""
        var firstIdNum = ""
        var lastIdNum = ""
        var birth = ""
        // 신분증 타입
        var authType = ""
        // 촬영모드 (OCR 인식/일반 촬영/직접 입력)
        var takeType = ""
        var isAuthComplete = false
        var issuedDate = ""
        var issuedOffice = ""
        var driveLicenseNo = ""
    }
}