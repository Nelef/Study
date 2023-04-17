package com.inzisoft.ibks.data.web

import com.google.gson.annotations.SerializedName

data class SendImageDataInfo(
    @SerializedName("tFieldId") val tFieldId: String,
    @SerializedName("type") val type: String,
    @SerializedName("name") val name: String,
) {
    var base64Image: String? = null
    var path: String? = null
}
