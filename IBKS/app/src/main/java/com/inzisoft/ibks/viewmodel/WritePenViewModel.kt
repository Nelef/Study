package com.inzisoft.ibks.viewmodel

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.inzisoft.ibks.PathManager
import com.inzisoft.ibks.base.BaseDialogFragmentViewModel
import com.inzisoft.ibks.base.UiState
import com.inzisoft.ibks.data.internal.*
import com.inzisoft.ibks.util.FileUtils
import com.inzisoft.ibks.util.log.QLog
import com.inzisoft.paperless.inputmethod.data.PenPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject
import kotlin.io.path.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.nameWithoutExtension

@HiltViewModel
class WritePenViewModel @Inject constructor(
    private val pathManager: PathManager
) : BaseDialogFragmentViewModel() {

    var uiState by mutableStateOf<UiState<PenDialogData>>(UiState.None)
    var penDialogTypeState by mutableStateOf<PenDialogType?>(null)
    var sealImageData by mutableStateOf<Bitmap?>(null)
    var sealMode by mutableStateOf(false)

    private val _popupState = mutableStateOf<UiState<AlertData>>(UiState.None)
    val popupState: State<UiState<AlertData>> = _popupState

    fun alert(data: AlertData) {
        _popupState.value = UiState.Success(data)
    }

    fun dismissAlert() {
        _popupState.value = UiState.None
    }

    fun load(data: PenDialogData) {
        uiState = UiState.Loading()

        viewModelScope.launch(Dispatchers.IO) {

            penDialogTypeState = data.type

            val state = when (penDialogTypeState) {
                PenDialogType.PENSEAL,
                PenDialogType.PEN -> {
                    val namePenData = data.penData!!.let { penData ->
                        if (penData.imagePath == null)
                            penData.copy(imagePath = pathManager.getPenCacheImagePath(penData.id))
                        else {
                            penData.copy(pointList = readPenPoint(penData.imagePath))
                        }
                    }

                    val signPenData = data.signPenData?.let { signPenData ->
                        if (signPenData.imagePath == null)
                            signPenData.copy(
                                imagePath = pathManager.getPenCacheImagePath(
                                    signPenData.id
                                )
                            )
                        else
                            signPenData.copy(pointList = readPenPoint(signPenData.imagePath))
                    }

                    if (penDialogTypeState == PenDialogType.PENSEAL) {
                        val sealData = data.sealData?.let { sealData ->
                            if (sealData.imagePath == null)
                                sealData.copy(imagePath = pathManager.getPenCacheImagePath(sealData.id))
                            else {
                                if (File(sealData.imagePath).exists()) {
                                    setSealImage(BitmapFactory.decodeFile(sealData.imagePath))
                                }
                                sealData
                            }
                        }

                        if (signPenData?.pointList == null && sealImageData != null) sealMode = true
                        if (signPenData?.pointList != null && sealImageData != null) sealImageData = null

                        UiState.Success(
                            data.copy(
                                penData = namePenData,
                                signPenData = signPenData,
                                sealData = sealData
                            )
                        )
                    } else {
                        QLog.i("signPenData2: ${signPenData.toString()}")
                        UiState.Success(data.copy(penData = namePenData, signPenData = signPenData))
                    }
                }

                PenDialogType.SEAL -> {
                    val sealData = data.sealData!!.let { sealData ->
                        if (sealData.imagePath == null)
                            sealData.copy(imagePath = pathManager.getPenCacheImagePath(sealData.id))
                        else {
                            if (File(sealData.imagePath).exists()) {
                                setSealImage(BitmapFactory.decodeFile(sealData.imagePath))
                            }
                            sealData
                        }
                    }

                    UiState.Success(data.copy(sealData = sealData))
                }

                PenDialogType.PENSEALONLY -> {
                    val namePenData = data.penData!!.let { penData ->
                        if (penData.imagePath == null)
                            penData.copy(imagePath = pathManager.getPenCacheImagePath(penData.id))
                        else {
                            penData.copy(pointList = readPenPoint(penData.imagePath))
                        }
                    }

                    if (penDialogTypeState == PenDialogType.PENSEALONLY) {
                        val sealData = data.sealData?.let { sealData ->
                            if (sealData.imagePath == null)
                                sealData.copy(imagePath = pathManager.getPenCacheImagePath(sealData.id))
                            else {
                                if (File(sealData.imagePath).exists()) {
                                    setSealImage(BitmapFactory.decodeFile(sealData.imagePath))
                                }
                                sealData
                            }
                        }

                        UiState.Success(data.copy(penData = namePenData, sealData = sealData))
                    } else {
                        UiState.Success(data.copy(penData = namePenData))
                    }
                }
                else -> UiState.None
            }

            delay(300)

            uiState = state
        }
    }

    fun confirm(
        penData: PenData?,
        penImage: Bitmap?,
        pointList: List<PenPoint>?,
        signPenData: PenData?,
        signPenImage: Bitmap?,
        signPointList: List<PenPoint>?,
        sealData: PenData?,
        sealImage: Bitmap?,
        onSaveComplete: (result: String) -> Unit
    ) = viewModelScope.launch(Dispatchers.IO) {

        val penDataList = mutableListOf<ResultPenData>()

        penData?.let {
            penDataList.add(
                save(
                    penData.id,
                    penData.imagePath,
                    penImage,
                    pointList
                )
            )
        }

        signPenData?.let {
            penDataList.add(
                save(
                    signPenData.id,
                    signPenData.imagePath,
                    signPenImage,
                    signPointList
                )
            )
        }

        sealData?.let {
            penDataList.add(
                save(
                    sealData.id,
                    sealData.imagePath,
                    sealImage,
                    null
                )
            )
        }

        val result = Gson().toJson(penDataList)

        viewModelScope.launch(Dispatchers.Main) {
            onSaveComplete(result)
        }
    }

    private fun save(
        id: String,
        path: String?,
        image: Bitmap?,
        pointList: List<PenPoint>?,
    ): ResultPenData {
        return if (image == null) {
            ResultPenData(id, false, "")
        } else {
            val imagePath = path ?: pathManager.getPenCacheImagePath(id)
            val ret = saveImage(imagePath, image, pointList)
            ResultPenData(id, ret, imagePath)
        }
    }

    private fun saveImage(path: String, bitmap: Bitmap?, pointList: List<PenPoint>?): Boolean {
        val file = File(path)
        val pointFile = File(getPointPathFromImagePath(path))

        var ret = false

        if (bitmap == null) {
            file.delete()
            pointFile.delete()
        } else {
            file.parentFile?.apply {
                mkdirs()
            }

            ret = FileUtils.saveBitmap(path, bitmap, Bitmap.CompressFormat.PNG, 100)
            if (ret && pointList != null) {
                ret = savePenPoint(
                    path,
                    pointList
                )
            } else if (ret && pointList == null) {
                pointFile.delete()
            }

            bitmap.recycle()
        }

        return ret
    }

    private fun setSealImage(sealImage: Bitmap) {
        sealImageData = sealImage
    }

    private fun savePenPoint(imagePath: String, pointList: List<PenPoint>?): Boolean {
        val jsonStr = Gson().toJson(pointList)
        val pointPath = getPointPathFromImagePath(imagePath)
        File(pointPath).writeText(jsonStr)
        return true
    }

    private fun readPenPoint(imagePath: String): List<PenPoint>? {
        val pointPath = getPointPathFromImagePath(imagePath)

        if (!File(pointPath).exists()) return null

        val jsonStr = File(pointPath).readText()
        return Gson().fromJson(
            jsonStr,
            TypeToken.getParameterized(List::class.java, PenPoint::class.java).type
        )
    }

    private fun getPointPathFromImagePath(imagePath: String): String {
        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            val imageFile = File(imagePath)
            File(imageFile.parent, "${imageFile.nameWithoutExtension}.json").absolutePath
        } else {
            val path = Path(imagePath)
            "${path.parent?.absolutePathString()}/${path.nameWithoutExtension}.json"
        }
    }
}