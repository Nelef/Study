package com.inzisoft.ibks.data.remote.model

import com.google.gson.annotations.SerializedName

data class ApiResponse<T>(val status: Int, val message: String, val data: T? = null)

data class LoginRequest(
    val deviceId: String,
    val from: String,
    val userId: String,
    val userPswr: String
)

// Login Response
data class LoginResponse(
    val agntCpGrade: String?,
    val colprCd: String,
    val emadr: String,
    val inspctFnctCd: String?,
    val inspctId: String,
    val mpno: String,
    val offcTlno: String,
    val officeCd: String,
    val officeNm: String?,
    val provId: String,
    val socRgstDate: String?,
    val userId: String,
    val userNm: String
)

// Version Response
data class ApplicationVersionResponse(
    val name: String,
    val version: String,
    val fileRefNo: String
)

data class SendAttachImageRequest(
    @SerializedName("etryId") val entryId: String,
//    @SerializedName("provId") val provId: String,
//    @SerializedName("userId") val userId: String,
    @SerializedName("docCtgCd") val docCtgCd: String,
    @SerializedName("attachCd") val attachCd: String
)