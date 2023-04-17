package com.inzisoft.ibks.viewmodel

import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.inzisoft.ibks.Constants.KEY_SCRIPT_FUN_NAME
import com.inzisoft.ibks.PathManager
import com.inzisoft.ibks.R
import com.inzisoft.ibks.data.internal.CameraConfig
import com.inzisoft.ibks.data.internal.DocInfoData
import com.inzisoft.ibks.data.repository.LocalRepository
import com.inzisoft.ibks.util.FileUtils
import com.inzisoft.mobile.recogdemolib.LibConstants
import com.inzisoft.ibks.data.repository.CameraRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NormalAuthCameraViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    localRepository: LocalRepository,
    cameraRepository: CameraRepository,
    pathManager: PathManager,
    @ApplicationContext context: Context
) : BaseNormalCameraViewModel(context, savedStateHandle, pathManager, localRepository, cameraRepository) {

    override fun saveImageBitmap(bitmap: Bitmap, imageFilePath: String) {
        FileUtils.saveToJpgImageFile(context = context, bitmap = bitmap, imagePath = imageFilePath)
        viewModelScope.launch(Dispatchers.IO) {
            val authInfo = localRepository.getAuthInfo().first().copy(
                name = "",
                firstIdNum = "",
                lastIdNum = "",
                birth = "",
                issuedDate = "",
                issuedOffice = "",
                driveLicenseNo = ""
            )
            localRepository.setAuthInfo(authInfo)
        }
    }

    override fun initDocInfoData() {
        scriptFunName = savedStateHandle[KEY_SCRIPT_FUN_NAME]
        docInfoData = DocInfoData(
            docCode = authCameraData.cameraType,
            docName = getCameraTitle(authCameraData.cameraType),
            maskingYn = false,
            docImageDataList = mutableListOf(),
            docCtgCd = ""
        )

        imageMaxCount = 1
    }

    private fun getCameraTitle(cameraType: String): String {
        return when (cameraType) {
            "foreign" -> context.getString(R.string.foreign_scan)
            "passport" -> context.getString(R.string.passport_scan)
            "oversea" -> context.getString(R.string.oversea_scan)
            "compatriot" -> context.getString(R.string.compatriot_scan)
            else -> ""
        }
    }

    override fun getCameraConfig(): CameraConfig {
        return CameraConfig(LibConstants.TYPE_PAPER, 6000000, docInfoData.docName, false)
    }
}