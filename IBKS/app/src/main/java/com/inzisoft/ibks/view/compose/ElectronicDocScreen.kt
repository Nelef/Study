package com.inzisoft.ibks.view.compose

import android.webkit.*
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.webkit.WebViewAssetLoader
import com.inzisoft.ibks.R
import com.inzisoft.ibks.data.internal.Thumbnail
import com.inzisoft.ibks.view.compose.theme.*
import com.skydoves.landscapist.glide.GlideImage
import kotlinx.coroutines.delay
import java.io.File
import java.io.FileInputStream
import java.io.IOException

@Preview(widthDp = 1280, heightDp = 800, backgroundColor = 0xFFFFFFFF)
@Composable
fun PreviewInfoScreen() {
    IBKSTheme {
        PreviewInfo()
    }
}

@Composable
fun PreviewInfo(onGone: () -> Unit = {}) {
    var visible by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        delay(3000)
        visible = false
        onGone()
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(initialAlpha = 1f),
        exit = fadeOut()
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier.clickable { visible = false },
                shape = RoundedCornerShape(16.dp),
                backgroundColor = Color(0x99000000)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterVertically),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Text(
                        text = stringResource(id = R.string.preview_message_title),
                        color = Color.White,
                        style = MaterialTheme.typography.h4
                    )

                    Text(
                        text = stringResource(id = R.string.preview_message),
                        color = Color.White,
                        style = MaterialTheme.typography.subtitle1
                    )

                }
            }
        }
    }
}

@Preview(widthDp = 1280, heightDp = 800)
@Composable
fun PreviewPreViewScreen() {
    PreviewScreen(
        showPaperless = true,
        paperlessArea = {},
        imagesArea = {},
        pageText = "2/3",
        showPrevBtn = true,
        onPrevPage = {},
        showNextBtn = true
    ) {

    }
}

@Composable
fun PreviewScreen(
    showPaperless: Boolean,
    paperlessArea: @Composable () -> Unit,
    imagesArea: @Composable BoxScope.() -> Unit,
    visiblePageText: Boolean = false,
    pageText: String,
    showPrevBtn: Boolean,
    onPrevPage: () -> Unit,
    showNextBtn: Boolean,
    onNextPage: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        paperlessArea()

        if (!showPaperless) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.White)
                    .clickable(enabled = false) { }) {
                imagesArea()
            }
        }

        if (visiblePageText) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 12.dp)
                    .size(80.dp, 48.dp)
                    .background(Color(0x66000000), RoundedCornerShape(16.dp))
                    .clip(RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = pageText,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.subtitle1
                )
            }
        }

        if (showPrevBtn) {
            IconButton(
                modifier = Modifier
                    .size(96.dp)
                    .align(Alignment.CenterStart),
                onClick = onPrevPage,
                shape = CircleShape,
                backgroundColor = Color(0x33000000),
                icon = R.drawable.view_back,
                pressedIcon = R.drawable.view_back_on
            )
        }

        if (showNextBtn) {
            IconButton(
                modifier = Modifier
                    .size(96.dp)
                    .align(Alignment.CenterEnd),
                onClick = onNextPage,
                shape = CircleShape,
                backgroundColor = Color(0x33000000),
                icon = R.drawable.view_next,
                pressedIcon = R.drawable.view_next_on
            )
        }

    }
}

@Composable
fun ThumbnailPopup(
    thumbnailList: List<Thumbnail>,
    currentPage: Int,
    onClickThumbnail: (index: Int) -> Unit
) {
    Card(
        modifier = Modifier
            .padding(20.dp)
            .fillMaxHeight()
            .fillMaxWidth(0.15f),
        shape = RoundedCornerShape(16.dp),
        backgroundColor = MaterialTheme.colors.background1Color,
        elevation = 2.dp
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            itemsIndexed(thumbnailList) { index, thumbnail ->

                val filter = if (currentPage == index + 1) null else ColorFilter.tint(
                    Color(0x33000000), BlendMode.Multiply
                )

                val border = BorderStroke(
                    width = if (currentPage == index + 1) 3.dp else 0.dp,
                    color = MaterialTheme.colors.point1Color
                )

                Box(
                    modifier = Modifier
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
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.h1
                    )
                }

                if (thumbnail.isShowDivider) {
                    Divider(
                        modifier = Modifier.padding(top = 10.dp),
                        color = MaterialTheme.colors.divider1Color,
                        thickness = 2.dp
                    )
                }
            }
        }
    }
}

@Preview(widthDp = 1280, heightDp = 800)
@Composable
fun PreviewMemoBottomDialog() {
    IBKSTheme {
        MemoBottomDialog(
            visible = true,
            memo = "",
            onMemoChange = {},
            onCancel = {},
            onConfirm = {}
        )
    }
}

@Composable
fun MemoBottomDialog(
    visible: Boolean,
    memo: String,
    currentBytes: Int = 0,
    totalBytes: Int = 1000,
    onMemoChange: (String) -> Unit,
    onCancel: () -> Unit,
    onConfirm: () -> Unit
) {
    BasicBottomDialog(
        title = stringResource(id = R.string.memo_title),
        modifier = Modifier
            .width(840.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { },
        onClose = onCancel,
        visible = visible
    ) {
        Column {
            Box(
                modifier = Modifier
                    .padding(36.dp)
                    .fillMaxWidth()
                    .height(264.dp)
            ) {
                OutlinedTextField(
                    value = memo,
                    onValueChange = { onMemoChange(it) },
                    modifier = Modifier.fillMaxSize(),
                    shape = RectangleShape,
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        backgroundColor = MaterialTheme.colors.unfocusedColor,
                        focusedBorderColor = MaterialTheme.colors.point1Color,
                        cursorColor = MaterialTheme.colors.point1Color
                    )
                )

                Text(
                    text = "$currentBytes/${totalBytes}byte",
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(12.dp),
                    color = MaterialTheme.colors.sub1Color,
                    style = MaterialTheme.typography.body1
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                GrayDialogButton(
                    onClick = onCancel,
                    modifier = Modifier.weight(0.5f),
                    text = stringResource(id = R.string.cancel),
                    buttonStyle = ButtonStyle.Big
                )
                ColorDialogButton(
                    onClick = onConfirm,
                    text = stringResource(id = R.string.confirm),
                    modifier = Modifier.weight(0.5f),
                    buttonStyle = ButtonStyle.Big
                )
            }
        }
    }
}

@Preview(widthDp = 1280, heightDp = 800)
@Composable
fun PreviewTransmitScreen1() {
    IBKSTheme {
        TransmitScreen(
            title = "해당 신청서를 제출하고 있습니다.",
            message = "잠시만 기다려 주세요.",
        ) {
            ProgressArea("전자문서 생성 중(1/3)", 0.25f)
        }
    }
}

@Preview(widthDp = 1280, heightDp = 800)
@Composable
fun PreviewTransmitScreen2() {
    IBKSTheme {
        TransmitScreen(
            title = "해당 신청서를 제출하고 있습니다.",
            message = "잠시만 기다려 주세요.",
        ) {
            ProgressArea("제출 파일 준비 중", 0.5f)
        }
    }
}

@Preview(widthDp = 1280, heightDp = 800)
@Composable
fun PreviewTransmitScreen3() {
    IBKSTheme {
        TransmitScreen(
            title = "해당 신청서를 제출하고 있습니다.",
            message = "잠시만 기다려 주세요.",
        ) {
            ProgressArea("서버 전송 중", 0.75f)
        }
    }
}

@Preview(widthDp = 1280, heightDp = 800)
@Composable
fun PreviewTransmitScreen4() {
    IBKSTheme {
        TransmitScreen(
            title = "해당 신청서를 제출하고 있습니다.",
            message = "잠시만 기다려 주세요.",
        ) {
            ProgressArea("접수 완료", 1f)
        }
    }
}

@Preview(widthDp = 1280, heightDp = 800)
@Composable
fun PreviewTransmitScreenError() {
    IBKSTheme {
        TransmitScreen(
            title = "네트워크 연결에 실패했습니다.",
            titleColor = MaterialTheme.colors.point4Color,
            message = "네트워크를 연결할 수 없어 전송에 실패했습니다.\n" +
                    "연결 상태를 확인 후 다시 시도하거나 임시저장하기를 눌러주세요.",
        ) {
            ErrorArea(onSaveTemp = { }) {

            }
        }
    }
}

@Preview(widthDp = 1280, heightDp = 800)
@Composable
fun PreviewTransmitScreenComplete() {
    IBKSTheme {
        TransmitScreen(
            title = "해당 신청서를 제출하였습니다.",
            message = "메인 화면에서 해당 접수 현황을 확인 할 수 있습니다.",
        ) {
            CompleteArea {

            }
        }
    }
}

@Composable
fun TransmitScreen(
    title: String,
    titleColor: Color = MaterialTheme.colors.mainColor,
    message: String,
    message2: AnnotatedString? = null,
    actionArea: @Composable BoxScope.() -> Unit
) {
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { focusManager.clearFocus() }
            .background(color = Color.White),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id = R.drawable.logo_ibks_c),
                contentDescription = "logo",
                modifier = Modifier.size(width = 256.dp, height = 52.dp)
            )

            Text(
                text = stringResource(id = R.string.transmit_ods_title),
                modifier = Modifier.padding(start = 16.dp),
                color = Color(0xFF0056A4),
                fontWeight = FontWeight.ExtraBold,
                style = MaterialTheme.typography.h2
            )
        }


        Text(
            text = title,
            modifier = Modifier.padding(top = 100.dp),
            color = titleColor,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.h1
        )

        Text(
            text = message,
            modifier = Modifier.padding(top = 24.dp),
            color = Color(0xFF111111),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.subtitle1
        )

        if (message2 == null) {
            Spacer(modifier = Modifier.height(136.dp))
        } else {
            Text(
                text = message2,
                modifier = Modifier.padding(top = 48.dp),
                color = Color(0xFF111111),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.subtitle1
            )
            Spacer(modifier = Modifier.height(68.dp))
        }

        Box(contentAlignment = Alignment.Center) {
            actionArea()
        }

//        Column(horizontalAlignment = Alignment.CenterHorizontally) {


//            when (state) {
//                is TransmitState.Complete -> {
//                    ColorButton(
//                        onClick = onGoMain,
//                        modifier = Modifier.width(360.dp),
//                        text = stringResource(id = R.string.go_main),
//                        buttonStyle = ButtonStyle.Big
//                    )
//                }
//
//                is TransmitState.Error -> {
//                    Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
//                        GrayColorButton(
//                            onClick = onSaveTemp,
//                            modifier = Modifier.width(240.dp),
//                            text = stringResource(id = R.string.do_save_temp)
//                        )
//                        ColorButton(
//                            onClick = onRetransmit,
//                            modifier = Modifier.width(240.dp),
//                            text = stringResource(id = R.string.do_retransmit),
//                            buttonStyle = ButtonStyle.Big
//                        )
//                    }
//                }
//
//                is TransmitState.MakeResult,
//                is TransmitState.Transmit,
//                is TransmitState.ZipFile -> {
//                    Text(
//                        text = progressMessage,
//                        color = Color(0xFF111111),
//                        style = MaterialTheme.typography.body1
//                    )
//
//                    LinearProgressIndicator(
//                        progress = progress, modifier = Modifier
//                            .padding(top = 8.dp)
//                            .fillMaxWidth(0.38f)
//                            .height(8.dp)
//                    )
//                }
//            }
//        }
    }
}


@Composable
fun ProgressArea(message: String, progress: Float) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {

        Text(
            text = message,
            color = Color(0xFF111111),
            style = MaterialTheme.typography.body1
        )

        LinearProgressIndicator(
            progress = progress, modifier = Modifier
                .padding(top = 8.dp)
                .fillMaxWidth(0.38f)
                .height(8.dp)
        )
    }
}

@Composable
fun ErrorArea(onSaveTemp: () -> Unit, onRetransmit: () -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
//        GrayColorButton(
//            onClick = onSaveTemp,
//            modifier = Modifier.width(240.dp),
//            text = stringResource(id = R.string.do_save_temp)
//        )
        ColorButton(
            onClick = onRetransmit,
            modifier = Modifier.width(240.dp),
            text = stringResource(id = R.string.do_retransmit),
            buttonStyle = ButtonStyle.Big
        )
    }
}

@Composable
fun CompleteArea(onGoMain: () -> Unit) {
    ColorButton(
        onClick = onGoMain,
        modifier = Modifier.width(360.dp),
        text = stringResource(id = R.string.go_main),
        buttonStyle = ButtonStyle.Big
    )
}

@Composable
fun ImageViewer(
    modifier: Modifier = Modifier,
    imagePaths: List<String>,
    onScrollBottom: () -> Unit,
    onError: (e: Exception) -> Unit
) {

    var onReady by remember { mutableStateOf(false) }
    var onDestroy by remember { mutableStateOf(false) }

    DisposableEffect(key1 = onDestroy) {
        onDispose {
            onDestroy = true
        }
    }

    AndroidView(factory = { context ->
        WebView(context).apply {

            addJavascriptInterface(object {

                @JavascriptInterface
                fun onScrollBottom() {
                    onScrollBottom()
                }
            }, "InnerWebInterface")

            settings.apply {
                setSupportZoom(true)
                builtInZoomControls = true
                displayZoomControls = false
                loadWithOverviewMode = true
                useWideViewPort = true
                javaScriptEnabled = true
            }

            val loader = WebViewAssetLoader.Builder()
                .addPathHandler("/assets/", WebViewAssetLoader.AssetsPathHandler(context))
                .addPathHandler("/local/") { path ->
                    try {
                        val input =
                            FileInputStream(path)
                        WebResourceResponse("image/jpg", null, input)
                    } catch (e: IOException) {
                        onError(e)
                        WebResourceResponse(null, null, null)
                    }
                }
                .build()

            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    onReady = true
                }

                override fun shouldInterceptRequest(
                    view: WebView?,
                    request: WebResourceRequest
                ): WebResourceResponse? {
                    return loader.shouldInterceptRequest(request.url)
                }
            }

            loadUrl("https://appassets.androidplatform.net/assets/imageViewer.html")
        }
    },
        modifier = Modifier.then(modifier),
        update = { webView ->
            if (onDestroy) {
                webView.destroy()
            }

            if (onReady) {
                webView.evaluateJavascript("clear()", null)
                imagePaths.forEach {
                    webView.evaluateJavascript("addImage(\"$it\")", null)
                }
            }
        })
}