package com.inzisoft.ibks.view.fragment

import android.app.Activity
import android.content.Intent
import android.os.Message
import android.util.Log
import android.view.ViewGroup
import android.webkit.*
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.clearFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import com.inzisoft.ibks.*
import com.inzisoft.ibks.Constants.KEY_DOC_CAMERA_DATA
import com.inzisoft.ibks.Constants.KEY_IMAGE_DIR_PATH
import com.inzisoft.ibks.Constants.KEY_PREVIEW_DOC_TYPE
import com.inzisoft.ibks.Constants.KEY_SCRIPT_FUN_NAME
import com.inzisoft.ibks.R
import com.inzisoft.ibks.base.*
import com.inzisoft.ibks.data.web.*
import com.inzisoft.ibks.util.log.QLog
import com.inzisoft.ibks.util.skeypad.SecureKeyPad.finish
import com.inzisoft.ibks.util.skeypad.SecureKeypadActivity
import com.inzisoft.ibks.view.compose.*
import com.inzisoft.ibks.view.compose.theme.IBKSTheme
import com.inzisoft.ibks.view.dialog.WritePenDialogFragmentDirections
import com.inzisoft.ibks.viewmodel.MainViewModel
import com.inzisoft.ibks.viewmodel.WebViewControl
import com.inzisoft.ibks.viewmodel.WebViewDialogState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONObject


@AndroidEntryPoint
class MainFragment : BaseFragment() {

    companion object {
        private const val WEB_INTERFACE_NAME = "WebInterface"
    }

    private val viewModel: MainViewModel by viewModels()

    private val portraitActivity: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { activityResult ->
        when (activityResult.resultCode) {
            Activity.RESULT_OK -> {
                activityResult.data?.apply {
                    val scriptFunName = getStringExtra(KEY_SCRIPT_FUN_NAME)
                    val docCameraResultData: DocCameraResultData =
                        getSerializableExtra("docCameraResult") as DocCameraResultData
                    viewModel.webViewControl = WebViewControl.JavaScript(
                        WebViewResponse(
                            scriptFunName!!,
                            Gson().toJson(docCameraResultData)
                        )
                    )
                }
            }
            else -> {

            }
        }
    }

    init {
        WebView.setWebContentsDebuggingEnabled(BuildConfig.DEBUG)
        baseCompose.content = {
            when (val loadData = viewModel.webViewLoadInitData) {
                is UiState.Error -> {
                    showAlertDialog(
                        contentText = R.string.load_application_info_fail,
                        rightBtnText = R.string.confirm,
                        onDismissRequest = {
                            viewModel.finish()
                        }
                    )
                }
                is UiState.Success -> {
                    MainFragmentContent(loadData.data)
                }
                else -> {}
            }
            MainBackHandler()

            FloatingRecordButton(
                showFloatingRecordButton = (activity as MainActivity).viewModel.showFloatingRecordButton,
                recordState = (activity as MainActivity).viewModel.recordState.value,
                recordTime = (activity as MainActivity).viewModel.recordTime.value,
                recordFileList = (activity as MainActivity).viewModel.recordList,
                onRecord = { (activity as MainActivity).recordStart() },
                onResume = { (activity as MainActivity).recordResume() },
                onPause = { (activity as MainActivity).recordPause() },
                onStop = {
                    showAlertDialog(
                        contentText = R.string.alert_finish_record,
                        leftBtnText = R.string.cancel,
                        rightBtnText = R.string.finish,
                        onDismissRequest = { state ->
                            if (state == Right) {
                                (activity as MainActivity).recordStop()
                                viewModel.webViewState = UiState.None
                            }
                        }
                    )
                },
                onRecordList = { (activity as MainActivity).viewModel.updateRecordList() }
            )
        }

        baseCompose.surface = {
            hideKeyboard()

            if (viewModel.isFinish) finish()

            when (val uiState = viewModel.webViewState) {
                is UiState.Loading -> {
                    if (uiState.message == null) {
                        Loading()
                    } else {
                        LoadingPopup(message = uiState.message)
                    }
                }
                is UiState.Error -> {
                    showAlertDialog(
                        contentText = uiState.message,
                        rightBtnText = getString(R.string.confirm),
                        onDismissRequest = {
                            viewModel.webViewState = UiState.None
                        }
                    )
                }
                else -> {}
            }

            // FIXME: 테스트 코드 제거 해야함
            TestElectronicPopup(
                visible = viewModel.dialogState == WebViewDialogState.TestElectronicDoc,
                onClosed = {
                    viewModel.dialogState = WebViewDialogState.None
                },
                onConfirm = {
                    viewModel.showElectronicDocInputMode("", it, false)
                }
            )

            when (val state = viewModel.dialogState) {
                is WebViewDialogState.WebViewDialog -> WebViewDialog(
                    resultMsg = state.resultMessage,
                    cookieData = viewModel.cookieData
                ) {
                    viewModel.dialogState = WebViewDialogState.None
                }
                is WebViewDialogState.SecureKeyPad -> ShowSecureKeypad(state)
                is WebViewDialogState.WritePen -> showWritePenDialog(state)
                is WebViewDialogState.OcrCamera -> showOcrCamera(state)
                is WebViewDialogState.NormalAuthCamera -> showNormalAuthCamera(state)
                is WebViewDialogState.DocCamera -> showDocCamera(state)
                is WebViewDialogState.PreviewDoc -> showPreviewDoc(state)
                is WebViewDialogState.OpenSetting -> showSetting()
                is WebViewDialogState.Instruction -> showInstruction(state)
//                is WebViewDialogState.ElectronicDoc -> {
//                    if (BuildConfig.DEBUG || BuildConfig.TEST_BUTTON) {
//                        ShowWebData(state = state)
//                    } else {
//                        showElectronicDoc(state)
//                    }
//                }
                is WebViewDialogState.ElectronicInputDoc -> {
                    if (BuildConfig.DEBUG || BuildConfig.TEST_BUTTON) {
                        ShowWebData(state = state)
                    } else {
                        showElectronicDocInputMode(state)
                    }
                }
                is WebViewDialogState.ElectronicPreviewDoc -> showElectronicDocPreviewMode(state)
                else -> {}
            }
        }
    }

    @Composable
    private fun MainBackHandler() {
        BackHandler(enabled = false, onBack = {
            viewModel.onBack()
        })
    }

    override fun onDestroyView() {
        clearFragmentResultListener(FragmentRequest.WritePen.key)
        super.onDestroyView()
    }

    private fun goMain() {
        (activity as MainActivity).recordCancel()
        navigate(MainFragmentDirections.actionMainFragmentToLoginFragment())
    }

    @Preview(widthDp = 1280, heightDp = 800)
    @Composable
    fun MainFragmentScreen() {
        baseCompose.baseScreen.invoke()
    }

    @Preview(widthDp = 1280, heightDp = 60)
    @Composable
    fun TopBar() {
        IBKSTheme {
            Surface(Modifier.fillMaxSize()) {
                MainTopBar(
                    titleText = "TopbarText",
                    userId = "gdHong",
                    userName = "홍길동",
                    //onAlarm = { },
                    onLogout = { }) {
                }
            }
        }
    }

    @Composable
    private fun MainFragmentContent(webViewInitData: WebViewLoadInitData) {
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
                        "${BuildConfig.WEB_PROTOCOL}://${BuildConfig.WEB_URL}:${BuildConfig.WEB_PORT}"
                    )
                    QLog.i("$url")
                    CookieManager.getInstance().setAcceptCookie(true)
                    CookieManager.getInstance().setCookie(
                        "https://${BuildConfig.API_SERVER_URL}",
                        "${webViewInitData.cookie}"
                    )
                    CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)

                    loadUrl(url)
                }
            }, update = { webView ->
                if (onDestroy) {
                    webView.destroy()
                    return@AndroidView
                }

                when (val webViewControl = viewModel.webViewControl) {
                    WebViewControl.Back -> webView.goBack()
                    WebViewControl.Forward -> webView.goForward()
                    WebViewControl.None -> {}
                    WebViewControl.Refresh -> webView.reload()
                    is WebViewControl.JavaScript -> {
                        val response = webViewControl.response

                        Log.d(
                            "WebViewFragment",
                            "responseNative(\'${response.scriptFunName}\', \'${response.data}\')"
                        )
                        // 웹에서 네이티브 브릿지 함수를 재호출시 콜백이 늦게와서 webViewControl가 none으로 되어 상태를 못받는 현상 생겨서 수정함.
                        viewModel.webViewControl = WebViewControl.None
                        webView.evaluateJavascript(
                            "responseNative(\'${response.scriptFunName}\', \'${response.data}\')",
                            null
                        )
                    }
                }
            }
        )

        if (BuildConfig.DEBUG || BuildConfig.TEST_BUTTON) {
            var onTestButton by remember { mutableStateOf(false) }

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                ColorButton(onClick = { onTestButton = !onTestButton }, text = "테스트 버튼")
                if (onTestButton) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Black.copy(alpha = 0.5f)),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "웹 주소 : ${BuildConfig.WEB_PROTOCOL}://${BuildConfig.WEB_URL}:${BuildConfig.WEB_PORT} ",
                            color = Color.White
                        )
                        ColorButton(
                            onClick = { goMain() },
                            text = "go초기화면"
                        )
                        ColorButton(
                            onClick = {
                                lifecycleScope.launch {
                                    viewModel.generateEntryId("test", "{\"code\":\"INV\"}")

                                    delay(1000)
                                    viewModel.dialogState = WebViewDialogState.TestElectronicDoc
                                }
                            },
                            text = "전자문서"
                        )
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Black.copy(alpha = 0.5f)),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "촬영기능테스트 : ",
                            color = Color.White
                        )
                        ColorButton(
                            text = "신분증OCR",
                            onClick = {
                                viewModel.launchAuthCamera(
                                    "aa",
                                    "{\"cameraType\":\"oversea\", \"takeType\":\"ocr\", \"requireIssueDate\":\"Y\",\"requireIssueOffice\":\"Y\"}"
                                )
                            })
                        ColorButton(
                            text = "기타신분증",
                            onClick = {
                                viewModel.launchAuthCamera(
                                    "aa",
                                    "{\"cameraType\":\"foreign\", \"docCode\":\"foreign\", \"takeType\":\"normal\", \"requireIssueDate\":\"Y\",\"requireIssueOffice\":\"Y\"}"
                                )
                            })
                        ColorButton(
                            text = "문서촬영",
                            onClick = {
                                viewModel.launchDocCamera(
                                    "aa",
                                    "{\"docCode\":\"aaa\", \"docName\":\"aaaa\", \"takeCount\":3,\"maskingYn\":\"Y\"}"
                                )
                            })
                        ColorButton(
                            text = "미리보기",
                            onClick = {
                                viewModel.previewDoc(
                                    "aa",
                                    "{\"docCode\":\"aaa\", \"docName\":\"aaaa\"}"
                                )
                            })
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Black.copy(alpha = 0.5f)),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "입력기능테스트 : ",
                            color = Color.White
                        )
                        ColorButton(
                            text = "기명서명인감", onClick = {
                                viewModel.loadPen(
                                    "loadpen1",
                                    "{" +
                                            "\"type\":\"penseal\", " +
                                            "\"title\":\"기명/서명/인감\", " +
                                            "\"pen\": " +
                                            "{ " +
                                            "\"id\":\"NAME_1\", \"subtitle\":\"이름을 정자로 써주세요.\", \"placeholder\":\"기명\"" +
                                            " }," +
                                            "\"signPen\": " +
                                            "{ " +
                                            "\"id\":\"SIGN_1\", \"subtitle\":\"서명을 해주세요.\", \"placeholder\":\"서명!!!!\"" +
                                            " }," +
                                            "\"seal\": " +
                                            "{ " +
                                            // 현재 웹에는 서명과 인감 id가 같음 - 테스트코드 : SIGN_1
                                            "\"id\":\"SIGN_1\", \"subtitle\":\"인감 촬영을 해주세요.\", \"placeholder\":\"인감!!!!\"" +
                                            " }" +
                                            "}"
                                )
                            })
                        ColorButton(
                            text = "기명인감", onClick = {
                                viewModel.loadPen(
                                    "loadpen2",
                                    "{" +
                                            "\"type\":\"pensealonly\", " +
                                            "\"title\":\"기명/인감\", " +
                                            "\"pen\": " +
                                            "{ " +
                                            "\"id\":\"NAME_1\", \"subtitle\":\"이름을 정자로 써주세요.\", \"placeholder\":\"기명\"" +
                                            " }," +
                                            "\"seal\": " +
                                            "{ " +
                                            "\"id\":\"SEAL_1\", \"subtitle\":\"인감 촬영을 해주세요.\", \"placeholder\":\"인감!!!!\"" +
                                            " }" +
                                            "}"
                                )
                            })
                        ColorButton(
                            text = "따라쓰기",
                            onClick = {
                                viewModel.loadPen(
                                    "loadpen3",
                                    "{" +
                                            "\"type\":\"pen\", " +
                                            "\"title\":\"따라쓰기\", " +
                                            "\"pen\": " +
                                            "{ " +
                                            "\"id\":\"DDARA\", \"subtitle\":\"따라쓰기를 해주세요.\", \"placeholder\":\"따라쓰기 입니다.\"" +
                                            " }" +
                                            "}"
                                )
                            })
                        ColorButton(
                            text = "인감",
                            onClick = {
                                viewModel.loadPen(
                                    "seal1",
                                    "{" +
                                            "\"type\":\"seal\", " +
                                            "\"title\":\"인감\", " +
                                            "\"seal\": " +
                                            "{ " +
                                            "\"id\":\"SEAL11\", " +
                                            "\"subtitle\":\"인감을 촬영해 주세요.\"" +
                                            " }" +
                                            "}"
                                )
                            }
                        )
                        ColorButton(
                            text = "보안키패드",
                            onClick = {
                                viewModel.showSecureKeyPad(
                                    "secure",
                                    "{\n" +
                                            "    \"title\": \"주민번호 뒷자리 7자리\",\n" +
                                            "    \"isNumber\": true,\n" +
                                            "    \"placeholder\": \"주민번호 뒷자리를 입력해주세요.\",\n" +
                                            "    \"maxLength\": 7\n" +
                                            "}"
                                )
                            }
                        )
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Black.copy(alpha = 0.5f)),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "전자문서테스트 : ",
                            color = Color.White
                        )
                        ColorButton(
                            text = "핵심설명서",
                            onClick = {
                                viewModel.showInstruction(
                                    "instruction",
                                    "{\n" +
                                            "\t\"title\" : \"핵심\",\n" +
                                            "\t\"productCode\" : \"KRZF06897DC4\"\n" +
                                            "}"
                                )
                            }
                        )

                        ColorButton(onClick = {
                              viewModel.downloadForms("download", "{\n" +
                                      "    \"docs\":\n" +
                                      "    [\n" +
                                      "        {\n" +
                                      "            \"order\": 0,\n" +
                                      "            \"docType\": \"SPC\",\n" +
                                      "            \"docTypeNm\": \"간이투자설명서\",\n" +
                                      "            \"formNm\": \"KR5301642546_간이투자설명서_미래에셋아시아퍼시픽업종대표증권자투자신탁1호(주식)_(20221229)\",\n" +
                                      "            \"formCd\": \"T0000024\",\n" +
                                      "            \"formDir\": \"report\"\n" +
                                      "        }\n" +
                                      "    ]\n" +
                                      "}")
                        }, text = "상품다운로드")

                        ColorButton(
                            text = "new전자문서",
                            onClick = {
                                lifecycleScope.launch {
                                    viewModel.generateEntryId(
                                        "test",
                                        "{\"code\":\"PRD\", \"subCode\":\"PR002\"}"
                                    )

                                    delay(1000)

                                    viewModel.showElectronicDocInputMode(
                                        "newPaperlessView",
                                        "{\n" +
                                                "  \"isSend\":true,\n" +
                                                "  \"electronicDocInfo\": [\n" +
                                                // 첫번째 탭
                                                "    {\n" +
                                                "      \"openType\": \"normal\",\n" +
                                                "      \"businessLogic\": \"PR001_01\",\n" +
                                                "      \"title\": \"상품신청서\",\n" +
                                                "      \"appendForm\": [\"ZPR00101\",\"ZPRD0001\"],\n" +
                                                "      \"data\": {\n" +
                                                "        \"cus_invest_type_no\": \"3\",\n" +
                                                "        \"cus_invest_type\": \"위험중립형\",\n" +
                                                "        \"cus_account\": \"111112222\",\n" +
                                                "        \"prod_nm\": \"종목명\",\n" +
                                                "        \"order_cnt\": \"109\",\n" +
                                                "        \"order_price\": \"100000\"\n" +
                                                "      }\n" +
                                                "    }\n" +
                                                // 두번째 탭
                                                "    , {\n" +
                                                "      \"openType\": \"normal\",\n" +
                                                "      \"businessLogic\": \"PR002_01\",\n" +
                                                "      \"title\": \"테스트\",\n" +
                                                "      \"appendForm\": [\"ZPR00101\",\"ZPRD0001\"],\n" +
                                                "      \"data\": {\n" +
                                                "        \"cus_invest_type_no\": \"3\",\n" +
                                                "        \"cus_invest_type\": \"위험중립형\",\n" +
                                                "        \"cus_account\": \"111112222\",\n" +
                                                "        \"prod_nm\": \"종목명\",\n" +
                                                "        \"order_cnt\": \"109\",\n" +
                                                "        \"order_price\": \"100000\"\n" +
                                                "      }\n" +
                                                "    }\n" +
                                                "  ],\n" +
                                                "  \"addition\": {\n" +
                                                "    \"exData\": {\n" +
                                                "      \"quest1\": \"1\",\n" +
                                                "      \"quest2\": \"1\"\n" +
                                                "    },\n" +
                                                "    \"info\": {\n" +
                                                "      \"appCd\": \"ODS\",\n" +
                                                "      \"bzwkDvcd\": \"PRD\",\n" +
                                                "      \"subBzwkDvcd\": \"PR001\",\n" +
                                                "      \"dsrbCd\": \"GG00601\",\n" +
                                                "      \"dsrbNm\": \"IBK WM센터 목동\",\n" +
                                                "      \"hndrNo\": \"201108\",\n" +
                                                "      \"hndrNm\": \"홍길동\",\n" +
                                                "      \"index01\": \"고길동\",\n" +
                                                "      \"index02\": \"12345\",\n" +
                                                "      \"index03\": \"01010016903\",\n" +
                                                "      \"index04\": \"01012341234\",\n" +
                                                "      \"index05\": \"19830101\",\n" +
                                                "      \"index06\": \"abc@inzisoft.com\"\n" +
                                                "    }\n" +
                                                "  }\n" +
                                                "}",
                                        false
                                    )
                                }
                            }
                        )

                        ColorButton(
                            text = "최종전자문서",
                            onClick = {
                                viewModel.showElectronicDocPreviewMode(
                                    "newPaperlessPreview",
                                    "{\n" +
                                            " \"docs\": [\n" +
                                            "    {\n" +
                                            "      \"code\": \"aaa\",\n" +
                                            "      \"name\": \"증빙\",\n" +
                                            "      \"count\":2\n" +
                                            "    }\n" +
                                            "   ]," +
                                            "    \"info\": {\n" +
                                            "      \"appCd\": \"ODS\",\n" +
                                            "      \"bzwkDvcd\": \"PRD\",\n" +
                                            "      \"subBzwkDvcd\": \"PR001\",\n" +
                                            "      \"dsrbCd\": \"GG00601\",\n" +
                                            "      \"dsrbNm\": \"IBK WM센터 목동\",\n" +
                                            "      \"hndrNo\": \"201108\",\n" +
                                            "      \"hndrNm\": \"홍길동\",\n" +
                                            "      \"index01\": \"고길동\",\n" +
                                            "      \"index02\": \"12345\",\n" +
                                            "      \"index03\": \"01010016903\",\n" +
                                            "      \"index04\": \"01012341234\",\n" +
                                            "      \"index05\": \"19830101\",\n" +
                                            "      \"index06\": \"abc@inzisoft.com\"\n" +
                                            "    }\n" +
                                            "}"
                                )
                            }
                        )
                    }
                }
            }
        }
    }

    private fun showOcrCamera(state: WebViewDialogState.OcrCamera) {
        viewModel.dialogState = WebViewDialogState.None
        setFragmentResultListener(FragmentRequest.OcrCamera) { scriptFunName, result ->
            clearFragmentResultListener(FragmentRequest.OcrCamera.key)
            when (result) {
                is FragmentResult.Cancel -> {}
                is FragmentResult.Error -> {}
                is FragmentResult.OK -> {
                    viewModel.sendAuthDataToWeb(scriptFunName)
                }
            }
        }

        navigate(
            MainFragmentDirections.actionMainFragmentToOcrCameraDialogFragment(
                state.scriptFunName,
                state.authCameraData
            )
        )
    }

    private fun showNormalAuthCamera(state: WebViewDialogState.NormalAuthCamera) {
        viewModel.dialogState = WebViewDialogState.None
        setFragmentResultListener(FragmentRequest.NormalAuthCamera) { scriptFunName, result ->
            clearFragmentResultListener(FragmentRequest.NormalAuthCamera.key)
            when (result) {
                is FragmentResult.Cancel -> {}
                is FragmentResult.Error -> {}
                is FragmentResult.OK -> {
                    viewModel.sendNormalAuthDataToWeb(scriptFunName, state.authCameraData)
                }
            }
        }

        navigate(
            MainFragmentDirections.actionMainFragmentToNormalAuthCameraDialogFragment(
                state.scriptFunName,
                state.authCameraData
            )
        )
    }

    private fun showDocCamera(state: WebViewDialogState.DocCamera) {
        val intent = Intent(requireActivity(), PortraitCameraActivity::class.java)
        intent.putExtra(KEY_SCRIPT_FUN_NAME, state.scriptFunName)
        intent.putExtra(KEY_DOC_CAMERA_DATA, state.docCameraData)
        intent.putExtra(KEY_PREVIEW_DOC_TYPE, PreviewDocType.TAKE_DOC)
        portraitActivity.launch(intent)
        viewModel.dialogState = WebViewDialogState.None
    }

    private fun showPreviewDoc(state: WebViewDialogState.PreviewDoc) {
        val intent = Intent(requireActivity(), PortraitCameraActivity::class.java)
        intent.putExtra(KEY_SCRIPT_FUN_NAME, state.scriptFunName)
        intent.putExtra(KEY_DOC_CAMERA_DATA, state.docCameraData)
        intent.putExtra(KEY_IMAGE_DIR_PATH, state.imageDirPath)
        intent.putExtra(KEY_PREVIEW_DOC_TYPE, PreviewDocType.PREVIEW_DOC)
        portraitActivity.launch(intent)
        viewModel.dialogState = WebViewDialogState.None
    }

    private fun showWritePenDialog(state: WebViewDialogState.WritePen) {
        setFragmentResultListener(FragmentRequest.WritePen) { scriptFunName, result ->
            clearFragmentResultListener(FragmentRequest.WritePen.key)

            when (result) {
                is FragmentResult.Cancel -> {}
                is FragmentResult.Error -> {}
                is FragmentResult.OK -> {
                    result.data?.let {
                        viewModel.completeWritePen(scriptFunName, it)
                    }
                }
            }
        }

        navigate(
            WritePenDialogFragmentDirections.actionGlobalWritePenDialogFragment(
                state.data,
                state.scriptFunName
            )
        )

        viewModel.dialogState = WebViewDialogState.None
    }

    @Composable
    private fun ShowWebData(state: WebViewDialogState) {
        if (state is WebViewDialogState.ElectronicInputDoc) {
            val json = with(state.json) {
                JSONObject(this).toString(4)
            }

            TextPopup(content = json,
                onCancel = { viewModel.dialogState = WebViewDialogState.None },
                onConfirm = { showElectronicDocInputMode(state) }
            )
        }
    }

    private fun showElectronicDocInputMode(state: WebViewDialogState.ElectronicInputDoc) {
        viewModel.dialogState = WebViewDialogState.None
        setFragmentResultListener(FragmentRequest.ElectronicInputDoc) { scriptFunName, json ->
            clearFragmentResultListener(FragmentRequest.ElectronicInputDoc.key)
            when (json) {
                is FragmentResult.Cancel -> {}
                is FragmentResult.Error -> {}
                is FragmentResult.OK -> {
                    json.data?.let {
                        viewModel.onResultElectronicDoc(scriptFunName, it)
                    }
                }
            }
        }

        navigate(
            MainFragmentDirections.actionMainFragmentToElectronicDocInputModeFragment(
                state.scriptFunName,
                state.json,
                state.isSend
            )
        )
    }

    private fun showElectronicDocPreviewMode(state: WebViewDialogState.ElectronicPreviewDoc) {
        viewModel.dialogState = WebViewDialogState.None
        setFragmentResultListener(FragmentRequest.ElectronicPreviewDoc) { scriptFunName, json ->
            clearFragmentResultListener(FragmentRequest.ElectronicPreviewDoc.key)
            when (json) {
                is FragmentResult.Cancel -> {}
                is FragmentResult.Error -> {}
                is FragmentResult.OK -> {
                    json.data?.let {
                        viewModel.onResultElectronicDoc(scriptFunName, it)
                    }
                }
            }
        }

        navigate(
            MainFragmentDirections.actionMainFragmentToElectronicDocPreviewModeFragment(
                state.scriptFunName,
                state.json
            )
        )
    }

    private fun showSetting() {
        viewModel.dialogState = WebViewDialogState.None
        navigate(MainFragmentDirections.actionMainFragmentToSettingFragment())
    }

    @Composable
    private fun ShowSecureKeypad(state: WebViewDialogState.SecureKeyPad) {

        val intent = with(state.secureKeyPadInfo) {
            Intent(context, SecureKeypadActivity::class.java)
                .putExtra(SecureKeypadActivity.ARGS_TITLE, title)
                .putExtra(SecureKeypadActivity.ARGS_KEYPAD_NUMBER, isNumber)
                .putExtra(SecureKeypadActivity.ARGS_PLACEHOLDER, placeholder)
                .putExtra(SecureKeypadActivity.ARGS_MIN_LENGTH, minLength)
                .putExtra(SecureKeypadActivity.ARGS_MAX_LENGTH, maxLength)
        }

        val launcher =
            rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    result.data?.run {
                        val str = getStringExtra(SecureKeypadActivity.RESULT_STR) ?: ""
                        val enc =
                            getByteArrayExtra(SecureKeypadActivity.RESULT_ENC) ?: byteArrayOf()
                        val decPart = getStringExtra(SecureKeypadActivity.RESULT_DEC_PART) ?: ""
                        viewModel.onResultSecureKeypad(state.scriptFunName, str, enc, decPart)
                    }
                }
                viewModel.dialogState = WebViewDialogState.None
            }

        SideEffect {
            launcher.launch(intent)
        }
    }

    private fun showInstruction(state: WebViewDialogState.Instruction) {
        setFragmentResultListener(FragmentRequest.Instruction) { scriptFunName, result ->
            clearFragmentResultListener(FragmentRequest.Instruction.key)

            when (result) {
                is FragmentResult.OK -> viewModel.onResultInstruction(scriptFunName, true)
                else -> viewModel.onResultInstruction(scriptFunName, false)
            }
        }

        navigate(
            MainFragmentDirections.actionMainFragmentToInstructionDialogFragment(
                state.scriptFunName,
                state.json
            )
        )

        viewModel.dialogState = WebViewDialogState.None
    }

    inner class WebInterface(
        private val mainViewModel: MainViewModel
    ) {

        /**
         * 페이지 정보 전달
         *
         * @param json
         */
        @JavascriptInterface
        fun notifyPage(json: String) {
            mainViewModel.notifyPage(json)
        }

        /**
         * callLogout/ 로그아웃 호출
         */
        @JavascriptInterface
        fun logout() {
            QLog.i("logout: 호출됨.")
            viewModel.logout {
                goMain()
            }
        }

        /**
         * 보안 키패드 호출
         *
         */
        @JavascriptInterface
        fun secureKeyPad(scriptFunName: String, json: String) {
            mainViewModel.showSecureKeyPad(scriptFunName, json)
        }

        /**
         * 신분증 인식 카메라 호출
         */
        @JavascriptInterface
        fun authCamera(scriptFunName: String, json: String) {
            Log.e("SW_DEBUG", "authCamera: $json")
            mainViewModel.launchAuthCamera(scriptFunName, json)
        }

        /**
         * 문서촬영
         */
        @JavascriptInterface
        fun docCamera(scriptFunName: String, json: String) {
            Log.e("SW_DEBUG", "docCamera: $json")
            viewModel.launchDocCamera(scriptFunName, json)
        }

        /**
         * 문서촬영 미리보기
         */
        @JavascriptInterface
        fun previewDoc(scriptFunName: String, json: String) {
            Log.e("SW_DEBUG", "previewDoc: $json")
            viewModel.previewDoc(scriptFunName, json)
        }

        /**
         * User Data 호출
         */
        @JavascriptInterface
        fun getUserInfo(scriptFunName: String) {
            mainViewModel.sendUserDataToWeb(scriptFunName)
        }

        /**
         * 핵심설명서
         *
         */
        @JavascriptInterface
        fun showInstruction(scriptFunName: String, json: String) {
            mainViewModel.showInstruction(scriptFunName, json)
        }

        /**
         * Setting 화면 호출
         */
        @JavascriptInterface
        fun openSetting() {
            mainViewModel.openSetting()
        }

        /**
         * 전자문서키 생성
         *
         * @param scriptFunName
         * @param json
         */
        @JavascriptInterface
        fun genEntryId(scriptFunName: String, json: String) {
            mainViewModel.generateEntryId(scriptFunName, json)
        }

        /**
         * 펜입력
         *
         * @param scriptFunName
         * @param json
         */

        @JavascriptInterface
        fun writePen(scriptFunName: String, json: String) {
            mainViewModel.loadPen(scriptFunName, json)
        }

        /**
         * 상품코드로 버전 체크 xml에서 서식 코드 조회
         *
         * @param scriptFunName
         * @param productCode   상품코드
         */
        @JavascriptInterface
        fun getFormList(scriptFunName: String, productCode: String) {
            mainViewModel.getFormList(scriptFunName, productCode)
        }

        /**
         * 전자문서 폼 삭제 호출
         *
         * @param scriptFunName
         * @param json
         */
        @JavascriptInterface
        fun clearFormList(scriptFunName: String, json: String) {
            mainViewModel.clearFormList(scriptFunName, json)
        }

        /**
         * Fund docs 호출
         */
        @JavascriptInterface
        fun getDocsList(scriptFunName: String, prdCd: String) {
            mainViewModel.getDocsList(scriptFunName, prdCd)
        }

        /**
         * 서식 다운로드
         *
         * @param scriptFunName
         * @param json
         */
        @JavascriptInterface
        fun downloadForms(scriptFunName: String, json: String) {
            mainViewModel.downloadForms(scriptFunName, json)
        }

        /**
         * 전자문서 호출
         *
         * @param scriptFunName
         * @param json
         */
        @JavascriptInterface
        fun openElectronicDoc(scriptFunName: String, json: String, isSend: Boolean) {
            mainViewModel.showElectronicDocInputMode(scriptFunName, json, isSend)
        }

        /**
         * 전자문서 최종 호출
         *
         * @param scriptFunName
         * @param json
         */
        @JavascriptInterface
        fun previewElectronicDoc(scriptFunName: String, json: String) {
            mainViewModel.showElectronicDocPreviewMode(scriptFunName, json)
        }

        /**
         * 녹취 시작 호출
         *
         * @param scriptFunName
         * @param json
         */
        @JavascriptInterface
        fun recordStart(scriptFunName: String, json: String) {
            val recordData = Gson().fromJson(json, RecordDataList::class.java)

            (activity as MainActivity).viewModel.webRecords = recordData.records
            (activity as MainActivity).viewModel.getEntryId()

            // 녹취이 실행중이지 않을 때에만 녹취 시작
            if (!(activity as MainActivity).viewModel.showFloatingRecordButton) {
                showAlertDialog(
                    contentText = R.string.start_record,
                    rightBtnText = R.string.confirm_record_start,
                    onDismissRequest = {
                        (activity as MainActivity).viewModel.onRecordCreate()
                        (activity as MainActivity).recordStart()

                        viewModel.webViewControl = WebViewControl.JavaScript(
                            WebViewResponse(scriptFunName, Gson().toJson(true))
                        )
                    }
                )
            }
        }

        /**
         * 녹취 종료 호출
         *
         * @param scriptFunName
         * @param json
         */
        @JavascriptInterface
        fun recordEnd() {
            (activity as MainActivity).recordCancel()
        }
    }
}

// 테스트 : compose textsize에 dp 사용할 수 있는 함수.
@Composable
fun dpToSp(dp: Dp) = with(LocalDensity.current) { dp.toSp() }
