package com.inzisoft.ibks.base

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.inzisoft.ibks.BuildConfig
import com.inzisoft.ibks.R
import com.inzisoft.ibks.view.compose.theme.IBKSTheme

class BaseCompose {
    var topBar: @Composable (() -> Unit)? = null
    var content: @Composable (PaddingValues) -> Unit = {}
    var surface: @Composable (() -> Unit)? = null
    var bottomBar: @Composable (() -> Unit)? = null

    val baseScreen: @Composable () -> Unit = {
        IBKSTheme {
            BackHandler(true) {
                // Back Key를 기본적으로 막음.
            }
            Scaffold(
                topBar = { topBar?.invoke() },
                content = {
                    Box(modifier = Modifier.fillMaxSize()) {
                        content.invoke(it)

                        if (stringResource(id = R.string.env_name).isNotEmpty()) {
                            Text(
                                text = stringResource(id = R.string.env_name) + " / v" + BuildConfig.VERSION_NAME,
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(10.dp),
                                color = colorResource(id = R.color.env_name),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                },
                bottomBar = { bottomBar?.invoke() }
            )
            surface?.invoke()
        }
    }
}

