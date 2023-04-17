package com.inzisoft.ibks.data.repository

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import com.inzisoft.ibks.AppKeySet
import com.inzisoft.ibks.PathManager
import com.inzisoft.ibks.data.internal.*
import com.inzisoft.ibks.data.remote.model.LoginResponse
import com.inzisoft.ibks.util.FileUtils
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class LocalRepository @Inject constructor(
    private val preferenceDataSource: PreferenceDataSource,
    private val userInfoDataSource: UserInfoDataSource,
    private val authInfoDataSource: AuthInfoDataSource,
    private val paperlessSaveInfoDataSource: PaperlessSaveInfoDataSource,
    private val pathManager: PathManager
) {

    private val _expiredSession = mutableStateOf(false)
    val expiredSession: State<Boolean> = _expiredSession

    fun getAccessToken(): Flow<String> {
        return preferenceDataSource.getPreference(AppKeySet.ACCESS_TOKEN)
    }

    fun getCookie(): Flow<String> {
        return preferenceDataSource.getPreference(AppKeySet.SET_COOKIE)
    }

    suspend fun setAccessToken(token: String) {
        preferenceDataSource.setPreference(AppKeySet.ACCESS_TOKEN, token)
    }

    suspend fun setCookie(cookie: String) {
        preferenceDataSource.setPreference(AppKeySet.SET_COOKIE, cookie)
    }

    suspend fun clearAccessToken() {
        setAccessToken("")

        if (userInfoDataSource.hasUserInfo()) {
            _expiredSession.value = true
        }
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

    suspend fun clearUserInfo() {
        userInfoDataSource.clearUserInfo()
    }

    fun getUserInfo(): Flow<UserInfoDataSource.UserInfo> {
        return userInfoDataSource.getUserInfo()
    }

    suspend fun setUserInfo(loginResponse: LoginResponse, handPhone: String) {
        val userInfo = UserInfoDataSource.UserInfo(
            name = loginResponse.name,
            sabun = loginResponse.sabun,
            jikgubNm = loginResponse.jikgubNm,
            jikgubCd = loginResponse.jikgubCd,
            orgNm = loginResponse.orgNm,
            orgCd = loginResponse.orgCd,
            mailId = loginResponse.mailId,
            chiefYn = loginResponse.chiefYn,
            handPhone = handPhone,
            brnNo = loginResponse.brnNo,
            brnNm = loginResponse.brnNm,
            tabletOtpPass = loginResponse.tabletOtpPass,
            tabletSndAuth = loginResponse.tabletSndAuth
        )

        userInfoDataSource.setUserInfo(userInfo)

        val crashlytics = Firebase.crashlytics
        crashlytics.setUserId(userInfo.sabun)
        crashlytics.setCustomKey("이름", userInfo.jikgubNm)
        crashlytics.setCustomKey("지점번호", userInfo.brnNo)
        crashlytics.setCustomKey("지점명", userInfo.brnNm)
    }

    suspend fun clear() {
        clearAuthInfo()
        clearUserInfo()
        clearAccessToken()
        removeIdCardImage()
        _expiredSession.value = false
    }

    fun clearCacheDir() {
        FileUtils.delete(pathManager.getCacheDir())
    }

    fun removeIdCardImage() {
        val imageDirPath = pathManager.getIdCardTempDir()
        FileUtils.delete(imageDirPath)
    }

    fun saveIdCardImage(context: Context, docCode: String, bitmap: Bitmap?) {
        bitmap?.apply {
            val imagePath = pathManager.getIdCardJpgImage(docCode = docCode, index = 0)
            FileUtils.saveToJpgImageFile(context = context, bitmap = this, imagePath = imagePath)
        }
    }

    suspend fun saveElectronicSaveData(paperlessSaveInfoData: PaperlessSaveInfoDataSource.PaperlessSaveInfoData) {
        paperlessSaveInfoDataSource.addPaperlessSaveInfo(paperlessSaveInfoData)
    }

    suspend fun removeElectronicSaveData(removeList: List<String>?) {
        paperlessSaveInfoDataSource.removePaperlessSaveInfo(removeList)
    }

    suspend fun clearElectronicSaveInfo() {
        paperlessSaveInfoDataSource.clearPaperlessSaveInfo()
    }

    fun getPaperlessSaveInfo(): Flow<PaperlessSaveInfoDataSource.PaperlessSaveInfo> {
        return paperlessSaveInfoDataSource.getPaperlessSaveInfo()
    }
}