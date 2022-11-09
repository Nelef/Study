package com.inzisoft.ibks.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.Rect
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import com.inzisoft.ibks.Constants
import com.inzisoft.ibks.Constants.KEY_SCRIPT_FUN_NAME
import com.inzisoft.ibks.PathManager
import com.inzisoft.ibks.data.internal.CameraConfig
import com.inzisoft.ibks.data.internal.DocInfoData
import com.inzisoft.ibks.data.web.DocCameraData
import com.inzisoft.ibks.util.FileUtils
import com.inzisoft.mobile.data.RecognizeResult
import com.inzisoft.mobile.recogdemolib.LibConstants
import com.inzisoft.mobile.util.CommonUtils
import com.inzisoft.ibks.data.repository.LocalRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.ByteArrayInputStream
import javax.inject.Inject

@HiltViewModel
class DocCameraViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    localRepository: LocalRepository,
    pathManager: PathManager,
    @ApplicationContext context: Context
) : BaseNormalCameraViewModel(context, savedStateHandle, pathManager, localRepository) {

    override fun saveImageBitmap(bitmap: Bitmap, imageFilePath: String) {
        // 문서촬영 카메라는 세로UI 이고 촬영후 이미지가 가로로 나오기때문에 회전해서 저장한다.
        val matrix = Matrix()
        matrix.postRotate(90f)
        val rotatedBitmap = Bitmap.createBitmap(
            bitmap,
            0,
            0,
            bitmap.width,
            bitmap.height,
            matrix,
            true
        )

        FileUtils.saveToJpgImageFile(context = context, bitmap = rotatedBitmap, imagePath = imageFilePath)
    }

    fun saveExternalImageBitmap(context: Context, bitmap: Bitmap) {
        // 결과 이미지를 관리하는 객체에 갤러리에서 선택된 원본 이미지를 설정한다.
        RecognizeResult.getInstance().storeOrigin(
            context,
            ByteArrayInputStream(CommonUtils.bitmapToByteArray(bitmap))
        )

        onCompleteTakePicture(Rect(), 0)
    }

    override fun initDocInfoData() {
        Log.e("SW_DEBUG", "DocCameraViewModel(Parent) initDocInfoData")
        val docCameraData: DocCameraData = savedStateHandle.get<DocCameraData>(Constants.KEY_DOC_CAMERA_DATA)!!
        scriptFunName = savedStateHandle[KEY_SCRIPT_FUN_NAME]
        docInfoData = DocInfoData(
            docName = docCameraData.docName,
            docCode = docCameraData.docCode,
            maskingYn = docCameraData.getMaskingYn(),
            docImageDataList = mutableListOf(),
            docCtgCd = docCameraData.docCtgCd?: ""
        )

        imageMaxCount = docCameraData.takeCount
    }

    override fun getCameraConfig(): CameraConfig {
        return CameraConfig(LibConstants.TYPE_PAPER, 6000000, docInfoData.docName)
    }
}