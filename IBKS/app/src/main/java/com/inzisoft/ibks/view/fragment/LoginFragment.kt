package com.inzisoft.ibks.view.fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.fragment.app.clearFragmentResultListener
import androidx.fragment.app.viewModels
import com.inzisoft.ibks.*
import com.inzisoft.ibks.R
import com.inzisoft.ibks.base.BaseFragment
import com.inzisoft.ibks.base.Right
import com.inzisoft.ibks.util.CommonUtil
import com.inzisoft.ibks.util.CryptoUtil
import com.inzisoft.ibks.util.log.QLog
import com.inzisoft.ibks.view.compose.*
import com.inzisoft.ibks.view.compose.theme.*
import com.inzisoft.ibks.viewmodel.LoginState
import com.inzisoft.ibks.viewmodel.LoginViewModel
import dagger.hilt.android.AndroidEntryPoint

@SuppressLint("MissingPermission")
@AndroidEntryPoint
class LoginFragment : BaseFragment() {

    private val viewModel: LoginViewModel by viewModels()

    init {
        baseCompose.content = {
            val contentResolver = LocalContext.current.contentResolver
            val errorMessage = when (val state = viewModel.loginState) {
                is LoginState.LoginCommonFail -> state.message
                is LoginState.ModifyPwd.Error -> state.message
                else -> null
            }

            when (viewModel.loginState) {
                is LoginState.ModifyPwd -> {
                    ModifyPwdScreen(
                        isInitial = { viewModel.checkInitial() },
                        errorMessage = errorMessage,
                        onModifyPwd = { viewModel.requestModifyPwd() },
                        currentPwd = viewModel.currentPwd,
                        onCurrentPwdChange = { viewModel.onCurrentPwdChange(it) },
                        newPwd = viewModel.newPwd,
                        onNewPwdChange = { viewModel.onNewPwdChange(it) }
                    )
                }
                else -> {
                    LoginScreen(
                        id = viewModel.id,
                        onIdChange = { viewModel.onIdChange(it) },
                        pw = viewModel.password,
                        onPwChange = { viewModel.onPassChange(it) },
                        errorMessage = errorMessage,
                        onLogin = {
                            val number = CommonUtil.getDeviceNumber(requireContext())
                            QLog.i("SIM 전화번호 : [ getPhoneNumber ] >>> $number")
                            // 네트워크 체크
                            if (availableNetwork()) {
                                viewModel.requestLogin(contentResolver, number)
                            } else {
                                viewModel.loginState = LoginState.LoginFail(
                                    ErrorCode.ERROR_CODE_NOT_AVAILBLE_NETWORK,
                                    getString(R.string.form_update_error_network)
                                )
                            }
                        },
                        onGoHelp = {
                            val helpIntent =
                                Intent(Intent.ACTION_VIEW, Uri.parse("http://www.naver.com"))
                            startActivity(helpIntent)
                        }
                    )
                }
            }

            LoginBackHandler()
        }

        baseCompose.surface = {
            LoginStateCompose(viewModel.loginState)

            if (BuildConfig.DEBUG || BuildConfig.TEST_BUTTON) {
                Row {
                    FloatingActionButton(onClick = {
                        viewModel.onIdChange("999918")
                        viewModel.onPassChange(CryptoUtil.encrypt(requireContext(), "ibks2099@"))
                    }) {
                        Text("장우영")
                    }
                    FloatingActionButton(onClick = {
                        viewModel.loginState = LoginState.CheckFormVersionState.NoUpdateForm
                    }) {
                        Text("SKIP")
                    }
                }
            }
            // 로그인 실패시 Dialog
            val contentResolver = LocalContext.current.contentResolver
            when (val state = viewModel.loginState) {
                is LoginState.LoginFail -> {
                    when (state.code) {
                        ErrorCode.ERROR_CODE_UNREGISTERED_DEVICE -> {
                            // 미등록 기기일 경우
                            showAlertDialog(
                                state.message,
                                stringResource(id = R.string.no),
                                stringResource(id = R.string.yes)
                            ) { popupState ->
                                when (popupState) {
                                    Right -> {
                                        viewModel.registerDevice(contentResolver)
                                    }
                                    else -> {
                                        showAlertDialog(
                                            contentText = getString(R.string.login_alert_register_device),
                                            rightBtnText = getString(R.string.confirm),
                                            onDismissRequest = { viewModel.none() }
                                        )
                                    }
                                }
                            }
                        }
                        ErrorCode.ERROR_CODE_NOT_AVAILBLE_NETWORK -> {
                            // 네트워크 오류일 경우
                            showAlertDialog(
                                contentText = getString(R.string.form_update_error_network),
                                rightBtnText = getString(R.string.confirm),
                                onDismissRequest = { viewModel.none() }
                            )
                        }
                        else -> {
                            showAlertDialog(
                                contentText = R.string.login_error_server,
                                rightBtnText = R.string.confirm,
                                onDismissRequest = { viewModel.none() }
                            )
                        }
                    }
                }
                is LoginState.RegisterFailed -> {
                    showAlertDialog(
                        contentText = state.message,
                        rightBtnText = getString(R.string.confirm),
                        onDismissRequest = { viewModel.none() }
                    )
                }
                is LoginState.RegisterSuccess -> {
                    showAlertDialog(
                        contentText = getString(R.string.register_device_success),
                        rightBtnText = getString(R.string.confirm),
                        onDismissRequest = { viewModel.none() }
                    )
                }
                else -> {}
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

    @Preview(widthDp = 1280, heightDp = 800)
    @Composable
    fun PreviewLoginScreen4() {
        IBKSTheme {
            ModifyPwdScreen(
                isInitial = { false },
                errorMessage = "오류 메시지",
                onModifyPwd = {},
                currentPwd = "",
                onCurrentPwdChange = {},
                newPwd = "",
                onNewPwdChange = {}
            )
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
//                Row(
//                    modifier = Modifier.padding(top = 50.dp),
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
//                    Image(
//                        modifier = Modifier.size(width = 36.dp, height = 36.dp),
//                        painter = painterResource(id = R.drawable.bicon_call),
//                        contentDescription = null,
//                        contentScale = ContentScale.FillWidth
//                    )
//
//                    Text(
//                        modifier = Modifier
//                            .padding(start = 16.dp),
//                        text = stringResource(R.string.login_help_button),
//                        style = MaterialTheme.typography.body2,
//                        color = MaterialTheme.colors.background1Color
//                    )
//                }
            }
        }
    }

    @Composable
    fun ModifyPwdScreen(
        isInitial: () -> Boolean, errorMessage: String? = null, onModifyPwd: () -> Unit,
        currentPwd: String, onCurrentPwdChange: (String) -> Unit,
        newPwd: String, onNewPwdChange: (String) -> Unit
    ) {
        var hashNewPwd by remember { mutableStateOf("") }
        var hashNewPwdRepeat by remember { mutableStateOf("") }
        var pwdErrorMessage by remember { mutableStateOf("") }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background1Color)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .align(Alignment.Center),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = buildAnnotatedString {
                        withStyle(style = SpanStyle(color = MaterialTheme.colors.textColor)) {
                            append(
                                stringResource(R.string.modify_password_title_1)
                            )
                        }
                        withStyle(style = SpanStyle(color = MaterialTheme.colors.point4Color)) {
                            append(
                                stringResource(R.string.modify_password_title_2)
                            )
                        }
                        withStyle(style = SpanStyle(color = MaterialTheme.colors.textColor)) {
                            append(
                                stringResource(R.string.modify_password_title_3)
                            )
                        }
                    },
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.h2,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = buildAnnotatedString {
                        if (isInitial()) {
                            withStyle(style = SpanStyle(color = MaterialTheme.colors.point4Color)) {
                                append(
                                    stringResource(R.string.modify_password_subtitle_initial_1)
                                )
                            }
                            withStyle(style = SpanStyle(color = MaterialTheme.colors.textColor)) {
                                append(
                                    stringResource(R.string.modify_password_subtitle_initial_2)
                                )
                            }
                        } else {
                            withStyle(style = SpanStyle(color = MaterialTheme.colors.textColor)) {
                                append(
                                    stringResource(R.string.modify_password_subtitle_three_month_1)
                                )
                            }
                            withStyle(style = SpanStyle(color = MaterialTheme.colors.point4Color)) {
                                append(
                                    stringResource(R.string.modify_password_subtitle_three_month_2)
                                )
                            }
                            withStyle(style = SpanStyle(color = MaterialTheme.colors.textColor)) {
                                append(
                                    stringResource(R.string.modify_password_subtitle_three_month_3)
                                )
                            }
                        }
                    },
                    modifier = Modifier.padding(top = 24.dp, bottom = 36.dp),
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    lineHeight = 1.5.em,
                    style = MaterialTheme.typography.subtitle1
                )

                Column(
                    modifier = Modifier
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colors.disableColor,
                            shape = RoundedCornerShape(0.dp)
                        )
                        .padding(horizontal = 94.dp, vertical = 24.dp)
                        .align(Alignment.CenterHorizontally),
                    verticalArrangement = Arrangement.Center
                ) {
                    Row(
                        modifier = Modifier.width(370.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(id = R.string.modify_password_current),
                            fontWeight = FontWeight.Medium,
                            style = MaterialTheme.typography.body1,
                            textAlign = TextAlign.Center
                        )
                        SecureInput(
                            visibleValue = currentPwd,
                            modifier = Modifier.width(244.dp),
                            placeholder = stringResource(id = R.string.modify_password_current_hint),
                            errorAlignment = TextAlign.Center,
                            showErrorMessage = true
                        ) {
                            QLog.i("newPwd : $it")
                            onCurrentPwdChange(String(it))
                        }
                    }
                }
                Column(
                    modifier = Modifier
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colors.disableColor,
                            shape = RoundedCornerShape(0.dp)
                        )
                        .padding(horizontal = 94.dp, vertical = 24.dp)
                        .align(Alignment.CenterHorizontally),
                    verticalArrangement = Arrangement.Center
                ) {
                    Row(
                        modifier = Modifier.width(370.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(id = R.string.modify_password_new),
                            fontWeight = FontWeight.Medium,
                            style = MaterialTheme.typography.body1,
                            textAlign = TextAlign.Center
                        )
                        SecureInput(
                            visibleValue = newPwd,
                            modifier = Modifier.width(244.dp),
                            placeholder = stringResource(id = R.string.modify_password_new_hint),
                            errorAlignment = TextAlign.Center,
                            showErrorMessage = true,
                            onHashValueChange = {
                                hashNewPwd = it
                                pwdErrorMessage = if (hashNewPwd != hashNewPwdRepeat) {
                                    "신규 비밀번호가 다릅니다."
                                } else {
                                    ""
                                }
                            }
                        ) {
                            onNewPwdChange(String(it))
                        }
                    }
                    Row(
                        modifier = Modifier.width(370.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box { }
                        Text(
                            modifier = Modifier
                                .width(244.dp)
                                .padding(top = 8.dp, bottom = 16.dp),
                            text = stringResource(id = R.string.modify_password_new_hint_help),
                            fontWeight = FontWeight.Normal,
                            style = MaterialTheme.typography.body2,
                            textAlign = TextAlign.Start
                        )
                    }
                    Row(
                        modifier = Modifier.width(370.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(id = R.string.modify_password_new_repeat),
                            fontWeight = FontWeight.Medium,
                            style = MaterialTheme.typography.body1,
                            textAlign = TextAlign.Center
                        )
                        SecureInput(
                            visibleValue = hashNewPwdRepeat,
                            modifier = Modifier.width(244.dp),
                            placeholder = stringResource(id = R.string.modify_password_new_repeat_hint),
                            errorAlignment = TextAlign.Center,
                            errorMessage = pwdErrorMessage,
                            showErrorMessage = true,
                            onHashValueChange = {
                                hashNewPwdRepeat = it
                                pwdErrorMessage = if (hashNewPwd != hashNewPwdRepeat) {
                                    "신규 비밀번호가 다릅니다."
                                } else {
                                    ""
                                }
                            }
                        ) {}
                    }
                }

                errorMessage?.let {
                    if (it.isNotEmpty()) {
                        Text(
                            text = errorMessage,
                            modifier = Modifier.padding(top = 24.dp),
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colors.errorColor,
                            style = MaterialTheme.typography.body2
                        )
                    }
                }

                ColorButton(
                    onClick = onModifyPwd,
                    modifier = Modifier
                        .padding(top = 24.dp)
                        .size(240.dp, 56.dp),
                    enabled = (currentPwd.isNotEmpty() && hashNewPwd.isNotEmpty() && hashNewPwdRepeat.isNotEmpty() && pwdErrorMessage.isEmpty()),
                    text = stringResource(id = R.string.modify_password_change),
                    buttonStyle = ButtonStyle.Big
                )
            }
        }
    }

    @Composable
    private fun LoginStateCompose(state: LoginState) {
        when (state) {
            is LoginState.Loading -> Loading()
            is LoginState.AuthCellPhone -> ShowAuthCellPhone(state.cellphone)
            is LoginState.CheckFormVersionState.CheckingFormVersion ->
                LoadingPopup(
                    message = stringResource(
                        id = R.string.form_version_check
                    )
                )
            is LoginState.CheckFormVersionState.Error ->
                showAlertDialog(state.message) { viewModel.loginState = LoginState.None }
            is LoginState.CheckFormVersionState.NoUpdateForm -> goToMain()
            is LoginState.CheckFormVersionState.OnReadyUpdate -> goToFormUpdate()
            is LoginState.ModifyPwd.Loading -> Loading()
            is LoginState.ModifyPwd.ModifySuccess -> viewModel.checkFormVersion()
            else -> {}
        }
    }

    @Composable
    private fun ShowAuthCellPhone(cellphone: String) {
        setFragmentResultListener(FragmentRequest.AuthCellPhone) { _, result ->
            clearFragmentResultListener(FragmentRequest.AuthCellPhone.key)

            when (result) {
                is FragmentResult.OK -> {
                    // 만료된 비밀번호거나, 초기화된 비밀번호라면
                    if (viewModel.expired == "1" || viewModel.resultCode == "4")
                        viewModel.loginState = LoginState.ModifyPwd.None
                    else
                        viewModel.checkFormVersion()
                }

                is FragmentResult.Cancel -> {
                    viewModel.loginState = LoginState.LoginCommonFail("OTP 인증 실패")
                    viewModel.logout()
                }

                is FragmentResult.Error -> {

                }
            }
        }

        navigate(
            LoginFragmentDirections.actionLoginFragmentToAuthCellphoneDialogFragment(
                cellphone = cellphone
            )
        )
    }


    private fun goToFormUpdate() {
        navigate(LoginFragmentDirections.actionLoginFragmentToFormUpdateFragment())
    }

    private fun goToMain() {
        navigate(LoginFragmentDirections.actionLoginFragmentToMainFragment())
    }
}