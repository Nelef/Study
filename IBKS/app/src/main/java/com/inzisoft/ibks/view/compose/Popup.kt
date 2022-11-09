package com.inzisoft.ibks.view.compose

import android.os.Message
import android.util.Log
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.inzisoft.ibks.R
import com.inzisoft.ibks.view.compose.theme.*

@Preview(device = Devices.AUTOMOTIVE_1024p)
@Composable
fun PreviewPopupScreen() {
    IBKSTheme {
//        val openDialog = remember { mutableStateOf(true) }
//        val openAlert = remember { mutableStateOf(false) }
//        BasicDialog(
//            openDialog = openDialog.value,
//            titleText = "Title",
//            contentText = "팝업 세부 내용을 입력하세요. 팝업 세부 내용을 입력하세요. 팝업 세부 내용을 입력하세요. 팝업 세부 내용을 입력하세요. 팝업 세부 내용을 입력하세요.",
//            leftBtnText = "취소",
//            rightBtnText = "확인",
//            onRightBtnClick = {
//                openDialog.value = false
//            }
//        )
//
//        AlertDialog(
//            openDialog = openAlert.value,
//            onDismissRequest = {},
//            contentText = "팝업 세부 내용을 입력하세요. 팝업 세부 내용을 입력하세요. 팝업 세부 내용을 입력하세요. 팝업 세부 내용을 입력하세요. 팝업 세부 내용을 입력하세요.",
//            onRightBtnClick = {
//                openAlert.value = false
//            }
//        )

        Column {
            BasicDialogContent(
                titleText = "알림",
                contentText = buildAnnotatedString { append("팝업 세부 내용을 입력하세요. 팝업 세부 내용을 입력하세요. 팝업 세부 내용을 입력하세요. 팝업 세부 내용을 입력하세요. 팝업 세부 내용을 입력하세요.") },
                rightBtnText = "확인",
                onRightBtnClick = {},
                buttons = 1,
            )

            AlertDialogContent(
                content = buildAnnotatedString { append("팝업 세부 내용을 입력하세요. 팝업 세부 내용을 입력하세요. 팝업 세부 내용을 입력하세요.") },
                leftBtnText = "취소",
                onRightBtnClick = {}
            )

            Toast(text = "Toast message Toast message Toast message")
        }
    }

}

@Preview(device = Devices.AUTOMOTIVE_1024p, showBackground = true, backgroundColor = 0xFFFFFF)
@Composable
fun PreviewBasicBottomDialog() {
    IBKSTheme {
        BasicBottomDialog(
            "Title",
            modifier = Modifier
                .size(500.dp, 400.dp),
            onClose = {},
            visible = true
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                Text(text = "Contents", modifier = Modifier.weight(1f))
                Row(
                    Modifier.height(60.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    GrayDialogButton(
                        onClick = { },
                        text = "취소",
                        modifier = Modifier.weight(1f)
                    )
                    ColorDialogButton(
                        onClick = { },
                        text = "확인",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun BasicBottomDialog(
    title: String,
    modifier: Modifier = Modifier,
    onClose: () -> Unit,
    shape: Shape = RoundedCornerShape(0.dp),
    visible: Boolean,
    content: @Composable () -> Unit
) {
    BottomPopupScope.Transition(visible = visible) {
        Column(
            modifier = Modifier
                .shadow(elevation = 2.dp)
                .animateEnterExit(
                    enter = slideInVertically { it },
                    exit = slideOutVertically { it }
                )
                .background(color = MaterialTheme.colors.background1Color, shape = shape)
                .then(modifier)
        ) {
            Row(
                modifier = Modifier
                    .height(56.dp)
                    .fillMaxWidth()
                    .padding(start = 24.dp, top = 8.dp, end = 12.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    color = MaterialTheme.colors.sub1Color,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.h5
                )
                IconButton(
                    onClick = onClose,
                    modifier = Modifier.size(40.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.micon_cancle),
                        contentDescription = ""
                    )
                }
            }
            Divider(
                modifier = Modifier
                    .height(1.dp)
                    .fillMaxWidth(),
                color = DividerColor
            )
            content()
        }
    }
}

@Composable
fun BasicDialog(
    modifier: Modifier = Modifier,
    titleText: String,
    contentText: String,
    onClosed: () -> Unit = {},
    leftBtnText: String? = null,
    onLeftBtnClick: () -> Unit = {},
    rightBtnText: String,
    onRightBtnClick: () -> Unit,
    onDismissRequest: () -> Unit = {},
    properties: DialogProperties = DialogProperties(
        dismissOnBackPress = false,
        dismissOnClickOutside = false
    )
) {
    BasicDialog(
        modifier = modifier,
        titleText = titleText,
        contentText = buildAnnotatedString { append(contentText) },
        onClosed = onClosed,
        leftBtnText = leftBtnText,
        onLeftBtnClick = onLeftBtnClick,
        rightBtnText = rightBtnText,
        onRightBtnClick = onRightBtnClick,
        onDismissRequest = onDismissRequest,
        properties = properties
    )
}

@Composable
fun BasicDialog(
    modifier: Modifier = Modifier,
    titleText: String,
    contentText: AnnotatedString,
    onClosed: () -> Unit = {},
    leftBtnText: String? = null,
    onLeftBtnClick: () -> Unit = {},
    rightBtnText: String,
    onRightBtnClick: () -> Unit,
    onDismissRequest: () -> Unit = {},
    properties: DialogProperties = DialogProperties(
        dismissOnBackPress = false,
        dismissOnClickOutside = false
    )
) {
    Dialog(onDismissRequest = onDismissRequest, properties = properties) {
        BasicDialogContent(
            modifier,
            titleText,
            contentText,
            onClosed,
            leftBtnText,
            onLeftBtnClick,
            rightBtnText,
            onRightBtnClick
        )
    }
}

@Composable
fun BasicDialogContent(
    modifier: Modifier = Modifier,
    titleText: String,
    contentText: AnnotatedString,
    onClosed: () -> Unit = {},
    leftBtnText: String? = null,
    onLeftBtnClick: () -> Unit = {},
    rightBtnText: String,
    onRightBtnClick: () -> Unit,
    buttons: Number = 2,
) {
    Card(
        modifier = Modifier
            .defaultMinSize(minWidth = 360.dp, minHeight = 212.dp)
            .shadow(elevation = 2.dp)
            .composed { modifier },
        shape = RoundedCornerShape(0.dp),
        backgroundColor = MaterialTheme.colors.background1Color
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                modifier = Modifier
                    .height(56.dp)
                    .fillMaxWidth()
                    .padding(
                        start = 24.dp,
                        end = 12.dp,
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = titleText,
                    style = MaterialTheme.typography.h6.merge(
                        TextStyle(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colors.sub1Color
                        )
                    ),
                    modifier = Modifier.weight(1f)
                )

                IconButton(
                    onClick = onClosed,
                    modifier = Modifier.size(40.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.micon_cancle),
                        contentDescription = ""
                    )
                }
            }

            Column(
                modifier = Modifier.padding(
                    start = 24.dp,
                    top = 24.dp,
                    end = 24.dp,
                    bottom = 36.dp
                ),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = contentText,
                    style = MaterialTheme.typography.subtitle1,
                    textAlign = TextAlign.Center
                )
            }

            Row(
                Modifier.height(60.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                leftBtnText?.let {
                    GrayDialogButton(
                        onClick = onLeftBtnClick,
                        text = leftBtnText,
                        buttons = buttons,
                        modifier = Modifier.weight(1f),
                    )
                }

                ColorDialogButton(
                    onClick = onRightBtnClick,
                    text = rightBtnText,
                    buttons = buttons,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
fun AlertDialog(
    modifier: Modifier = Modifier,
    contentText: String,
    leftBtnText: String? = null,
    onLeftBtnClick: () -> Unit = {},
    rightBtnText: String = stringResource(id = R.string.confirm),
    onRightBtnClick: () -> Unit,
    onDismissRequest: () -> Unit = {},
    properties: DialogProperties = DialogProperties(
        dismissOnBackPress = false,
        dismissOnClickOutside = false
    )
) {
    AlertDialog(
        modifier = modifier,
        contentText = buildAnnotatedString { append(contentText) },
        leftBtnText = leftBtnText,
        onLeftBtnClick = onLeftBtnClick,
        rightBtnText = rightBtnText,
        onRightBtnClick = onRightBtnClick,
        onDismissRequest = onDismissRequest,
        properties = properties
    )
}

@Composable
fun AlertDialog(
    modifier: Modifier = Modifier,
    contentText: AnnotatedString,
    leftBtnText: String? = null,
    onLeftBtnClick: () -> Unit = {},
    rightBtnText: String = stringResource(id = R.string.confirm),
    onRightBtnClick: () -> Unit,
    onDismissRequest: () -> Unit = {},
    properties: DialogProperties = DialogProperties(
        dismissOnBackPress = false,
        dismissOnClickOutside = false
    )
) {
    Dialog(onDismissRequest = onDismissRequest, properties = properties) {
        AlertDialogContent(
            modifier,
            contentText,
            leftBtnText,
            onLeftBtnClick,
            rightBtnText,
            onRightBtnClick
        )
    }
}

@Composable
fun AlertDialogContent(
    modifier: Modifier = Modifier,
    content: AnnotatedString,
    leftBtnText: String? = null,
    onLeftBtnClick: () -> Unit = {},
    rightBtnText: String = stringResource(id = R.string.confirm),
    onRightBtnClick: () -> Unit,
    buttons: Number = 2,
) {
    Card(
        modifier = Modifier
            .defaultMinSize(minWidth = 360.dp, minHeight = 100.dp)
            .width(360.dp)
            .shadow(elevation = 2.dp)
            .composed { modifier },
        shape = RoundedCornerShape(0.dp),
        backgroundColor = MaterialTheme.colors.background1Color
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Column(
                modifier = Modifier.padding(
                    start = 24.dp,
                    top = 36.dp,
                    end = 24.dp,
                    bottom = 36.dp
                ),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = content,
                    style = MaterialTheme.typography.subtitle1,
                    textAlign = TextAlign.Center
                )
            }

            Row(
                Modifier.height(60.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                leftBtnText?.let {
                    GrayDialogButton(
                        onClick = onLeftBtnClick,
                        text = leftBtnText,
                        buttons = buttons,
                        modifier = Modifier.weight(1f),
                    )
                }

                ColorDialogButton(
                    onClick = onRightBtnClick,
                    text = rightBtnText,
                    buttons = buttons,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Preview
@Composable
fun PreviewWebViewDialog() {
    WebViewDialog(resultMsg = null) {}
}

@Composable
fun WebViewDialog(resultMsg: Message?, onClosed: () -> Unit) {
    val webViewLoading = remember { mutableStateOf(false) }
    val webViewDestroy = remember { mutableStateOf(false) }

    Dialog(
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        ),
        onDismissRequest = {
            webViewDestroy.value = true
        }) {
        Card(
            shape = RoundedCornerShape(0.dp),
            backgroundColor = MaterialTheme.colors.background1Color
        ) {
            Column(
                modifier = Modifier
                    .wrapContentSize()
                    .padding(
                        start = 24.dp,
                        end = 24.dp,
                        bottom = 20.dp
                    ),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {

                val title = remember { mutableStateOf("") }

                Row(
                    modifier = Modifier
                        .height(56.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title.value,
                        style = MaterialTheme.typography.h5.merge(
                            TextStyle(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colors.point1Color
                            )
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    IconButton(
                        onClick = { webViewDestroy.value = true },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.micon_cancle),
                            contentDescription = ""
                        )
                    }
                }

                Box {
                    AndroidView(
                        modifier = Modifier.fillMaxSize(),
                        factory = { context ->
                            WebView(context).apply {

                                settings.apply {
                                    javaScriptEnabled = true
                                    cacheMode = WebSettings.LOAD_NO_CACHE
                                }
                                webViewClient = WebViewClient()
                                webChromeClient = object : WebChromeClient() {

                                    override fun onProgressChanged(
                                        view: WebView?,
                                        newProgress: Int
                                    ) {
                                        super.onProgressChanged(view, newProgress)

                                        webViewLoading.value = newProgress != 100
                                        Log.i("jhkim", "$newProgress")
                                    }

                                    override fun onReceivedTitle(view: WebView?, t: String?) {
                                        title.value = t ?: ""
                                    }

                                    override fun onCloseWindow(window: WebView?) {
                                        webViewDestroy.value = true
                                    }
                                }
                            }.also {
                                val transport = resultMsg?.obj as? WebView.WebViewTransport
                                transport?.webView = it
                                resultMsg?.sendToTarget()
                            }
                        }, update = {
                            if (webViewDestroy.value) {
                                it.destroy()
                                onClosed.invoke()
                            }
                        })


                    if (webViewLoading.value) {
                        LoadingImage(modifier = Modifier.align(Alignment.Center))
                    }
                }
            }
        }
    }
}

@Composable
fun Toast(text: String) {
    Surface(
        modifier = Modifier.defaultMinSize(minWidth = 186.dp, minHeight = 56.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colors.toastColor
    ) {
        Row(
            modifier = Modifier.padding(start = 24.dp, end = 24.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = text,
                color = MaterialTheme.colors.background1Color,
                textAlign = TextAlign.Center,
                maxLines = 1,
                style = MaterialTheme.typography.subtitle1
            )
        }

    }
}


object BottomPopupScope {

    @Composable
    fun Transition(visible: Boolean, content: @Composable AnimatedVisibilityScope.() -> Unit) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(initialAlpha = 1f),
            exit = fadeOut()
        ) {

            val focusManager = LocalFocusManager.current

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x33000000))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { focusManager.clearFocus() },
                contentAlignment = Alignment.BottomCenter
            ) {
                content()
            }
        }
    }

}