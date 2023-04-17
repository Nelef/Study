package com.inzisoft.ibks.viewmodel

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.inzisoft.ibks.DocType
import com.inzisoft.ibks.PathManager
import com.inzisoft.ibks.data.remote.UpdateFormDataSource
import com.inzisoft.ibks.data.remote.model.DocsResponse
import com.inzisoft.ibks.data.repository.ElectronicDocRepository
import com.inzisoft.ibks.data.repository.LocalRepository
import com.inzisoft.ibks.data.web.*
import com.inzisoft.ibks.util.log.QLog
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ElectronicDocPreviewModeViewModel @Inject constructor(
    pathManager: PathManager,
    localRepository: LocalRepository,
    repository: ElectronicDocRepository,
    updateFormDataSource: UpdateFormDataSource
) : BaseElectronicDocViewModel(pathManager, localRepository, repository, updateFormDataSource) {
    lateinit var addtionData: Addition

    private val completeTransmitState = CompleteTransmitState()

    override fun init(json: String) = viewModelScope.launch(Dispatchers.IO) {
        runCatching {
            isFinalMode = true

            val data = Gson().fromJson(json, Addition::class.java)
                ?: throw IllegalArgumentException("json is empty.")

            entryId = localRepository.getAuthInfo().first().edocKey

            if (entryId.isEmpty()) {
                throw IllegalArgumentException("entryId is empty.")
            }

            tabletSndAuth = localRepository.getUserInfo().first().tabletSndAuth

            data
        }.onSuccess {
            addtionData = it
            initElectronicTabData()
        }.onFailure {
            QLog.e(it)
        }
    }

    override fun initElectronicTabData() {
        viewModelScope.launch {
            val paperlessSaveInfo = localRepository.getPaperlessSaveInfo().first()
            Log.e("SW_DEBUG", "paperlessSaveInfo: $paperlessSaveInfo")
            paperlessSaveInfo.paperlessSaveInfoList.forEach { paperlessSaveInfoData ->
                Log.e("SW_DEBUG", "paperlessSaveInfoData: $paperlessSaveInfoData")
                electronicTabDataList.add(
                    ElectronicTabData(
                        DocType.NORMAL,
                        paperlessSaveInfoData.businessLogic,
                        paperlessSaveInfoData.title,
                        paperlessSaveInfoData.saveFormDataList
                    )
                )
            }

            if (isFinalMode && hasEvidenceDocument()) {
                electronicTabDataList.add(
                    ElectronicTabData(
                        DocType.ADDED_DOC,
                        "",
                        "증빙서류"
                    )
                )
            }

            changeTopBar(true)
            paperlessState.postValue(PaperlessUiState.Init(electronicTabDataList))
        }
    }

    override fun checkMustEntryComplete(result: Boolean) {
        paperlessState.postValue(
            if (result) {
                changeTopBar(isPreviewMode = true)
                PaperlessUiState.PreviewMode
            } else {
                PaperlessUiState.None
            }
        )
    }

    override fun getInfo(): Map<String, String> {
        return addtionData.info
    }

    override fun getTitle(): String {
        return "미리보기"
    }

    override fun getExData(): Pair<String, Map<String, String>?> {
        return Pair(addtionData.exFileName ?: "", addtionData.exData)
    }

    override fun getDocsList(): List<EvidenceDocument> {
        return addtionData.docs ?: listOf()
    }

    override fun getDocsDataList(): List<DocsResponse> {
        return addtionData?.docsData ?: listOf()
    }

    override fun hasEvidenceDocument(): Boolean {
        return addtionData.docs?.isNotEmpty() ?: false
    }

    override fun getAddedDocs(): List<AddedDocument> {
        return addtionData.addedDocs ?: listOf()
    }

    override fun getSendImageDataInfos(): List<SendImageDataInfo> {
        return addtionData.sendImageInfo ?: listOf()
    }

    override fun onComplete(tFieldData: TFieldData?): String {
        completeTransmitState.isComplete = true
        if (tFieldData != null) {
            completeTransmitState.tFieldData = tFieldData.tFieldData
        }
        return Gson().toJson(completeTransmitState)
    }


    override fun onCancel(): String {
        completeTransmitState.isComplete = false
        return Gson().toJson(completeTransmitState)
    }
}