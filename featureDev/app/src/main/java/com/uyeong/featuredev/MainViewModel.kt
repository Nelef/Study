package com.uyeong.featuredev

import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

val AndroidViewModel.context: Context
    get() = getApplication<Application>().applicationContext

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val contactsExtraction = ContactsExtraction(context)
    var webViewControl by mutableStateOf<WebViewControl>(WebViewControl.None)

    var url by mutableStateOf("http://61.109.169.166:9001/") // 장우영
//    var url by mutableStateOf("http://61.109.169.126:9001/") // 신영난
//    var url by mutableStateOf("http://61.109.169.171:9001/") // 최영수
//    var url by mutableStateOf("http://61.109.146.195:9001/") // 강민우

    val showDialogLiveData = MutableLiveData<String>()

    fun showAlertDialog(json: String) {
        // LiveData에 데이터를 설정하여 액티비티 또는 프래그먼트에 알립니다.
        showDialogLiveData.postValue(json)
    }

    fun exportContactsToJSONAndVcf() {
        val contactsJSON = contactsExtraction.exportContactsToVcf()

        sendWebViewResponse(ok = true, data = contactsJSON)
    }

    fun testAPI(json: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = Json.decodeFromString<TestAPIJSON>(json)

            // n초 대기 후에 "Hello, World!"를 출력합니다.
            delay(result.second.toLong() * 1000)

            if (result.bool) {
                sendWebViewResponse(ok = true, data = TestAPIJSONResponse(true, "trueString~~"))
            } else {
                sendWebViewResponse<Unit>(ok = false, message = "falseMessage~~~~~")
            }
            showDialogLiveData.postValue("testAPI 함수 호출 완료 / ${result.second}초 경과 / ${result.bool}")
        }
    }

    fun getAppVersion() {
        try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            sendWebViewResponse(ok = true, data = AppVersion(os = "android", version = packageInfo.versionName))
        } catch (e: PackageManager.NameNotFoundException) {
            Log.e("getAppVersion", e.toString())
            sendWebViewResponse<Unit>(ok = false, message = "android 앱 버전 불러오기 실패")
        }
    }

    private inline fun <reified T> sendWebViewResponse(
        ok: Boolean,
        data: T? = null,
        message: String? = null
    ) {
        val response = Json.encodeToString(WebViewResponse(ok, data, message))
        webViewControl = WebViewControl.JavaScript(response)
    }
}

sealed class WebViewControl {
    object None : WebViewControl()
    object Back : WebViewControl()
    object Forward : WebViewControl()
    object Refresh : WebViewControl()
    data class JavaScript(val response: String) : WebViewControl()
    object NewCookie : WebViewControl()
}

@Serializable
data class WebViewResponse<T>(val ok: Boolean, val data: T? = null, val message: String? = null)

@Serializable
data class TestAPIJSON(val bool: Boolean, val second: Int = 0)

@Serializable
data class TestAPIJSONResponse(val test1: Boolean, val test2: String)

@Serializable
data class AppVersion(val os: String, val version: String)