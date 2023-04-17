package com.inzisoft.ibks.data.remote.model

import com.google.gson.annotations.SerializedName


data class RequestEntryIdData(
    @SerializedName("provId") val provId: String = "IS",
    @SerializedName("bzwkDvcd") val code: String,
    @SerializedName("subBzwkDvcd") val subCode: String,
    @SerializedName("officeCd") val officeCd: String,
    @SerializedName("userId") val userId: String,
    @SerializedName("custNm") val customerName: String,
    @SerializedName("prcsTycd") val prcsTycd: String = "ODS"
)

data class ResponseEntryIdData(
    @SerializedName("etryId") val entryId: String
)

data class RequestResultInfo(
    @SerializedName("etryId") val entryId: String,
    @SerializedName("provId") val provId: String = "IS",
    @SerializedName("memo") val memo: String
)

data class RequestEDSBasicData(
    @SerializedName("etryId") val entryId: String,
    @SerializedName("userId") val userId: String
)

data class RequestEDSBatchData(
    @SerializedName("edocId") val entryId: String,
    @SerializedName("fileName") val fileName: String
)