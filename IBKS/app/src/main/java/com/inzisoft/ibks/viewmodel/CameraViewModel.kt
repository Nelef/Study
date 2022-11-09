package com.inzisoft.ibks.viewmodel

import android.app.Activity
import android.content.Context
import android.graphics.Rect
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.inzisoft.ibks.data.internal.CameraConfig
import com.inzisoft.mobile.recogdemolib.CameraPreviewInterface
import com.inzisoft.mobile.view.overlay.CameraOverlayView
import com.inzisoft.ibks.data.repository.LocalRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

open class CameraViewModel constructor(
    context: Context,
    savedStateHandle: SavedStateHandle,
    localRepository: LocalRepository
) : AuthViewModel(context, savedStateHandle, localRepository) {
    var cameraState by mutableStateOf<CameraState>(CameraState.CameraPreviewState)
    val config: CameraConfig by lazy { getCameraConfig() }
    var cameraPreviewInterface: CameraPreviewInterface? = null

    lateinit var edocKey: String

    init {
        viewModelScope.launch(Dispatchers.IO) {
            edocKey = localRepository.getAuthInfo().first().edocKey
        }
    }
    /**
     * 촬영 완료 후 인식 화면으로 이동하기 위한 리스너
     */
    private val moveToRecognizeActivityListener: CameraPreviewInterface.MoveToRecognizeActivityListener =
        object : CameraPreviewInterface.MoveToRecognizeActivityListener {
            override fun callback(pictureROI: Rect, resultCode: Int) {
                Log.e("SW_DEBUG", "moveToRecognizeActivityListener.callback $pictureROI // $resultCode")
                cameraState = CameraState.CameraTakeCompleteState
                onCompleteTakePicture(pictureROI, resultCode)
            }

            override fun onCameraStarted() {
                Log.e("SW_DEBUG", "onCameraStarted")
            }
        }

    /**
     * 카메라 시작 실패 리스너
     */
    private val startCameraFailedListener: CameraPreviewInterface.StartCameraFailedListener =
        CameraPreviewInterface.StartCameraFailedListener {
            Log.e("SW_DEBUG", "startCameraFailedListener")
        }

    fun initCamera(
        activity: Activity,
        takeButton: Button,
        overlayView: CameraOverlayView,
        cameraPreview: View
    ) {
        createCameraInterfaceConfig(activity)
        cameraPreviewInterface!!.initLayout(takeButton, overlayView, cameraPreview)
        cameraPreviewInterface!!.onResume(true)
    }

    open fun getCameraConfig(): CameraConfig { return CameraConfig(-1, 0,"") }
    open fun createCameraInterfaceConfig(activity: Activity) {
        cameraPreviewInterface = CameraPreviewInterface(activity, moveToRecognizeActivityListener)
        cameraPreviewInterface!!.setPictureDesireResolution(config.pictureDesireResolution)
        cameraPreviewInterface!!.setRecogType(config.recogType)
        cameraPreviewInterface!!.setStartCameraFailedListener(startCameraFailedListener)
        cameraPreviewInterface!!.setPreviewPictureRecogEnable(false)
        cameraPreviewInterface!!.setAutoCaptureEnable(false)
    }

    open fun onCompleteTakePicture(pictureROI: Rect, resultCode: Int) {
    }

    fun retake() {
        authDataState.dataMap.clear()
        authDataState.idCardBitmap?.recycle()
        cameraResume()
        cameraState = CameraState.CameraPreviewState
    }

    fun cameraPause() {
        cameraPreviewInterface?.onPause()
    }

    fun cameraRelease() {
        cameraPreviewInterface?.release()
    }

    fun cameraResume() {
        cameraPreviewInterface?.onResume(true)
        cameraState = CameraState.CameraPreviewState
    }
}

sealed class CameraState {
    object CameraPreviewState: CameraState()
    object CameraOcrResultState: CameraState()
    object CameraTakeCompleteState: CameraState()
    object CameraMaxTake: CameraState()
}