package com.uyeong.featuredev

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.PermissionRequest
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
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
    private val CONTACTS_PERMISSION_REQUEST_CODE = 101
    private val CAMERA_PERMISSION_REQUEST_CODE = 102
    private val STORAGE_PERMISSION_REQUEST_CODE = 103
    private lateinit var viewModel: MainViewModel

    // 파일 업로드
    private var uploadMessage: ValueCallback<Array<Uri>>? = null
    private lateinit var contentActivityResultLauncher: ActivityResultLauncher<Intent>

    companion object {
        private const val WEB_INTERFACE_NAME = "WebInterface"
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ViewModel 초기화
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        viewModel.showDialogLiveData.observe(this) { json -> showAlertDialog(json) }

        setContent {
            FeatureDevTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
                ) {
                    TestWeb(viewModel.url) { newUrl -> viewModel.url = newUrl }
                    TestUI()
                }
            }
        }

        // WebView 파일 업로드와 관련된 초기 설정 코드
        contentActivityResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result: ActivityResult ->
            if (result.resultCode == AppCompatActivity.RESULT_OK && result.data != null) {
                uploadMessage?.onReceiveValue(
                    WebChromeClient.FileChooserParams.parseResult(
                        result.resultCode,
                        result.data
                    )
                )
            } else {
                uploadMessage?.onReceiveValue(null)
            }
            uploadMessage = null
        }

    }

    private fun hasContactsPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this, Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun hasCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun hasStoragePermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this, Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestContactsPermission() {
        ActivityCompat.requestPermissions(
            this, arrayOf(Manifest.permission.READ_CONTACTS), CONTACTS_PERMISSION_REQUEST_CODE
        )
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(
            this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_REQUEST_CODE
        )
    }

    private fun requestStoragePermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
            STORAGE_PERMISSION_REQUEST_CODE
        )
    }

    // 퍼미션 요청 완료 후 하는 일
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // 주소록 권한 요청 후
            if (requestCode == CONTACTS_PERMISSION_REQUEST_CODE) {
                if (hasContactsPermission()) {
                    viewModel.exportContactsToJSONAndVcf()
                } else {
                    requestContactsPermission()
                }
            }
            // 카메라 권한 요청 후
            if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
                if (hasCameraPermission()) {
                    Toast.makeText(baseContext, "카메라 권한 획득 완료.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(baseContext, "카메라 권한 획득 요청", Toast.LENGTH_SHORT).show()
                    requestCameraPermission()
                }
            }
        } else {
            Toast.makeText(this, "권한이 필요합니다.", Toast.LENGTH_SHORT).show()
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    // 테스트 버튼
    @Composable
    fun TestUI() {
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
                    Button(onClick = {
                        if (hasContactsPermission()) {
                            viewModel.exportContactsToJSONAndVcf()
                        } else {
                            requestContactsPermission()
                        }
                    }) {
                        Text(text = "주소록 획득")
                    }
                    Button(onClick = { viewModel.showAlertDialog("test") }) {
                        Text(text = "다이얼로그 호출")
                    }
                    Button(onClick = {
                        if (hasCameraPermission()) {
                            Toast.makeText(baseContext, "카메라 권한 획득 완료.", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(baseContext, "카메라 권한 획득 요청", Toast.LENGTH_SHORT).show()
                            requestCameraPermission()
                        }
                    }) {
                        Text(text = "카메라 권한 획득")
                    }
                }
            }
        }
    }

    // 웹 화면
    @RequiresApi(Build.VERSION_CODES.O)
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

                            webChromeClient = object : WebChromeClient() {
                                override fun onPermissionRequest(request: PermissionRequest) {
                                    request.grant(request.resources)
                                }

                                // 파일 선택 핸들러
                                override fun onShowFileChooser(
                                    webView: WebView?,
                                    filePathCallback: ValueCallback<Array<Uri>>,
                                    fileChooserParams: FileChooserParams
                                ): Boolean {
                                    uploadMessage?.onReceiveValue(null)
                                    uploadMessage = filePathCallback

                                    val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                                        addCategory(Intent.CATEGORY_OPENABLE)
                                        type = "image/*"
                                    }

                                    contentActivityResultLauncher.launch(intent)
                                    return true
                                }

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
                            webViewClient = WebViewClient()

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

    private fun showAlertDialog(json: String) {
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setTitle("알림")
        alertDialogBuilder.setMessage("안드로이드 다이얼로그 예제입니다.\n$json")
        alertDialogBuilder.setPositiveButton("확인") { dialog, _ ->
            // 확인 버튼을 눌렀을 때 수행할 동작을 여기에 작성하세요.
            dialog.dismiss()
        }

        // 다이얼로그 표시
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    inner class WebInterface {

        // 테스트 API
        @JavascriptInterface
        fun testAPI(json: String) {
            viewModel.testAPI(json)
        }

        // 다이얼로그 호출
        @JavascriptInterface
        fun showAlertDialog(json: String) {
            Log.d("showAlertDialog", "showAlertDialog 호출 $json")
            viewModel.showAlertDialog(json)
        }

        // 주소록 호출
        @JavascriptInterface
        fun contactsExtraction(json: String) {
            if (hasContactsPermission()) {
                viewModel.exportContactsToJSONAndVcf()
            } else {
                requestContactsPermission()
            }
        }

        // 카메라 권한 호출
        @JavascriptInterface
        fun getCameraPermission(json: String) {
            if (hasCameraPermission()) {
                Toast.makeText(baseContext, "카메라 권한 획득 완료.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(baseContext, "카메라 권한 획득 요청", Toast.LENGTH_SHORT).show()
                requestCameraPermission()
            }
        }

    }

    @Preview(showBackground = true)
    @Composable
    fun Preview() {
        FeatureDevTheme {
            Column {
                TestUI()
            }
        }
    }
}