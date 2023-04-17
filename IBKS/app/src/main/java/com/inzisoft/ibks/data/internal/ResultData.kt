package com.inzisoft.ibks.data.internal

import com.google.gson.annotations.SerializedName

data class ResultData(
    val resultFormList: List<ResultFormInfo>,   // 결과 xml 리스트
    val terminalInfo: Map<String, String>,      // 비즈로직 TerminalInfo 정보
)

data class ResultFormInfo(
    val formCode: String,                   // 서식 코드
    val formName: String,                   // 서식 명
    val formPageCount: Int,                 // 서식 페이지 수
    val formVersion: String,                // 서식 버전
    val resultXmlDirPath: String            // 결과 xml 상위 폴더 경로
)

data class DataConfig(
    @SerializedName("ADDED_GUID") val addedGUID: List<Any> = listOf(),
    @SerializedName("bswr_ecode") val bSWRECode: String = "",
    @SerializedName("EDOC_IDX_NO") val entryId: String = "",
    @SerializedName("ecc_data") val eccData: String = "",
    @SerializedName("edocBffcWrtnNo") val eDocBFFCWriteNo: String = "",
    @SerializedName("FLDR_CNT") val fLDRCnt: Int = 0,
    @SerializedName("FLDR_DATA") val fLDRData: List<FlDrData> = listOf(),
    @SerializedName("GUID") val gUID: String = "",
    @SerializedName("IMG_KEY") val imgKey: String = "",
    @SerializedName("memo_data") val memoData: String = "",
    @SerializedName("refNo") val refNo: String = "",
    @SerializedName("rgsnBrcd") val rgSnBrCd: String = "",
    @SerializedName("rgsnEmn") val rgSnEmn: String = "",
    @SerializedName("SCAN_CNT") val scanCnt: Int = 0,
    @SerializedName("SCAN_DATA") val scanData: List<ScanData> = listOf(),
    @SerializedName("SCREEN_NO") val screenNo: String = "",
    @SerializedName("tempSaveEdocIndex") val tempSaveEDocIndex: String = ""
)

data class FlDrData(
    @SerializedName("FLDR_NM") val fLdrName: String,
    @SerializedName("FORMCODE") val formCode: String,
    @SerializedName("FORMNAME") val formName: String,
    @SerializedName("FORMVER") val formVersion: String,
    @SerializedName("IMG_CNT") val imgCount: Int = 0,
    @SerializedName("SEALIMG_DATA") val sealImageData: List<String> = listOf(),
    @SerializedName("XML_NM") val xmlName: String
)

data class FlDrDataExt(
    val flDrData: FlDrData,
    val dirPath: String
)

data class ScanData(
    @SerializedName("FILENAME") val fileName: String,
    @SerializedName("FLDR_NM") val fLDRName: String,
    @SerializedName("FORMCODE") val formCode: String,
    @SerializedName("FORMNAME") val formName: String,
    @SerializedName("PAGE") val page: Int,
    @SerializedName("WRHS_REG_YN") val wRHSRegYN: String = "N"
)

data class FormInfo(
    @SerializedName("formCd") val code: String,
    @SerializedName("formNm") val name: String,
    @SerializedName("pageCnt") val pages: String,
    @SerializedName("docOrder") val order: String,
    @SerializedName("autoMail") val autoMail: String = "N"
)

data class TerminalInfo(
    @SerializedName("fieldNm") val key: String,
    @SerializedName("fieldVal")val value: String
)

data class AddScanInfo(
    @SerializedName("formCd") val code: String,
    @SerializedName("formNm") val name: String
)