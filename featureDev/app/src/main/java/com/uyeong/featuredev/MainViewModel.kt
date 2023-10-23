package com.uyeong.featuredev

import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel

val AndroidViewModel.context: Context
    get() = getApplication<Application>().applicationContext

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val contactsExtraction = ContactsExtraction(context)
    var webViewControl by mutableStateOf<WebViewControl>(WebViewControl.None)
    var url by mutableStateOf("http://61.109.169.166:9001/")

    fun exportContactsToJSONAndVcf() {
        val contactsJSON = contactsExtraction.exportContactsToVcf()
        webViewControl = WebViewControl.JavaScript(WebViewResponse("ok", contactsJSON, null))
    }
}

sealed class WebViewControl {
    object None : WebViewControl()
    object Back : WebViewControl()
    object Forward : WebViewControl()
    object Refresh : WebViewControl()
    data class JavaScript(val response: WebViewResponse) : WebViewControl()
    object NewCookie : WebViewControl()
}

data class WebViewResponse(val ok: String, val data: String?, val message: String?)
