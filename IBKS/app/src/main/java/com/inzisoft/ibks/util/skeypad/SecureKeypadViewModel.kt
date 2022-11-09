package com.inzisoft.ibks.util.skeypad

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inzisoft.ibks.BuildConfig
import com.inzisoft.ibks.data.remote.converter.CryptoService
import com.nprotect.keycryptm.Defines
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SecureKeypadViewModel @Inject constructor(private val cryptoService: CryptoService) :
    ViewModel() {
    var title by mutableStateOf("")
    var placeholder by mutableStateOf("")
    var keypadType by mutableStateOf(Defines.KEYPAD_TYPE_QWERTY)
    var minLength by mutableStateOf(0)
    var maxLength by mutableStateOf(0)

    var input by mutableStateOf("")

    fun onInputChange(input: String) {
        this.input = input
    }

    fun encrypt(data: ByteArray?, result: (ByteArray?) -> Unit) =
        viewModelScope.launch(Dispatchers.Default) {
            val ret =
                if (data == null) null else {
                    if (BuildConfig.ENCRYPT_KEYPAD) {
                        cryptoService.encrypt(data, true)
                    } else {
                        data
                    }
                }

            viewModelScope.launch(Dispatchers.Main) {
                result(ret)
            }
        }
}