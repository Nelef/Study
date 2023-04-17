package com.inzisoft.ibks.util.skeypad

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.inzisoft.ibks.R
import com.inzisoft.ibks.databinding.ActivitySecureKeypadBinding
import com.inzisoft.ibks.util.log.QLog
import com.inzisoft.ibks.view.compose.defaultGuideTextStyle
import com.inzisoft.ibks.view.compose.theme.IBKSTheme
import com.inzisoft.ibks.view.compose.theme.background1Color
import com.nprotect.keycryptm.Defines
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SecureKeypadActivity : AppCompatActivity() {

    companion object {
        const val ARGS_TITLE = "args_title"
        const val ARGS_PLACEHOLDER = "args_placeholder"
        const val ARGS_KEYPAD_NUMBER = "args_keypad_number"
        const val ARGS_MIN_LENGTH = "args_min_length"
        const val ARGS_MAX_LENGTH = "args_max_length"
        const val RESULT_ENC = "result_enc"
        const val RESULT_STR = "result_str"
        const val RESULT_ENC_HASH = "result_enc_hash"
        const val RESULT_DEC_PART = "result_dec_part"
    }

    val viewModel: SecureKeypadViewModel by viewModels()
    private lateinit var binding: ActivitySecureKeypadBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        hideSystemUI()

        viewModel.title =
            intent?.getStringExtra(ARGS_TITLE) ?: getString(R.string.secure_keypad_default_title)
        viewModel.placeholder = intent?.getStringExtra(ARGS_PLACEHOLDER)
            ?: getString(R.string.secure_keypad_default_placeholder)
        viewModel.keypadType =
            if (intent?.getBooleanExtra(ARGS_KEYPAD_NUMBER, false) == true)
                Defines.KEYPAD_TYPE_NUMBER
            else
                Defines.KEYPAD_TYPE_QWERTY
        viewModel.minLength = intent?.getIntExtra(ARGS_MIN_LENGTH, 0) ?: 0
        viewModel.maxLength = intent?.getIntExtra(ARGS_MAX_LENGTH, 14) ?: 14

        binding = ActivitySecureKeypadBinding.inflate(layoutInflater)

        binding.composeView.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                IBKSTheme {
                    SecureKeypadScreen(
                        title = viewModel.title,
                        input = viewModel.input,
                        placeholder = viewModel.placeholder
                    )
                }
            }
        }

        setContentView(binding.root)
    }

    override fun onResume() {
        super.onResume()

        SecureKeyPad.show(this@SecureKeypadActivity,
            keyPadType = viewModel.keypadType,
            minLength = viewModel.minLength,
            maxLength = viewModel.maxLength,
            onKeypadChangeHeight = {},
            onTextChanged = {
                viewModel.onInputChange(it)
            },
            onConfirm = { realText, hashText ->
                onResult(realText, hashText) }
        )
    }

    private fun onResult(data: ByteArray?, data2: String?) {
        viewModel.encrypt(data) { encryptResult ->
            if (encryptResult == null) {
                setResult(RESULT_CANCELED)
            } else {
                setResult(
                    RESULT_OK, Intent()
                        .putExtra(RESULT_ENC, encryptResult)
                        .putExtra(RESULT_STR, viewModel.input)
                        .putExtra(RESULT_DEC_PART, viewModel.decPart)
                        .putExtra(RESULT_ENC_HASH, data2)
                )
            }
            QLog.i("encryptResult : $encryptResult")
            finish()
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        SecureKeyPad.onConfigurationChanged()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        // Back 키를 막는다.
    }

    override fun onPause() {
        super.onPause()
        SecureKeyPad.hide()
    }

    override fun onDestroy() {
        super.onDestroy()
        SecureKeyPad.finish()
    }

    private fun hideSystemUI() {
        with(WindowCompat.getInsetsController(window, window.decorView)) {
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            hide(WindowInsetsCompat.Type.systemBars())
        }
    }

    @Composable
    fun SecureKeypadScreen(
        title: String,
        input: String,
        placeholder: String
    ) {
        val focusRequest = remember { FocusRequester() }

        Column(
            modifier = Modifier
                .background(color = MaterialTheme.colors.background1Color)
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = 24.dp,
                        end = 12.dp,
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.h3,
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = { finish() },
                    modifier = Modifier.size(80.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.micon_cancle),
                        contentDescription = ""
                    )
                }
            }

            OutlinedTextField(
                value = input,
                onValueChange = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequest),
                textStyle = MaterialTheme.typography.h3,
                enabled = true,
                readOnly = true,
                placeholder = {
                    Text(text = placeholder, style = defaultGuideTextStyle().copy(fontSize = 28.sp))
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                maxLines = 1
            )

            LaunchedEffect(key1 = focusRequest) {
                focusRequest.requestFocus()
            }
        }

    }

    @Preview(widthDp = 1280, heightDp = 800)
    @Composable
    fun PreviewSecureKeypadScreen1() {
        IBKSTheme {
            SecureKeypadScreen(
                getString(R.string.secure_keypad_default_title),
                "",
                getString(R.string.secure_keypad_default_placeholder)
            )
        }
    }

    @Preview(widthDp = 1280, heightDp = 800)
    @Composable
    fun PreviewSecureKeypadScreen2() {
        IBKSTheme {
            SecureKeypadScreen(
                getString(R.string.secure_keypad_default_title),
                "***2",
                getString(R.string.secure_keypad_default_placeholder)
            )
        }
    }

}