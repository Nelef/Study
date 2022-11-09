package com.inzisoft.ibks.viewmodel.presenter

import android.content.Context
import com.inzisoft.ibks.data.internal.CameraConfig
import com.inzisoft.mobile.recogdemolib.LibConstants

class SealPresenter(title: String): OcrCameraPresenter(title) {
    override fun getCameraConfig(): CameraConfig {
        return CameraConfig(LibConstants.TYPE_SEAL, 3000000, title)
    }

    override fun checkPreviewRecogResult(context: Context): Boolean {
        return true
    }
}