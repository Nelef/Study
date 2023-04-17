package com.inzisoft.ibks

import android.graphics.Bitmap
import java.util.*

object ExceptionCode {
    const val Exception_CODE_SOCKET_TIMEOUT = -99996
    const val EXCEPTION_CODE_OTP_TIMEOUT = -99997
    const val EXCEPTION_CODE_INTERNAL_CONNECTION = -99998
    const val EXCEPTION_CODE_INTERNAL_UNKNOWN = -99999
}

object ErrorCode {
    const val ERROR_CODE_UNREGISTERED_DEVICE = 406
    const val ERROR_CODE_OTP_TIMEOUT = 451
    const val ERROR_CODE_NOT_AVAILBLE_NETWORK = 2137
}

object ErrorStatus {
    const val ERROR_STATUS_INVALID_TOKEN = 900
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
    const val SET_COOKIE = "Set-Cookie"
    const val WEB_TEMP_DATA = "webTempData"
}

enum class PreviewDocType(val type: String) {
    PREVIEW_DOC("preview_doc"),
    NORMAL_AUTH("normal_auth"),
    TAKE_DOC("take_doc")
}

enum class DocType(val type: String) {
    NORMAL("normal"),
    USE_VERSION_CHECK_XML("useVersionCheckXml"),
    ADDED_DOC("addedDoc"),
    NORMAL_PDF("normalPdf");

    companion object {
        fun getDocType(type: String): DocType {
            values().forEach {
                if (it.type == type) {
                    return it
                }
            }

            return NORMAL
        }
    }
}

enum class AuthType(val type: String, val code: String, val title: String) {
    // 주민등록증
    ID_CARD("idcard", "IDV00001", "주민등록증"),

    // 운전면허증
    DRIVE_LICENSE("drive_license", "IDV00002", "운전면허증"),

    // 외국인 등록증
    FOREIGN("foreign", "IDV00003", "외국인등록증"),

    // 재외국민 등록증
    OVERSEA("oversea", "IDV00001", "재외국민등록증"),

    // 여권
    PASSPORT("passport", "IDV00003", "여권"),

    // 국내거소 신고증
    COMPATRIOT("compatriot", "IDV00003", "국내거소 신고증");

    companion object {

        fun getAuthType(code: String): AuthType? {
            values().forEach {
                if (it.code == code) {
                    return it
                }
            }

            return null
        }

    }
}

enum class TakeType(val type: String) {
    OCR("ocr"),
    NORMAL("normal"),
    DIRECT_INPUT("direct"),
    DOC("doc")
}

sealed class FragmentRequest<T : Any>(val key: String, val resultType: Class<out T>) {
    object WritePen : FragmentRequest<String>("write_pen", String::class.java)
    object AuthCellPhone : FragmentRequest<Unit>("auth_cell_phone", Unit::class.java)
    object Instruction : FragmentRequest<Unit>("instruction", Unit::class.java)
    object ElectronicDoc : FragmentRequest<Int>("electronic_document", Int::class.java)
    object ElectronicInputDoc : FragmentRequest<String>("electronic_document", String::class.java)
    object ElectronicPreviewDoc : FragmentRequest<String>("electronic_document", String::class.java)
    object OcrCamera : FragmentRequest<Unit>("ocr_camera", Unit::class.java)
    object NormalAuthCamera : FragmentRequest<Unit>("normal_camera", Unit::class.java)
    object DocCamera : FragmentRequest<Unit>("doc_camera", Unit::class.java)
    object PreviewDoc : FragmentRequest<Int>("preview_doc", Int::class.java)
    object DatePicker : FragmentRequest<Date>("date_picker", Date::class.java)
    object SealCamera : FragmentRequest<Bitmap>("seal_camera", Bitmap::class.java)
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