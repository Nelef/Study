package com.inzisoft.ibks.viewmodel

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
class ElectronicDocInputModeViewModel @Inject constructor(
    pathManager: PathManager,
    localRepository: LocalRepository,
    repository: ElectronicDocRepository,
    updateFormDataSource: UpdateFormDataSource
) : BaseElectronicDocViewModel(pathManager, localRepository, repository, updateFormDataSource) {
    lateinit var paperlessData: ElectronicData

    private val completeTransmitState = CompleteTransmitState()

    override fun init(json: String) = viewModelScope.launch(Dispatchers.IO) {
        runCatching {
            val data = Gson().fromJson(json, ElectronicData::class.java)
                ?: throw IllegalArgumentException("json is empty.")

            entryId = localRepository.getAuthInfo().first().edocKey

            if (entryId.isEmpty()) {
                throw IllegalArgumentException("entryId is empty.")
            }

            tabletSndAuth = localRepository.getUserInfo().first().tabletSndAuth

            data
        }.onSuccess {
            paperlessData = it
            initElectronicTabData()
        }.onFailure {
            QLog.e(it)
        }
    }

    override fun initElectronicTabData() {
        paperlessData.electronicDocInfo.forEach { electronicDocInfo ->
            electronicTabDataList.add(
                ElectronicTabData(
                    docType = DocType.getDocType(electronicDocInfo.openType),
                    businessLogic = electronicDocInfo.businessLogic,
                    title = electronicDocInfo.title,
                    tFieldsAfterComplete = electronicDocInfo.tFieldIdForWeb ?: listOf()
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

        changeTopBar(false)
        paperlessState.postValue(PaperlessUiState.Init(electronicTabDataList))
    }

    override fun checkMustEntryComplete(result: Boolean) {
        paperlessState.postValue(
            if (result) {
                if (isFinalMode) {
                    changeTopBar(isPreviewMode = true)
                    PaperlessUiState.PreviewMode
                } else {
                    PaperlessUiState.SaveResultXml(
                        electronicTabDataList,
                        pathManager.getTempRoot(entryId)
                    )
                }
            } else {
                PaperlessUiState.None
            }
        )
    }

    fun getElectronicDocInfo(title: String): ElectronicDocInfo {
        paperlessData.electronicDocInfo.forEach {
            if (it.title == title) {
                return it
            }
        }

        throw IllegalStateException("doesn't find tab title = $title")
    }

    override fun getInfo(): Map<String, String> {
        return paperlessData.addition?.info ?: mapOf()
    }

    override fun getTitle(): String {
        return paperlessData.electronicDocTitle ?: "신청서 작성"
    }

    override fun getExData(): Pair<String, Map<String, String>?> {
        return Pair(
            paperlessData.addition?.exFileName ?: "",
            paperlessData.addition?.exData
        )
    }

    override fun getDocsList(): List<EvidenceDocument> {
        return paperlessData.addition?.docs ?: listOf()
    }

    override fun getDocsDataList(): List<DocsResponse> {
        return paperlessData.addition?.docsData ?: listOf()
    }

    override fun hasEvidenceDocument(): Boolean {
        return paperlessData.addition?.docs?.isNotEmpty() ?: false
    }

    override fun getAddedDocs(): List<AddedDocument> {
        return paperlessData.addition?.addedDocs ?: listOf()
    }

    override fun getSendImageDataInfos(): List<SendImageDataInfo> {
        return paperlessData.addition?.sendImageInfo ?: listOf()
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