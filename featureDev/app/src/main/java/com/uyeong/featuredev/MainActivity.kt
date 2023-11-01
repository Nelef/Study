package com.uyeong.featuredev

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.uyeong.featuredev.ui.theme.FeatureDevTheme


class MainActivity : ComponentActivity() {
    private val PERMISSION_REQUEST_CODE = 100
    private lateinit var viewModel: MainViewModel

    companion object {
        private const val WEB_INTERFACE_NAME = "WebInterface"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ViewModel 초기화
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        setContent {
            FeatureDevTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
                ) {
                    TestWeb(viewModel.url) { newUrl ->
                        viewModel.url = newUrl
                    }
                    TestUI {
                        if (hasContactsPermission()) {
                            viewModel.exportContactsToJSONAndVcf()
                        } else {
                            requestContactsPermission()
                        }
                    }
                }
            }
        }

    }

    private fun hasContactsPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this, Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestContactsPermission() {
        ActivityCompat.requestPermissions(
            this, arrayOf(Manifest.permission.READ_CONTACTS), PERMISSION_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            viewModel.exportContactsToJSONAndVcf()
        } else {
            Toast.makeText(this, "권한이 필요합니다.", Toast.LENGTH_SHORT).show()
        }
    }

    // 테스트 버튼
    @Composable
    fun TestUI(onClick: () -> Unit) {
        var onTestButton by remember { mutableStateOf(false) }

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(30.dp))
            if (!onTestButton)
                Button(
                    onClick = { onTestButton = !onTestButton },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Black.copy(alpha = 0.2F)
                    )
                ) {
                    Text(text = "▼")
                }
            else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Button(
                        onClick = { onTestButton = !onTestButton },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Black.copy(alpha = 0.2F)
                        )
                    ) {
                        Text(text = "▲")
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(onClick = { onClick() }) {
                        Text(text = "주소록 획득")
                    }
                }
            }
        }
    }

    // 웹 화면
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun TestWeb(url: String, changeUrl: (newUrl: String) -> Unit) {
        // WebView 인스턴스를 remember를 통해 저장합니다.
        val webView = remember { WebView(this) }

        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    modifier = Modifier.weight(1f),
                    value = url,
                    onValueChange = { changeUrl(it) })
                Button(
                    onClick = { webView.loadUrl(viewModel.url) }) {
                    Text(text = "이동")
                }
            }
            Card {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = {
                        webView.apply {
                            settings.apply {
                                userAgentString += " saleskit_android_app";

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
                                WebInterface(),
                                WEB_INTERFACE_NAME
                            )

                            webViewClient = WebViewClient()

                            webChromeClient = object : WebChromeClient() {

                                override fun onProgressChanged(
                                    view: WebView?,
                                    newProgress: Int
                                ) {
                                    super.onProgressChanged(view, newProgress)
                                    if (newProgress < 100) {
                                        viewModel.url += "로딩중"
                                    } else {
                                        viewModel.url = view?.url.toString()
                                    }
                                }

                            }
                            // url 설정
                            loadUrl(url)
                        }
                    }, update = { webView ->
                        when (val webViewControl = viewModel.webViewControl) {
                            is WebViewControl.JavaScript -> {
                                val response = webViewControl.response

                                Log.d(
                                    "webViewResponse",
                                    "responseNative(\'${response}\')"
                                )
                                // 웹에서 네이티브 브릿지 함수를 재호출시 콜백이 늦게와서 webViewControl가 none으로 되어 상태를 못받는 현상 생겨서 수정함.
                                viewModel.webViewControl = WebViewControl.None

                                webView.evaluateJavascript(
                                    "responseNative(${response})",
                                    null
                                )
                            }

                            else -> {}
                        }
                    }
                )
            }
        }
    }

    inner class WebInterface {

        @JavascriptInterface
        fun contactsExtraction() {
            viewModel.exportContactsToJSONAndVcf()
        }

        @JavascriptInterface
        fun testAPI(uuid: String, json: String) {
            Log.d("uuid TEST", "uuid: $uuid, json: $json")
            viewModel.testAPI(uuid, json)
        }
    }

    @Preview(showBackground = true)
    @Composable
    fun Preview() {
        FeatureDevTheme {
            Column {
                TestUI {}
            }
        }
    }
}