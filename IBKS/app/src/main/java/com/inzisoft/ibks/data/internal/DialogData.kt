package com.inzisoft.ibks.data.internal

import androidx.compose.ui.text.AnnotatedString
import com.inzisoft.ibks.base.PopupState
import com.inzisoft.paperless.inputmethod.data.PenPoint
import java.io.Serializable

data class DialogData(
    val titleText: String,
    val contentText: AnnotatedString,
    val leftBtnText: String? = null,
    val rightBtnText: String,
    val onDismissRequest: (popupState: PopupState) -> Unit
)

data class AlertData(
    val contentText: String,
    val leftBtnText: String? = null,
    val rightBtnText: String,
    val onDismissRequest: (popupState: PopupState) -> Unit
)

data class PenDialogData(
    val type: PenDialogType,
    val title: String,
    val penData: PenData? = null,
    val signPenData: PenData? = null,
    val sealData: PenData? = null
) : Serializable

enum class PenDialogType(val type: String) {
    NONE("none"),
    PEN("pen"), // 따라쓰기
    PENSEAL("penseal"), // 성명 + 싸인 or 인감
    PENSEALONLY("pensealonly"), // 성명 + 인감
    SEAL("seal") // 인감
}

fun getPenDialogType(type: String): PenDialogType {
    return PenDialogType.values().find { it.type == type } ?: PenDialogType.NONE
}

data class PenData(
    val id: String = "",
    val subtitle: String? = null,
    val placeholder: String? = null,
    val imagePath: String? = null,
    val pointList: List<PenPoint>? = null
) : Serializable

data class ResultPenData(
    val id: String = "",
    val result: Boolean = false,
    val value: String = ""
)

sealed class AuthDialogData {
    object None : AuthDialogData()
    object Loading : AuthDialogData()
    object ShowAuthGuidePopup : AuthDialogData()
    object ShowOcrFailedPopup : AuthDialogData()
    object AuthComplete : AuthDialogData()
    data class AuthFailedPopup(val message: String) : AuthDialogData()
}

data class Thumbnail(
    val title: String,
    val image: Image,
    val isFirst: Boolean,
    val isShowDivider: Boolean,
    val isComplete: Boolean? = null
)

data class Image(
    val path: String,
    val width: Int,
    val height: Int
)

data class OutLine(
    val title: String,
    val pageIndex: Int
)

data class InstructionData(val title: String, val imagePaths: List<String>)
