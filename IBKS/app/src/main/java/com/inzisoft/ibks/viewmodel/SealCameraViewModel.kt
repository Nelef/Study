package com.inzisoft.ibks.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Point
import android.graphics.Rect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.inzisoft.ibks.R
import com.inzisoft.ibks.data.internal.CameraConfig
import com.inzisoft.ibks.data.repository.CameraRepository
import com.inzisoft.ibks.data.repository.LocalRepository
import com.inzisoft.mobile.data.RecognizeResult
import com.inzisoft.mobile.recogdemolib.LibConstants
import com.inzisoft.mobile.sealextractor.SealAndSignatureScanner
import com.inzisoft.mobile.util.CommonUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SealCameraViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    localRepository: LocalRepository,
    cameraRepository: CameraRepository,
    @ApplicationContext context: Context
) : OcrCameraViewModel(savedStateHandle, localRepository, cameraRepository, context) {
    // 인감스캔시 사용
    private val SEAL_RECT_WIDTH_INCH = 1.574803f // 도장등록 원장의 도장이 찍히는 사각형의 실제 가로사이즈를 인치로 환산. //4cm
    private val SEAL_RECT_HEIGHT_INCH = 1.574803f // 도장등록 원장의 도장이 찍히는 사각형의 실제 세로사이즈를 인치로 환산. //4cm
    private val FIRST_CROP_OFFSET = 20 // 인식용 사각형 제거에 사용

    private val MAX_RED_RANGE1 = floatArrayOf(360f, 1f, 1f)
    private val MIN_RED_RANGE1 = floatArrayOf(300f, 0.3f, 0f)

    private val MAX_RED_RANGE2 = floatArrayOf(15f, 1f, 1f)
    private val MIN_RED_RANGE2 = floatArrayOf(0f, 0.3f, 0f)

    var sealScanResultState by mutableStateOf<SealScanState>(SealScanState.None)

    private fun startSealScan() {
        viewModelScope.launch(Dispatchers.IO) {
            val cameraPreviewSize: Point = cameraPreviewInterface!!.previewResolution
            val overlayGuideRect: Rect = cameraPreviewInterface!!.overlayView.guideRect
            val overlayViewRect: Rect = cameraPreviewInterface!!.overlayView.screenRoi
            val overlayViewSize = Point(overlayViewRect.width(), overlayViewRect.height())
            val pictureSize: Point = cameraPreviewInterface!!.pictureSize

            val pictureROI = CommonUtils.convertDisplayROIToPictureROI(
                overlayViewSize,
                cameraPreviewSize,
                pictureSize,
                overlayGuideRect
            )

            val sealAndSignatureScanner =
                SealAndSignatureScanner(context)
            val sealScanBitmap: Bitmap? = sealAndSignatureScanner.scanSealAndSigNature(
                pictureROI,
                true,
                FIRST_CROP_OFFSET,
                SEAL_RECT_WIDTH_INCH,
                SEAL_RECT_HEIGHT_INCH,
                MAX_RED_RANGE1,
                MIN_RED_RANGE1,
                MAX_RED_RANGE2,
                MIN_RED_RANGE2
            ) //scan 시작
            RecognizeResult.getInstance().clean()

            if(sealScanBitmap != null) {
                sealScanResultState = SealScanState.SealScanComplete(sealScanBitmap)
            } else {
                sealScanResultState = SealScanState.Error
            }
        }
    }

    override fun onCompleteTakePicture(pictureROI: Rect, resultCode: Int) {
        initRecognizeInterface(pictureROI)
        sealScanResultState = SealScanState.Loading
        startTakeModeRecognize()
    }


    override fun onTakeModeRecognizeResult() {
        startSealScan()
    }

    override fun onRecognizeResultFailed() {
        sealScanResultState = SealScanState.Error
    }

    override fun getCameraConfig(): CameraConfig {
        return CameraConfig(
            LibConstants.TYPE_SEAL,
            3000000,
            context.getString(R.string.seal_scan),
            false
        )
    }
}

sealed class SealScanState {
    object None: SealScanState()
    object Loading: SealScanState()
    object Error: SealScanState()
    data class SealScanComplete(val bitmap: Bitmap): SealScanState()
}