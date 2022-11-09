package com.inzisoft.ibks.viewmodel.presenter

import android.content.Context
import com.inzisoft.ibks.data.internal.CameraConfig

class PassportPresenter(title: String): OcrCameraPresenter(title) {
    override fun getCameraConfig(): CameraConfig {
        TODO("Not yet implemented")
    }

    override fun checkPreviewRecogResult(context: Context): Boolean {
        TODO("Not yet implemented")
    }
}