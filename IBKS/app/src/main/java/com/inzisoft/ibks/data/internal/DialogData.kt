package com.inzisoft.ibks.data.internal

import android.graphics.Bitmap
import com.inzisoft.ibks.base.PopupState
//import com.ml.inputmethod.data.PenPoint
//import com.ml.qservice.base.PopupState
import java.io.Serializable

data class DialogData(
    val titleText: String,
    val contentText: String,
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

//data class PenDialogData(
//    val title: String,
//    val penData1: PenData,
//    val penData2: PenData? = null
//) : Serializable
//
//data class PenData(
//    val id: String = "",
//    val subtitle: String? = null,
//    val placeholder: String? = null,
//    val imagePath: String? = null,
//    val pointList: List<PenPoint>? = null
//) : Serializable

data class ResultPenData(
    val id: String = "",
    val result: Boolean = false,
    val value: String = ""
)

sealed class AuthDialogData {
    object None : AuthDialogData()
    object Loading: AuthDialogData()
    object ShowAuthGuidePopup : AuthDialogData()
    object ShowOcrFailedPopup : AuthDialogData()
    data class AuthFailedPopup(val message: String) : AuthDialogData()
}

data class IssueAlternativeNumData(
    val entryId: String,
    val firstIdNum: String,
    val lastIdNum: String
)

data class Thumbnail(
    val image: Image,
    val isShowDivider: Boolean
)

data class Image(
    val path: String,
    val width: Int,
    val height: Int
)

data class RecruiterDialogData(
    val id: String,
    val name: String,
    val companyName: String,
    val officeNumber: String,
    val phoneNumber: String,
    val profileImage: Bitmap
)
