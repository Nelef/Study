package com.inzisoft.ibks.view.compose

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusState
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.inzisoft.ibks.util.skeypad.SecureKeypadActivity
import com.inzisoft.ibks.findActivity
import com.inzisoft.ibks.util.skeypad.SecureKeyPad
import com.nprotect.keycryptm.Defines
import kotlinx.coroutines.launch


@Composable
fun SecureInput(
    visibleValue: String = "",
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    placeholder: String? = null,
    errorMessage: String? = null,
    errorAlignment: TextAlign? = null,
    showErrorMessage: Boolean,
    keypadTitle: String? = null,
    keypadPlaceholder: String? = null,
    isNumKeyboard: Boolean = false,
    minLength: Int = 0,
    maxLength: Int = 14,
    onHashValueChange: (String) -> Unit = {},
    onSecureValueChange: (ByteArray) -> Unit
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    var value by remember { mutableStateOf(visibleValue) }
    val launcher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                value = result.data?.getStringExtra(SecureKeypadActivity.RESULT_STR) ?: ""
                result.data?.getByteArrayExtra(SecureKeypadActivity.RESULT_ENC)?.let {
                    onSecureValueChange(it)
                } ?: onSecureValueChange(byteArrayOf())
                result.data?.getStringExtra(SecureKeypadActivity.RESULT_DEC_PART) ?: ""
                result.data?.getStringExtra(SecureKeypadActivity.RESULT_ENC_HASH)?.let {
                    onHashValueChange(it)
                } ?: onHashValueChange(String())
            } else {
                value = ""
                onSecureValueChange(byteArrayOf())
            }

            focusManager.clearFocus()
        }

    val onFocusChanged: (FocusState) -> Unit = {
        if (it.isFocused) launcher.launch(
            Intent(context, SecureKeypadActivity::class.java)
                .putExtra(SecureKeypadActivity.ARGS_KEYPAD_NUMBER, isNumKeyboard)
                .putExtra(SecureKeypadActivity.ARGS_TITLE, keypadTitle)
                .putExtra(SecureKeypadActivity.ARGS_PLACEHOLDER, keypadPlaceholder)
                .putExtra(SecureKeypadActivity.ARGS_MIN_LENGTH, minLength)
                .putExtra(SecureKeypadActivity.ARGS_MAX_LENGTH, maxLength)
        )
    }

    if (showErrorMessage) {
        Input(
            value = value,
            onValueChange = {},
            modifier = modifier
                .onFocusChanged {
                    onFocusChanged(it)
                },
            enabled = enabled,
            readOnly = true,
            placeholder = placeholder,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            errorMessage = errorMessage,
            errorMessageAlignment = errorAlignment,
            visualTransformation = PasswordVisualTransformation('\u002a')
        )
    } else {
        InputNoMessage(
            value = value,
            onValueChange = {},
            modifier = modifier
                .onFocusChanged {
                    onFocusChanged(it)
                },
            enabled = enabled,
            readOnly = true,
            placeholder = placeholder,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            visualTransformation = PasswordVisualTransformation('\u002a')
        )
    }
}

object SecureKeyPadScope {

    @Composable
    fun SecureKeypadScreen(
        scrollState: ScrollState = rememberScrollState(),
        showKeypad: Boolean,
        keyPadType: Int = Defines.KEYPAD_TYPE_QWERTY,
        minLength: Int = 4,
        maxLength: Int = 14,
        onKeypadChangeHeight: (Int) -> Unit = {},
        onTextChanged: (String) -> Unit,
        onConfirm: () -> Unit,
        content: @Composable () -> Unit
    ) {

        var height by remember { mutableStateOf(0) }
        val scrollable = if (height > 0) Modifier.verticalScroll(scrollState) else Modifier

        Column(modifier = scrollable) {
            content()
            Spacer(modifier = Modifier.height(height.dp))
        }

        val context = LocalContext.current
        val coroutineScope = rememberCoroutineScope()
        val focusManager = LocalFocusManager.current

        if (showKeypad) {
            val activity = context.findActivity()

            SecureKeyPad.show(
                activity = activity,
                keyPadType = keyPadType,
                minLength = minLength,
                maxLength = maxLength,
                onKeypadChangeHeight = { h ->
                    height = h

                    coroutineScope.launch {
                        scrollState.animateScrollTo(h - 100)
                    }

                    onKeypadChangeHeight(h)
                },
                onTextChanged = onTextChanged,
                onConfirm = { _, _ ->
                    focusManager.clearFocus()
                    onConfirm()
                })

        } else {
            SecureKeyPad.hide()
        }

        DisposableEffect(SecureKeyPad) {
            onDispose {
                SecureKeyPad.hide()
                SecureKeyPad.finish()
            }
        }
    }

}