package com.inzisoft.ibks.viewmodel.presenter

import android.content.Context
import com.inzisoft.ibks.data.internal.CameraConfig
import com.inzisoft.mobile.data.RecognizeResult
import com.inzisoft.ibks.viewmodel.AuthData

abstract class OcrCameraPresenter(val title: String) {
    abstract fun getCameraConfig() : CameraConfig

    abstract fun checkPreviewRecogResult(context: Context): Boolean

    open fun getRecogData(context: Context, recogResult: RecognizeResult): AuthData {
        return AuthData.IdCradData()
    }
}