package com.inzisoft.ibks.data.remote.model

import com.google.gson.annotations.SerializedName

data class ApiResponse<T>(val status: Int, val message: String, val data: T? = null)

data class RegistDeviceRequest(
    val id: String,
    val deviceId: String
)

data class LoginRequest(
    val sabun: String,
    val pwd: String,
    val deviceId: String,
)

// Login Response
data class LoginResponse(
    val name: String,
    val sabun: String,
    val jikgubNm: String,
    val jikgubCd: String,
    val orgNm: String,
    val orgCd: String,
    val mailId: String,
    val chiefYn: String?,
    val handPhone: String,
    val brnNo: String,
    val brnNm: String,
    val resultCode: String,
    val expired: String,
    val tabletOtpPass: Boolean,
    val tabletSndAuth: Boolean
)

data class ModifyPwdRequest(
    val sabun: String,
    val pwd: String,
    val newPwd: String,
)

// ModifyPwd Response
data class ModifyPwdResponse(
    val resultCode: String
)

data class SignOutRequest(
    val userid: String
)

// Fund docs Response
data class DocsResponse(
    val order: String,
    val docType: String,
    val docTypeNm: String,
    val formNm: String,
    val formCd: String,
    val formDir: String,
    val autoMail: String
)

// Version Response
data class ApplicationVersionResponse(
    val name: String,
    val version: String,
    val fileRefNo: String
)

data class AuthRequest(
    @SerializedName("loginBrnNo") val loginBrnNo: String,           // 로그인 지점 코드
    @SerializedName("loginEmpNo") val loginEmpNo: String,           // 로그인 사번
    @SerializedName("loginPhoneNo") val loginPhoneNo: String,       // 로그인 핸드폰 번호
    @SerializedName("idKindCode") val idKindCode: String,           // 신분증종류코드(1.주민등록증, 2.운전면허증)
    @SerializedName("juminNo1") val juminNo1: String,               // 주민번호 앞자리
    @SerializedName("juminNo2") val juminNo2: String,               // 주민번호 뒷자리
    @SerializedName("inptPsnNm") val inptPsnNm: String,             // 입력자성명
    @SerializedName("license01") var license01: String? = null,     // 면허번호(발급지역 - 지역명 or 지역번호) (종류코드가 2일 때 필수)
    @SerializedName("license02") var license02: String? = null,     // 면허번호(교부년도 2자리) (종류코드가 2일 때 필수)
    @SerializedName("license03") var license03: String? = null,     // 면허번호(일련번호 6자리) (종류코드가 2일 때 필수)
    @SerializedName("license04") var license04: String? = null,     // 면허번호(끝 2자리) (종류코드가 2일 때 필수)
    @SerializedName("idIssDt") val idIssDt: String,                 // 신분증발급일자
    @SerializedName("imgLen2") val imgLen2: Int,                    // 사진정보 길이
    @SerializedName("strData5K") val strData5K: String,             // 사진정보
    @SerializedName("imgEvalScore") val imageScore: String          // 사진평가 점수
)

data class SendAttachImageRequest(
    @SerializedName("etryId") val entryId: String,
    @SerializedName("provId") val provId: String,
    @SerializedName("userId") val userId: String,
    @SerializedName("docCtgCd") val docCtgCd: String,
    @SerializedName("attachCd") val attachCd: String
)