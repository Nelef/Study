package com.inzisoft.ibks.data.web

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class AuthCameraData(
    @SerializedName("cameraType") val cameraType: String,
    @SerializedName("takeType") val takeType: String,
    @SerializedName("docCode") val docCode: String? = "",
    @SerializedName("requireIssueDate") val requireIssueDate: String,
    @SerializedName("requireIssueOffice") val requireIssueOffice: String
) : Serializable {
    fun getRequireIssueDate(): Boolean {
        return requireIssueDate.equals("Y", true)
    }

    fun getRequireIssueOffice(): Boolean {
        return requireIssueOffice.equals("Y", true)
    }
}

data class DocCameraData(
    val docName: String,
    val docCode: String,    // 문서코드
    val takeCount: Int,
    val maskingYn: String,
    val docCtgCd: String? = ""    // 서류 구분 코드
) : Serializable {
    fun getMaskingYn(): Boolean {
        return when (maskingYn) {
            "Y" -> true
            else -> false
        }
    }
}

data class DocCameraResultData(
    val isComplete: Boolean,
    val takeCountNum: Int
) : Serializable