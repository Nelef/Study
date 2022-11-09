package com.inzisoft.ibks

import com.inzisoft.ibks.data.web.AgreeTerm

object ExceptionCode {
    const val EXCEPTION_CODE_OTP_TIMEOUT = -99997
    const val EXCEPTION_CODE_INTERNAL_CONNECTION = -99998
    const val EXCEPTION_CODE_INTERNAL_UNKNOWN = -99999
}

object JavaScriptFunctionName {
    const val ON_CLICKED_TEMP_SAVE = "onClickedTempSave"
}

object Constants {
    const val TEMP_DATA_WEB_JSON_FILE_NAME = "webdata.json"
    const val TEMP_DATA_API_MULTIPART_JSON_NAME = "bzwkInfo"
    const val KEY_PREVIEW_DOC_TYPE = "previewDocType"
    const val KEY_IMAGE_DIR_PATH = "imageDirPath"
    const val KEY_SCRIPT_FUN_NAME = "scriptFunName"
    const val KEY_DOC_CAMERA_DATA = "docCameraData"
    const val KEY_AUTH_CAMERA_DATA = "authCameraData"
}

object AppKeySet {
    const val ACCESS_TOKEN = "Access-Token"
    const val WEB_TEMP_DATA = "webTempData"
}

enum class PreviewDocType(val type: String) {
    PREVIEW_DOC("preview_doc"),
    NORMAL_AUTH("normal_auth"),
    TAKE_DOC("take_doc")
}

enum class AuthType(val type: String) {
    // 주민등록증
    IDCARD("idcard"),
    
    // 운전면허증
    DRIVE_LECENSE("drive_license"),
    
    // 외국인 등록증
    FOREIGN("foreign"),

    // 재외국민 등록증
   OVERSEA("oversea"),

    // 여권
    PASSPORT("passport"),

    // 국내거소 신고증
    COMPATRIOT("compatriot")
}

enum class TakeType(val type: String) {
    OCR("ocr"),
    NORMAL("normal"),
    DIRECT_INPUT("direct"),
    DOC("doc")
}

enum class ProvIdSet(val provId: String) {
    HN("하나"),
    IB("IBK"),
    NH("NH농협")
}

sealed class FragmentRequest<T : Any>(val key: String, val resultType: Class<out T>) {
    object ShowPdfs: FragmentRequest<List<String>>("show_pdfs", listOf<String>()::class.java)
    object ShowTerms : FragmentRequest<List<AgreeTerm>>("show_terms", listOf<AgreeTerm>()::class.java)
    object WritePen : FragmentRequest<String>("write_pen", String::class.java)
    object AuthCellPhone : FragmentRequest<Unit>("auth_cell_phone", Unit::class.java)
    object IssueAlternativeNum : FragmentRequest<String>("issue_alternative_num", String::class.java)
    object Preview : FragmentRequest<Int>("preview", Int::class.java)
    object OcrCamera : FragmentRequest<Unit>("ocr_camera", Unit::class.java)
    object NormalAuthCamera : FragmentRequest<Unit>("normal_camera", Unit::class.java)
    object DirectInput : FragmentRequest<Unit>("direct_input", Unit::class.java)
    object DocCamera : FragmentRequest<Unit>("doc_camera", Unit::class.java)
    object PreviewDoc : FragmentRequest<Int>("preview_doc", Int::class.java)
    object Unknown : FragmentRequest<String>("unknown", String::class.java)

    fun fromKey(key: String): Boolean {
        return this.key == key
    }
}

sealed class FragmentResult<T> {
    companion object {
        const val OK = "OK"
        const val CANCEL = "CANCEL"
        const val ERROR = "ERROR"
    }

    class Cancel<T> : FragmentResult<T>()
    data class OK<T>(val data: T?) : FragmentResult<T>()
    data class Error<T>(val message: String) : FragmentResult<T>()
}