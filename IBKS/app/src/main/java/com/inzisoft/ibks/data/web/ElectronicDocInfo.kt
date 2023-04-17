package com.inzisoft.ibks.data.web


import com.google.gson.annotations.SerializedName
import com.inzisoft.ibks.DocType

data class ElectronicDocInfo(
    @SerializedName("openType") val openType: String = DocType.NORMAL.type,
    @SerializedName("businessLogic") val businessLogic: String = "EMPTY_ID",
    @SerializedName("penSeal") val penSealImg: List<String>? = null,
    @SerializedName("title") val title: String = "",
    @SerializedName("appendForm") val appendForm: List<String>? = null,
    @SerializedName("productCode") val productCode: String? = null,
    @SerializedName("data") val data: Map<String, String>? = null,
    @SerializedName("tFieldIdForWeb") val tFieldIdForWeb: List<String>? = null,
) {
    override fun equals(other: Any?): Boolean {
        other?.let {
            if (other is ElectronicDocInfo) {
                return (businessLogic == other.businessLogic) && (title == other.title)
            }
        }

        return false
    }

    override fun hashCode(): Int {
        return businessLogic.hashCode()
    }
}