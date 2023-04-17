package com.inzisoft.ibks.view.compose

import android.media.MediaPlayer
import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import com.inzisoft.ibks.DocType
import com.inzisoft.ibks.R
import com.inzisoft.ibks.base.Right
import com.inzisoft.ibks.data.internal.*
import com.inzisoft.ibks.util.log.QLog
import com.inzisoft.ibks.view.compose.theme.*
import com.inzisoft.ibks.viewmodel.CameraState
import com.inzisoft.ibks.viewmodel.ElectronicTabData
import com.skydoves.landscapist.glide.GlideImage
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.timer


@Preview(
    widthDp = 1280,
    heightDp = 800,
    showBackground = true,
    backgroundColor = 0xFFFFFF
)
@Composable
fun PreviewTopBarScreen() {

    IBKSTheme {
        Scaffold(topBar = {
            Column {
                MainTopBar(
                    titleText = "MainBar",
                    userId = "User1",
                    userName = "김모집",
                    onLogout = { },
                    onSetting = { }
                )

//                PreviewTopBar(
//                    title = "ProcessBar",
//                    thumbnailList = listOf(),
//                    currentPage = 0,
//                    onClickThumbnail = { },
//                    onThumbnail = { },
//                    onShowApplication = { },
//                    onShowInstruction = { },
//                    onShowEvidenceDocument = { },
//                    onEdit = { },
//                    onCancel = { },
//                    onTransmit = { },
//                    onAlert = { }
//                )

//                val samplePreviewDataList = listOf(
//                    PaperlessPreviewData(
//                        "d",
//                        "핵심상품설명서",
//                        PaperlessType.PRODUCT_DESCRIPTION) {},
//                    PaperlessPreviewData(
//                        "a",
//                        "상품설명서",
//                        PaperlessType.PRODUCT_DESCRIPTION) {},
//                    PaperlessPreviewData(
//                        "b",
//                        "신청서",
//                        PaperlessType.APPLICATION) {},
//                    PaperlessPreviewData(
//                        "c",
//                        "증빙서류",
//                        PaperlessType.DOC) {}
//                )

                val tab1 = ElectronicTabData(DocType.NORMAL, "1", "상품설명서")
                val tab2 = ElectronicTabData(DocType.NORMAL, "2", "상품신청서")

                PaperlessPreviewTopBar(
                    title = "Preview",
                    electronicTabDataList = listOf(tab1, tab2),
                    onClickThumbnail = { },
                    pageInfo = "1/10",
                    showThumbnail = false,
                    onTabClick = { },
                    onEdit = { },
                    selectedTab = tab1,
                    onCancel = { },
                    onTransmit = { },
                    onAlert = { },
                    tabletSndAuth = true
                )

                PaperlessInputTopBar(
                    title = "InputMode",
                    electronicTabDataList = listOf(tab1, tab2),
                    onClickThumbnail = { },
                    pageInfo = "1/10",
                    onTabClick = { },
                    selectedTab = tab1,
                    showThumbnail = false,
                    showHighlighter = false,
                    onClickHighlighter = {},
                    onReleaseHighlighter = { },
                    onHighlighter = { },
                    onEraser = { },
                    onCancel = {},
                    onAlert = {},
                )

//                ElectronicDocTopBar(
//                    title = "TempProcessBar",
//                    thumbnailList = listOf(),
//                    currentPage = 0,
//                    onClickThumbnail = { },
//                    onThumbnail = { },
//                    pageInfo = "1/10",
//                    showApplication = true,
//                    onShowApplication = { },
//                    hasInstruction = true,
//                    showInstruction = false,
//                    onShowInstruction = { },
//                    onReleaseHighlighter = { },
//                    onHighlighter = { },
//                    onEraser = { },
//                    onCancel = {},
//                    onAlert = {},
//                )

                BasicTopBar(title = "BasicBar")

                OcrCameraTopBar(
                    title = "CameraBar",
                    cameraState = CameraState.CameraPreviewState,
                    onRetake = { },
                    onCancel = { },
                    onAuth = { },
                )

                NormalCameraTopBar(title = "NormalCameraBar", onCancel = {}) {}

                PreviewDocTopBar(
                    title = "PreviewDoc",
                    showBack = true,
                    onBack = {},
                    showCompleteBtn = true
                ) {

                }
            }
        }) {

        }
    }
}

@Composable
fun MainTopBar(
    titleText: String,
    userId: String,
    userName: String,
//    onAlarm: () -> Unit,
    onLogout: () -> Unit,
    onSetting: () -> Unit
) {

    val textStyle = TextStyle(
        color = MaterialTheme.colors.background1Color,
        fontSize = 12.sp
    )

    TopBar(titleText = titleText,
        backgroundColor = Color(0xFF358FC5),
        contentTextColor = MaterialTheme.colors.background1Color,
        navigationContents = {
            Row(
                modifier = Modifier.fillMaxHeight(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = Modifier.width(24.dp))
                Image(
                    painter = painterResource(R.drawable.logo_ibks_w),
                    modifier = Modifier.height(24.dp),
                    contentDescription = ""
                )
                Spacer(modifier = Modifier.width(12.dp))
                Divider(
                    modifier = Modifier
                        .height(12.dp)
                        .width(1.dp),
                    color = MaterialTheme.colors.background1Color
                )
            }
        },
        topBarButtons = {
            Row(
                modifier = Modifier.fillMaxHeight(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.micon_member_on),
                    contentDescription = ""
                )

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = userId,
                    style = textStyle
                )

                Spacer(modifier = Modifier.width(8.dp))

                Divider(
                    modifier = Modifier
                        .height(12.dp)
                        .width(1.dp),
                    color = MaterialTheme.colors.background1Color
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = userName,
                    style = textStyle
                )

                Spacer(modifier = Modifier.width(24.dp))

                // TODO: 향후 push 개발
//                Spacer(modifier = Modifier.width(24.dp))
//
//                TopBarDivider(MaterialTheme.colors.background1Color)
//
//                Spacer(modifier = Modifier.width(24.dp))
//
//                CircleImageButton(
//                    onClick = onAlarm,
//                    icon = R.drawable.micon_alarm,
//                    pressedIcon = R.drawable.micon_alarm_on
//                )
//
//                Spacer(modifier = Modifier.width(24.dp))

                TopBarDivider(MaterialTheme.colors.background1Color)

                Spacer(modifier = Modifier.width(12.dp))

                CircleImageButton(
                    onClick = onLogout,
                    icon = R.drawable.micon_logout_on,
                    pressedIcon = R.drawable.micon_logout
                )

                Spacer(modifier = Modifier.width(12.dp))

                CircleImageButton(
                    onClick = onSetting,
                    icon = R.drawable.micon_menu_on,
                    pressedIcon = R.drawable.micon_menu
                )

                Spacer(modifier = Modifier.width(12.dp))
            }
        }
    )
}

@Composable
fun OcrCameraTopBar(
    title: String,
    cameraState: CameraState = CameraState.CameraPreviewState,
    showRetake: Boolean = true,
    onRetake: () -> Unit = {},
    onCancel: () -> Unit,
    showAuth: Boolean = true,
    onAuth: () -> Unit = {},
) {
    TopBar(titleText = title) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (showRetake) {
                TopBarButton(
                    onClick = onRetake,
                    enabled = cameraState is CameraState.CameraOcrResultState,
                    icon = R.drawable.micon_camera,
                    pressedIcon = R.drawable.micon_camera_on,
                    text = stringResource(id = R.string.camera_retake)
                )
                TopBarDivider()
            }
            TopBarButton(
                onClick = onCancel,
                icon = R.drawable.micon_cancle,
                pressedIcon = R.drawable.micon_cancle_on,
                text = stringResource(id = R.string.cancel)
            )
            if (showAuth) {
                TopBarDivider()
                TopBarButton(
                    onClick = onAuth,
                    enabled = cameraState is CameraState.CameraOcrResultState,
                    icon = R.drawable.micon_check,
                    pressedIcon = R.drawable.micon_check_on,
                    text = stringResource(id = R.string.camera_auth)
                )
            }
        }
    }
}

@Composable
fun NormalCameraTopBar(
    title: String,
    canComplete: Boolean = false,
    onCancel: () -> Unit,
    onComplete: () -> Unit
) {
    TopBar(titleText = title) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TopBarButton(
                onClick = onCancel,
                icon = R.drawable.micon_cancle,
                pressedIcon = R.drawable.micon_cancle_on,
                text = stringResource(id = R.string.cancel)
            )
            TopBarDivider()
            TopBarButton(
                onClick = onComplete,
                enabled = canComplete,
                icon = R.drawable.micon_check,
                pressedIcon = R.drawable.micon_check_on,
                text = stringResource(id = R.string.take_complete)
            )
        }
    }
}

@Composable
fun PreviewDocTopBar(
    title: String,
    showBack: Boolean,
    onBack: () -> Unit,
    showCompleteBtn: Boolean,
    onComplete: () -> Unit
) {
    TopBar(titleText = title,
        navigationContents = {
            if (showBack) {
                TopNavigationButton(
                    onClick = onBack, icon = R.drawable.micon_back,
                    pressedIcon = R.drawable.micon_back_on
                )
            }
        }) {
        if (showCompleteBtn) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                TopBarButton(
                    onClick = onComplete,
                    icon = R.drawable.micon_check,
                    pressedIcon = R.drawable.micon_check_on,
                    text = stringResource(id = R.string.complete)
                )
            }
        }
    }
}

@Composable
fun ElectronicDocTab(
    showApplication: Boolean = true,
    onShowApplication: () -> Unit,
    hasInstruction: Boolean = true,
    showInstruction: Boolean = false,
    onShowInstruction: () -> Unit,
    hasEvidenceDocument: Boolean = true,
    showEvidenceDocument: Boolean = false,
    onShowEvidenceDocument: () -> Unit
) {
    if (!hasInstruction && !hasEvidenceDocument) return

    Row(
        modifier = Modifier
            .padding(end = 24.dp)
            .background(
                color = MaterialTheme.colors.disableColor,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (hasInstruction)
            MainToggleButton(
                selected = showInstruction,
                onClick = { onShowInstruction() },
                hasBorder = true,
                icon = R.drawable.micon_book,
                selectedIcon = R.drawable.micon_book_on,
                text = stringResource(id = R.string.product_core_instruction)
            )

        MainToggleButton(
            selected = showApplication,
            onClick = { onShowApplication() },
            hasBorder = true,
            icon = R.drawable.micon_docu,
            selectedIcon = R.drawable.micon_docu_on,
            text = stringResource(id = R.string.application)
        )

        if (hasEvidenceDocument)
            MainToggleButton(
                selected = showEvidenceDocument,
                onClick = { onShowEvidenceDocument() },
                hasBorder = true,
                icon = R.drawable.micon_docus,
                selectedIcon = R.drawable.micon_docus_on,
                text = stringResource(id = R.string.documentary_evidence)
            )
    }
}

@Composable
fun PaperlessPreviewTab(
    electronicTabDataList: List<ElectronicTabData>,
    onTabClick: (ElectronicTabData) -> Unit,
    selectedTab: ElectronicTabData
) {
    if (electronicTabDataList.isEmpty()) return

//    val (selectedOption, onOptionSelected) = remember { mutableStateOf(selectedTab)}

    Row(
        modifier = Modifier
            .selectableGroup()
            .padding(top = 6.dp)
    ) {
        electronicTabDataList.forEach { electronicTabData ->
            ElectronicTabButton(
                selected = electronicTabData.title == selectedTab.title,
                onClick = {
//                    onOptionSelected(electronicTabData)
                    onTabClick(electronicTabData)
                },
                text = electronicTabData.title
            )
        }
    }
}

@Composable
fun PaperlessPreviewTopBar(
    title: String,
    electronicTabDataList: List<ElectronicTabData>,
    showThumbnail: Boolean,
    onClickThumbnail: (Boolean) -> Unit,
    pageInfo: String,
    onTabClick: (ElectronicTabData) -> Unit,
    selectedTab: ElectronicTabData?,
    onEdit: () -> Unit,
    onCancel: () -> Unit,
    enabledTransmit: Boolean = true,
    onTransmit: () -> Unit,
    onAlert: (AlertData) -> Unit,
    recordState: RecordState = RecordState.Recoding,
    tabletSndAuth: Boolean
) {

    val context = LocalContext.current

    var enabledTopBarButton by remember { mutableStateOf(true) }

    enabledTopBarButton = recordState != RecordState.Paused

    Column {
        TopBar(
            titleText = title,
            navigationContents = {
                Spacer(modifier = Modifier.width(12.dp))

                IconToggleButton(
                    onToggle = showThumbnail,
                    icon = R.drawable.micon_contents,
                    pressedIcon = R.drawable.micon_contents_on
                ) {
                    onClickThumbnail(!showThumbnail)
                }
            },
            endTitleContent = {
                Box(
                    modifier = Modifier
                        .padding(start = 26.dp)
                        .size(84.dp, 32.dp)
                        .background(MaterialTheme.colors.unfocusedColor)
                        .border(
                            BorderStroke(1.dp, MaterialTheme.colors.disableColor),
                            RoundedCornerShape(4.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = pageInfo, style = MaterialTheme.typography.body1)
                }
            }
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                TopBarButton(
                    onClick = {
                        onAlert(
                            AlertData(
                                contentText = context.getString(R.string.popup_content_cancel_transmit),
                                leftBtnText = context.getString(R.string.no),
                                rightBtnText = context.getString(R.string.yes),
                                onDismissRequest = { state ->
                                    if (state == Right) {
                                        onCancel()
                                    }
                                })
                        )
                    },
                    icon = R.drawable.micon_fail,
                    pressedIcon = R.drawable.micon_fail_on,
                    enabled = enabledTopBarButton,
                    text = stringResource(id = R.string.cancel_transmit)
                )

                TopBarDivider()

                TopBarButton(
                    onClick = {
                        onAlert(
                            AlertData(
                                contentText = context.getString(R.string.popup_content_transmit),
                                leftBtnText = context.getString(R.string.no),
                                rightBtnText = context.getString(R.string.yes),
                                onDismissRequest = { state ->
                                    if (state == Right) {
                                        onTransmit()
                                    }
                                })
                        )
                    },
                    icon = R.drawable.micon_send_on,
                    pressedIcon = R.drawable.micon_send_on,
                    enabled = enabledTransmit && enabledTopBarButton && tabletSndAuth,
                    text = stringResource(id = R.string.transmit),
                    backgroundColor = MaterialTheme.colors.point4Color
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .background(color = MaterialTheme.colors.mainColor)
                .padding(horizontal = 20.dp),
            contentAlignment = Alignment.BottomStart
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                selectedTab?.let {
                    PaperlessPreviewTab(
                        electronicTabDataList = electronicTabDataList,
                        onTabClick = onTabClick,
                        selectedTab = it
                    )
                }
            }
        }

        Divider(color = MaterialTheme.colors.divider1Color)
    }


}

@Composable
fun PaperlessInputTopBar(
    title: String,
    electronicTabDataList: List<ElectronicTabData>,
    onClickThumbnail: (Boolean) -> Unit,
    showThumbnail: Boolean,
    showHighlighter: Boolean,
    onClickHighlighter: (Boolean) -> Unit,
    pageInfo: String,
    onTabClick: (ElectronicTabData) -> Unit,
    selectedTab: ElectronicTabData?,
    onReleaseHighlighter: () -> Unit,
    onHighlighter: () -> Unit,
    onEraser: () -> Unit,
    onCancel: () -> Unit,
    enabledTransmit: Boolean = true,
    onFillOutComplete: () -> Unit = {},
    onAlert: (AlertData) -> Unit,
    recordState: RecordState = RecordState.Recoding
) {
    val context = LocalContext.current

    var enabledTopBarButton by remember { mutableStateOf(true) }

    enabledTopBarButton = recordState != RecordState.Paused

    Column {
        TopBar(
            titleText = title,
            navigationContents = {
                Spacer(modifier = Modifier.width(12.dp))

                IconToggleButton(
                    onToggle = showThumbnail,
                    icon = R.drawable.micon_contents,
                    pressedIcon = R.drawable.micon_contents_on
                ) {
                    onClickThumbnail(!showThumbnail)
                }
            },
            endTitleContent = {
                Box(
                    modifier = Modifier
                        .padding(start = 26.dp)
                        .size(84.dp, 32.dp)
                        .background(MaterialTheme.colors.unfocusedColor)
                        .border(
                            BorderStroke(1.dp, MaterialTheme.colors.disableColor),
                            RoundedCornerShape(4.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = pageInfo, style = MaterialTheme.typography.body1)
                }

            }
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Card(
                    backgroundColor = MaterialTheme.colors.background2Color,
                    shape = RoundedCornerShape(26.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(36.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            PopupToggleButton(
                                selected = showHighlighter,
//                                onClick = {
//                                    penPopupControl = !penPopupControl
//                                },
                                onClick = { onClickHighlighter(showHighlighter) },
                                icon = R.drawable.micon_pen,
                                selectedIcon = R.drawable.micon_pen_on,
                            )
                            if (showHighlighter) {
                                Box {
                                    Popup(
                                        alignment = Alignment.TopCenter,
                                    ) {
                                        PenBubble(
                                            onHighlighter = onHighlighter,
                                            onEraser = onEraser,
                                            onNone = onReleaseHighlighter
                                        )
                                    }
                                }
                            } else {
                                onReleaseHighlighter()
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.width(36.dp))
                TopBarDivider()
                TopBarButton(
                    onClick = {
                        onAlert(
                            AlertData(
                                contentText = context.getString(R.string.popup_content_cancel),
                                leftBtnText = context.getString(R.string.no),
                                rightBtnText = context.getString(R.string.yes),
                                onDismissRequest = { state ->
                                    if (state == Right) {
                                        onCancel()
                                    }
                                })
                        )
                    },
                    enabled = enabledTopBarButton,
                    icon = R.drawable.micon_cancle,
                    pressedIcon = R.drawable.micon_cancle_on,
                    text = stringResource(id = R.string.cancel_write)
                )
                TopBarDivider()
                TopBarButton(
                    onClick = {
                        onAlert(
                            AlertData(
                                contentText = context.getString(R.string.popup_content_complete),
                                leftBtnText = context.getString(R.string.no),
                                rightBtnText = context.getString(R.string.yes),
                                onDismissRequest = { state ->
                                    if (state == Right) {
                                        onFillOutComplete()
                                    }
                                })
                        )
                    },
                    enabled = enabledTransmit && enabledTopBarButton,
                    icon = R.drawable.micon_check_on,
                    pressedIcon = R.drawable.micon_check_on,
                    text = stringResource(id = R.string.complete_write),
                    backgroundColor = MaterialTheme.colors.point4Color
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .background(color = MaterialTheme.colors.mainColor)
                .padding(horizontal = 20.dp),
            contentAlignment = Alignment.BottomStart
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                selectedTab?.let {
                    PaperlessPreviewTab(
                        electronicTabDataList = electronicTabDataList,
                        onTabClick = onTabClick,
                        selectedTab = it
                    )
                }
            }
        }
        Divider(color = MaterialTheme.colors.divider1Color)
    }
}

@Composable
fun BasicTopBar(
    title: String,
    showBack: Boolean = true,
    onBack: () -> Unit = { },
) {
    TopBar(titleText = title,
        navigationContents = {
            if (showBack) {
                TopNavigationButton(
                    onClick = onBack, icon = R.drawable.micon_back,
                    pressedIcon = R.drawable.micon_back_on
                )
            }
        },
        topBarButtons = {})
}

@Composable
fun TopBar(
    titleText: String? = null,
    backgroundColor: Color = MaterialTheme.colors.background1Color,
    contentTextColor: Color = MaterialTheme.colors.sub1Color,
    endTitleContent: @Composable (() -> Unit)? = null,
    navigationContents: @Composable (() -> Unit)? = null,
    topBarButtons: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .background(color = backgroundColor, shape = RectangleShape),
        contentAlignment = Alignment.BottomStart
    ) {
        titleText?.let {
            Row(
                modifier = Modifier.align(Alignment.CenterStart),
                verticalAlignment = Alignment.CenterVertically
            ) {
                navigationContents?.invoke()

                Text(
                    text = titleText,
                    modifier = Modifier.padding(start = 12.dp),
                    color = contentTextColor,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Start,
                    maxLines = 1
                )

                endTitleContent?.invoke()
            }
        }

        Box(modifier = Modifier.align(Alignment.CenterEnd)) {
            topBarButtons()
        }

        Divider(color = MaterialTheme.colors.divider1Color)
    }
}

@Composable
fun TopBarDivider(dividerColor: Color = Divider1Color, height: Dp = 24.dp, width: Dp = 1.dp) {
    Divider(
        modifier = Modifier
            .height(height)
            .width(width),
        color = dividerColor
    )
}

@Preview
@Composable
fun PreviewMicBubble() {

    var state by remember { mutableStateOf<RecordState>(RecordState.None) }
    val list = remember {
        mutableListOf(
            RecordData("test1", "path", "test1.mp3", 1000L, 100L),
            RecordData("test2", "path", "test2.mp3", 1000L, 100L),
            RecordData("test3", "path", "test3.mp3", 1000L, 100L),
            RecordData("test4", "path", "test4.mp3", 1000L, 100L)
        )
    }

    IBKSTheme {
        MicBubble(
            recordState = state,
            recordTime = "01:00",
            recordFileList = list,
            onRecord = { state = RecordState.Recoding },
            onResume = { state = RecordState.Recoding },
            onPause = { state = RecordState.Paused },
            onStop = { state = RecordState.None },
            onRecordList = { }
        )
    }
}

@Composable
fun MicBubble(
    recordState: RecordState,
    recordTime: String,
    recordFileList: List<RecordData>,
    onRecord: () -> Unit,
    onResume: () -> Unit,
    onPause: () -> Unit,
    onStop: () -> Unit,
    onRecordList: () -> Unit,
    onClose: () -> Unit = {},
    isMainButton: Boolean = false
) {
    var listPopupControl by remember { mutableStateOf(false) }
    var selectedRecordData by remember { mutableStateOf<RecordData?>(null) }

    Column {
        Bubble(modifier = Modifier.defaultMinSize(minWidth = 368.dp), showSub = isMainButton) {
            Row(
                modifier = Modifier.padding(10.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                when (recordState) {
                    RecordState.None -> {
                        BubbleButton(
                            selected = false,
                            // 녹취 시작
                            onClick = {
                                onRecord()
                                listPopupControl = false
                                selectedRecordData = null
                            },
                            icon = R.drawable.micon_record,
                            selectedIcon = R.drawable.micon_record_on,
                            text = "녹취"
                        )
                    }
                    RecordState.Paused -> {
                        Box(
                            Modifier
                                .size(105.dp, 40.dp)
                                .background(color = Color.Transparent)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row {
                                    Box(
                                        modifier = Modifier
                                            .size(5.dp, 14.dp)
                                            .background(Color(0xFFDFDFDF))
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(5.dp, 14.dp)
                                            .background(Color(0xFFDFDFDF))
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = recordTime,
                                    style = TextStyle(
                                        color = MaterialTheme.colors.background1Color,
                                        fontSize = 14.sp
                                    )
                                )
                            }
                        }
                    }
                    RecordState.Recoding -> {
                        Box(
                            Modifier
                                .size(105.dp, 40.dp)
                                .background(color = Color.Transparent)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(14.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFEC6730))
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = recordTime,
                                    style = TextStyle(
                                        color = MaterialTheme.colors.background1Color,
                                        fontSize = 14.sp
                                    )
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                TopBarDivider(dividerColor = Divider2Color, height = 16.dp)
                Spacer(modifier = Modifier.width(12.dp))
                BubbleButton(
                    selected = false,
                    onClick = { if (recordState == RecordState.Paused) onResume() else onPause() },
                    enabled = recordState != RecordState.None,
                    icon = if (recordState == RecordState.Paused) R.drawable.micon_record else R.drawable.micon_pause_on,
                    selectedIcon = if (recordState == RecordState.Paused) R.drawable.micon_record_on else R.drawable.micon_pause_on
                )
                Spacer(modifier = Modifier.width(8.dp))
                BubbleButton(
                    selected = false,
                    // 녹취 종료
                    onClick = {
                        onStop()
                    },
                    enabled = false, // 23.02.23 기준 녹음 중 중지 불가.
                    icon = R.drawable.micon_stop_on,
                    selectedIcon = R.drawable.micon_stop_on
                )
                Spacer(modifier = Modifier.width(12.dp))
                TopBarDivider(dividerColor = Divider2Color, height = 16.dp)
                Spacer(modifier = Modifier.width(12.dp))
                BubbleButton(
                    selected = listPopupControl,
                    onClick = {
                        onRecordList()
                        listPopupControl = !listPopupControl
                        selectedRecordData = null
                    },
                    enabled = recordState == RecordState.None,
                    icon = R.drawable.micon_list_on,
                    selectedIcon = R.drawable.micon_list_on,
                    text = "목록"
                )
                if (isMainButton) {
                    Spacer(modifier = Modifier.width(12.dp))
                    TopBarDivider(dividerColor = Divider2Color, height = 16.dp)
                    Spacer(modifier = Modifier.width(12.dp))
                    BubbleButton(
                        selected = false,
                        // 플로팅버튼 닫기
                        onClick = {
                            onClose()
                        },
                        icon = R.drawable.micon_cancle_on,
                        selectedIcon = R.drawable.micon_cancle_on
                    )
                }
            }
        }
        AnimatedVisibility(listPopupControl) {
            Column(modifier = Modifier.width(368.dp)) {
                Spacer(modifier = Modifier.height(8.dp))
                Bubble(
                    showSub = true,
                    shape = RoundedCornerShape(16.dp),
                    backgroundColor = MaterialTheme.colors.background4Color.copy(alpha = 0.8f),
                    modifier = Modifier.requiredSizeIn(
                        minWidth = 368.dp,
                        maxWidth = 368.dp,
                        minHeight = 40.dp,
                        maxHeight = 160.dp
                    )
                ) {
                    if (recordFileList.isEmpty()) {
                        Text(
                            text = "녹취된 음성이 없습니다.", style = TextStyle(
                                color = MaterialTheme.colors.background1Color,
                                fontSize = 14.sp
                            )
                        )
                    } else {
                        LazyColumn {
                            items(items = recordFileList) { item ->
                                FileItem(file = item, onClick = {
                                    selectedRecordData = item
                                })
                                Divider(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(1.dp),
                                    color = Divider2Color
                                )
                            }
                        }
                    }
                }
            }
        }
        AnimatedVisibility(selectedRecordData != null) {
            selectedRecordData?.let {
                Column {
                    Spacer(modifier = Modifier.height(8.dp))
                    PlayerBubble(
                        recordData = it,
                        onClose = { selectedRecordData = null })
                }
            }
        }
    }
}

@Composable
fun FileItem(
    file: RecordData,
    selected: Boolean = false,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }

    val selectableModifier = Modifier.selectable(
        selected = selected,
        onClick = onClick,
        enabled = enabled,
        role = Role.RadioButton,
        interactionSource = interactionSource,
        indication = null
    )

    val pressed by interactionSource.collectIsPressedAsState()

    val color = if (selected or pressed) MaterialTheme.colors.point4Color else Color.Transparent

    val textStyle = TextStyle(
        color = MaterialTheme.colors.background1Color,
        fontSize = 14.sp
    )
    Box(
        Modifier
            .defaultMinSize(40.dp, 40.dp)
            .then(modifier)
            .background(color = color)
            .then(selectableModifier),
        propagateMinConstraints = true
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .padding(horizontal = 24.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = file.fileName, style = textStyle)
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = SimpleDateFormat(" mm : ss ").format(file.recordLength),
                style = textStyle
            )
            Spacer(modifier = Modifier.width(12.dp))
            TopBarDivider(dividerColor = Divider2Color, height = 12.dp)
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "${String.format("%.2f", file.fileSize.toDouble() / 1024 / 1024)}mb",
                style = textStyle
            )
        }
    }
}

@Composable
fun PlayerBubble(
    recordData: RecordData,
    onClose: () -> Unit = { }
) {
    Bubble(
        showSub = true,
        shape = RoundedCornerShape(16.dp),
        backgroundColor = MaterialTheme.colors.background4Color.copy(alpha = 0.8f),
        modifier = Modifier.size(368.dp, 116.dp)
    ) {
        var mediaPlayer by remember { mutableStateOf(MediaPlayer()) }
        var timerTask by remember { mutableStateOf(Timer()) }
        var currentPosition by remember { mutableStateOf(0) }

        DisposableEffect(recordData) {
            try {
                mediaPlayer = MediaPlayer().apply {
                    setDataSource(recordData.filePath)    //경로+파일명
                    prepare()
                    start()     //음악 재생
                    isLooping = false   //반복 재생x
                }

                timerTask = timer(period = 500) {
                    currentPosition = mediaPlayer.currentPosition
                }


            } catch (e: Exception) {
                e.localizedMessage?.let { QLog.e(it) }
            }

            onDispose {
                mediaPlayer.stop()
                timerTask.cancel()
            }
        }

        Column {
            Row(
                modifier = Modifier.size(368.dp, 60.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.micon_playing_on),
                    contentDescription = "",
                    contentScale = ContentScale.Inside
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = recordData.fileName,
                    style = TextStyle(color = MaterialTheme.colors.background1Color)
                )
            }
            Row(
                modifier = Modifier
                    .size(368.dp, 56.dp)
                    .background(MaterialTheme.colors.background4Color),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Spacer(modifier = Modifier.weight(1f))
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF00A18C))
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${SimpleDateFormat(" mm : ss ").format(currentPosition)} / ${
                        SimpleDateFormat(
                            " mm : ss "
                        ).format(recordData.recordLength)
                    }",
                    style = TextStyle(
                        color = MaterialTheme.colors.background1Color,
                        fontSize = 14.sp
                    )
                )
                Spacer(modifier = Modifier.weight(1f))
                TopBarDivider(dividerColor = Divider2Color, height = 16.dp)
                Spacer(modifier = Modifier.width(12.dp))
                BubbleButton(
                    selected = false,
                    onClick = {
                        if (mediaPlayer.isPlaying) {
                            mediaPlayer.pause()
                        } else {
                            mediaPlayer.start()
                        }
                    },
                    icon = if (mediaPlayer.isPlaying) R.drawable.micon_pause_on else R.drawable.micon_play,
                    selectedIcon = if (mediaPlayer.isPlaying) R.drawable.micon_pause_on else R.drawable.micon_play,
                )
                Spacer(modifier = Modifier.width(8.dp))
                BubbleButton(
                    selected = false,
                    onClick = {
                        mediaPlayer.pause()
                        mediaPlayer.seekTo(0)
                    },
                    icon = R.drawable.micon_stop_on,
                    selectedIcon = R.drawable.micon_stop_on
                )
                Spacer(modifier = Modifier.width(8.dp))
                BubbleButton(
                    selected = false,
                    onClick = { onClose() },
                    icon = R.drawable.micon_cancle_on,
                    selectedIcon = R.drawable.micon_cancle_on
                )
                Spacer(modifier = Modifier.width(12.dp))
            }
        }
    }
}

@Preview
@Composable
fun PreviewPenBubble() {
    PenBubble(
        onHighlighter = { },
        onEraser = { },
        onNone = { }
    )
}

@Composable
fun PenBubble(
    onHighlighter: () -> Unit,
    onEraser: () -> Unit,
    onNone: () -> Unit
) {
    var highlighterMode by remember { mutableStateOf(false) }
    var eraserMode by remember { mutableStateOf(false) }

    Bubble {
        Row(
            modifier = Modifier.padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BubbleButton(
                selected = highlighterMode,
                onClick = {
                    if (highlighterMode) {
                        highlighterMode = false
                        eraserMode = false
                        onNone()
                    } else {
                        highlighterMode = true
                        eraserMode = false
                        onHighlighter()
                    }
                },
                icon = R.drawable.micon_mark_on,
                selectedIcon = R.drawable.micon_mark_on,
                text = stringResource(id = R.string.highlighter)
            )

            TopBarDivider(dividerColor = Divider2Color, height = 16.dp)

            BubbleButton(
                selected = eraserMode,
                onClick = {
                    if (eraserMode) {
                        eraserMode = false
                        highlighterMode = false
                        onNone()
                    } else {
                        eraserMode = true
                        highlighterMode = false
                        onEraser()
                    }
                },
                icon = R.drawable.micon_eraser_on,
                selectedIcon = R.drawable.micon_eraser_on,
                text = stringResource(id = R.string.eraser)
            )
        }
    }
}

@Preview
@Composable
fun AllsignBubble() {
    Bubble {
        Row(
            modifier = Modifier.padding(10.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BubbleButton(
                selected = false,
                onClick = { /*TODO*/ },
                icon = R.drawable.micon_allsign_on,
                selectedIcon = R.drawable.micon_allsign_on,
                text = "일괄서명(고객)"
            )
            Spacer(modifier = Modifier.width(12.dp))
            TopBarDivider(dividerColor = Divider2Color, height = 16.dp)
            Spacer(modifier = Modifier.width(12.dp))
            BubbleButton(
                selected = true,
                onClick = { /*TODO*/ },
                icon = R.drawable.micon_allsign_on,
                selectedIcon = R.drawable.micon_allsign_on,
                text = "일괄서명(직원)"
            )
        }
    }
}

@Composable
fun Bubble(
    showSub: Boolean = false,
    modifier: Modifier = Modifier,
    triangleAlign: Alignment.Horizontal = Alignment.CenterHorizontally,
    triangleOffsetX: Dp = 0.dp,
    shape: Shape = RoundedCornerShape(30.dp),
    backgroundColor: Color = MaterialTheme.colors.background4Color,
    content: @Composable (() -> Unit)? = null,
) {
    Column(
        modifier = Modifier
            .defaultMinSize(80.dp, 60.dp)
            .then(modifier)
    ) {
        if (!showSub) {
            Canvas(
                modifier = Modifier
                    .align(triangleAlign)
                    .size(12.dp, 8.dp)
                    .offset(x = triangleOffsetX)
            ) {
                drawPath(
                    path = Path().apply {
                        moveTo(size.width * 0.5f, size.height * 0f)
                        lineTo(size.width * 0f, size.height * 1f)
                        lineTo(size.width * 1f, size.height * 1f)
                        close()
                    },
                    color = backgroundColor,
                )
            }
        }
        Row(
            modifier = Modifier
                .defaultMinSize(80.dp, 60.dp)
                .clip(shape)
                .background(backgroundColor)
                .then(modifier),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            content?.invoke()
        }
    }
}


@Composable
fun PopupToggleButton(
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    enabled: Boolean = true,
    @DrawableRes icon: Int,
    @DrawableRes selectedIcon: Int,
) {
    val interactionSource = remember { MutableInteractionSource() }

    val selectableModifier = Modifier.selectable(
        selected = selected,
        onClick = onClick,
        enabled = enabled,
        role = Role.RadioButton,
        interactionSource = interactionSource,
        indication = null
    )

    val pressed by interactionSource.collectIsPressedAsState()

    val color = if (selected or pressed) MaterialTheme.colors.point1Color else Color.Transparent
    val shape = CircleShape
    val imageId = if (selected or pressed) selectedIcon else icon

    Box(
        Modifier
            .defaultMinSize(40.dp, 40.dp)
            .then(modifier)
            .background(
                color = color,
                shape = shape
            )
            .clip(shape)
            .then(selectableModifier),
        propagateMinConstraints = true
    ) {
        Image(
            painter = painterResource(id = imageId),
            contentDescription = "",
            contentScale = ContentScale.Inside,
            alpha = if (enabled) DefaultAlpha else ContentAlpha.disabled
        )
    }
}

@Composable
fun BubbleButton(
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    enabled: Boolean = true,
    @DrawableRes icon: Int,
    @DrawableRes selectedIcon: Int,
    text: String? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }

    val selectableModifier = Modifier.selectable(
        selected = selected,
        onClick = onClick,
        enabled = enabled,
        role = Role.RadioButton,
        interactionSource = interactionSource,
        indication = null
    )
    val pressed by interactionSource.collectIsPressedAsState()

    val color = if (selected or pressed) MaterialTheme.colors.point4Color else Color.Transparent

    val shape = RoundedCornerShape(24.dp)

    val imageId = if (selected or pressed) selectedIcon else icon

    val textStyle = TextStyle(
        color = if (enabled) {
            MaterialTheme.colors.background1Color
        } else MaterialTheme.colors.disableColor,
        fontSize = 16.sp
    )

    Box(
        Modifier
            .defaultMinSize(if (text == null) 40.dp else 105.dp, 40.dp)
            .then(modifier)
            .background(
                color = color,
                shape = shape
            )
            .clip(shape)
            .then(selectableModifier),
        propagateMinConstraints = true
    ) {
        Row(
            modifier = if (text != null) {
                Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
            } else {
                Modifier.padding(8.dp)
            },
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = imageId),
                contentDescription = "",
                contentScale = ContentScale.FillWidth,
                alpha = if (enabled) DefaultAlpha else ContentAlpha.disabled
            )

            if (text != null) {
                Text(modifier = Modifier.padding(start = 12.dp), text = text, style = textStyle)
            }
        }
    }
}

@Composable
fun NewThumbnailButton(
    onClickThumbnail: (Boolean) -> Unit,
    show: Boolean
) {
//    var showThumbnail by remember { mutableStateOf(show) }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        TopNavigationButton(
            onClick = {
                onClickThumbnail(!show)
            },
            icon = R.drawable.micon_contents,
            pressedIcon = R.drawable.micon_contents_on
        )
    }
}

@Preview(widthDp = 600)
@Composable
fun PreviewThumbnailBubble() {
    val image = com.inzisoft.ibks.data.internal.Image("", 210, 297)

    IBKSTheme {
        Row() {
            LeftMenuBubble(
                thumbnailList = listOf(
                    Thumbnail(
                        "서식1",
                        image,
                        isFirst = true,
                        isShowDivider = false,
                        isComplete = true
                    ),
                    Thumbnail(
                        "서식1",
                        image,
                        isFirst = false,
                        isShowDivider = true,
                        isComplete = false
                    ),
                    Thumbnail("서식2", image, isFirst = true, isShowDivider = false),
                    Thumbnail("서식2", image, isFirst = false, isShowDivider = true),
                    Thumbnail("서식3", image, isFirst = true, isShowDivider = false),
                    Thumbnail("서식3", image, isFirst = false, isShowDivider = true),
                    Thumbnail("서식4", image, isFirst = true, isShowDivider = false),
                    Thumbnail("서식4", image, isFirst = false, isShowDivider = true),
                    Thumbnail("서식5", image, isFirst = true, isShowDivider = false),
                    Thumbnail("서식5", image, isFirst = false, isShowDivider = false),
                    Thumbnail("서식5", image, isFirst = false, isShowDivider = true),
                ),
                currentPage = 1
            ) {

            }

            LeftMenuBubble(
                thumbnailList = listOf(
                    Thumbnail(
                        "위탁종합_주식매매핵심설명서",
                        image,
                        isFirst = true,
                        isShowDivider = false,
                        isComplete = true
                    ),
                    Thumbnail(
                        "연금저축핵심설명서 및 설명서(23.01.01. 소득세 개정으로 인한 정정)",
                        image,
                        isFirst = false,
                        isShowDivider = true,
                        isComplete = false
                    ),
                    Thumbnail("CMA 핵심 설명서", image, isFirst = true, isShowDivider = false),
                    Thumbnail("CMA 상품설명서(20210325)", image, isFirst = true, isShowDivider = true),
                    Thumbnail(
                        "CMA 서비스약관(개정 20160404)",
                        image,
                        isFirst = true,
                        isShowDivider = false
                    ),
                    Thumbnail(
                        "연금저축가입 시 유의사항(23.01.01. 소득세법 개정사항 반영)",
                        image,
                        isFirst = true,
                        isShowDivider = true
                    ),
                    Thumbnail("서식4", image, isFirst = true, isShowDivider = false),
                    Thumbnail("서식4", image, isFirst = false, isShowDivider = true),
                    Thumbnail("서식5", image, isFirst = true, isShowDivider = false),
                    Thumbnail("서식5", image, isFirst = false, isShowDivider = false),
                    Thumbnail("서식5", image, isFirst = false, isShowDivider = true),
                ),
                currentPage = 1
            ) {

            }
        }
    }
}

@Composable
fun LeftMenuBubble(
    thumbnailList: List<Thumbnail>,
    currentPage: Int,
    onClickThumbnail: (index: Int) -> Unit
) {
    val columnState = rememberLazyListState()
    var leftMenu by remember { mutableStateOf(LeftMenu.THUMBNAIL) }

    LaunchedEffect(Unit) {
        if (currentPage > 0) {
            columnState.scrollToItem(currentPage - 1)
        }
    }

    Card(
        modifier = Modifier.fillMaxHeight(),
        shape = RectangleShape,
        backgroundColor = MaterialTheme.colors.background1Color,
        border = BorderStroke(1.dp, Color.LightGray),
        elevation = 2.dp
    ) {

        Column() {

            Row(
                modifier = Modifier
                    .height(60.dp)
                    .padding(horizontal = 10.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                IconToggleButton(
                    onToggle = leftMenu == LeftMenu.THUMBNAIL,
                    icon = R.drawable.micon_thumbnail,
                    pressedIcon = R.drawable.micon_thumbnail_on
                ) { leftMenu = LeftMenu.THUMBNAIL }

                IconToggleButton(
                    onToggle = leftMenu == LeftMenu.OUTLINE,
                    icon = R.drawable.micon_list,
                    pressedIcon = R.drawable.micon_list_on
                ) { leftMenu = LeftMenu.OUTLINE }

            }

            when (leftMenu) {
                LeftMenu.THUMBNAIL -> Thumbnail(
                    thumbnailList = thumbnailList,
                    currentPage = currentPage,
                    columnState = columnState,
                    onClickThumbnail = onClickThumbnail
                )
                LeftMenu.OUTLINE -> Outline(
                    thumbnailList = thumbnailList,
                    currentPage = currentPage,
                    columnState = columnState,
                    onClickThumbnail = onClickThumbnail
                )
            }
        }

    }


}

@Composable
fun Thumbnail(
    thumbnailList: List<Thumbnail>,
    currentPage: Int,
    columnState: LazyListState,
    onClickThumbnail: (index: Int) -> Unit
) {

    LazyColumn(
        modifier = Modifier
            .width(240.dp)
            .fillMaxHeight()
            .background(color = MaterialTheme.colors.unfocusedColor),
        state = columnState,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        itemsIndexed(thumbnailList) { index, thumbnail ->

            val filter =
                if (currentPage == index + 1)
                    null
                else
                    ColorFilter.tint(Color(0x33000000), BlendMode.Multiply)

            val border =
                if (currentPage == index + 1)
                    BorderStroke(width = 2.dp, color = MaterialTheme.colors.point8Color)
                else
                    BorderStroke(width = 1.dp, color = MaterialTheme.colors.disableColor)

            val textColor =
                if (currentPage == index + 1)
                    MaterialTheme.colors.sub1Color
                else
                    Color.White


            if (index == 0) {
                Spacer(modifier = Modifier.height(36.dp))
            }

            if (thumbnail.isFirst) {
                Text(
                    text = thumbnail.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(bottom = 8.dp),
                    color = MaterialTheme.colors.sub1Color,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 2,
                    style = MaterialTheme.typography.h6
                )
            }

            Box(
                modifier = Modifier
                    .padding(horizontal = 50.dp)
                    .fillParentMaxWidth()
                    .aspectRatio(0.7f)
                    .border(border)
                    .clickable { onClickThumbnail(index) },
                contentAlignment = Alignment.Center
            ) {
                GlideImage(
                    imageModel = File(thumbnail.image.path),
                    modifier = Modifier.fillMaxSize(),
                    contentScale = if (thumbnail.image.width < thumbnail.image.height) ContentScale.FillHeight else ContentScale.FillWidth,
                    colorFilter = filter,
                    previewPlaceholder = R.drawable.loading
                )

                Text(
                    text = "${index + 1}",
                    color = textColor,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.h3
                )

                thumbnail.isComplete?.let {
                    GlideImage(
                        imageModel = if (it) R.drawable.micon_agree_on else R.drawable.micon_agree,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp)
                            .size(24.dp),
                        contentScale = ContentScale.Fit,
                        previewPlaceholder = if (it) R.drawable.micon_agree_on else R.drawable.micon_agree
                    )
                }
            }

            if (thumbnail.isShowDivider) {
                Divider(
                    modifier = Modifier.padding(start = 36.dp, top = 10.dp, end = 36.dp),
                    color = MaterialTheme.colors.divider1Color,
                    thickness = 2.dp
                )
            }

            if (index == thumbnailList.lastIndex) {
                Spacer(modifier = Modifier.height(36.dp))
            }
        }
    }

}

@Composable
fun Outline(
    thumbnailList: List<Thumbnail>,
    currentPage: Int,
    columnState: LazyListState,
    onClickThumbnail: (index: Int) -> Unit
) {
    val outLineList = mutableListOf<OutLine>()
    var currentIndex by remember { mutableStateOf(0) }

    thumbnailList.forEachIndexed { index, thumbnail ->
        if (thumbnail.isFirst) {
            outLineList.add(OutLine(thumbnail.title, index))
        }

        if (currentPage == index + 1) {
            currentIndex = outLineList.lastIndex
        }
    }

    LazyColumn(
        modifier = Modifier
            .width(240.dp)
            .fillMaxHeight()
            .background(color = MaterialTheme.colors.unfocusedColor),
        state = columnState
    ) {

        itemsIndexed(outLineList) { index, outLine ->

            val backgroundColor =
                if (currentIndex == index)
                    Color(0xFFDFDFDF)
                else
                    Color.Transparent


            Box(modifier = Modifier
                .height(50.dp)
                .fillMaxWidth()
                .background(color = backgroundColor)
                .clickable { onClickThumbnail(outLine.pageIndex) }
                .padding(horizontal = 12.dp),
                contentAlignment = Alignment.CenterStart
            ) {

                Text(
                    text = outLine.title,
                    color = Color(0xFF111111),
                    fontWeight = FontWeight.Medium,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                    style = MaterialTheme.typography.h6
                )

            }
        }
    }
}

enum class LeftMenu {
    THUMBNAIL,
    OUTLINE
}