package com.inzisoft.ibks.view.compose

import android.graphics.drawable.Drawable
import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.inzisoft.ibks.R
import com.inzisoft.ibks.view.compose.theme.background1Color
import com.skydoves.landscapist.glide.GlideImage

@Preview
@Composable
fun PreviewLoadingPopup() {
    LoadingPopup(message = "미리보기 생성중...")
}

@Preview
@Composable
fun PreviewLoading() {
    Loading()
}

@Preview
@Composable
fun PreviewLoading2() {
    Loading(LoadingType.WAIT)
}

@Composable
fun LoadingPopup(
    loadingType: LoadingType = LoadingType.LOADING,
    backgroundColor: Color = Color(0x33000000),
    message: String? = null
) {
    Box(modifier = Modifier
        .fillMaxSize()
        .clickable(enabled = false) { }
        .background(color = backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.clickable(enabled = false) { },
            backgroundColor = MaterialTheme.colors.background1Color
        ) {
            Column(
                modifier = Modifier.defaultMinSize(250.dp, 150.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                LoadingImage(loadingType = loadingType)

                message?.let {
                    Text(
                        text = message,
                        modifier = Modifier.padding(20.dp),
                        style = MaterialTheme.typography.subtitle1
                    )
                }
            }
        }
    }

}

@Composable
fun Loading(
    loadingType: LoadingType = LoadingType.LOADING,
    backgroundColor: Color = Color(0x33000000)
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(enabled = false) {}
            .background(color = backgroundColor)
    ) {
        LoadingImage(loadingType = loadingType, modifier = Modifier.align(Alignment.Center))
    }
}

@Composable
fun LoadingImage(
    loadingType: LoadingType = LoadingType.LOADING,
    modifier: Modifier = Modifier,
) {
    val gifListener = object : RequestListener<Drawable> {
        override fun onLoadFailed(
            e: GlideException?,
            model: Any?,
            target: Target<Drawable>?,
            isFirstResource: Boolean
        ): Boolean {
            return true
        }

        override fun onResourceReady(
            resource: Drawable?,
            model: Any?,
            target: Target<Drawable>?,
            dataSource: DataSource?,
            isFirstResource: Boolean
        ): Boolean {
            if (resource is GifDrawable) {
                resource.start()
            }
            return true
        }
    }

    GlideImage(
        imageModel = loadingType.resId,
        modifier = Modifier
            .size(loadingType.minSize.first, loadingType.minSize.second)
            .then(modifier),
        requestBuilder = { Glide.with(LocalContext.current).asDrawable() },
        requestListener = gifListener,
        previewPlaceholder = loadingType.resId
    )
}

enum class LoadingType(@DrawableRes val resId: Int, val minSize: Pair<Dp, Dp>) {
    LOADING(R.drawable.loading, 188.dp to 16.dp),
    WAIT(R.drawable.wait, 128.dp to 128.dp)
}