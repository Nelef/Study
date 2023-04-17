package com.inzisoft.ibks.data.internal

import android.content.Context
import androidx.annotation.Keep
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject

class UserInfoDataSource @Inject constructor(private val context: Context) {
    private val Context.userInfoDataStore: DataStore<UserInfo> by dataStore(
        fileName = "user_info.pb",
        serializer = UserInfoSerializer
    )

    fun getUserInfo(): Flow<UserInfo> {
        return context.userInfoDataStore.data
    }

    suspend fun setUserInfo(userInfo: UserInfo) {
        context.userInfoDataStore.updateData {
            userInfo
        }
    }

    suspend fun clearUserInfo() {
        context.userInfoDataStore.updateData {
            UserInfoSerializer.defaultValue
        }
    }

    suspend fun hasUserInfo(): Boolean {
        val userInfo = getUserInfo().first()
        return userInfo != UserInfoSerializer.defaultValue
    }

    object UserInfoSerializer : Serializer<UserInfo> {
        override val defaultValue: UserInfo
            get() = UserInfo(
                "", "", "", "", "",
                "", "", "", "", "", "", false, false
            )

        override fun readFrom(input: InputStream): UserInfo {
            return Gson().fromJson(input.readBytes().decodeToString(), UserInfo::class.java)
        }

        override fun writeTo(t: UserInfo, output: OutputStream) {
            output.write(Gson().toJson(t).toByteArray())
        }
    }

    @Keep
    data class UserInfo(
        val name: String,
        val sabun: String,
        val jikgubNm: String,
        val jikgubCd: String,
        val orgNm: String,
        val orgCd: String,
        val mailId: String,
        val chiefYn: String?,
        val handPhone: String,
        val brnNo: String,
        val brnNm: String,
        val tabletOtpPass: Boolean,
        val tabletSndAuth: Boolean
    )
}