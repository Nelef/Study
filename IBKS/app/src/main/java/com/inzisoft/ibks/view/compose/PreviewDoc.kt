package com.inzisoft.ibks.view.compose

import android.graphics.BitmapFactory
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.graphics.drawable.toBitmap
import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.skydoves.landscapist.glide.GlideImage
import com.skydoves.landscapist.glide.GlideImageState


@Preview(
    widthDp = 1280,
    heightDp = 800,
    showBackground = true,
    backgroundColor = 0xFFFFFF
)
@Composable
fun previewDocTopBar() {
    PreviewDocTopBar(
        title = "PreviewDoc",
        showBack = true,
        onBack = {
        },
        showCompleteBtn = true
    ) {
    }
}

@Composable
fun ZoomImageView(modifier: Modifier = Modifier, imagePath: String) {
    GlideImage(
        imageModel = BitmapFactory.decodeFile(imagePath),
        modifier = modifier,
        success = { imageState: GlideImageState.Success ->
            imageState.drawable?.let { drawable ->
                AndroidView(
                    factory = {
                        SubsamplingScaleImageView(it).apply {
                            isZoomEnabled = true
                            maxScale = 6f
                            minScale = 1f
                            setMinimumScaleType(SubsamplingScaleImageView.SCALE_TYPE_START)
                            setImage(ImageSource.bitmap(drawable.toBitmap()))
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    )
}

@Composable
fun previewDocContents() {
    Box(modifier = Modifier.fillMaxSize()) {

    }
//    if (showPrevBtn) {
//        IconButton(
//            modifier = Modifier
//                .size(96.dp)
//                .align(Alignment.CenterStart),
//            onClick = onPrevPage,
//            shape = CircleShape,
//            backgroundColor = Color(0x33000000),
//            icon = R.drawable.view_back,
//            pressedIcon = R.drawable.view_back_on
//        )
//    }
//
//    if (showNextBtn) {
//        IconButton(
//            modifier = Modifier
//                .size(96.dp)
//                .align(Alignment.CenterEnd),
//            onClick = onNextPage,
//            shape = CircleShape,
//            backgroundColor = Color(0x33000000),
//            icon = R.drawable.view_next,
//            pressedIcon = R.drawable.view_next_on
//        )
//    }
}