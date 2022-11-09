package com.inzisoft.ibks.base

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
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
                content = content,
                bottomBar = { bottomBar?.invoke() }
            )
            surface?.invoke()
        }
    }
}

