package com.inzisoft.ibks.view.compose

import android.os.Message
import android.webkit.*
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.*
import com.google.gson.Gson
import com.inzisoft.ibks.BuildConfig
import com.inzisoft.ibks.R
import com.inzisoft.ibks.data.web.ElectronicData
import com.inzisoft.ibks.data.web.ElectronicDocInfo
import com.inzisoft.ibks.view.compose.theme.*
import java.io.File

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
                buttonStyle = ButtonStyle.Dialog
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

@Composable
fun TextPopup(
    content: String,
    onCancel: () -> Unit,
    onConfirm: () -> Unit
) {
    Popup(
        popupPositionProvider = WindowCenterOffsetPositionProvider(),
        onDismissRequest = { },
        properties = PopupProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        )
    ) {

        Card(
            modifier = Modifier
                .fillMaxSize()
                .shadow(elevation = 2.dp),
            shape = RoundedCornerShape(0.dp),
            backgroundColor = MaterialTheme.colors.background1Color
        ) {
            Column {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(
                            start = 24.dp,
                            top = 36.dp,
                            end = 24.dp,
                            bottom = 36.dp
                        )
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = content,
                        style = MaterialTheme.typography.subtitle1,
                    )
                }

                Row(
                    Modifier.height(60.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    GrayDialogButton(
                        onClick = onCancel,
                        text = stringResource(id = R.string.cancel),
                        buttonStyle = ButtonStyle.Basic,
                        modifier = Modifier.weight(1f),
                    )

                    ColorDialogButton(
                        onClick = onConfirm,
                        text = stringResource(id = R.string.confirm),
                        buttonStyle = ButtonStyle.Basic,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Preview(widthDp = 1280, heightDp = 800)
@Composable
fun PreviewTestElectronicPopup() {
    TestElectronicPopup(visible = true, onClosed = { }, onConfirm = {})
}

@Composable
fun TestElectronicPopup(
    visible: Boolean,
    onClosed: () -> Unit,
    onConfirm: (json: String) -> Unit
) {
    val electronicData = remember { mutableStateOf<ElectronicData?>(null) }
    val biz = remember { mutableStateOf("") }
    val data = remember { mutableStateOf("") }

    val path = "${LocalContext.current.getExternalFilesDir(null)?.absolutePath}/temp.json"

    LaunchedEffect(Unit) {
        try {
            if (!File(path).exists()) return@LaunchedEffect

            val json = File(path).readText()
            electronicData.value = Gson().fromJson(json, ElectronicData::class.java).apply {
                with(electronicDocInfo) {
                    if (isNotEmpty()) {
                        biz.value = this[0].businessLogic
                        var tmp = ""
                        this[0].data?.forEach {
                            tmp += "${it.key}|${it.value}^"
                        }
                        data.value = tmp
                    }
                }
            }
        } catch (e: Exception) {
            electronicData.value = ElectronicData(null, listOf(), null)
        }
    }

    BasicBottomDialog(
        title = "전자문서 테스트 호출",
        modifier = Modifier
            .fillMaxWidth(0.8f)
            .fillMaxHeight(0.7f),
        onClose = onClosed,
        visible = visible
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            Text(text = "비즈로직")

            OutlinedTextField(
                value = biz.value,
                onValueChange = { biz.value = it },
                shape = RectangleShape,
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    backgroundColor = MaterialTheme.colors.unfocusedColor,
                    focusedBorderColor = MaterialTheme.colors.point1Color,
                    cursorColor = MaterialTheme.colors.point1Color
                )
            )
        }

        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(20.dp)
        ) {

            Text(text = "매핑데이터")

            OutlinedTextField(
                value = data.value,
                onValueChange = { data.value = it },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                shape = RectangleShape,
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    backgroundColor = MaterialTheme.colors.unfocusedColor,
                    focusedBorderColor = MaterialTheme.colors.point1Color,
                    cursorColor = MaterialTheme.colors.point1Color
                )
            )
        }

        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                Modifier.height(60.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                GrayDialogButton(
                    onClick = onClosed,
                    text = "취소",
                    modifier = Modifier.weight(1f)
                )
                ColorDialogButton(
                    onClick = {
                        val d = data.value
                        val map = mutableMapOf<String, String>()
                        d.split("^").forEach {
                            val dd = it.split("|")
                            if (dd.size < 2) return@forEach

                            map[dd[0]] = dd[1]
                        }

                        val json = Gson().toJson(
                            ElectronicData(
                                null,
                                listOf(
                                    ElectronicDocInfo(
                                        openType = "normal",
                                        businessLogic = biz.value.trim(),
                                        penSealImg = listOf(),
                                        title = "",
                                        appendForm = listOf(),
                                        productCode = "",
                                        data = map,
                                        tFieldIdForWeb = listOf()
                                    )
                                ),
                                null
                            )
                        )

                        File(path).delete()
                        File(path).writeText(json)

                        onConfirm(json)
                    },
                    text = "확인",
                    modifier = Modifier.weight(1f)
                )
            }
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
    content: @Composable ColumnScope.() -> Unit
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
                color = Divider1Color
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
    properties: PopupProperties = PopupProperties(
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
    properties: PopupProperties = PopupProperties(
        dismissOnBackPress = false,
        dismissOnClickOutside = false
    )
) {
    Popup(
        popupPositionProvider = WindowCenterOffsetPositionProvider(),
        onDismissRequest = onDismissRequest,
        properties = properties
    ) {
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
    buttonStyle: ButtonStyle = ButtonStyle.Basic,
) {
    Card(
        modifier = Modifier
            .requiredSizeIn(minWidth = 360.dp, minHeight = 212.dp, maxWidth = 800.dp)
            .padding(20.dp)
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
                        buttonStyle = buttonStyle,
                        modifier = Modifier.weight(1f),
                    )
                }

                ColorDialogButton(
                    onClick = onRightBtnClick,
                    text = rightBtnText,
                    buttonStyle = buttonStyle,
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
    properties: PopupProperties = PopupProperties(
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
    properties: PopupProperties = PopupProperties(
        dismissOnBackPress = false,
        dismissOnClickOutside = false
    )
) {
    Popup(
        popupPositionProvider = WindowCenterOffsetPositionProvider(),
        onDismissRequest = onDismissRequest,
        properties = properties
    ) {
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

class WindowCenterOffsetPositionProvider(
    private val x: Int = 0,
    private val y: Int = 0
) : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize
    ): IntOffset {
        return IntOffset(
            (windowSize.width - popupContentSize.width) / 2 + x,
            (windowSize.height - popupContentSize.height) / 2 + y
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
    buttonStyle: ButtonStyle = ButtonStyle.Basic
) {
    Card(
        modifier = Modifier
            .requiredSizeIn(minWidth = 360.dp, minHeight = 100.dp, maxWidth = 500.dp)
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
                    style = MaterialTheme.typography.h5,
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
                        buttonStyle = buttonStyle,
                        modifier = Modifier.weight(1f),
                    )
                }

                ColorDialogButton(
                    onClick = onRightBtnClick,
                    text = rightBtnText,
                    buttonStyle = buttonStyle,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Preview
@Composable
fun PreviewWebViewDialog() {
    WebViewDialog(resultMsg = null, cookieData = "") {}
}

@Composable
fun WebViewDialog(resultMsg: Message?, cookieData: String, onClosed: () -> Unit) {
    var webViewLoading by remember { mutableStateOf(false) }
    var webViewDestroy by remember { mutableStateOf(false) }

    Dialog(
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        ),
        onDismissRequest = {
            webViewDestroy = true
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
                        onClick = { webViewDestroy = true },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.micon_cancle),
                            contentDescription = ""
                        )
                    }
                }

                Box {
                    if (LocalInspectionMode.current)
                        Box(modifier = Modifier.fillMaxSize())
                    else
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

                                            webViewLoading = newProgress != 100
                                        }

                                        override fun onReceivedTitle(view: WebView?, t: String?) {
                                            title.value = t ?: ""
                                        }

                                        override fun onCloseWindow(window: WebView?) {
                                            webViewDestroy = true
                                        }
                                    }
                                    CookieManager.getInstance().setAcceptCookie(true)
                                    CookieManager.getInstance().setCookie(
                                        "${BuildConfig.WEB_PROTOCOL}://${BuildConfig.API_SERVER_URL}",
                                        cookieData
                                    )
                                    CookieManager.getInstance()
                                        .setAcceptThirdPartyCookies(this, true)
                                }.also {
                                    val transport = resultMsg?.obj as? WebView.WebViewTransport
                                    transport?.webView = it
                                    resultMsg?.sendToTarget()
                                }
                            }, update = {
                                if (webViewDestroy) {
                                    it.destroy()
                                    onClosed.invoke()
                                }
                            })


                    if (webViewLoading) {
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