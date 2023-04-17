package com.inzisoft.ibks.data.web


import com.google.gson.annotations.SerializedName
import com.inzisoft.ibks.data.remote.model.DocsResponse

data class Addition(
    @SerializedName("exFileName") val exFileName: String?,
    @SerializedName("exData") val exData: Map<String, String>?,
    @SerializedName("info") val info: Map<String, String>,
    @SerializedName("docs") val docs: List<EvidenceDocument>?,
    @SerializedName("addedDocs") val addedDocs: List<AddedDocument>?,
    @SerializedName("sendImageInfo") val sendImageInfo: List<SendImageDataInfo>?,
    @SerializedName("docsData") val docsData: List<DocsResponse>?
)