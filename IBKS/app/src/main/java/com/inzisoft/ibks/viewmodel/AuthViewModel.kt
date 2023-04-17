package com.inzisoft.ibks.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.util.Base64
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.inzisoft.ibks.AuthType
import com.inzisoft.ibks.Constants.KEY_AUTH_CAMERA_DATA
import com.inzisoft.ibks.R
import com.inzisoft.ibks.TakeType
import com.inzisoft.ibks.base.BaseDialogFragmentViewModel
import com.inzisoft.ibks.data.internal.AuthDialogData
import com.inzisoft.ibks.data.remote.model.ApiResult
import com.inzisoft.ibks.data.remote.model.AuthRequest
import com.inzisoft.ibks.data.repository.CameraRepository
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.*

open class AuthViewModel constructor(
    val context: Context,
    internal val savedStateHandle: SavedStateHandle,
    internal val repository: CameraRepository,
    internal val localRepository: LocalRepository
) : BaseDialogFragmentViewModel() {
    var authCameraData: AuthCameraData =
        savedStateHandle.get<AuthCameraData>(KEY_AUTH_CAMERA_DATA) ?: AuthCameraData("", "", "", "")

    var dialogState by mutableStateOf<AuthDialogData>(AuthDialogData.None)
    var authDataState by mutableStateOf<AuthData>(AuthData.IdCradData())

    private fun check(dataMap: SnapshotStateMap<String, String>): Boolean {
        dataMap.keys.forEach { key ->
            if (dataMap[key].toString().isEmpty()) {
                return false
            }
        }

        return true
    }

    fun auth(takeType: TakeType) {
        if (authDataState.faceImage == null) {
            showAuthFailedDialog("얼굴사진 없음")
            return
        }
        dialogState = AuthDialogData.Loading
        viewModelScope.launch(Dispatchers.IO) {
            if (check(authDataState.dataMap)) {
                val faceDetectHelper = FaceDetectHelper()
                    faceDetectHelper.processFaceDetect(
                    context,
                    authDataState.faceImage!!,
                    0
                ) { result, messageOrFaDetectString, imageScore ->
                    viewModelScope.launch(Dispatchers.IO) {
                        faceDetectHelper.releaseFaceDetect()
                        if (result) {
                            val userInfo = localRepository.getUserInfo().first()
                            val typePair = when (authDataState) {
                                is AuthData.OverSea,
                                is AuthData.IdCradData -> {
                                    if (authDataState is AuthData.OverSea) {
                                        Pair("1", AuthType.OVERSEA) // 재외국민 신분증
                                    } else {
                                        Pair("1", AuthType.ID_CARD) // 주민등록증
                                    }
                                }
                                else -> Pair("2", AuthType.DRIVE_LICENSE) // 운전면허증
                            }

                            val authRequest = AuthRequest(
                                loginBrnNo = userInfo.brnNo,
                                loginEmpNo = userInfo.sabun,
                                loginPhoneNo = userInfo.handPhone,
                                idKindCode = typePair.first,
                                juminNo1 = authDataState.dataMap[FRONT_IDNUM].toString(),
                                juminNo2 = authDataState.dataMap[LAST_IDNUM].toString(),
                                inptPsnNm = authDataState.dataMap[NAME].toString(),
                                imgLen2 = messageOrFaDetectString.length,
                                strData5K = messageOrFaDetectString,
                                idIssDt = authDataState.dataMap[ISSUE_DATE].toString(),
                                imageScore = imageScore
                            )

                            if (typePair.first == "2") {
                                authRequest.license01 = authDataState.dataMap[LICNUM0_1]
                                authRequest.license02 = authDataState.dataMap[LICNUM2_3]
                                authRequest.license03 = authDataState.dataMap[LICNUM4_9]
                                authRequest.license04 = authDataState.dataMap[LICNUM10_11]
                            }
                            repository.verifyIdcard(authRequest).collectLatest { apiResult ->
                                handleApiResult(
                                    apiResult,
                                    typePair.second,
                                    takeType,
                                    authDataState.dataMap,
                                    authDataState.idCardBitmap
                                )
                            }
                        } else {
                            showAuthFailedDialog(messageOrFaDetectString)
                        }
                    }
                }
            } else {
                showAuthFailedDialog(context.getString(R.string.empty_auth_field))
            }
        }
    }

    private suspend fun handleApiResult(
        apiResult: ApiResult<Unit>,
        authType: AuthType,
        takeType: TakeType,
        dataMap: Map<String, String>,
        idCardBitmap: Bitmap?
    ) {
        when (apiResult) {
            is ApiResult.Loading -> {
                dialogState = AuthDialogData.Loading
            }
            is ApiResult.Success -> {
                val authInfo = localRepository.getAuthInfo().first().copy(
                    name = dataMap[NAME].toString().trim(),
                    firstIdNum = dataMap[FRONT_IDNUM].toString().trim(),
                    lastIdNum = dataMap[LAST_IDNUM].toString().trim(),
                    authType = authType.type,
                    takeType = takeType.type,
                    birth = getBirth(
                        dataMap[FRONT_IDNUM].toString().trim()
                    ),
                    isAuthComplete = true,
                    issuedDate = dataMap[ISSUE_DATE].toString().trim(),
                    issuedOffice = dataMap[ISSUE_OFFICE].toString().trim(),
                    driveLicenseNo = if (authType == AuthType.DRIVE_LICENSE) "${dataMap[LICNUM0_1]}-${dataMap[LICNUM2_3]}-${dataMap[LICNUM4_9]}-${dataMap[LICNUM10_11]}" else ""
                )

                localRepository.setAuthInfo(authInfo = authInfo)
                localRepository.removeIdCardImage()
                localRepository.saveIdCardImage(
                    context = context,
                    docCode = authType.code,
                    bitmap = idCardBitmap
                )
                dialogState = AuthDialogData.AuthComplete
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

    private fun getBirth(firstIdNum: String): String {
        val birth2_3 = firstIdNum.substring(0, 2).toInt()
        val year = Calendar.getInstance().get(Calendar.YEAR) - 2000
        val strYear = if (birth2_3 > year) {
            "19"
        } else {
            "20"
        }

        return "${strYear}${firstIdNum.substring(0, 2)}-${firstIdNum.substring(2, 4)}-${firstIdNum.substring(4)}"
    }
}

sealed class AuthData(
    val dataMap: SnapshotStateMap<String, String> = mutableStateMapOf(),
    val idCardBitmap: Bitmap? = null,
    var faceImage: ByteArray? = null
) {
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

    class IdCradData(bitmap: Bitmap? = null, faceImage: ByteArray? = null) :
        AuthData(idCardBitmap = bitmap, faceImage = faceImage)

    class DriveLicenseData(bitmap: Bitmap? = null, faceImage: ByteArray? = null) :
        AuthData(idCardBitmap = bitmap, faceImage = faceImage)

    class OverSea(bitmap: Bitmap? = null, faceImage: ByteArray? = null) :
        AuthData(idCardBitmap = bitmap, faceImage = faceImage)

    class ForeignData : AuthData()
    class PassportData : AuthData()
}