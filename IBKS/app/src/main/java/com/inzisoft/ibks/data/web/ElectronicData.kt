package com.inzisoft.ibks.data.web


import com.google.gson.annotations.SerializedName

data class ElectronicData(
    @SerializedName("electronicDocTitle") val electronicDocTitle: String?,
    @SerializedName("electronicDocInfo") val electronicDocInfo: List<ElectronicDocInfo>,
    @SerializedName("addition") val addition: Addition?,
)