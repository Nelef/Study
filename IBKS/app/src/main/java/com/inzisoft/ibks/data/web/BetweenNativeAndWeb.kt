package com.inzisoft.ibks.data.web

import java.io.Serializable

data class TermsInfo(val forms: List<FormInfo> = listOf(), val mustScroll: Boolean = false) : Serializable

data class FormInfo(val title: String = "", val formCode: String = "", val essential: Boolean = true) : Serializable

data class AgreeTerm(val formCode: String = "", val result: Boolean = false) : Serializable

data class WritePenInfo(
    val title: String,
    val pen1: WebPenData,
    val pen2: WebPenData? = null
)

data class WebPenData(
    val id: String,
    val subtitle: String?,
    val placeholder: String?
)

data class WebResultPenData(
    val id: String,
    val result: Boolean
)


data class WebViewLoadInitData(val access_token: String, val provId: String, val step: String)

data class AlternativeNum(val subNo: String)

data class PreviewData(
    val data: Map<String, String> = mapOf(),
    val agreement: Map<String, Map<String, String>> = mapOf(),
    val forms: List<String> = listOf(),
    val docs: List<EvidenceDocument> = listOf()
)

data class EvidenceDocument(val code: String, val name: String)
