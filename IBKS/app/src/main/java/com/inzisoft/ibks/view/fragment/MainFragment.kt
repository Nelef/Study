package com.inzisoft.ibks.view.fragment

import android.os.Message
import android.util.Log
import android.view.ViewGroup
import android.webkit.*
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.clearFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.inzisoft.ibks.BuildConfig
import com.inzisoft.ibks.FragmentRequest
import com.inzisoft.ibks.base.BaseFragment
import com.inzisoft.ibks.util.log.QLog
import com.inzisoft.ibks.view.compose.GrayDialogButton
import com.inzisoft.ibks.view.compose.MainTopBar
import com.inzisoft.ibks.view.compose.theme.IBKSTheme
import com.inzisoft.ibks.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainFragment : BaseFragment() {

    companion object {
        private const val WEB_INTERFACE_NAME = "WebInterface"
    }

    private val viewModel: MainViewModel by viewModels()

    init {
        // topBar 제거
//        baseCompose.topBar = {
//            MainTopBar(
//                titleText = "ODS 서비스에 오신것을 환영합니다.",
//                userId = viewModel.userId.value,
//                userName = viewModel.userName.value,
//                onAlarm = { },
//                onLogout = { logout() },
//                onSetting = { goSettings() })
//        }
        baseCompose.content = {
            MainFragmentContent()

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
            MainBackHandler()
        }

        baseCompose.surface = {
        }
    }

    @Composable
    private fun MainBackHandler() {
        BackHandler(enabled = true, onBack = { })
    }

    override fun onDestroyView() {
        clearFragmentResultListener(FragmentRequest.WritePen.key)
        super.onDestroyView()
    }

    private fun goMain() {
        // TODO logout
//        viewModel.logout()
        findNavController().navigate(MainFragmentDirections.actionMainFragmentToLoginFragment())
    }

    private fun goPreview() {
        findNavController().navigate(MainFragmentDirections.actionMainFragmentToPreviewFragment())
    }

    @Preview(widthDp = 1280, heightDp = 800)
    @Composable
    fun MainFragmentScreen() {
        baseCompose.baseScreen.invoke()
    }

    @Preview(widthDp = 1280, heightDp = 40)
    @Composable
    fun TopBar() {
        IBKSTheme {
            Surface(Modifier.fillMaxSize()) {
                MainTopBar(
                    titleText = "TopbarText",
                    userId = "gdHong",
                    userName = "홍길동",
                    onAlarm = { },
                    onLogout = { }) {
                }
            }
        }
    }

    @Preview(widthDp = 1280, heightDp = 760)
    @Composable
    fun PreviewMainFragmentContent() {
        IBKSTheme {
            MainFragmentContent()
        }
    }

    @Composable
    private fun MainFragmentContent() {
        var onDestroy by remember { mutableStateOf(false) }

        DisposableEffect(key1 = onDestroy) {
            onDispose {
                onDestroy = true
            }
        }

        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                WebView(context).apply {
                    settings.apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        domStorageEnabled = true
                        javaScriptEnabled = true
                        javaScriptCanOpenWindowsAutomatically = true
                        setSupportMultipleWindows(true)
                        cacheMode = WebSettings.LOAD_NO_CACHE
                    }
                    addJavascriptInterface(
                        WebInterface(viewModel),
                        WEB_INTERFACE_NAME
                    )
                    webViewClient = WebViewClient()

                    webChromeClient = object : WebChromeClient() {

                        override fun onProgressChanged(view: WebView?, newProgress: Int) {
                            super.onProgressChanged(view, newProgress)
                            if (newProgress < 100) viewModel.loadingWebView() else viewModel.loadedWebView()
                        }

                        override fun onCreateWindow(
                            view: WebView?,
                            isDialog: Boolean,
                            isUserGesture: Boolean,
                            resultMsg: Message?
                        ): Boolean {
                            view ?: return false

                            if (isDialog) {
                                viewModel.showWebViewDialog(resultMsg)
                                return true
                            }

                            return super.onCreateWindow(
                                view,
                                isDialog,
                                isUserGesture,
                                resultMsg
                            )
                        }
                    }
                    // 쿠키에 토큰 설정
                    val url = String.format(
                        "${BuildConfig.PROTOCOL}://${BuildConfig.WEB_URL}:${BuildConfig.WEB_PORT}"
                    )
                    QLog.i("$url")
                    CookieManager.getInstance().setAcceptCookie(true)
//                    CookieManager.getInstance().setCookie(
//                        url,
//                        "Access-Token=${webViewInitData.access_token}"
//                    )
                    CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)

                    loadUrl(url)
                }
            }, update = { webView ->
                if (onDestroy) {
                    webView.destroy()
                    return@AndroidView
                }

                viewModel.webViewResponse?.let { response ->
                    Log.d(
                        "WebViewFragment",
                        "responseNative(\'${response.scriptFunName}\', \'${response.data}\')"
                    )
                    // 웹에서 네이티브 브릿지 함수를 재호출시 콜백이 늦게와서 WebViewResponse가 null로 되어 상태를 못받는 현상 생겨서 수정함.
                    viewModel.webViewResponse = null
                    webView.evaluateJavascript(
                        "responseNative(\'${response.scriptFunName}\', \'${response.data}\')",
                        null
                    )
                }
            }
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "${BuildConfig.PROTOCOL}://${BuildConfig.WEB_URL}:${BuildConfig.WEB_PORT}")
            GrayDialogButton(
                onClick = {goPreview() },
                text = "go녹음테스트"
            )
            GrayDialogButton(
                onClick = { goMain() },
                text = "go초기화면"
            )
        }
    }

    inner class WebInterface(
        private val mainViewModel: MainViewModel
    ) {
        /**
         * callLogout/ 로그아웃 호출
         */
        @JavascriptInterface
        fun logout() {
            QLog.i("logout: 호출됨.")
            goMain()
        }

        /**
         * testCall/ callback 테스트
         */
        @JavascriptInterface
        fun testCall(scriptFunName: String, json: String) {
            Toast.makeText(context, json, Toast.LENGTH_SHORT).show()
            mainViewModel.SendDataTest(scriptFunName, json)
        }
    }
}

// 테스트 : compose textsize에 dp 사용할 수 있는 함수.
@Composable
fun dpToSp(dp: Dp) = with(LocalDensity.current) { dp.toSp() }
