package com.inzisoft.ibks.view.fragment

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.inzisoft.ibks.R
import com.inzisoft.ibks.base.BaseFragment
import com.inzisoft.ibks.base.Right
import com.inzisoft.ibks.view.compose.*
import com.inzisoft.ibks.view.compose.theme.*
import com.inzisoft.ibks.viewmodel.CheckFormVersionState
import com.inzisoft.ibks.viewmodel.LoginState
import com.inzisoft.ibks.viewmodel.LoginViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginFragment : BaseFragment() {

    private val viewModel: LoginViewModel by viewModels()

    init {
        baseCompose.content = {
            val contentResolver = LocalContext.current.contentResolver
            val errorMessage = when (val state = viewModel.loginState) {
                is LoginState.LoginFail -> state.message
                else -> null
            }

            LoginScreen(
                id = viewModel.id,
                onIdChange = { viewModel.onIdChange(it) },
                pw = viewModel.password,
                onPwChange = { viewModel.onPassChange(it) },
                errorMessage = errorMessage,
                onLogin = { viewModel.requestLogin(contentResolver) },
                onGoHelp = {
                    val helpIntent = Intent(Intent.ACTION_VIEW, Uri.parse("http://www.naver.com"))
                    startActivity(helpIntent)
                }
            )

            LoginBackHandler()
        }

        baseCompose.surface = {
            LoginStateCompose(viewModel.loginState)
            FormVersionCheck(viewModel.checkFormVersionState)

            Row {
                FloatingActionButton(onClick = {
                    viewModel.onIdChange("ib_testid")
                    viewModel.onPassChange("ib_testpw")
                }) {
                    Text("IBK")
                }
            }
        }
    }

    @Composable
    private fun LoginBackHandler() {
        BackHandler(enabled = true, onBack = {
            showAlertDialog(
                contentText = getString(R.string.alert_close_app),
                rightBtnText = getString(R.string.yes),
                leftBtnText = getString(R.string.no),
                onDismissRequest = {
                    if (it == Right) {
                        terminateApplication()
                    }
                }
            )
        })
    }

    @Preview(widthDp = 1280, heightDp = 800)
    @Composable
    fun PreviewLoginScreen1() {
        IBKSTheme {
            LoginScreen(
                id = "",
                onIdChange = {},
                pw = "",
                onPwChange = {},
                onLogin = { },
                onGoHelp = {})
        }
    }

    @Preview(widthDp = 1280, heightDp = 800)
    @Composable
    fun PreviewLoginScreen2() {
        IBKSTheme {
            LoginScreen(
                id = "test",
                onIdChange = {},
                pw = "test",
                onPwChange = {},
                onLogin = { },
                onGoHelp = {})
        }
    }

    @Preview(widthDp = 1280, heightDp = 800)
    @Composable
    fun PreviewLoginScreen3() {
        IBKSTheme {
            LoginScreen(
                id = "test",
                onIdChange = {},
                pw = "test",
                onPwChange = {},
                errorMessage = "아이디 또는 비밀번호 오류",
                onLogin = { },
                onGoHelp = {})
        }
    }

    @Composable
    fun LoginScreen(
        id: String,
        onIdChange: (String) -> Unit,
        pw: String,
        onPwChange: (String) -> Unit,
        errorMessage: String? = null,
        onLogin: () -> Unit,
        onGoHelp: () -> Unit
    ) {
        val focusManager = LocalFocusManager.current

        Box(
            modifier = Modifier
                .background(MaterialTheme.colors.background1Color)
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    focusManager.clearFocus()
                }
        )
        {
            Image(
                modifier = Modifier.fillMaxSize(),
                painter = painterResource(id = R.drawable.bg_main_01),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                alignment = Alignment.TopCenter,
            )

            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .width(640.dp)
                    .fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            )
            {
                Row(
                    modifier = Modifier
                        .padding(bottom = 117.dp)
                        .height(52.dp),
                    verticalAlignment = Alignment.Bottom,
                ) {
                    Image(
                        modifier = Modifier.size(width = 256.dp, height = 52.dp),
                        painter = painterResource(id = R.drawable.logo_ibks_w),
                        contentDescription = null,
                        contentScale = ContentScale.FillWidth
                    )

                    Text(
                        modifier = Modifier
                            .padding(start = 16.dp),
                        text = stringResource(R.string.ibks_title_message),
                        style = MaterialTheme.typography.h4,
                        color = MaterialTheme.colors.background1Color
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .shadow(elevation = 2.dp)
                        .background(MaterialTheme.colors.background1Color),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(90.dp))

                    Input(
                        value = id,
                        onValueChange = onIdChange,
                        modifier = Modifier
                            .width(312.dp),
                        placeholder = stringResource(id = R.string.id),
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        )
                    )

                    Spacer(modifier = Modifier.height(26.dp))

                    SecureInput(
                        visibleValue = pw,
                        modifier = Modifier.width(312.dp),
                        placeholder = stringResource(id = R.string.password),
                        errorMessage = errorMessage,
                        errorAlignment = TextAlign.Center,
                        showErrorMessage = true
                    ) {
                        onPwChange(String(it))
                    }

                    Spacer(modifier = Modifier.height(22.dp))

                    ColorButton(
                        onClick = {
                            focusManager.clearFocus()
                            onLogin()
                        },
                        text = stringResource(id = R.string.login),
                        modifier = Modifier
                            .padding(bottom = 90.dp)
                            .width(320.dp)
                            .height(56.dp),
                        enabled = id.isNotEmpty() && pw.isNotEmpty()
                    )
                }
                Row(
                    modifier = Modifier.padding(top = 50.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        modifier = Modifier.size(width = 36.dp, height = 36.dp),
                        painter = painterResource(id = R.drawable.bicon_call),
                        contentDescription = null,
                        contentScale = ContentScale.FillWidth
                    )

                    Text(
                        modifier = Modifier
                            .padding(start = 16.dp),
                        text = ("서비스 이용 문의: 02-0000-0000\n운영시간: 평일 09:00~18:00"),
                        style = MaterialTheme.typography.body2,
                        color = MaterialTheme.colors.background1Color
                    )
                }
            }
        }
    }

    @Composable
    private fun LoginStateCompose(state: LoginState) {
        when (state) {
            LoginState.Loading -> Loading()
            else -> {}
        }
    }

    @Composable
    private fun FormVersionCheck(state: CheckFormVersionState) {
        // OTP test
        var authNumber = remember { mutableStateOf("") }

        when (state) {
            CheckFormVersionState.CheckingFormVersion -> LoadingPopup(message = stringResource(id = R.string.form_version_check))
            is CheckFormVersionState.Error -> showAlertDialog(state.message,
                rightBtnText = stringResource(
                    id = R.string.confirm
                ),
                onDismissRequest = {
                    viewModel.checkFormVersionState = CheckFormVersionState.None
                    viewModel.loginState = LoginState.None
                })
            CheckFormVersionState.NoUpdateForm -> goToMain()
            CheckFormVersionState.None -> {}
            CheckFormVersionState.OnReadyUpdate -> goToFormUpdate()

            // OTP test
            CheckFormVersionState.AuthCellPhone -> AuthCellPhoneDialog(
                visible = true,
                name = "test_userId",
                cellPhone = "010-0000-1111",
                onClose = {
                    viewModel.loginState = LoginState.LoginFail("OTP 인증 실패")
                    viewModel.checkFormVersionState = CheckFormVersionState.None
                },
                onRequestCellphoneAuth = { hideKeyboard() },
                authNumber = authNumber.value,
                onAuthNumberChange = { authNumber.value = it },
                validTime = "06:30",
                onCancel = {
                    viewModel.loginState = LoginState.LoginFail("OTP 인증 실패")
                    viewModel.checkFormVersionState = CheckFormVersionState.None
                },
                onConfirm = {
                    viewModel.loginState = LoginState.LoginSuccess
                    viewModel.checkFormVersionState = CheckFormVersionState.OnReadyUpdate
                })
        }
    }

    private fun goToFormUpdate() {
        // TODO 로그인 후 폼 업데이트로
        findNavController().navigate(LoginFragmentDirections.actionLoginFragmentToFormUpdateFragment())
    }

    private fun goToMain() {
        // TODO 로그인 후 메인으로
//        findNavController().navigate(LoginFragmentDirections.actionLoginFragmentToMainFragment())
    }
}