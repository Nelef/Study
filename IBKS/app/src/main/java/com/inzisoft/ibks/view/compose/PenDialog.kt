@file:OptIn(ExperimentalAnimationApi::class)

package com.inzisoft.ibks.view.compose

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.CustomViewTarget
import com.bumptech.glide.request.transition.Transition
import com.inzisoft.ibks.R
import com.inzisoft.ibks.view.compose.theme.Divider1Color
import com.inzisoft.ibks.view.compose.theme.IBKSTheme
import com.inzisoft.ibks.view.compose.theme.disableColor
import com.inzisoft.ibks.view.compose.theme.sub1Color
import com.inzisoft.paperless.inputmethod.data.PenPoint
import com.inzisoft.paperless.inputmethod.tools.pen.SignView
import java.io.File

@Preview(device = Devices.AUTOMOTIVE_1024p, showBackground = true, backgroundColor = 0xFFFFFF)
@Composable
fun PreviewPenDialog() {
    IBKSTheme {
        WritePenDialog(
            visible = true,
            title = stringResource(id = R.string.write_name_sign),
            onCancel = {},
            onConfirm = { _, _, _, _ -> },
            subtitle1 = stringResource(id = R.string.write_name_sign),
            placeholder1 = stringResource(id = R.string.write_name_placeholder),
            imagePath1 = ""
        )
    }
}

@Preview(device = Devices.AUTOMOTIVE_1024p, showBackground = true, backgroundColor = 0xFFFFFF)
@Composable
fun PreviewDoublePenDialog() {
    IBKSTheme {
        WritePenDialog(
            visible = true,
            title = stringResource(id = R.string.write_name_sign),
            onCancel = {},
            onConfirm = { _, _, _, _ -> },
            subtitle1 = stringResource(id = R.string.write_name_subtitle),
            placeholder1 = stringResource(id = R.string.write_name_placeholder),
            imagePath1 = "",
            showPen2 = true,
            subtitle2 = stringResource(id = R.string.write_sign_subtitle),
            placeholder2 = stringResource(id = R.string.write_sign_placeholder)
        )
    }
}

@Preview(device = Devices.AUTOMOTIVE_1024p, showBackground = true, backgroundColor = 0xFFFFFF)
@Composable
fun PreviewPenSealOnlyDialog() {
    IBKSTheme {
        WritePenSealOnlyDialog(
            visible = true,
            title = stringResource(id = R.string.write_name_seal_only),
            onCancel = {},
            onConfirm = { _, _, _ -> },
            imagePath = "",
            onRetake = {}
        )
    }
}

@Preview(device = Devices.AUTOMOTIVE_1024p, showBackground = true, backgroundColor = 0xFFFFFF)
@Composable
fun PreviewPenSealDialog() {
    IBKSTheme {
        WritePenSealDialog(
            visible = true,
            title = stringResource(id = R.string.write_name_sign),
            onCancel = {},
            onConfirm = { _, _, _, _, _ -> },
            imagePath1 = "",
            onRetake = {},
            onChoice = { _, _ -> false }
        )
    }
}

@Preview(device = Devices.AUTOMOTIVE_1024p, showBackground = true, backgroundColor = 0xFFFFFF)
@Composable
fun PreviewSealDialog() {
    IBKSTheme {
        SealDialog(
            visible = true,
            title = stringResource(id = R.string.seal_scan),
            onCancel = {},
            onRetake = {},
            onConfirm = { _ -> },
        )
    }
}

@Composable
fun WritePenSealOnlyDialog(
    visible: Boolean,
    title: String,
    onCancel: () -> Unit,
    onConfirm: (penImage: Bitmap?, pointList: List<PenPoint>?, sealImage: Bitmap?) -> Unit,
    imagePath: String,
    pointList: List<PenPoint>? = null,
    sealBitmap: Bitmap? = null,
    subtitle: String = stringResource(id = R.string.seal_scan_subtitle),
    onRetake: () -> Unit,
) {
    var isEmptyPen by remember { mutableStateOf(true) }
    var isEmptySeal = sealBitmap == null
    val penView = PenView { isEmptyPen = it }

    BottomPopupScope.Transition(visible = visible) {
        Column(modifier = Modifier
            .animateEnterExit(
                enter = slideInVertically { it },
                exit = slideOutVertically { it }
            )
            .fillMaxWidth(0.96f)
            .fillMaxHeight(0.61f)
            .background(Color.White)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colors.sub1Color,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 24.dp)
                )
                Divider(
                    modifier = Modifier
                        .height(24.dp)
                        .width(1.dp),
                    color = Divider1Color
                )
                TopBarButton(
                    onClick = onCancel,
                    icon = R.drawable.micon_cancle,
                    pressedIcon = R.drawable.micon_cancle_on,
                    text = stringResource(
                        id = R.string.cancel
                    )
                )
                Divider(
                    modifier = Modifier
                        .height(24.dp)
                        .width(1.dp),
                    color = Divider1Color
                )
                TopBarButton(
                    onClick = {
                        onConfirm(
                            penView.capture(true),
                            penView.arrVertex,
                            sealBitmap
                        )
                    },
                    enabled = !isEmptyPen && !isEmptySeal,
                    icon = R.drawable.micon_check,
                    pressedIcon = R.drawable.micon_check_on,
                    text = stringResource(
                        id = R.string.confirm
                    )
                )
            }

            Divider(color = Divider1Color)

            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(start = 48.dp, top = 24.dp, end = 48.dp, bottom = 49.dp)
            ) {
                PenContent(
                    modifier = Modifier
                        .weight(1f),
                    title = stringResource(id = R.string.write_name_subtitle),
                    onRewrite = { penView.clearPenData() },
                    placeholder = stringResource(id = R.string.name),
                    penView = penView
                )

                SealContent(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 32.dp),
                    subtitle = subtitle,
                    onRetake = onRetake,
                    sealBitmap = sealBitmap
                )
            }
        }
    }

    val context = LocalContext.current

    LaunchedEffect(context, visible) {
        if (pointList == null) {
            loadImage(context, penView, imagePath)
        } else {
            penView.arrVertex = ArrayList(pointList)
        }
    }

    DisposableEffect(!visible) {
        onDispose {
            penView.clearPenData()
        }
    }
}

@Composable
fun WritePenSealDialog(
    visible: Boolean,
    title: String,
    onCancel: () -> Unit,
    onConfirm: (penImage1: Bitmap?, pointList1: List<PenPoint>?, penImage2: Bitmap?, pointList2: List<PenPoint>?, sealImage: Bitmap?) -> Unit,
    imagePath1: String,
    pointList1: List<PenPoint>? = null,
    imagePath2: String? = null,
    pointList2: List<PenPoint>? = null,
    sealBitmap: Bitmap? = null,
    onRetake: () -> Unit,
    onChoice: (isSealChoice: Boolean, isEmptyOther: Boolean) -> Unit,
    sealMode: Boolean = false
) {
    var isEmptyPen1 by remember { mutableStateOf(true) }
    var isEmptyPen2 by remember { mutableStateOf(true) }
    var isEmptySeal = sealBitmap == null
    val penView1 = PenView { isEmptyPen1 = it }
    val penView2 = PenView { isEmptyPen2 = it }

    if(sealMode) {
        penView2.clearPenData()
    }

    BottomPopupScope.Transition(visible = visible) {
        Column(modifier = Modifier
            .animateEnterExit(
                enter = slideInVertically { it },
                exit = slideOutVertically { it }
            )
            .fillMaxWidth(0.96f)
            .fillMaxHeight(0.61f)
            .background(Color.White)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colors.sub1Color,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 24.dp)
                )
                Divider(
                    modifier = Modifier
                        .height(24.dp)
                        .width(1.dp),
                    color = Divider1Color
                )
                TopBarButton(
                    onClick = onCancel,
                    icon = R.drawable.micon_cancle,
                    pressedIcon = R.drawable.micon_cancle_on,
                    text = stringResource(
                        id = R.string.cancel
                    )
                )
                Divider(
                    modifier = Modifier
                        .height(24.dp)
                        .width(1.dp),
                    color = Divider1Color
                )
                TopBarButton(
                    onClick = {
                        onConfirm(
                            penView1.capture(true),
                            penView1.arrVertex,
                            penView2.capture(true),
                            penView2.arrVertex,
                            sealBitmap
                        )
                    },
                    enabled = !isEmptyPen1 && (!isEmptyPen2 || !isEmptySeal),
                    icon = R.drawable.micon_check,
                    pressedIcon = R.drawable.micon_check_on,
                    text = stringResource(id = R.string.confirm)
                )
            }

            Divider(color = Divider1Color)

            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(start = 48.dp, top = 24.dp, end = 48.dp, bottom = 49.dp)
            ) {
                PenContent(
                    modifier = Modifier
                        .weight(1f),
                    title = stringResource(id = R.string.write_name_subtitle),
                    onRewrite = { penView1.clearPenData() },
                    placeholder = stringResource(id = R.string.name),
                    penView = penView1
                )

                PenSealContent(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 32.dp),
                    onRewrite = { isSealMode ->
                        if (isSealMode) {
                            onRetake()
                        } else {
                            penView2.clearPenData()
                        }
                    },
                    penView = penView2,
                    onChoice = { isSealChoice ->
                        if (isSealChoice) {
                            // 누른것이 인감일때, 서명이 있으면 다이얼로그, 없으면 인감 선택됨.
                            onChoice(isSealChoice, isEmptyPen2) // 다이얼로그 확인 누르면 서명 삭제 후 인감 선택.
                        } else {
                            // 누른것이 서명일때, 인감이 있으면 다이얼로그, 없으면 서명 선택됨.
                            onChoice(isSealChoice, isEmptySeal) // 다이얼로그 확인 누르면 인감 삭제 후 서명 선택.
                        }
                    },
                    sealBitmap = sealBitmap,
                    isSealMode = sealMode
                )
            }
        }
    }

    val context = LocalContext.current

    LaunchedEffect(context, visible) {
        if (pointList1 == null) {
            loadImage(context, penView1, imagePath1)
        } else {
            penView1.arrVertex = ArrayList(pointList1)
        }

        if (pointList2 == null) {
            if(!sealMode)
                imagePath2?.let { loadImage(context, penView2, it) }
        } else {
            penView2.arrVertex = ArrayList(pointList2)
        }
    }

    DisposableEffect(!visible) {
        onDispose {
            penView1.clearPenData()
            penView2.clearPenData()
        }
    }
}

@Composable
fun WritePenDialog(
    visible: Boolean,
    title: String,
    onCancel: () -> Unit,
    onConfirm: (penImage1: Bitmap?, pointList1: List<PenPoint>?, penImage2: Bitmap?, pointList2: List<PenPoint>?) -> Unit,
    subtitle1: String,
    placeholder1: String,
    imagePath1: String,
    pointList1: List<PenPoint>? = null,
    showPen2: Boolean = false,
    subtitle2: String? = null,
    placeholder2: String? = null,
    imagePath2: String? = null,
    pointList2: List<PenPoint>? = null
) {
    var isEmptyPen1 by remember { mutableStateOf(true) }
    var isEmptyPen2 by remember { mutableStateOf(true) }
    val penView1 = PenView { isEmptyPen1 = it }
    val penView2 = PenView { isEmptyPen2 = it }

    BottomPopupScope.Transition(visible = visible) {
        Column(modifier = Modifier
            .animateEnterExit(
                enter = slideInVertically { it },
                exit = slideOutVertically { it }
            )
            .fillMaxWidth(0.96f)
            .fillMaxHeight(0.61f)
            .background(Color.White)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colors.sub1Color,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 24.dp)
                )
                Divider(
                    modifier = Modifier
                        .height(24.dp)
                        .width(1.dp),
                    color = Divider1Color
                )
                TopBarButton(
                    onClick = onCancel,
                    icon = R.drawable.micon_cancle,
                    pressedIcon = R.drawable.micon_cancle_on,
                    text = stringResource(
                        id = R.string.cancel
                    )
                )
                Divider(
                    modifier = Modifier
                        .height(24.dp)
                        .width(1.dp),
                    color = Divider1Color
                )
                TopBarButton(
                    onClick = {
                        onConfirm(
                            penView1.capture(true),
                            penView1.arrVertex,
                            penView2.capture(true),
                            penView2.arrVertex
                        )
                    },
                    enabled = !isEmptyPen1 && !isEmptyPen2,
                    icon = R.drawable.micon_check,
                    pressedIcon = R.drawable.micon_check_on,
                    text = stringResource(
                        id = R.string.confirm
                    )
                )
            }

            Divider(color = Divider1Color)

            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(start = 48.dp, top = 24.dp, end = 48.dp, bottom = 49.dp)
            ) {
                PenContent(
                    modifier = Modifier
                        .weight(1f),
                    title = subtitle1,
                    onRewrite = { penView1.clearPenData() },
                    placeholder = placeholder1,
                    penView = penView1
                )

                if (showPen2) {
                    PenContent(
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 32.dp),
                        title = subtitle2 ?: "",
                        onRewrite = { penView2.clearPenData() },
                        placeholder = placeholder2 ?: "",
                        penView = penView2
                    )
                } else {
                    isEmptyPen2 = false
                }
            }
        }
    }

    val context = LocalContext.current

    LaunchedEffect(context, visible) {
        if (pointList1 == null) {
            loadImage(context, penView1, imagePath1)
        } else {
            penView1.arrVertex = ArrayList(pointList1)
        }

        if (pointList2 == null) {
            imagePath2?.let { loadImage(context, penView2, it) }
        } else {
            penView2.arrVertex = ArrayList(pointList2)
        }
    }

    DisposableEffect(!visible) {
        onDispose {
            penView1.clearPenData()
            penView2.clearPenData()
        }
    }
}

@Composable
fun SealDialog(
    visible: Boolean,
    title: String,
    subtitle: String = stringResource(id = R.string.seal_scan_subtitle),
    onCancel: () -> Unit,
    onRetake: () -> Unit,
    onConfirm: (sealBitmap: Bitmap?) -> Unit,
    sealBitmap: Bitmap? = null
) {
    var isEmptySeal = sealBitmap == null
    BottomPopupScope.Transition(visible = visible) {
        Column(modifier = Modifier
            .animateEnterExit(
                enter = slideInVertically { it },
                exit = slideOutVertically { it }
            )
            .fillMaxWidth(0.5f)
            .fillMaxHeight(0.61f)
            .background(Color.White)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colors.sub1Color,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 24.dp)
                )
                Divider(
                    modifier = Modifier
                        .height(24.dp)
                        .width(1.dp),
                    color = Divider1Color
                )
                TopBarButton(
                    onClick = onCancel,
                    icon = R.drawable.micon_cancle,
                    pressedIcon = R.drawable.micon_cancle_on,
                    text = stringResource(
                        id = R.string.cancel
                    )
                )
                Divider(
                    modifier = Modifier
                        .height(24.dp)
                        .width(1.dp),
                    color = Divider1Color
                )
                TopBarButton(
                    onClick = {
                        onConfirm(sealBitmap)
                    },
                    enabled = !isEmptySeal,
                    icon = R.drawable.micon_check,
                    pressedIcon = R.drawable.micon_check_on,
                    text = stringResource(
                        id = R.string.confirm
                    )
                )
            }

            Divider(color = Divider1Color)

            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(start = 48.dp, top = 24.dp, end = 48.dp, bottom = 49.dp)
            ) {
                SealContent(subtitle = subtitle, onRetake = onRetake, sealBitmap = sealBitmap)
            }
        }
    }
}

fun loadImage(context: Context, penView: SignView, path: String) {
    if (path.isEmpty() || !File(path).exists()) return

    Glide.with(context).asBitmap().load(File(path))
        .diskCacheStrategy(DiskCacheStrategy.NONE)
        .skipMemoryCache(true)
        .into(object : CustomViewTarget<SignView, Bitmap>(penView) {
            override fun onLoadFailed(errorDrawable: Drawable?) {
                penView.bitmap = null
            }

            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                penView.bitmap = resource
            }

            override fun onResourceCleared(placeholder: Drawable?) {

            }

        })


}

//@Preview
@Composable
fun PreviewPenContent() {
    IBKSTheme {
        PenContent(
            title = "title",
            placeholder = "placeholder",
            onRewrite = {},
            penView = PenView {}
        )
    }
}

@Composable
fun PenSealContent(
    modifier: Modifier = Modifier,
    onRewrite: (isPenMode: Boolean) -> Unit,
    penView: SignView,
    isSealMode: Boolean,
    sealBitmap: Bitmap? = null,
    onChoice: ((isSealChoice: Boolean) -> Unit)
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .then(modifier)
    ) {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .selectableGroup()
            ) {
                ChoiceButton(
                    selected = !isSealMode,
                    onClick = {
                        onChoice(false)
                    },
                    text = stringResource(id = R.string.name_sign)
                )

                ChoiceButton(
                    selected = isSealMode,
                    onClick = {
                        onChoice(true)
                    },
                    text = stringResource(id = R.string.seal)
                )
            }

            GrayColorButton(
                onClick = {
                    onRewrite(isSealMode)
                },
                text = if (isSealMode) {
                    stringResource(id = R.string.do_seal_scan)
                } else {
                    stringResource(id = R.string.rewrite)
                },
                buttonStyle = ButtonStyle.Dialog
            )
        }

        if (isSealMode) {
            SealArea(placeholder = stringResource(id = R.string.seal), sealBitmap = sealBitmap)
        } else {
            // Seal Area
            PenArea(placeholder = stringResource(id = R.string.name_sign), penView = penView)
        }

    }
}

@Composable
fun SealContent(
    subtitle: String,
    modifier: Modifier = Modifier,
    onRetake: () -> Unit,
    sealBitmap: Bitmap? = null,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .then(modifier)
    ) {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = subtitle,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.h6
            )
            GrayColorButton(
                onClick = onRetake,
                text = stringResource(id = R.string.do_seal_scan),
                buttonStyle = ButtonStyle.Dialog
            )
        }

        SealArea(placeholder = stringResource(id = R.string.seal), sealBitmap = sealBitmap)
    }
}

@Composable
fun PenArea(
    placeholder: String,
    placeholderSize: TextUnit = 100.sp,
    penView: SignView
) {
    Box(
        modifier = Modifier
            .padding(top = 12.dp)
            .border(1.dp, Color(0xFFD7D7D7))
            .fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        AutoSizeText(
            text = placeholder,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            color = MaterialTheme.colors.disableColor,
            fontSizeMax = placeholderSize,
            textAlign = TextAlign.Center,
        )
        AndroidView(
            factory = {
                penView
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
fun SealArea(
    placeholder: String,
    placeholderSize: TextUnit = 100.sp,
    sealBitmap: Bitmap? = null
) {
    Box(
        modifier = Modifier
            .padding(top = 12.dp)
            .border(1.dp, Color(0xFFD7D7D7))
            .fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        AutoSizeText(
            text = placeholder,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            color = MaterialTheme.colors.disableColor,
            fontSizeMax = placeholderSize,
            textAlign = TextAlign.Center,
        )

        sealBitmap?.let {
            Image(
                modifier = Modifier
                    .fillMaxSize()
                    .wrapContentSize(Alignment.Center),
                bitmap = it.asImageBitmap(),
                contentDescription = ""
            )
        }
    }
}

@Composable
fun PenContent(
    modifier: Modifier = Modifier,
    title: String,
    placeholder: String,
    placeholderSize: TextUnit = 100.sp,
    onRewrite: () -> Unit,
    penView: SignView
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .then(modifier)
    ) {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = title, modifier = Modifier.weight(1f), style = MaterialTheme.typography.h6)
            GrayColorButton(
                onClick = onRewrite,
                text = stringResource(id = R.string.rewrite),
                buttonStyle = ButtonStyle.Dialog
            )
        }

        PenArea(placeholder = placeholder, placeholderSize = placeholderSize, penView = penView)
    }
}

@Composable
fun PenView(strokeChanged: (isEmpty: Boolean) -> Unit): SignView {
    val context = LocalContext.current

    return remember {
        SignView(context).apply {
            setPenColor(android.graphics.Color.BLACK)
            setPenStrokeWidth(10.dp.value.toInt())
            setAntiAlias(true)
            setPenEnable(true)
            setStrokeChangeListener({
                strokeChanged(this.isEmpty)
            }, false)
        }
    }
}