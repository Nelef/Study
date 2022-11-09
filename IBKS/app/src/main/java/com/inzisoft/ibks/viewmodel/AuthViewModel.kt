package com.inzisoft.ibks.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.util.Base64
import android.util.Pair
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import bio.face.FaceDetect
import com.inzisoft.ibks.AuthType
import com.inzisoft.ibks.Constants.KEY_AUTH_CAMERA_DATA
import com.inzisoft.ibks.R
import com.inzisoft.ibks.TakeType
import com.inzisoft.ibks.base.BaseDialogFragmentViewModel
import com.inzisoft.ibks.data.internal.AuthDialogData
import com.inzisoft.ibks.data.remote.model.ApiResult
import com.inzisoft.ibks.data.repository.LocalRepository
import com.inzisoft.ibks.data.web.AuthCameraData
import com.inzisoft.ibks.util.CryptoUtil
import com.inzisoft.ibks.util.FaceDetectHelper
import com.inzisoft.ibks.viewmodel.AuthData.Companion.FRONT_IDNUM
import com.inzisoft.ibks.viewmodel.AuthData.Companion.ISSUE_DATE
import com.inzisoft.ibks.viewmodel.AuthData.Companion.ISSUE_OFFICE
import com.inzisoft.ibks.viewmodel.AuthData.Companion.LAST_IDNUM
import com.inzisoft.ibks.viewmodel.AuthData.Companion.LICNUM0_1
import com.inzisoft.ibks.viewmodel.AuthData.Companion.LICNUM10_11
import com.inzisoft.ibks.viewmodel.AuthData.Companion.LICNUM2_3
import com.inzisoft.ibks.viewmodel.AuthData.Companion.LICNUM4_9
import com.inzisoft.ibks.viewmodel.AuthData.Companion.NAME
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.*

open class AuthViewModel constructor(
    val context: Context,
    internal val savedStateHandle: SavedStateHandle,
    internal val localRepository: LocalRepository
) : BaseDialogFragmentViewModel() {
    var authCameraData: AuthCameraData =
        savedStateHandle.get<AuthCameraData>(KEY_AUTH_CAMERA_DATA) ?: AuthCameraData("", "", "", "", "")

    var dialogState by mutableStateOf<AuthDialogData>(AuthDialogData.None)
    var authDataState by mutableStateOf<AuthData>(AuthData.IdCradData())

    private fun check(dataMap: SnapshotStateMap<String, String>): Boolean {
        dataMap.keys.forEach { key ->
            if(dataMap[key].toString().isEmpty()) {
                return false
            }
        }

        return true
    }

    fun auth(takeType: TakeType, result: (Boolean) -> Unit) {
        if(authDataState.faceImage == null) {
            showAuthFailedDialog("얼굴사진 없음")
            return
        }

        viewModelScope.launch {
            if (check(authDataState.dataMap)) {
                FaceDetectHelper().processFaceDetect(
                    context,
                    authDataState.faceImage!!,
                    0
                ) { result, messageOrFaDetectString ->
                    if (result) {
                        when (authDataState) {
                            is AuthData.OverSea,
                            is AuthData.IdCradData -> {
                                val authType = if (authDataState is AuthData.OverSea) {
                                    AuthType.OVERSEA
                                } else {
                                    AuthType.IDCARD
                                }
                                // TODO 신분증 진위확인 인증
//                        repository.verifyIdcard(authDataState.dataMap)
//                            .collect { apiResult ->
//                                handleApiResult(
//                                    apiResult,
//                                    authType,
//                                    takeType,
//                                    authDataState.dataMap,
//                                    result
//                                )
//                            }
                            }
                            is AuthData.DriveLicenseData -> {}
//                        repository.verifyDriverLicense(authDataState.dataMap)
//                        .collect { apiResult ->
//                            handleApiResult(
//                                apiResult,
//                                AuthType.DRIVE_LECENSE,
//                                takeType,
//                                authDataState.dataMap,
//                                result
//                            )
//                        }
                        }
                    } else {
                        showAuthFailedDialog(messageOrFaDetectString)
                    }
                }
            } else {
                showAuthFailedDialog(context.getString(R.string.empty_auth_field))
            }
        }
    }

    private suspend fun handleApiResult(apiResult: ApiResult<Unit>, authType: AuthType, takeType: TakeType, dataMap: Map<String, String>, result: (Boolean) -> Unit) {
        when (apiResult) {
            is ApiResult.Loading -> { dialogState = AuthDialogData.Loading }
            is ApiResult.Success -> {
                val authInfo = localRepository.getAuthInfo().first()
                authInfo.name = dataMap[NAME].toString().trim()
                authInfo.firstIdNum = dataMap[FRONT_IDNUM].toString().trim()
                authInfo.lastIdNum = dataMap[LAST_IDNUM].toString().trim()
                authInfo.authType = authType.type
                authInfo.takeType = takeType.type
                authInfo.birth = getBirth(dataMap[FRONT_IDNUM].toString().trim(), dataMap[LAST_IDNUM].toString().trim())
                authInfo.isAuthComplete = true
                authInfo.issuedDate = dataMap[ISSUE_DATE].toString().trim()
                authInfo.issuedOffice = dataMap[ISSUE_OFFICE].toString().trim()
                if(authType == AuthType.DRIVE_LECENSE) {
                    authInfo.driveLicenseNo = "${dataMap[LICNUM0_1]}-${dataMap[LICNUM2_3]}-${dataMap[LICNUM4_9]}-${dataMap[LICNUM10_11]}"
                } else {
                    authInfo.driveLicenseNo = ""
                }
                localRepository.setAuthInfo(authInfo = authInfo)
                result(true)
            }
            is ApiResult.Error -> {
                showAuthFailedDialog(apiResult.message)
            }
        }
    }

    private fun showAuthFailedDialog(message: String) {
        dialogState = AuthDialogData.AuthFailedPopup(message)
    }

    fun showAuthGuideDialog() {
        dialogState = AuthDialogData.ShowAuthGuidePopup
    }

    fun closeAuthGuideDialog() {
        dialogState = AuthDialogData.None
    }

    fun setAuthData(authData: AuthData) {
        authDataState = authData
    }

    fun setDetailData(key: String, value: String) {
        authDataState.dataMap[key] = value
    }

    private fun getBirth(firstIdNum: String, lastEncIdNum: String): String {
        val encryptedLastIdNum = Base64.decode(lastEncIdNum, Base64.NO_WRAP)
        val decryptedLastIdNum: ByteArray = CryptoUtil.decrypt(context, encryptedLastIdNum)
        val firstIdNumFirstNum = when (Character.getNumericValue(decryptedLastIdNum[0].toInt())) {
            in 1..2 -> "19"
            else -> "20"
        }

        Arrays.fill(decryptedLastIdNum, 0)

        return "${firstIdNumFirstNum}${firstIdNum.substring(0, 2)}-${firstIdNum.substring(2, 4)}-${firstIdNum.substring(4)}"
    }
}

sealed class AuthData(val dataMap: SnapshotStateMap<String, String> = mutableStateMapOf(), val idCardBitmap: Bitmap? = null, val faceImage: ByteArray? = null) {
    companion object {
        val NAME = "ownerNm"
        val FRONT_IDNUM = "juminNo1"
        val LAST_IDNUM = "juminNo2"
        val ISSUE_DATE = "issueDt"
        val ISSUE_OFFICE = "issuedOffice"
//        val BIRTHDAY = "birth"
        val LICNUM0_1 = "license01"
        val LICNUM2_3 = "license02"
        val LICNUM4_9 = "license03"
        val LICNUM10_11 = "license04"
        val COUNTRY = "country"
    }

    class IdCradData(bitmap: Bitmap? = null, faceImage: ByteArray? = null) : AuthData(idCardBitmap = bitmap, faceImage = faceImage)
    class DriveLicenseData(bitmap: Bitmap? = null, faceImage: ByteArray? = null) : AuthData(idCardBitmap = bitmap, faceImage = faceImage)
    class OverSea(bitmap: Bitmap? = null, faceImage: ByteArray? = null) : AuthData(idCardBitmap = bitmap, faceImage = faceImage)
    class ForeignData : AuthData()
    class PassportData : AuthData()
}