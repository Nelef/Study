package com.inzisoft.ibks.viewmodel

import android.os.Message
import android.widget.Toast
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.inzisoft.ibks.PathManager
import com.inzisoft.ibks.base.BaseViewModel
import com.inzisoft.ibks.base.UiState
import com.inzisoft.ibks.data.web.WebViewResponse
import com.inzisoft.ibks.util.log.QLog
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val pathManager: PathManager
) : BaseViewModel() {
    var webViewState by mutableStateOf<UiState<Unit>>(UiState.None)
    var dialogState by mutableStateOf<WebViewDialogState>(WebViewDialogState.None)
    var webViewResponse by mutableStateOf<WebViewResponse?>(null)

    private var _userId = mutableStateOf("")
    val userId: State<String> = _userId

    private var _userName = mutableStateOf("")
    val userName: State<String> = _userName

    init {
        _userId.value = "test_userId"
        _userName.value = "test_userName"

        // TODO localRepository getUserInfo
//        viewModelScope.launch(Dispatchers.IO) {
//            FileUtils.delete(pathManager.getCacheDir())
//
//            val userInfo = localRepository.getUserInfo().first()
//            _userId.value = userInfo.userId
//            _userName.value = userInfo.userNm
//
//            switchBoard(MainListBoardFilter.SaveTempFilter)
//        }
    }

    fun loadingWebView() {
        webViewState = UiState.Loading()
    }

    fun loadedWebView() {
        webViewState = UiState.None
    }

    fun showWebViewDialog(resultMessage: Message?) {
        dialogState = WebViewDialogState.WebViewDialog(resultMessage)
    }

//    fun logout(scriptFunName: String, json: String) =
//        viewModelScope.launch(Dispatchers.IO) {
//            dialogState = WebViewDialogState.PreviewDoc(scriptFunName, docCameraData, imageDir)
//        }

    fun SendDataTest(scriptFunName: String, json: String) = viewModelScope.launch(Dispatchers.IO) {
        QLog.i("testCall: $json")

        val json = JSONObject()
        json.put("source", "test")

        webViewResponse = WebViewResponse(scriptFunName, json.toString())
    }
}

sealed class WebViewDialogState {
    object None : WebViewDialogState()
    data class Error(val error: Exception) : WebViewDialogState()
    data class ShowAlertDialog(val message: String) : WebViewDialogState()
    data class SendDataTest(val scriptFunName: String, val json: String) : WebViewDialogState()
    data class ShowPdf(val scriptFunName: String, val json: String) : WebViewDialogState()
    data class WebViewDialog(val resultMessage: Message?) : WebViewDialogState()
    data class AlternativeNum(val scriptFunName: String, val json: String) : WebViewDialogState()
    data class Preview(val scriptFunName: String, val json: String) : WebViewDialogState()
}