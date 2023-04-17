package com.inzisoft.ibks.data.remote.model

import com.google.gson.annotations.SerializedName

data class RequestIssueOtp(
    @SerializedName("custMpno") val cellphone: String
)

data class RequestConfirmOtp(
    @SerializedName("custMpno") val cellPhone: String,
    @SerializedName("otpCd") val otp: String
)