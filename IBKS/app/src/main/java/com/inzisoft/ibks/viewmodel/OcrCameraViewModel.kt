package com.inzisoft.ibks.viewmodel

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import com.inzisoft.ibks.Constants.KEY_SCRIPT_FUN_NAME
import com.inzisoft.ibks.R
import com.inzisoft.ibks.data.internal.AuthDialogData
import com.inzisoft.ibks.data.internal.CameraConfig
import com.inzisoft.ibks.data.repository.LocalRepository
import com.inzisoft.ibks.viewmodel.presenter.*
import com.inzisoft.mobile.data.RecognizeResult
import com.inzisoft.mobile.recogdemolib.CameraPreviewInterface
import com.inzisoft.mobile.recogdemolib.LibConstants
import com.inzisoft.mobile.util.BeepSoundPool
import com.inzisoft.mobile.util.CommonUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

@HiltViewModel
class OcrCameraViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    localRepository: LocalRepository,
    @ApplicationContext context: Context
) : CameraViewModel(context, savedStateHandle, localRepository) {
    private val cameraPresenter: OcrCameraPresenter = createOcrPresenter(authCameraData.cameraType)

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
                return cameraPresenter.checkPreviewRecogResult(context)
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
                dialogState = AuthDialogData.ShowOcrFailedPopup
            }

            override fun onRecognitionStarted() {
                // Preview 인식 시작.
            }

            override fun onRecognitionEnded() {
                // Preview 인식 완료. 결과 처리
                Log.e("SW_DEBUG", "preview recog ended")
                //camera shutter sound
                BeepSoundPool.getInstance().playBeepSound()

                val recogData = cameraPresenter.getRecogData(context, RecognizeResult.getInstance())
                authDataState = recogData
                cameraState = CameraState.CameraOcrResultState
                cameraPreviewInterface?.onPause(true)
            }
        }

    private fun createOcrPresenter(cameraType: String): OcrCameraPresenter {
        return when (cameraType) {
            "seal" -> SealPresenter(context.getString(R.string.seal_scan))
            "foreign" -> ForeignCardPresenter(context.getString(R.string.foreign_scan))
            "passport" -> PassportPresenter(context.getString(R.string.passport_scan))
            else -> IdCardPresenter(context.getString(R.string.camera_guide_title))
        }
    }

    override fun getCameraConfig(): CameraConfig {
        return cameraPresenter.getCameraConfig()
    }

    override fun createCameraInterfaceConfig(activity: Activity) {
        super.createCameraInterfaceConfig(activity)
        Log.e("SW_DEBUG","overideCameraInterfaceConfig")
        when(authCameraData.cameraType) {
            "passport" -> {
            }
            else -> {
                cameraPreviewInterface!!.setPreviewRecognizeListener(previewRecognizeCheckListener)
                cameraPreviewInterface!!.setPreviewPictureRecogEnable(true)
                cameraPreviewInterface!!.setAutoCaptureEnable(false)
            }
        }

        cameraPreviewInterface!!.onCreate()
    }
}