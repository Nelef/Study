package com.inzisoft.ibks.viewmodel

import android.graphics.Bitmap
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.AnnotatedString
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.inzisoft.ibks.DocType
import com.inzisoft.ibks.PathManager
import com.inzisoft.ibks.base.BaseDialogFragmentViewModel
import com.inzisoft.ibks.base.UiState
import com.inzisoft.ibks.data.internal.AlertData
import com.inzisoft.ibks.data.internal.PaperlessSaveInfoDataSource
import com.inzisoft.ibks.data.internal.ResultData
import com.inzisoft.ibks.data.internal.ResultFormInfo
import com.inzisoft.ibks.data.remote.UpdateFormDataSource
import com.inzisoft.ibks.data.remote.model.ApiResult
import com.inzisoft.ibks.data.remote.model.DocsResponse
import com.inzisoft.ibks.data.repository.ElectronicDocRepository
import com.inzisoft.ibks.data.repository.LocalRepository
import com.inzisoft.ibks.data.web.AddedDocument
import com.inzisoft.ibks.data.web.EvidenceDocument
import com.inzisoft.ibks.data.web.SendImageDataInfo
import com.inzisoft.paperless.data.PaperlessSaveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File

abstract class BaseElectronicDocViewModel constructor(
    val pathManager: PathManager,
    val localRepository: LocalRepository,
    val repository: ElectronicDocRepository,
    val updateFormDataSource: UpdateFormDataSource
) : BaseDialogFragmentViewModel() {
    companion object {
        const val MAX_BYTES_MEMO = 1000
    }

    lateinit var entryId: String
    var isFinalMode: Boolean = false

    var paperlessTopBarState by mutableStateOf<PaperlessTopBarState>(PaperlessTopBarState.None)
    var paperlessState = MutableLiveData<PaperlessUiState>()
    var paperlessTransmitState by mutableStateOf<PaperlessTransmitState>(PaperlessTransmitState.None)

    private val _popupState = mutableStateOf<UiState<AlertData>>(UiState.None)
    val popupState: State<UiState<AlertData>> = _popupState

    val electronicTabDataList = mutableListOf<ElectronicTabData>()

    var showMemoDialog = mutableStateOf(false)
    var memo = mutableStateOf("")
    val memoBytes = mutableStateOf(0)

    var loadComplete by mutableStateOf(false)
    var pageInfo by mutableStateOf(Pair(0, 0))

    var selectedElectronicTabData = mutableStateOf<ElectronicTabData?>(null)

    var tabletSndAuth by mutableStateOf(false)

    abstract fun init(json: String): Job
    abstract fun checkMustEntryComplete(result: Boolean)
    abstract fun initElectronicTabData()
    abstract fun getInfo(): Map<String, String>
    abstract fun getTitle(): String
    abstract fun getDocsList(): List<EvidenceDocument>
    abstract fun getDocsDataList(): List<DocsResponse>
    abstract fun getExData(): Pair<String, Map<String, String>?>
    abstract fun hasEvidenceDocument(): Boolean

    abstract fun getAddedDocs(): List<AddedDocument>
    abstract fun getSendImageDataInfos(): List<SendImageDataInfo>

    abstract fun onComplete(tFieldData: TFieldData? = null): String
    abstract fun onCancel(): String

    fun moveTab(electronicDocInfo: ElectronicTabData) {
        paperlessState.postValue(PaperlessUiState.Load(electronicDocInfo))
        selectedElectronicTabData.value = electronicDocInfo
    }

    fun changeTopBar(isPreviewMode: Boolean) {
        paperlessTopBarState = if (isPreviewMode) {
            PaperlessTopBarState.PreviewMode(electronicTabDataList)
        } else {
            PaperlessTopBarState.InputMode(electronicTabDataList)
        }
    }

    fun changeMode(isPreviewMode: Boolean) {
        changeTopBar(isPreviewMode)
        paperlessState.postValue(
            if (isPreviewMode) {
                PaperlessUiState.PreviewMode
            } else {
                PaperlessUiState.InputMode
            }
        )
    }

    fun getElectronicTabData(businessLogic: String): ElectronicTabData? {
        for (electronicTabData: ElectronicTabData in electronicTabDataList) {
            if (businessLogic == electronicTabData.businessLogic)
                return electronicTabData
        }

        return null
    }

    fun setSealImage(sealImage: Bitmap?) {
        paperlessState.postValue(PaperlessUiState.SetSeal(sealImage))
    }

    fun getFirstElectronicDocInfo(): ElectronicTabData {
        return electronicTabDataList.first()
    }

    fun alert(data: AlertData) {
        _popupState.value = UiState.Success(data)
    }

    fun dismissAlert() {
        _popupState.value = UiState.None
    }

    fun clickInputComplete() {
        paperlessState.postValue(PaperlessUiState.FillOutComplete(electronicTabDataList))
    }

    fun savedEachPaperlessComplete(
        savedPaperlessCount: Int,
        totoalPaperlessCount: Int,
        businessLogic: String? = null,
        saveDataList: List<PaperlessSaveData> = listOf(),
        callback: () -> Unit = {}
    ) {
        viewModelScope.launch(Dispatchers.IO) {

            businessLogic?.let {
                val saveFormDataList = mutableListOf<PaperlessSaveInfoDataSource.SaveFormData>()
                saveDataList.forEach { paperlessSaveData ->
                    File(paperlessSaveData.saveFilePath).apply {
                        saveFormDataList.add(
                            PaperlessSaveInfoDataSource.SaveFormData(
                                formId = paperlessSaveData.formId,
                                formName = paperlessSaveData.formName,
                                formVersion = paperlessSaveData.formVersion,
                                formPageCount = paperlessSaveData.formPageCount,
                                saveFileDirPath = parent
                            )
                        )
                    }
                }

                paperlessTransmitState = PaperlessTransmitState.MakeResult(
                    savedPaperlessCount,
                    totoalPaperlessCount,
                    savedPaperlessCount.toFloat() / totoalPaperlessCount
                )

                var save = false
                electronicTabDataList.forEach { electronicTabData ->
                    if (it == electronicTabData.businessLogic) {
                        save = true

                        localRepository.saveElectronicSaveData(
                            PaperlessSaveInfoDataSource.PaperlessSaveInfoData(
                                it,
                                electronicTabData.title,
                                saveFormDataList
                            )
                        )
                        callback()
                    }
                }

                if (!save) {
                    callback()
                }
            }
        }
    }

    fun saveResultXmlComplete() = viewModelScope.launch(Dispatchers.IO) {
//        if (isFinalMode) {
//            localRepository.clearElectronicSaveInfo()
//        }
//        paperlessSaveInfoDataList.forEach { paperlessSaveInfoData ->
//            localRepository.saveElectronicSaveData(
//                paperlessSaveInfoData
//            )
//        }

        paperlessState.postValue(
            if (isFinalMode) {
                PaperlessUiState.PaperlessSend
            } else {
                PaperlessUiState.PaperlessComplete
            }
        )
    }

    fun noneState() {
        paperlessState.postValue(PaperlessUiState.None)
        paperlessTransmitState = PaperlessTransmitState.None
    }

    fun onTransmit() {
        showMemoDialog.value = true
    }

    fun onMemoChange(memoText: String) = viewModelScope.launch(Dispatchers.IO) {
        val bytes = memoText.toByteArray().size

        if (bytes <= MAX_BYTES_MEMO) {
            memoBytes.value = bytes
            memo.value = memoText
        }
    }

    fun onResultMemo(result: Boolean) {
        showMemoDialog.value = false

        if (result) {
            paperlessState.postValue(
                PaperlessUiState.SaveResultXml(
                    electronicTabDataList,
                    pathManager.getTempRoot(entryId)
                )
            )
        }
    }

    fun sendPaperless(terminalInfoMap: Map<String, String>) =
        viewModelScope.launch(Dispatchers.IO) {
            val paperlessSaveInfo = localRepository.getPaperlessSaveInfo().first()
            val resultFormList = mutableListOf<ResultFormInfo>()
            paperlessSaveInfo.paperlessSaveInfoList.forEach { paperlessSaveInfoData ->
                paperlessSaveInfoData.saveFormDataList.forEach { saveFormData ->
                    resultFormList.add(
                        ResultFormInfo(
                            formCode = saveFormData.formId,
                            formName = saveFormData.formName,
                            formPageCount = saveFormData.formPageCount,
                            formVersion = saveFormData.formVersion,
                            resultXmlDirPath = saveFormData.saveFileDirPath
                        )
                    )
                }
            }
            val resultData =
                ResultData(resultFormList = resultFormList, terminalInfo = terminalInfoMap)
            val exData = getExData()
            repository.makeResultZipFile(
                entryId = entryId,
                resultData = resultData,
                info = getInfo(),
                resultDocs = getDocsList(),
                addedDocs = getAddedDocs(),
                exFileName = exData.first,
                exData = exData.second,
                memo = memo.value,
                sendImageDataInfoList = getSendImageDataInfos(),
                autoMailList = getDocsDataList(),
            ).collectLatest { apiResult ->
                when (apiResult) {
                    is ApiResult.Error -> {
                        FirebaseCrashlytics.getInstance().recordException(
                            IllegalStateException(
                                "Fail to make result zip.",
                                apiResult.exception
                            )
                        )
                        paperlessTransmitState =
                            PaperlessTransmitState.Error(9, apiResult.message)
                    }
                    is ApiResult.Loading -> {}
                    is ApiResult.Success -> {
                        paperlessTransmitState =
                            PaperlessTransmitState.ZipFile(1f)
                        repository.transmitResultFile(entryId = entryId, memo = memo.value)
                            .collectLatest { result ->
                                paperlessTransmitState = when (result) {
                                    is ApiResult.Error -> {
                                        FirebaseCrashlytics.getInstance().recordException(
                                            result.exception
                                                ?: IllegalStateException("code : ${result.code}\n${result.message}")
                                        )
                                        PaperlessTransmitState.Error(
                                            result.code,
                                            result.message
                                        )
                                    }
                                    is ApiResult.Loading -> PaperlessTransmitState.Transmit(0f)
                                    is ApiResult.Success -> PaperlessTransmitState.Complete
                                }
                            }
                    }
                }
            }
        }
}

sealed class PaperlessUiState {
    object None : PaperlessUiState()
    data class Init(val electronicTabDataList: List<ElectronicTabData>) : PaperlessUiState()
    data class Load(val electronicTabData: ElectronicTabData) : PaperlessUiState()
    data class SetSeal(val sealImage: Bitmap?) : PaperlessUiState()
    data class FillOutComplete(val electronicTabDataList: List<ElectronicTabData>) :
        PaperlessUiState()

    object PreviewMode : PaperlessUiState()
    object InputMode : PaperlessUiState()
    data class SaveResultXml(
        val electronicTabDataList: List<ElectronicTabData>,
        val saveResultRootPath: String
    ) : PaperlessUiState()

    object PaperlessSend : PaperlessUiState()
    object PaperlessComplete : PaperlessUiState()
}

sealed class PaperlessTopBarState {
    object None : PaperlessTopBarState()
    data class InputMode(val electronicTabDataList: List<ElectronicTabData>) :
        PaperlessTopBarState()

    data class PreviewMode(val electronicTabDataList: List<ElectronicTabData>) :
        PaperlessTopBarState()
}

sealed class PaperlessTransmitState {
    object None : PaperlessTransmitState()
    data class MakeResult(val current: Int, val total: Int, val progress: Float) :
        PaperlessTransmitState()

    data class ZipFile(val progress: Float) : PaperlessTransmitState()
    data class Transmit(val progress: Float) : PaperlessTransmitState()
    data class Error(
        val code: Int,
        val message: String,
        val message2: AnnotatedString? = null,
        val exception: Throwable? = null
    ) : PaperlessTransmitState()

    object Complete : PaperlessTransmitState()
}

data class ElectronicTabData(
    val docType: DocType,
    val businessLogic: String,
    val title: String = "기타",
    var saveFormDataList: List<PaperlessSaveInfoDataSource.SaveFormData> = listOf(),
    val tFieldsAfterComplete: List<String> = listOf()
)

data class TFieldData(val tFieldData: Map<String, Map<String, String>>)