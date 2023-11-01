package com.uyeong.featuredev

import android.app.Application
import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

val AndroidViewModel.context: Context
    get() = getApplication<Application>().applicationContext

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val contactsExtraction = ContactsExtraction(context)
    var webViewControl by mutableStateOf<WebViewControl>(WebViewControl.None)
    var url by mutableStateOf("http://61.109.169.171:9001/")

    fun exportContactsToJSONAndVcf() {
        val contactsJSON = contactsExtraction.exportContactsToVcf()

        sendWebViewResponse(ok = true, data = contactsJSON)
    }

    fun testAPI(uuid: String, json: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = Json.decodeFromString<TestAPIJSON>(json)

            // n초 대기 후에 "Hello, World!"를 출력합니다.
            delay(result.second.toLong() * 1000)

            if (result.bool) {
                sendWebViewResponse(uuid = uuid, ok = true, data = TestAPIJSONResponse(true, "trueString~~"))
            } else {
                sendWebViewResponse<Unit>(uuid = uuid, ok = false, message = "falseMessage~~~~~")
            }
            withContext(Dispatchers.Main) {
                Toast.makeText(
                    context,
                    "testAPI 함수 호출 완료 / ${result.second}초 경과 / ${result.bool}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private inline fun <reified T> sendWebViewResponse(
        ok: Boolean,
        data: T? = null,
        message: String? = null,
        uuid: String? = null
    ) {
        val response = if(uuid.isNullOrBlank()){
            Json.encodeToString(WebViewResponse(ok, data, message))
        } else {
            Json.encodeToString(WebViewResponseUUID(uuid, WebViewResponse(ok, data, message)))
        }

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

// UUID TEST
@Serializable
data class WebViewResponseUUID<T>(val UUID: String, val json: WebViewResponse<T>)

@Serializable
data class WebViewResponse<T>(val ok: Boolean, val data: T? = null, val message: String? = null)

@Serializable
data class TestAPIJSON(val bool: Boolean, val second: Int = 0)

@Serializable
data class TestAPIJSONResponse(val test1: Boolean, val test2: String)