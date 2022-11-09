package com.inzisoft.ibks.view.fragment

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.Popup
import androidx.fragment.app.clearFragmentResultListener
import androidx.navigation.fragment.findNavController
import com.inzisoft.ibks.FragmentRequest
import com.inzisoft.ibks.base.BaseFragment
import com.inzisoft.ibks.view.compose.TempProcessTopBar
import com.inzisoft.ibks.view.compose.theme.IBKSTheme
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class PreviewFragment : BaseFragment() {

    init {
        // topBar 제거
        baseCompose.topBar = {
            TempProcessTopBar(
                title = "TempProcessBar",
                showBack = true,
                showPreview = true,
                showDelete = true,
                onCancel = {findNavController().popBackStack()},
                onShowTransmissive = true,
            )
        }
        baseCompose.content = {
            PreviewFragmentContent()

            // TODO webView viewModel
//            when (val loadData = viewModel.webViewLoadInitData) {
//                is UiState.Error -> {
//                    showAlertDialog(
//                        contentText = R.string.load_application_info_fail,
//                        rightBtnText = R.string.confirm,
//                        onDismissRequest = {
//                            viewModel.finish()
//                        }
//                    )
//                }
//                is UiState.Success -> {
//                    WebViewScreen(loadData.data)
//                }
//                else -> {}
//            }
            PreviewBackHandler()
        }

        baseCompose.surface = {
        }
    }

    @Composable
    private fun PreviewBackHandler() {
        BackHandler(enabled = true, onBack = { })
    }

    override fun onDestroyView() {
        clearFragmentResultListener(FragmentRequest.WritePen.key)
        super.onDestroyView()
    }

    @Preview(widthDp = 1280, heightDp = 800)
    @Composable
    fun PreviewFragmentScreen() {
        baseCompose.baseScreen.invoke()
    }

    @Preview(widthDp = 1280, heightDp = 40)
    @Composable
    fun TopBar() {
        IBKSTheme {
            Surface(Modifier.fillMaxSize()) {
                TempProcessTopBar(
                    title = "TempProcessBar",
                    showBack = true,
                    showPreview = true,
                    showDelete = true,
                    onCancel = {},
                    onShowTransmissive = true,
                )
            }
        }
    }

    @Preview(widthDp = 1280, heightDp = 760)
    @Composable
    fun PreviewPreviewFragmentContent() {
        IBKSTheme {
            PreviewFragmentContent()
        }
    }

    @Composable
    private fun PreviewFragmentContent() {
        var onDestroy by remember { mutableStateOf(false) }

        DisposableEffect(key1 = onDestroy) {
            onDispose {
                onDestroy = true
            }
        }

        Box(modifier = Modifier.fillMaxSize(),) {
            Text(text = "테스트 페이지")
        }

    }

    @Composable
    private fun MicPopup() {
        Popup() {

        }
    }
}