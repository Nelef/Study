package com.inzisoft.ibks.view.compose

import android.graphics.BitmapFactory
import android.util.Log
import androidx.appcompat.widget.AppCompatImageView
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.inzisoft.ibks.data.web.AuthCameraData
import com.inzisoft.ibks.util.FileUtils
import com.inzisoft.ibks.view.compose.theme.*
import com.inzisoft.ibks.R
import com.inzisoft.ibks.viewmodel.AuthData
import com.inzisoft.ibks.viewmodel.AuthData.Companion.ISSUE_DATE

fun AppCompatImageView.setTiffThumbnailImage(tiffImagePath: String) {
    val imagePathList = FileUtils.convertTiffToJpg(context = context, tiffImagePath)
    BitmapFactory.decodeFile(imagePathList.first()).apply {
        setImageBitmap(this)
    }
}

@Composable
fun IdCardDetail(
    authData: AuthData,
    authCameraData: AuthCameraData,
    onDatePickerShow: (key: String) -> Unit
) {
    val spacerWidth = 65.dp
    val spacerHeight = 16.dp

    Column {

        when (authData) {
            // 운전면허증
            is AuthData.DriveLicenseData -> {
                detailDrive(
                    spacerWidth = spacerWidth,
                    spacerHeight = spacerHeight,
                    driveLicenseData = authData,
                    placeHolderFirstResId = R.string.placeholder_first_jumin,
                    placeHolderLastResId = R.string.placeholder_last_jumin,
                    authCameraData = authCameraData,
                    onDatePickerShow = onDatePickerShow
                )
            }
            // 주민등록증/재외국인신분증
            is AuthData.OverSea,
            is AuthData.ForeignData,
            is AuthData.IdCradData -> {
                detailIdCard(
                    spacerWidth = spacerWidth,
                    spacerHeight = spacerHeight,
                    authData = authData,
                    authCameraData = authCameraData,
                    placeHolderFirstResId = R.string.placeholder_first_jumin,
                    placeHolderLastResId = R.string.placeholder_last_jumin,
                    onDatePickerShow = onDatePickerShow
                )
            }
            // 외국인등록증
            is AuthData.ForeignData -> {
                detailIdCard(
                    spacerWidth = spacerWidth,
                    spacerHeight = spacerHeight,
                    authData = authData,
                    authCameraData = authCameraData,
                    placeHolderFirstResId = R.string.placeholder_first_num,
                    placeHolderLastResId = R.string.placeholder_last_num,
                    onDatePickerShow = onDatePickerShow
                )
            }
        }
    }
}

@Composable
internal fun detailIdCard(
    spacerWidth: Dp,
    spacerHeight: Dp,
    authData: AuthData,
    authCameraData: AuthCameraData,
    placeHolderFirstResId: Int,
    placeHolderLastResId: Int,
    onDatePickerShow: (key: String) -> Unit
) {
    // 이름
    RowName(
        spaceWidth = spacerWidth,
        authData = authData
    )

    Spacer(modifier = Modifier.height(spacerHeight))

    // 주민등록번호/외국인등록번호
    RowIdNum(
        spaceWidth = spacerWidth,
        authData = authData,
        placeHolderFirst = stringResource(id = placeHolderFirstResId),
        placeHolderLast = stringResource(id = placeHolderLastResId)
    )

    // 발급일자
    Spacer(modifier = Modifier.height(spacerHeight))
    RowIssueDate(
        spaceWidth = spacerWidth,
        authData = authData,
        onDatePickerShow = onDatePickerShow
    )

    // 발급처
    if(authCameraData.getRequireIssueOffice()) {
        Spacer(modifier = Modifier.height(spacerHeight))
        RowIssueOffice(
            spaceWidth = spacerWidth,
            authData = authData
        )
    }
}

@Composable
internal fun detailDrive(
    spacerWidth: Dp,
    spacerHeight: Dp,
    driveLicenseData: AuthData.DriveLicenseData,
    placeHolderFirstResId: Int,
    placeHolderLastResId: Int,
    authCameraData: AuthCameraData,
    onDatePickerShow: (key: String) -> Unit
) {
    // 이름
    RowName(
        spaceWidth = spacerWidth,
        authData = driveLicenseData
    )

    Spacer(modifier = Modifier.height(spacerHeight))

    // 주민등록번호/외국인등록번호
    RowIdNum(
        spaceWidth = spacerWidth,
        authData = driveLicenseData,
        placeHolderFirst = stringResource(id = placeHolderFirstResId),
        placeHolderLast = stringResource(id = placeHolderLastResId)
    )

    //운전면허증 생년월일
//    RowBirthday(
//        spaceWidth = spacerWidth,
//        driveLicenseData = driveLicenseData,
//        onDatePickerShow = onDatePickerShow
//    )
    Spacer(modifier = Modifier.height(spacerHeight))
    
    //운전면허증 라이센스번호
    RowLicenseNum(
        spaceWidth = spacerWidth,
        driveLicenseData = driveLicenseData
    )

    // 발급일자
    if(authCameraData.getRequireIssueDate()) {
        Spacer(modifier = Modifier.height(spacerHeight))
        RowIssueDate(
            spaceWidth = spacerWidth,
            authData = driveLicenseData,
            onDatePickerShow = onDatePickerShow
        )
    }

    // 발급처
    if(authCameraData.getRequireIssueOffice()) {
        Spacer(modifier = Modifier.height(spacerHeight))
        RowIssueOffice(
            spaceWidth = spacerWidth,
            authData = driveLicenseData
        )
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
        IdCardSubTitle(visible = false)
        Spacer(modifier = Modifier.width(spacerWidth))
        Text(
            modifier = Modifier.padding(0.dp, 8.dp, 0.dp, 0.dp),
            text = stringResource(id = R.string.drive_license_input_guide),
            style = MaterialTheme.typography.body2,
            color = MaterialTheme.colors.sub1Color
        )
    }
}

@Composable
internal fun IdCardSubTitle(title: String = "", visible: Boolean = true) {
    Row(modifier = Modifier.width(67.dp)) {
        if(visible) {
            Text(
                text = title,
                style = MaterialTheme.typography.subtitle1,
                color = MaterialTheme.colors.sub1Color
            )
            Text(
                text = stringResource(id = R.string.star),
                style = MaterialTheme.typography.subtitle1,
                color = MaterialTheme.colors.point4Color
            )
        }
    }
}

@Composable
fun Hyphen() {
    Text(
        modifier = Modifier.width(26.dp),
        textAlign = TextAlign.Center,
        text = stringResource(id = R.string.hyphen),
        style = MaterialTheme.typography.subtitle1,
        color = MaterialTheme.colors.sub1Color
    )
}

@Composable
fun RowName(spaceWidth: Dp, authData: AuthData) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        IdCardSubTitle(title = stringResource(id = R.string.name))
        Spacer(modifier = Modifier.width(spaceWidth))
        val focusManager = LocalFocusManager.current
        ResultTextField(
            modifier = Modifier.width(244.dp),
            text = authData.dataMap[AuthData.NAME]?: "",
            placeholder = "이름 입력",
            keyboardActions = KeyboardActions { focusManager.clearFocus() }
        ) {
            authData.dataMap[AuthData.NAME] = it
        }
    }
}

@Composable
fun RowIssueOffice(spaceWidth: Dp, authData: AuthData) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        IdCardSubTitle(title = stringResource(id = R.string.issue_office))
        Spacer(modifier = Modifier.width(spaceWidth))
        val focusManager = LocalFocusManager.current
        ResultTextField(
            modifier = Modifier.width(244.dp),
            text = authData.dataMap[AuthData.ISSUE_OFFICE] ?: "",
            placeholder = "발급처 입력",
            keyboardActions = KeyboardActions { focusManager.clearFocus() }
        ) {
            authData.dataMap[AuthData.ISSUE_OFFICE] = it
        }
    }
}

@Composable
fun RowIdNum(
    spaceWidth: Dp,
    authData: AuthData,
    placeHolderFirst: String,
    placeHolderLast: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        IdCardSubTitle(title = stringResource(id = R.string.jumin_num))
        Spacer(modifier = Modifier.width(spaceWidth))
        val focusManager = LocalFocusManager.current
        ResultTextField(
            modifier = Modifier.width(94.dp),
            text = authData.dataMap[AuthData.FRONT_IDNUM] ?: "",
            placeholder = placeHolderFirst,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Right) }
            )
        ) {
            if (it.length <= 6) {
                authData.dataMap[AuthData.FRONT_IDNUM] = it
            }
        }
        Hyphen()

        val lastIdNum = authData.dataMap[AuthData.LAST_IDNUM] ?: ""
        SecureInput(
            visibleValue = lastIdNum.substring(0, lastIdNum.length.coerceAtMost(7)),
            modifier = Modifier.width(121.dp),
            placeholder = placeHolderLast,
            keypadTitle = placeHolderLast,
            showErrorMessage = false,
            keypadPlaceholder = stringResource(id = R.string.input_placeholder_last_jumin),
            isNumKeyboard = true,
            maxLength = 7
        ) {
            authData.dataMap[AuthData.LAST_IDNUM] = String(it)
        }
    }
}

@Composable
fun RowIssueDate(
    spaceWidth: Dp,
    authData: AuthData,
    onDatePickerShow: (key: String) -> Unit
) {
    val focusManager = LocalFocusManager.current
    Row(verticalAlignment = Alignment.CenterVertically) {
        IdCardSubTitle(title = stringResource(id = R.string.issue_date))
        Spacer(modifier = Modifier.width(spaceWidth))
        ResultTextField(
            modifier = Modifier.width(141.dp),
            text = authData.dataMap[ISSUE_DATE]?: "",
            placeholder = "발급일자 입력",
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number
            ),
            keyboardActions = KeyboardActions(
                onDone = { focusManager.clearFocus() }
            ),
            visualTransformation = DateVisualTransformation()
        ) {
            if(it.length <= 8) {
                authData.dataMap[ISSUE_DATE] = it
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        GrayColorButton(
            onClick = { onDatePickerShow(ISSUE_DATE) },
            text = "날짜선택",
            buttonStyle = ButtonStyle.Basic
        )
    }
}

//@Composable
//fun RowBirthday(
//    spaceWidth: Dp,
//    driveLicenseData: AuthData.DriveLicenseData,
//    onDatePickerShow: (key: String) -> Unit
//) {
//    val focusManager = LocalFocusManager.current
//    Row(verticalAlignment = Alignment.CenterVertically) {
//        IdCardSubTitle(title = stringResource(id = R.string.birthday))
//        Spacer(modifier = Modifier.width(spaceWidth))
//        ResultTextField(
//            modifier = Modifier.width(137.dp),
//            text = driveLicenseData.dataMap[BIRTHDAY]?: "",
//            placeholder = "",
//            keyboardOptions = KeyboardOptions(
//                keyboardType = KeyboardType.Number
//            ),
//            keyboardActions = KeyboardActions(
//                onDone = { focusManager.clearFocus() }
//            ),
//            visualTransformation = DateVisualTransformation()
//        ) {
//            if(it.length <= 8) {
//                driveLicenseData.dataMap[BIRTHDAY] = it
//            }
//        }
//        Spacer(modifier = Modifier.width(12.dp))
//        GrayColorButton(
//            onClick = { onDatePickerShow(BIRTHDAY) },
//            text = "날짜선택", buttonStyle = ButtonStyle.Basic
//        )
//    }
//}

@Composable
fun RowLicenseNum(
    spaceWidth: Dp,
    driveLicenseData: AuthData.DriveLicenseData
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        IdCardSubTitle(title = stringResource(id = R.string.drive_license))
        Spacer(modifier = Modifier.width(spaceWidth))
        val focusManager = LocalFocusManager.current
        ResultTextField(
            modifier = Modifier.width(36.dp),
            text = driveLicenseData.dataMap[AuthData.LICNUM0_1] ?: "",
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Right) }
            )
        ) {
            driveLicenseData.dataMap[AuthData.LICNUM0_1] = it
        }
        Hyphen()
        ResultTextField(
            modifier = Modifier.width(36.dp),
            text = driveLicenseData.dataMap[AuthData.LICNUM2_3]?: "",
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Right) }
            )
        ) {
            driveLicenseData.dataMap[AuthData.LICNUM2_3] = it
        }
        Hyphen()

        ResultTextField(
            modifier = Modifier.width(73.dp),
            text = driveLicenseData.dataMap[AuthData.LICNUM4_9]?: "",
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Right) }
            )
        ) {
            driveLicenseData.dataMap[AuthData.LICNUM4_9] = it
        }

//        val licNum4_9 = driveLicenseData.dataMap[AuthData.LICNUM4_9] ?: ""
//        SecureInput(
//            visibleValue = licNum4_9.substring(0, licNum4_9.length.coerceAtMost(6)),
//            modifier = Modifier.width(73.dp),
//            keypadTitle = stringResource(id = R.string.input_title_drive_license),
//            keypadPlaceholder = stringResource(id = R.string.input_placeholder_drive_license),
//            isNumKeyboard = true,
//            maxLength = 6
//        ) {
//            driveLicenseData.dataMap[AuthData.LICNUM4_9] = String(it)
//        }
        Hyphen()
        ResultTextField(
            modifier = Modifier.width(36.dp),
            text = driveLicenseData.dataMap[AuthData.LICNUM10_11] ?: "",
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { focusManager.clearFocus() }
            )
        ) {
            driveLicenseData.dataMap[AuthData.LICNUM10_11] = it
        }
    }
}

@Composable
fun ResultTextField(
    modifier: Modifier,
    text: String = "",
    placeholder: String = "",
    enabled: Boolean = true,
    readOnly: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    onValueChange: (String) -> Unit
) {
    InputNoMessage(
        modifier = modifier,
        value = text,
        placeholder = placeholder,
        enabled = enabled,
        readOnly = readOnly,
        onValueChange = onValueChange,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        visualTransformation = visualTransformation
    )
}

@Composable
fun CameraAuthGuidePopup(onClose: () -> Unit) {
    BasicBottomDialog(
        title = stringResource(id = R.string.camera_auth_guide_popup_title),
        modifier = Modifier.width(840.dp),
        onClose = onClose,
        visible = true
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        Column(Modifier.fillMaxWidth()) {
            val contentSytle = MaterialTheme.typography.h6
            val contentColor = MaterialTheme.colors.mainColor
            val contentBodyStyle = MaterialTheme.typography.body1
            val contentBodyColor = MaterialTheme.colors.sub1Color
            val contentBodyModifier = Modifier.padding(20.dp, 0.dp, 0.dp, 0.dp)

            Column(Modifier.padding(156.dp, 0.dp)) {
                Text(
                    text = stringResource(id = R.string.camera_auth_guide_popup_title),
                    style = MaterialTheme.typography.h4,
                    color = contentColor
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = stringResource(id = R.string.camera_auth_guide_popup_content1),
                    style = contentSytle,
                    color = contentColor
                )
                Text(
                    modifier = contentBodyModifier,
                    text = stringResource(id = R.string.camera_auth_guide_popup_content1_text),
                    style = contentBodyStyle,
                    color = contentBodyColor
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(id = R.string.camera_auth_guide_popup_content2),
                    style = contentSytle,
                    color = contentColor
                )
                Text(
                    modifier = contentBodyModifier,
                    text = stringResource(id = R.string.camera_auth_guide_popup_content2_text),
                    style = contentBodyStyle,
                    color = contentBodyColor
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(id = R.string.camera_auth_guide_popup_content3),
                    style = contentSytle,
                    color = contentColor
                )
                Text(
                    modifier = contentBodyModifier,
                    text = stringResource(id = R.string.camera_auth_guide_popup_content3_text),
                    style = contentBodyStyle,
                    color = contentBodyColor
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(id = R.string.camera_auth_guide_popup_content4),
                    style = contentSytle,
                    color = contentColor
                )
                Text(
                    modifier = contentBodyModifier,
                    text = stringResource(id = R.string.camera_auth_guide_popup_content4_text),
                    style = contentBodyStyle,
                    color = contentBodyColor
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(id = R.string.camera_auth_guide_popup_content5),
                    style = contentSytle,
                    color = contentColor
                )
                Text(
                    modifier = contentBodyModifier,
                    text = stringResource(id = R.string.camera_auth_guide_popup_content5_text),
                    style = contentBodyStyle,
                    color = contentBodyColor
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                ColorButton(
                    onClick = onClose,
                    text = stringResource(id = R.string.confirm),
                    modifier = Modifier
                        .padding(start = 24.dp)
                        .width(240.dp),
                    buttonStyle = ButtonStyle.Big
                )
            }

            Spacer(modifier = Modifier.height(45.dp))
        }
    }
}

internal class DateVisualTransformation: VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        return dateFilter(text)
    }

    private fun dateFilter(text: AnnotatedString): TransformedText {

        // Making yyyy-MM-dd string.
        val trimmed = if (text.text.length >= 8)
            text.text.substring(0..7) else text.text
        var out = ""
        for (i in trimmed.indices) {
            out += trimmed[i]
            if (i == 3 || i == 5) out += "-"
        }
        Log.e("SW_DEBUG", "dateFilter" + text.text + " // trimmed: $trimmed")

        val dateOffsetTranslator = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                val resultOffset = if (offset <= 3) offset
                else if (offset <= 5) offset + 1
                else if (offset <= 8) offset + 2
                else 10

                Log.e("SW_DEBUG", "originalToTransformed offset: $offset result: $resultOffset")
                return resultOffset
            }

            override fun transformedToOriginal(offset: Int): Int {
                val resultOffset =
                    if (offset <= 4) offset
                    else if (offset <= 7) offset - 1
                    else if (offset <= 10) offset - 2
                    else 8
                Log.e("SW_DEBUG", "transformedToOriginal offset: $offset result: $resultOffset")
                return resultOffset
            }
        }

        return TransformedText(AnnotatedString(out), dateOffsetTranslator)
    }
}