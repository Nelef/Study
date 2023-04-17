package com.inzisoft.ibks.viewmodel

import android.app.Activity
import android.content.Context
import android.graphics.Rect
import android.util.Base64
import android.util.Log
import androidx.core.text.isDigitsOnly
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.inzisoft.ibks.Constants.KEY_SCRIPT_FUN_NAME
import com.inzisoft.ibks.R
import com.inzisoft.ibks.data.internal.AuthDialogData
import com.inzisoft.ibks.data.internal.CameraConfig
import com.inzisoft.ibks.data.repository.CameraRepository
import com.inzisoft.ibks.data.repository.LocalRepository
import com.inzisoft.ibks.util.CryptoUtil
import com.inzisoft.izmobilereader.IZMobileReaderCommon
import com.inzisoft.mobile.data.IDCardRecognizeResult
import com.inzisoft.mobile.data.MIDReaderProfile
import com.inzisoft.mobile.data.RecognizeResult
import com.inzisoft.mobile.recogdemolib.CameraPreviewInterface
import com.inzisoft.mobile.recogdemolib.LibConstants
import com.inzisoft.mobile.recogdemolib.RecognizeInterface
import com.inzisoft.mobile.recogdemolib.RecognizeInterface.RecognizeFinishListener
import com.inzisoft.mobile.util.BeepSoundPool
import com.inzisoft.mobile.util.CommonUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.nio.charset.Charset
import java.util.*
import javax.inject.Inject

@HiltViewModel
open class OcrCameraViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    localRepository: LocalRepository,
    cameraRepository: CameraRepository,
    @ApplicationContext context: Context
) : CameraViewModel(context, savedStateHandle, localRepository, cameraRepository) {
    var recognizeInterface: RecognizeInterface? = null

    var activity: Activity? = null
    init {
        scriptFunName = savedStateHandle.get<String>(KEY_SCRIPT_FUN_NAME).toString()
    }

    /**
     * 프리뷰 인식 시 유효성 체크 및 실패 처리 리스너
     */
    private val previewRecognizeCheckListener: CameraPreviewInterface.PreviewRecognizeCheckListener =
        object : CameraPreviewInterface.PreviewRecognizeCheckListener {
            override fun onCheckValidation(): Boolean {
                // Preview 인식 Validation Check.
                Log.e("SW_DEBUG", "onCheckValidation")
                return checkPreviewRecogResult(context)
            }

            override fun onRecognitionFailed(type: Int, resultCode: Int) {
                // Preview 인식 실패.
                when (resultCode) {
                    LibConstants.ERR_CODE_PREVIEW_RECOG_FAIL_FINDEDGE -> CommonUtils.log(
                        "d",
                        "Error msg : find edge fail"
                    )
                    LibConstants.ERR_CODE_PREVIEW_RECOG_FAIL_TRANSFORM_PERSPECTIVE -> CommonUtils.log(
                        "d",
                        "Error msg : transform perspective fail"
                    )
                    LibConstants.ERR_CODE_PREVIEW_RECOG_FAIL_AUTOCROP -> CommonUtils.log(
                        "d",
                        "Error msg : autocrop fail"
                    )
                    LibConstants.ERR_CODE_PREVIEW_RECOG_FAIL_RECOGNIZE -> CommonUtils.log(
                        "d",
                        "Error msg : recognize fail"
                    )
                    LibConstants.ERR_CODE_PREVIEW_RECOG_FAIL_IDCARD_VALIDATION -> CommonUtils.log(
                        "d",
                        "Error msg : idcard validation check fail"
                    )
                    else -> CommonUtils.log("d", "Error msg : etc fail")
                }
                cameraPreviewInterface?.onPause(true)
                dialogState = AuthDialogData.ShowOcrFailedPopup
            }

            override fun onRecognitionStarted() {
                // Preview 인식 시작.
            }

            override fun onRecognitionEnded() {
                BeepSoundPool.getInstance().playBeepSound()
                recogEnded()
            }
        }

    /**
     * 인식결과를 받는 콜백 인터페이스
     */
    private val recognizeFinishListener =
        RecognizeFinishListener { resultValue ->
            dialogState = AuthDialogData.None
            Log.e("SW_DEBUG", "resultValue: $resultValue")
            if(resultValue == LibConstants.ERR_CODE_RECOGNITION_SUCCESS) {
                onTakeModeRecognizeResult()
            } else {
                onRecognizeResultFailed()
            }
        }

    protected fun initRecognizeInterface(pictureROI: Rect) {
        recognizeInterface = RecognizeInterface(
            activity,
            pictureROI,
            getCameraConfig().recogType,
            recognizeFinishListener
        )
    }

    /**
     * 촬영 인식 시작하는 함수
     */
    protected open fun startTakeModeRecognize() {
        recognizeInterface!!.startRecognizeAutoCrop()
    }

    fun clearPreviewRecogCount() {
        cameraPreviewInterface?.clearPreviewRecogFailCnt()
    }

    override fun getCameraConfig(): CameraConfig {
        // Type은 재외국민 신분증 인식타입으로 한다. (주민등록증/운전면허증/재외국민 인식 가능)
        return CameraConfig(
            LibConstants.TYPE_IDCARD_OVERSEA,
            3000000,
            context.getString(R.string.camera_guide_title),
            true
        )
    }

    override fun createCameraInterfaceConfig(activity: Activity) {
        super.createCameraInterfaceConfig(activity)
        Log.e("SW_DEBUG","overideCameraInterfaceConfig")
        this.activity = activity
        if(config.isPreviewMode) {
            cameraPreviewInterface!!.setPreviewRecognizeListener(previewRecognizeCheckListener)
        }

        cameraPreviewInterface!!.onCreate()
    }

    private fun juminCheck(context: Context): Boolean {
        val idResult = RecognizeResult.getInstance().idCardRecognizeResult
        if (MIDReaderProfile.getInstance().NEED_ENC_TEXT_DATA) {
            val (firstIdNumByte, lastIdNumByte) = getRrnByte(context, idResult.rrnByte)

            if(firstIdNumByte.size != 6 || lastIdNumByte.size != 7) {
                return false
            }
            firstIdNumByte.forEach { byte ->
                if (48 > byte || 57 < byte) {
                    return false
                }
            }
            Arrays.fill(firstIdNumByte, 0)
            lastIdNumByte.forEach { byte ->
                if (48 > byte || 57 < byte) {
                    return false
                }
            }
            Arrays.fill(lastIdNumByte, 0)
        } else {
            if(idResult.rrn.length != 13) return false
            if(!idResult.rrn.isDigitsOnly()) return false
        }

        return true
    }

    fun checkPreviewRecogResult(context: Context): Boolean {
        val idResult = RecognizeResult.getInstance().idCardRecognizeResult
        // 신분증 인식 성공 조건
        if(!juminCheck(context)) {
            return false
        }
        val (nameLength, licenseLength, dateLength) = if(MIDReaderProfile.getInstance().NEED_ENC_TEXT_DATA) {
            arrayOf(idResult.getNameLength(context), idResult.getLicenseNumberLength(context), idResult.getDateLength(context))
        } else {
            arrayOf(idResult.name.length, idResult.licenseNumberLength, idResult.dateLength)
        }

        return nameLength > 1 && (licenseLength == 12 || dateLength == 8)
    }

    private fun recogEnded() {
        // Preview 인식 완료. 결과 처리
        val recogData = getRecogData(context, RecognizeResult.getInstance())
        authDataState = recogData
        cameraState = CameraState.CameraOcrResultState
        cameraPreviewInterface?.onPause(true)
    }

    private fun getRecogData(context: Context, recogResult: RecognizeResult): AuthData {
        val result: IDCardRecognizeResult = recogResult.idCardRecognizeResult

        val rrn =
            if (MIDReaderProfile.getInstance().NEED_ENC_TEXT_DATA) reconstructionRrn(context, result.rrnByte)
            else reconstructionRrn(result.rrn)

        val name =
            if (MIDReaderProfile.getInstance().NEED_ENC_TEXT_DATA) result.getEncName(context)
            else result.name

        val date =
            if (MIDReaderProfile.getInstance().NEED_ENC_TEXT_DATA) result.getEncDate(context)
            else result.date

        val issueOffice =
            if (MIDReaderProfile.getInstance().NEED_ENC_TEXT_DATA) result.getEncIssueOffice(context)
            else result.issueOffice

        return when (recogResult.recogType) {
            IZMobileReaderCommon.IZMOBILEREADER_COMMON_RESULT_TYPE_ID_DRIVING_LICENSE -> {
                // 인식결과가 운전면허증일 경우
                val licenseNum =
                    if (MIDReaderProfile.getInstance().NEED_ENC_TEXT_DATA) getEncLicenseNum(context, result.licenseNumberByte)
                    else getLicenseNum(result.licenseNumber)
                val driveDate = AuthData.DriveLicenseData(bitmap = recogResult.getRecogResultImage(true), faceImage = recogResult.photoFaceByte)
                driveDate.dataMap[AuthData.NAME] = name.trim()
                driveDate.dataMap[AuthData.LICNUM0_1] = licenseNum[0].trim()
                driveDate.dataMap[AuthData.LICNUM2_3] = licenseNum[1].trim()
                driveDate.dataMap[AuthData.LICNUM4_9] = licenseNum[2].trim()
                driveDate.dataMap[AuthData.LICNUM10_11] = licenseNum[3].trim()
                driveDate.dataMap[AuthData.ISSUE_DATE] = date.trim()
                driveDate.dataMap[AuthData.ISSUE_OFFICE] = issueOffice.trim()
                driveDate.dataMap[AuthData.FRONT_IDNUM] = rrn.first.trim()
                driveDate.dataMap[AuthData.LAST_IDNUM] = rrn.second.trim()
                driveDate
            }
            else -> {
                val idData = if(recogResult.recogType == IZMobileReaderCommon.IZMOBILEREADER_COMMON_RESULT_TYPE_ID_RESIDENT_REGI) {
                    // 인식결과가 주민등록증일 경우
                    AuthData.IdCradData(bitmap = recogResult.getRecogResultImage(true), faceImage = recogResult.photoFaceByte)
                } else {
                    // 인식결과가 재외국민용 신분증일 경우
                    AuthData.OverSea(bitmap = recogResult.getRecogResultImage(true), faceImage = recogResult.photoFaceByte)
                }
                idData.dataMap[AuthData.NAME] = name.trim()
                idData.dataMap[AuthData.ISSUE_DATE] = date.trim()
                idData.dataMap[AuthData.ISSUE_OFFICE] = issueOffice.trim()
                idData.dataMap[AuthData.FRONT_IDNUM] = rrn.first.trim()
                idData.dataMap[AuthData.LAST_IDNUM] = rrn.second.trim()
                idData
            }
        }
    }

    private fun reconstructionRrn(rrn: String): Pair<String, String> {
        val firstIdNum = rrn.substring(0, 6)
        val lastIdNum = rrn.substring(6)
        return Pair(firstIdNum, lastIdNum)
    }

    private fun getRrnByte(context: Context, encryptedRrn: ByteArray): Pair<ByteArray, ByteArray> {
        val resultCharSize = Integer.SIZE / java.lang.Byte.SIZE
        val lastIdNumStartIndex = resultCharSize * 6
        val firstIdNumByte = ByteArray(6)
        val lastIdNumByte = ByteArray(7)
        val decryptRrn = CryptoUtil.decrypt(context, encryptedRrn)

        var lastIdNumIndex = 0
        var firstIdNumIndex = 0
        for(i in decryptRrn.indices step(resultCharSize)) {
            if(i < lastIdNumStartIndex) {
                firstIdNumByte[firstIdNumIndex++] = decryptRrn[i]
            } else {
                lastIdNumByte[lastIdNumIndex++] = decryptRrn[i]
            }
        }

        Arrays.fill(decryptRrn, 0)

        return Pair(firstIdNumByte, lastIdNumByte)
    }

    private fun reconstructionRrn(context: Context, encryptedRrn: ByteArray): Pair<String, String> {
        val (firstIdNumByte, lastIdNumByte) = getRrnByte(context, encryptedRrn)
        val lastEncIdNum = CryptoUtil.encrypt(context, lastIdNumByte)
        Arrays.fill(lastIdNumByte, 0)

        val firstIdNum = String(firstIdNumByte)
        Arrays.fill(firstIdNumByte, 0)
        return Pair(firstIdNum, Base64.encodeToString(lastEncIdNum, Base64.NO_WRAP))
    }

    private fun getLicenseNum(license: String): MutableList<String> {
        val result = mutableListOf<String>()
        for(i in license.indices) {
            when (i) {
                1 -> result.add(license.substring(0, 2))
                3 -> result.add(license.substring(2, 4))
                9 -> result.add(license.substring(4, 10))
                10 -> {
                    result.add(license.substring(10))
                    break
                }
            }
        }

        return result
    }

    private fun getEncLicenseNum(context: Context, encryptedLicense: ByteArray): MutableList<String> {
        val resultCharSize = Integer.SIZE / java.lang.Byte.SIZE

        val license0_1 = ByteArray(resultCharSize * 2)
        val license2_3 = ByteArray(2)
        val license4_9 = ByteArray(6)
        val license10_11 = ByteArray(2)
        val decryptLicense = CryptoUtil.decrypt(context, encryptedLicense)

        for(i in 0 until (decryptLicense.size/resultCharSize)) {
            when(i) {
                // 한글 or 숫자가 있는 첫번째 두번째자리는 4바이트씩 받아서 나중에 UTF-32LE charset으로 decode 한다.
                in 0..1 -> System.arraycopy(decryptLicense, i*resultCharSize, license0_1, i*resultCharSize, resultCharSize)
                in 2..3 -> System.arraycopy(decryptLicense, i*resultCharSize, license2_3, i-2, 1)
                in 4..9 -> System.arraycopy(decryptLicense, i*resultCharSize, license4_9, i-4, 1)
                in 10..11 -> System.arraycopy(decryptLicense, i*resultCharSize, license10_11, i-10, 1)
                else -> break
            }
        }

//        val encryptedLicense4_9 = CryptoUtil.encrypt(context, license4_9)
        Arrays.fill(decryptLicense, 0)
//        Arrays.fill(license4_9, 0)

        val result = mutableListOf<String>()
        result.add(0, String(license0_1, Charset.forName("UTF-32LE")))
        result.add(1, String(license2_3))
//        result.add(2, Base64.encodeToString(encryptedLicense4_9, Base64.NO_WRAP))
        result.add(2, String(license4_9))
        result.add(3, String(license10_11))


        return result
    }

    override fun onCompleteTakePicture(pictureROI: Rect, resultCode: Int) {
        dialogState = AuthDialogData.Loading
        viewModelScope.launch(Dispatchers.IO) {
            initRecognizeInterface(pictureROI)
            recognizeInterface!!.startRecognizeAutoCrop()
        }
    }

    open fun onTakeModeRecognizeResult() {
        recogEnded()
    }

    open fun onRecognizeResultFailed() {
        dialogState = AuthDialogData.ShowOcrFailedPopup
    }
}