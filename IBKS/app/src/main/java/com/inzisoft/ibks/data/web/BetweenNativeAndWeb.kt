package com.inzisoft.ibks.data.web

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.inzisoft.ibks.data.remote.model.DocsResponse
import kotlinx.android.parcel.Parcelize

data class EntryInfo(
    val custName: String? = "",
    val code: String,
    val subCode: String?
)

data class ResultEntryData(
    val result: Boolean = false,
    val entryId: String? = null
)

data class SecureKeyPadInfo(
    val title: String? = null,
    val isNumber: Boolean? = false,
    val placeholder: String? = null,
    val minLength: Int? = null,
    val maxLength: Int? = null
)

data class SecureKeyPadResult(
    val plain: String,
    val enc: String,
    val decPart: String
)

data class WritePenInfo(
    val type: String,
    val title: String,
    val pen: WebPenData? = null,
    val signPen: WebPenData? = null,
    val seal: WebPenData? = null
)

data class WebPenData(
    val id: String,
    val subtitle: String? = null,
    val placeholder: String? = null
)

data class WebResultPenData(
    val id: String,
    val result: Boolean
)


data class WebViewLoadInitData(val cookie: String, val provId: String)

data class InstructionInfo(
    val title: String = "",
    val productCode: String = "",
    val formCode: List<String> = listOf()
)

data class ElectronicDocData(
    val businessLogic: String = "",
    val data: Map<String, String> = mapOf(),
    val exData: Map<String, String> = mapOf(),
    val exDataFileName: String = "InvData",
    val options: ElectronicDocOptions = ElectronicDocOptions(),
    val pen: List<String> = listOf(),
    val docs: List<EvidenceDocument> = listOf(),
    val records: List<RecordOfVoice> = listOf(), // TODO 녹취 변경 삭제코드
    val info: Map<String, String> = mapOf()
)

data class RestoreElectronicDocData(
    val data: List<RestoreData>,
    val docs: List<EvidenceDocument> = listOf()
)

data class RestoreData(
    val businessLogic: String,
    val resultXmlPath: String
)

data class ElectronicDocOptions(
    val startPreview: Boolean = false
)

data class RecordDataList(val records: List<RecordOfVoice> = listOf())

data class RecordOfVoice(val code: String, val name: String)

data class RecordOfVoiceEx(val record: RecordOfVoice, val count: Int)

@Parcelize
data class EvidenceDocument(val code: String, val name: String, val count: Int) : Parcelable

data class AddedDocument(
    @SerializedName("formCd") val code: String,
    @SerializedName("docType") val type: String,
    @SerializedName("docTypeNm") val name: String,
    @SerializedName("formDir") val dir: String,
    @SerializedName("count") val count: Int
)

data class CompleteTransmitState(
    var isComplete: Boolean? = true,
    var tFieldData: Map<String, Map<String, String>>? = null
)

data class NotifyWebPage(val currentPage: String)

data class AppendFormList(val formList: List<String>)

data class DocsProductCode(val productCode: String)

data class AppendDocsList(
    val isComplete: Boolean,
    val docsList: List<String>,
    val showDocsList: List<String>,
    val docsData: List<DocsResponse>?
)

data class DownloadForms(val docs: List<DocsResponse>)

data class DownloadFormsResult(val isComplete: Boolean)

data class ClearFormList(val formList: List<String>?)