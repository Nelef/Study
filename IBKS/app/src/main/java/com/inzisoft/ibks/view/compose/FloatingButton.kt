package com.inzisoft.ibks.view.compose

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import com.inzisoft.ibks.R
import com.inzisoft.ibks.data.internal.RecordData
import com.inzisoft.ibks.data.internal.RecordState
import com.inzisoft.ibks.view.compose.theme.IBKSTheme
import com.inzisoft.ibks.view.compose.theme.background4Color
import kotlin.math.roundToInt

@Preview(device = Devices.AUTOMOTIVE_1024p, backgroundColor = 0xFFFFFF)
@Composable
fun FloatingButtons() {
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
        FloatingRecordButton(
            showFloatingRecordButton = true,
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
fun FloatingRecordButton(
    showFloatingRecordButton: Boolean,
    recordState: RecordState,
    recordTime: String,
    recordFileList: List<RecordData>,
    onRecord: () -> Unit,
    onResume: () -> Unit,
    onPause: () -> Unit,
    onStop: () -> Unit,
    onRecordList: () -> Unit,
) {
    val density = LocalDensity.current
    var offsetX by remember { mutableStateOf(with(density) { -30.dp.toPx() }) }
    var offsetY by remember { mutableStateOf(with(density) { 80.dp.toPx() }) }
    var extended by remember { mutableStateOf(false) }

    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp
    val screenWidth = configuration.screenWidthDp

    Popup(
        alignment = Alignment.TopEnd,
        offset = IntOffset(x = offsetX.roundToInt(), y = offsetY.roundToInt())
    ) {
        AnimatedVisibility(showFloatingRecordButton) {
            Box(modifier = Modifier
                .padding(5.dp)
                .shadow(
                    if (extended) 0.dp else 3.dp,
                    if (extended) RoundedCornerShape(0.dp) else CircleShape
                )
                .clip(if (extended) RoundedCornerShape(0.dp) else CircleShape)
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        offsetX += dragAmount.x
                        offsetY += dragAmount.y
                        // 화면 밖으로 나가는거 방지
                        if (offsetX > 0) {
                            offsetX = 0F
                        }
                        if (offsetY < 0) {
                            offsetY = 0F
                        }
                        if (-offsetX.toDp() > screenWidth.dp) {
                            offsetX = -screenWidth.dp.toPx()
                        }
                        if (offsetY.toDp() > screenHeight.dp) {
                            offsetY = screenHeight.dp.toPx()
                        }
                    }
                }
                .defaultMinSize(60.dp, 60.dp)
            ) {
                AnimatedVisibility(
                    extended,
                    enter = fadeIn() + expandHorizontally(expandFrom = Alignment.Start),
                    exit = fadeOut() + shrinkOut(shrinkTowards = Alignment.TopStart)
                ) {
                    MicBubble(
                        recordState = recordState,
                        recordTime = recordTime,
                        recordFileList = recordFileList,
                        onRecord = onRecord,
                        onResume = onResume,
                        onPause = onPause,
                        onStop = onStop,
                        onRecordList = onRecordList,
                        onClose = { extended = false },
                        isMainButton = true
                    )
                }
                AnimatedVisibility(
                    !extended,
                    enter = fadeIn() + expandIn(expandFrom = Alignment.TopStart),
                    exit = fadeOut() + shrinkHorizontally(shrinkTowards = Alignment.Start)
                ) {
                    Column(
                        modifier = Modifier
                            .size(60.dp, 60.dp)
                            .background(
                                when (recordState) {
                                    RecordState.None -> {
                                        Color(0xFFB5B8CA)
                                    }
                                    RecordState.Recoding -> {
                                        Color(0xFFEC6730)
                                    }
                                    RecordState.Paused -> {
                                        Color(0xFFB5B8CA)
                                    }
                                }
                            )
                            .clickable { extended = !extended },
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Image(
                            painter = painterResource(R.drawable.bicon_mic),
                            modifier = Modifier.height(32.dp),
                            contentDescription = ""
                        )
                    }
                }
            }
        }
    }

    if (showFloatingRecordButton) {
        when (recordState) {
            RecordState.Recoding -> {}
            RecordState.None,
            RecordState.Paused -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colors.background4Color.copy(alpha = 0.5f))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { }
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "진행하려면 녹음을 시작해주세요.", color = Color.White)
                    }
                }
            }
        }
    }
}