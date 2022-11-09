package com.inzisoft.ibks.viewmodel

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import com.inzisoft.ibks.PathManager
import com.inzisoft.ibks.data.internal.DocImageData
import com.inzisoft.ibks.data.internal.DocInfoData
import com.inzisoft.mobile.data.RecognizeResult
import com.inzisoft.ibks.data.repository.LocalRepository

abstract class BaseNormalCameraViewModel constructor(
    context: Context,
    savedStateHandle: SavedStateHandle,
    private val pathManager: PathManager,
    localRepository: LocalRepository,
) : CameraViewModel(context, savedStateHandle, localRepository) {

    var imageData by mutableStateOf<Bitmap?>(null)
//    var imageFileList = mutableListOf<String>()
    var imageIndex by mutableStateOf(0)
    var imageMaxCount: Int = 0
    lateinit var docInfoData: DocInfoData

    init {
        initDocInfoData()
    }

    override fun createCameraInterfaceConfig(activity: Activity) {
        super.createCameraInterfaceConfig(activity)
        Log.e("SW_DEBUG", "createCameraInterefaceConfig")
        cameraPreviewInterface!!.onCreate()
    }

    override fun onCompleteTakePicture(pictureROI: Rect, resultCode: Int) {
        val origin = RecognizeResult.getInstance().originImage

        val cacheOriginImageFilePath = pathManager.getEvidenceDocCacheNoneMaskJpgImage(
            entryId = edocKey,
            docCode = docInfoData.docCode,
            index = imageIndex
        )

        val realImageFilePath = pathManager.getEvidenceJpgImage(
            entryId = edocKey,
            docCode = docInfoData.docCode,
            index = imageIndex
        )

        saveImageBitmap(origin, cacheOriginImageFilePath)
        docInfoData.docImageDataList.add(
            DocImageData(
                cacheOriginImagePath = cacheOriginImageFilePath,
                realImagePath = realImageFilePath
            )
        )
//        imageFileList.add(imageFilePath)
        Log.e("SW_DEBUG"," imageCount: " + imageIndex + " // max : " + imageMaxCount)
        imageData = origin
        imageIndex++
        if (imageIndex < imageMaxCount) {
            super.cameraResume()
        } else {
            cameraState = CameraState.CameraMaxTake
        }
    }

    fun restartCamera() {
        imageIndex = docInfoData.docImageDataList.size

        if(imageIndex < imageMaxCount) {
            super.cameraResume()
            if(imageIndex == 0) {
                imageData = null
            } else {
                val lastImagePath = docInfoData.docImageDataList[imageIndex-1].cacheOriginImagePath
                imageData = BitmapFactory.decodeFile(lastImagePath)
            }
        }
    }

    fun clearAllImage() {
        for (docImageData in docInfoData.docImageDataList) {
            docImageData.clearImage()
        }
        docInfoData.docImageDataList.clear()
    }

    abstract fun saveImageBitmap(bitmap: Bitmap, imageFilePath: String)
    abstract fun initDocInfoData()
}