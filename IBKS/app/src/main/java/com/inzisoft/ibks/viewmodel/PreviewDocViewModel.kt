package com.inzisoft.ibks.viewmodel

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.inzisoft.ibks.Constants.KEY_PREVIEW_DOC_TYPE
import com.inzisoft.ibks.PathManager
import com.inzisoft.ibks.PreviewDocType
import com.inzisoft.ibks.base.BaseDialogFragmentViewModel
import com.inzisoft.ibks.data.internal.DocImageData
import com.inzisoft.ibks.data.internal.DocInfoData
import com.inzisoft.ibks.data.repository.LocalRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PreviewDocViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val pathManager: PathManager,
    private val localRepository: LocalRepository
) : BaseDialogFragmentViewModel() {

    val docInfoData = savedStateHandle.get<DocInfoData>("docInfoData")
    val previewDocType = savedStateHandle.get<PreviewDocType>(KEY_PREVIEW_DOC_TYPE)
    var indexLocationState by mutableStateOf<IndexLocationState>(IndexLocationState.FirstIndex)

    var image by mutableStateOf<Bitmap?>(null)
    var positionString by mutableStateOf("/")

    var previewDocState by mutableStateOf<PreviewDocState>(PreviewDocState.None)
    var dialogUiState by mutableStateOf<DialogUiState>(DialogUiState.None)
    var masked = false

    lateinit var entryId: String

    var currentImageIndex: Int = 0

    init {
        setImageData(index = 0)
        if(docInfoData!!.docImageDataList.size <= 1) {
            indexLocationState = IndexLocationState.UnderOneSize
        }

        viewModelScope.launch {
            entryId = localRepository.getAuthInfo().first().edocKey
        }
    }

    fun sendDocImageToServer() {
        viewModelScope.launch(Dispatchers.IO) {
            when (previewDocType) {
                PreviewDocType.PREVIEW_DOC -> {}
                PreviewDocType.NORMAL_AUTH,
                PreviewDocType.TAKE_DOC -> {
                    if (docInfoData!!.maskingYn && !masked) {
                        dialogUiState = DialogUiState.MaskingAlertDialog
                    } else {
                        docInfoData.apply {
                            if (previewDocType == PreviewDocType.NORMAL_AUTH) {
                                for (docImageData: DocImageData in docImageDataList) {
                                    docImageData.renameToRealImage()
                                }
                                dialogUiState =
                                    DialogUiState.SendSuccessDialog(docImageDataList.size)
                            } else {
//                                val userInfo = localRepository.getUserInfo().first()
//                                cameraRepository.sendAttachedImage(
//                                    sendAttachImageRequest = SendAttachImageRequest(
//                                        entryId = entryId,
////                                        provId = userInfo.provId.name,
////                                        userId = userInfo.userId,
//                                        docCtgCd = docCtgCd,
//                                        attachCd = docCode
//                                    ),
//                                    docImageDataList = docImageDataList,
//                                    isSendCacheImage = true
//                                ).collectLatest { result ->
//                                    dialogUiState = when (result) {
//                                        is ApiResult.Loading -> DialogUiState.Loading
//                                        is ApiResult.Success -> {
//                                            // 필요없는 이미지 삭제함.
//                                            val imageCount = docImageDataList.size
//                                            for (docImageData: DocImageData in docImageDataList) {
//                                                docImageData.renameToRealImage()
//                                                docImageData.clearImage()
//                                            }
//
//                                            DialogUiState.SendSuccessDialog(imageCount)
//                                        }
//                                        is ApiResult.Error -> {
//                                            DialogUiState.SendFailedDialog(result.message)
//                                        }
//                                    }
//                                }
                            }
                        }
                    }
                }
            }
        }
    }

    fun clickMaskingBtn() {
        dialogUiState = DialogUiState.Loading
        image?.apply {
            val docImageData = docInfoData!!.docImageDataList[currentImageIndex]
            docImageData.setMaskedImagePath(
                entryId = entryId,
                docCode = docInfoData.docCode,
                index = currentImageIndex,
                pathManager = pathManager
            )
            previewDocState =
                PreviewDocState.Masking(
                    docImageData,
                    this
                )

            masked = true
        }
    }

    fun clickUnmaskingBtn() {
        val docImageData = docInfoData!!.docImageDataList[currentImageIndex]
        docImageData.deleteMaskedImage()
        setImageData(currentImageIndex)
        previewDocState = PreviewDocState.None
        dialogUiState = DialogUiState.MaskingRemoveCompleteDialog
    }

    private fun setImageData(index: Int) {
        if (previewDocType != PreviewDocType.PREVIEW_DOC) {
            if (docInfoData!!.docImageDataList.size > 0) {
                val path = docInfoData!!.docImageDataList[index].getCacheAvailableImagePath()
                val bitmap = BitmapFactory.decodeFile(path)
                currentImageIndex = index
                positionString = "${currentImageIndex + 1}/${docInfoData!!.docImageDataList.size}"
                image = bitmap
            } else {
                image = null
            }
        }
    }

    fun deleteCurrentImage() {
        docInfoData!!.docImageDataList[currentImageIndex].clearImage()
        docInfoData!!.docImageDataList.removeAt(currentImageIndex)
        renameFileList()
        dialogUiState = if(docInfoData!!.docImageDataList.isEmpty()) {
            DialogUiState.ImageAllRemovedDialog
        } else {
            clickPrevBtn()
            DialogUiState.None
        }
    }

    private fun renameFileList() {
        viewModelScope.launch(Dispatchers.IO) {
            for (index in currentImageIndex until docInfoData!!.docImageDataList.size) {
                docInfoData!!.docImageDataList[index].renameTo(
                    entryId = entryId,
                    docCode = docInfoData!!.docCode,
                    index = index,
                    pathManager
                )
            }
        }
    }

    fun clickDeleteBtn() {
        dialogUiState = DialogUiState.DeleteImageAlertDialog
    }

    fun clickNextBtn() {
        clickMoveBtn(currentImageIndex+1)
    }

    fun clickPrevBtn() {
        clickMoveBtn(currentImageIndex-1)
    }

    private fun clickMoveBtn(index: Int) {
        val result = checkIndexBoundary(index)
        indexLocationState = result.first
        setImageData(result.second)
    }

    fun changePosition(position: Int) {
        val checkIndexPair = checkIndexBoundary(position)
        indexLocationState = checkIndexPair.first
        positionString = "${checkIndexPair.second + 1}/${docInfoData!!.docImageDataList.size}"
    }

    private fun checkIndexBoundary(index: Int): Pair<IndexLocationState, Int> {
        val imageCount = docInfoData!!.docImageDataList.size
        val state = when(imageCount) {
            in 0..1 -> IndexLocationState.UnderOneSize
            else -> {
                when(index) {
                    0 -> IndexLocationState.FirstIndex
                    imageCount -1 -> IndexLocationState.LastIndex
                    else -> IndexLocationState.InIndex
                }
            }
        }

        val resultIndex = index
            .coerceAtMost(imageCount - 1)
            .coerceAtLeast(0)

        return Pair(state, resultIndex)

    }
}

sealed class PreviewDocState {
    object None: PreviewDocState()
    data class Masking(val docImageData: DocImageData, val bitmap: Bitmap): PreviewDocState()
}

sealed class DialogUiState {
    object ImageAllRemovedDialog: DialogUiState()
    object DeleteImageAlertDialog: DialogUiState()
    object MaskingAlertDialog: DialogUiState()
    object MaskingAddCompleteDialog: DialogUiState()
    object MaskingRemoveCompleteDialog: DialogUiState()
    object None: DialogUiState()
    object Loading: DialogUiState()
    data class SendFailedDialog(val message: String): DialogUiState()
    data class SendSuccessDialog(val imageCount: Int): DialogUiState()
}

sealed class IndexLocationState {
    object LastIndex : IndexLocationState()
    object FirstIndex : IndexLocationState()
    object InIndex : IndexLocationState()
    object UnderOneSize : IndexLocationState()
}
