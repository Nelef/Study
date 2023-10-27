package com.uyeong.featuredev

import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

val AndroidViewModel.context: Context
    get() = getApplication<Application>().applicationContext

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val contactsExtraction = ContactsExtraction(context)
    var webViewControl by mutableStateOf<WebViewControl>(WebViewControl.None)
    var url by mutableStateOf("http://61.109.169.166:9001/")

    fun exportContactsToJSONAndVcf() {
        val contactsJSON = contactsExtraction.exportContactsToVcf()

        sendWebViewResponse(ok = true, data = contactsJSON)
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
data class Test(val test: String)