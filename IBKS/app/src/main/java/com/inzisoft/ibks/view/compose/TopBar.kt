package com.inzisoft.ibks.view.compose

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.inzisoft.ibks.R
import com.inzisoft.ibks.base.Left
import com.inzisoft.ibks.base.Right
import com.inzisoft.ibks.data.internal.AlertData
import com.inzisoft.ibks.view.compose.theme.*
import com.inzisoft.ibks.viewmodel.CameraState


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
                    onAlarm = { },
                    onSetting = { }
                )

                ProcessTopBar(
                    title = "ProcessBar",
                    showBack = true,
                    showPreview = true,
                    showDelete = true,
                    onCancel = {})

                TempProcessTopBar(
                    title = "TempProcessBar",
                    showBack = true,
                    showPreview = true,
                    showDelete = true,
                    onCancel = {},
                    onShowTransmissive = true,
                )

                BasicTopBar(title = "BasicBar")

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
    onAlarm: () -> Unit,
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
fun OcrCameraTopBar(
    title: String,
    cameraState: CameraState = CameraState.CameraPreviewState,
    onRetake: () -> Unit,
    onCancel: () -> Unit,
    onAuth: () -> Unit,
) {
    TopBar(titleText = title) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TopBarButton(
                onClick = onRetake,
                enabled = cameraState is CameraState.CameraOcrResultState,
                icon = R.drawable.micon_camera,
                pressedIcon = R.drawable.micon_camera_on,
                text = stringResource(id = R.string.camera_retake)
            )
            TopBarDivider()
            TopBarButton(
                onClick = onCancel,
                icon = R.drawable.micon_cancle,
                pressedIcon = R.drawable.micon_cancle_on,
                text = stringResource(id = R.string.cancel)
            )
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

@Composable
fun NormalCameraTopBar(
    title: String,
    onCancel: () -> Unit
) {
    TopBar(titleText = title) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TopBarButton(
                onClick = onCancel,
                icon = R.drawable.micon_cancle,
                pressedIcon = R.drawable.micon_cancle_on,
                text = stringResource(id = R.string.cancel)
            )
        }
    }
}

@Composable
fun ProcessTopBar(
    title: String,
    showBack: Boolean = true,
    onBack: () -> Unit = { },
    showPreview: Boolean = false,
    toggleApplication: Boolean = true,
    onShowApplication: (Boolean) -> Unit = { },
    onEdit: () -> Unit = {},
    showDelete: Boolean = true,
    deletable: Boolean = true,
    onDelete: () -> Unit = {},
    onCancel: () -> Unit,
    savable: Boolean = true,
    onSaveTemp: () -> Unit = {},
    onShowTransmissive: Boolean = false,
    onTransmit: () -> Unit = {},
) {

    val context = LocalContext.current
    val alertMessage = remember { mutableStateOf<AlertData?>(null) }

    val msg = alertMessage.value
    if (msg != null) {

        AlertDialog(
            contentText = msg.contentText,
            leftBtnText = msg.leftBtnText,
            onLeftBtnClick = {
                msg.onDismissRequest(Left)
                alertMessage.value = null
            },
            rightBtnText = msg.rightBtnText,
            onRightBtnClick = {
                msg.onDismissRequest(Right)
                alertMessage.value = null
            }
        )
    }

    TopBar(titleText = title,
        navigationContents = {
            if (showBack) {
                TopNavigationButton(
                    onClick = onBack, icon = R.drawable.micon_back,
                    pressedIcon = R.drawable.micon_back_on
                )
            }
        }) {
        Row(verticalAlignment = Alignment.CenterVertically) {

            if (showPreview) {
                Row(
                    modifier = Modifier.padding(end = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MainToggleButton(
                        selected = toggleApplication,
                        onClick = { onShowApplication(true) },
                        hasBorder = true,
                        icon = R.drawable.micon_docu,
                        selectedIcon = R.drawable.micon_docu_on,
                        text = stringResource(id = R.string.application)
                    )

                    MainToggleButton(
                        selected = !toggleApplication,
                        onClick = { onShowApplication(false) },
                        hasBorder = true,
                        icon = R.drawable.micon_docus,
                        selectedIcon = R.drawable.micon_docus_on,
                        text = stringResource(id = R.string.documentary_evidence)
                    )
                }

                TopBarDivider()


                TopBarButton(
                    onClick = {
                        alertMessage.value = AlertData(
                            contentText = context.getString(R.string.popup_content_edit),
                            leftBtnText = context.getString(R.string.no),
                            rightBtnText = context.getString(R.string.yes),
                            onDismissRequest = { state ->
                                if (state == Right) {
                                    onEdit()
                                }
                            })
                    },
                    icon = R.drawable.micon_write,
                    pressedIcon = R.drawable.micon_write_on,
                    text = stringResource(id = R.string.edit)
                )
            }

            if (showDelete) {
                TopBarDivider()
                TopBarButton(
                    onClick = {
                        alertMessage.value = AlertData(
                            contentText = context.getString(R.string.popup_content_delete),
                            leftBtnText = context.getString(R.string.no),
                            rightBtnText = context.getString(R.string.yes),
                            onDismissRequest = { state ->
                                if (state == Right) {
                                    onDelete()
                                }
                            })
                    },
                    enabled = deletable,
                    icon = R.drawable.micon_trash,
                    pressedIcon = R.drawable.micon_trash_on,
                    text = stringResource(id = R.string.delete)
                )
            }

            TopBarDivider()
            TopBarButton(
                onClick = {
                    alertMessage.value = AlertData(
                        contentText = context.getString(R.string.popup_content_cancel),
                        leftBtnText = context.getString(R.string.no),
                        rightBtnText = context.getString(R.string.yes),
                        onDismissRequest = { state ->
                            if (state == Right) {
                                onCancel()
                            }
                        })
                },
                icon = R.drawable.micon_cancle,
                pressedIcon = R.drawable.micon_cancle_on,
                text = stringResource(id = R.string.cancel_write)
            )

            TopBarDivider()
            TopBarButton(
                onClick = {
                    alertMessage.value = AlertData(
                        contentText = context.getString(R.string.popup_content_save),
                        leftBtnText = context.getString(R.string.no),
                        rightBtnText = context.getString(R.string.yes),
                        onDismissRequest = { state ->
                            if (state == Right) {
                                onSaveTemp()
                            }
                        })
                },
                enabled = savable,
                icon = R.drawable.micon_save,
                pressedIcon = R.drawable.micon_save_on,
                text = stringResource(id = R.string.save_temp)
            )

            if (onShowTransmissive) {
                TopBarDivider()
                TopBarButton(
                    onClick = {
                        alertMessage.value = AlertData(
                            contentText = context.getString(R.string.popup_content_transmit),
                            leftBtnText = context.getString(R.string.no),
                            rightBtnText = context.getString(R.string.yes),
                            onDismissRequest = { state ->
                                if (state == Right) {
                                    onTransmit()
                                }
                            })
                    },
                    icon = R.drawable.micon_send,
                    pressedIcon = R.drawable.micon_send_on,
                    text = stringResource(id = R.string.submit)
                )
            }
        }
    }

}

@Composable
fun TempProcessTopBar(
    title: String,
    showBack: Boolean = true,
    onBack: () -> Unit = { },
    showPreview: Boolean = false,
    toggleApplication: Boolean = true,
    onShowApplication: (Boolean) -> Unit = { },
    onEdit: () -> Unit = {},
    showDelete: Boolean = true,
    deletable: Boolean = true,
    onDelete: () -> Unit = {},
    onCancel: () -> Unit,
    savable: Boolean = true,
    onSaveTemp: () -> Unit = {},
    onShowTransmissive: Boolean = false,
    onTransmit: () -> Unit = {},
    onMic: () -> Unit = {},
    onPen: () -> Unit = {},
    onAllsign: () -> Unit = {},
) {

    val context = LocalContext.current
    val alertMessage = remember { mutableStateOf<AlertData?>(null) }
    var micPopupControl = remember { mutableStateOf (false) }

    val msg = alertMessage.value
    if (msg != null) {
        AlertDialog(
            contentText = msg.contentText,
            leftBtnText = msg.leftBtnText,
            onLeftBtnClick = {
                msg.onDismissRequest(Left)
                alertMessage.value = null
            },
            rightBtnText = msg.rightBtnText,
            onRightBtnClick = {
                msg.onDismissRequest(Right)
                alertMessage.value = null
            }
        )
    }

    TopBar(titleText = title,
        navigationContents = {
            if (showBack) {
                TopNavigationButton(
                    onClick = onBack, icon = R.drawable.micon_thumbnail,
                    pressedIcon = R.drawable.micon_thumbnail_on
                )
            }
        }) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Card(
                modifier = Modifier.size(200.dp, 48.dp),
                backgroundColor = MaterialTheme.colors.background2Color,
                shape = RoundedCornerShape(26.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    IconButton(
                        onClick = { micPopupControl.value = true }, icon = R.drawable.micon_mic_w,
                        pressedIcon = R.drawable.micon_mic,
                        shape = CircleShape,
                        modifier = Modifier.size(40.dp, 40.dp),
                        backgroundColor = MaterialTheme.colors.point1Color,
                    )
                    if(micPopupControl.value) {
                        Popup() {
                            Card(backgroundColor = Color.LightGray, modifier = Modifier.size(100.dp, 100.dp)) {
                                Text(text = "테스트 팝업창")
                            }
                        }
                    }
                    Spacer(modifier = Modifier.width(36.dp))
                    IconButton(
                        onClick = onPen, icon = R.drawable.micon_pen,
                        pressedIcon = R.drawable.micon_pen_on,
                        shape = CircleShape,
                        modifier = Modifier.size(40.dp, 40.dp),
                        backgroundColor = MaterialTheme.colors.background2Color,
                    )
                    Spacer(modifier = Modifier.width(36.dp))
                    IconButton(
                        onClick = onAllsign, icon = R.drawable.micon_allsign,
                        pressedIcon = R.drawable.micon_allsign_on,
                        shape = CircleShape,
                        modifier = Modifier.size(40.dp, 40.dp),
                        backgroundColor = MaterialTheme.colors.background2Color,
                    )
                }
            }
            Spacer(modifier = Modifier.width(36.dp))
            TopBarDivider()
            TopBarButton(
                onClick = {
                    alertMessage.value = AlertData(
                        contentText = context.getString(R.string.popup_content_cancel),
                        leftBtnText = context.getString(R.string.no),
                        rightBtnText = context.getString(R.string.yes),
                        onDismissRequest = { state ->
                            if (state == Right) {
                                onCancel()
                            }
                        })
                },
                icon = R.drawable.micon_cancle,
                pressedIcon = R.drawable.micon_cancle_on,
                text = stringResource(id = R.string.cancel_write)
            )
            TopBarDivider()

            if (showDelete) {
                TopBarDivider()
                TopBarButton(
                    onClick = {
                        alertMessage.value = AlertData(
                            contentText = context.getString(R.string.popup_content_delete),
                            leftBtnText = context.getString(R.string.no),
                            rightBtnText = context.getString(R.string.yes),
                            onDismissRequest = { state ->
                                if (state == Right) {
                                    onDelete()
                                }
                            })
                    },
                    enabled = deletable,
                    icon = R.drawable.micon_trash,
                    pressedIcon = R.drawable.micon_trash_on,
                    text = stringResource(id = R.string.delete)
                )
            }

            TopBarDivider()
            TopBarButton(
                onClick = {
                    alertMessage.value = AlertData(
                        contentText = context.getString(R.string.popup_content_save),
                        leftBtnText = context.getString(R.string.no),
                        rightBtnText = context.getString(R.string.yes),
                        onDismissRequest = { state ->
                            if (state == Right) {
                                onSaveTemp()
                            }
                        })
                },
                enabled = savable,
                icon = R.drawable.micon_save,
                pressedIcon = R.drawable.micon_save_on,
                text = stringResource(id = R.string.save_temp)
            )

            if (onShowTransmissive) {
                TopBarDivider()
                TopBarButton(
                    onClick = {
                        alertMessage.value = AlertData(
                            contentText = context.getString(R.string.popup_content_complete),
                            leftBtnText = context.getString(R.string.no),
                            rightBtnText = context.getString(R.string.yes),
                            onDismissRequest = { state ->
                                if (state == Right) {
                                    onTransmit()
                                }
                            })
                    },
                    icon = R.drawable.micon_check,
                    pressedIcon = R.drawable.micon_check_on,
                    text = stringResource(id = R.string.complete_write)
                )
            }
        }
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
    titleText: String,
    backgroundColor: Color = MaterialTheme.colors.background1Color,
    contentTextColor: Color = MaterialTheme.colors.sub1Color,
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
        Row {
            TopBarContent(
                titleText = titleText,
                contentTextColor = contentTextColor,
                navigationContents = navigationContents,
                topBarButtons = topBarButtons
            )
        }

        Divider(color = DividerColor)
    }
}

@Composable
fun TopBarContent(
    titleText: String,
    contentTextColor: Color,
    navigationContents: @Composable (() -> Unit)? = null,
    topBarButtons: @Composable () -> Unit
) {
    ConstraintLayout(
        Modifier
            .fillMaxWidth()
            .height(60.dp)
    ) {
        val (navi, title, buttons) = createRefs()

        Column(modifier = Modifier.constrainAs(navi) {
            start.linkTo(parent.start)
            width = Dimension.wrapContent
            height = Dimension.matchParent
        }) {
            navigationContents?.invoke()
        }

        Column(
            modifier = Modifier.constrainAs(title) {
                start.linkTo(navi.end)
                end.linkTo(buttons.start)
                width = Dimension.fillToConstraints
                height = Dimension.matchParent
            },
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = titleText,
                color = contentTextColor,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Start,
                maxLines = 1
            )
        }

        Column(modifier = Modifier.constrainAs(buttons) {
            end.linkTo(parent.end)
            width = Dimension.wrapContent
            height = Dimension.matchParent
        }) {
            topBarButtons()
        }
    }
}

@Composable
fun TopBarDivider(dividerColor: Color = DividerColor) {
    Divider(
        modifier = Modifier
            .height(24.dp)
            .width(1.dp),
        color = dividerColor
    )
}