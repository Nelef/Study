package com.inzisoft.ibks.viewmodel.presenter

import android.content.Context
import com.inzisoft.ibks.data.internal.CameraConfig
import com.inzisoft.mobile.recogdemolib.LibConstants

class NormalTakePresenter(title: String): OcrCameraPresenter(title) {
    override fun getCameraConfig(): CameraConfig {
        return CameraConfig(LibConstants.TYPE_OTHERS, 3000000, title)
    }

    override fun checkPreviewRecogResult(context: Context): Boolean {
        TODO("Not yet implemented")
    }
}