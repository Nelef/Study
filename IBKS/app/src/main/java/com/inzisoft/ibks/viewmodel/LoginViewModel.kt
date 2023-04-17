package com.inzisoft.ibks.viewmodel

import android.content.ContentResolver
import android.provider.Settings
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.inzisoft.ibks.AppKeySet
import com.inzisoft.ibks.ErrorCode
import com.inzisoft.ibks.ExceptionCode
import com.inzisoft.ibks.base.BaseViewModel
import com.inzisoft.ibks.data.remote.UpdateFormDataSource
import com.inzisoft.ibks.data.remote.model.ApiResult
import com.inzisoft.ibks.data.repository.LocalRepository
import com.inzisoft.ibks.data.repository.LoginRepository
import com.inzisoft.ibks.util.log.QLog
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginRepository: LoginRepository,
    private val localRepository: LocalRepository,
    private val updateFormDataSource: UpdateFormDataSource,
) : BaseViewModel() {

    companion object {
        private const val TAG = "LoginViewModel"
    }

    init {
        viewModelScope.launch {
            // 로그인 화면에서 UserInfo를 삭제함.
            localRepository.clear()
            localRepository.clearCacheDir()
            localRepository.clearElectronicSaveInfo()
        }
    }

    var loginState by mutableStateOf<LoginState>(LoginState.None)

    var id by mutableStateOf("")
    var password by mutableStateOf("")

    var currentPwd by mutableStateOf("")
    var newPwd by mutableStateOf("")
    var resultCode by mutableStateOf("")
    var expired by mutableStateOf("0")

    fun onIdChange(id: String) {
        this.id = id
    }

    fun onPassChange(password: String) {
        this.password = password
    }

    fun onCurrentPwdChange(currentPwd: String) {
        this.currentPwd = currentPwd
    }

    fun onNewPwdChange(newPwd: String) {
        this.newPwd = newPwd
    }

    fun requestLogin(contentResolver: ContentResolver, simNumber: String) =
        viewModelScope.launch(Dispatchers.IO) {
            loginState = LoginState.Loading

            val androidId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)

            loginRepository.requestLogin(id, password, androidId)
                .collectLatest { result ->
                    when (result) {
                        is ApiResult.Loading -> loginState = LoginState.Loading
                        is ApiResult.Success -> {
                            QLog.t(TAG).d("${result.data}")
                            localRepository.setUserInfo(result.data, simNumber)
                            localRepository.setAccessToken(
                                result.headers?.get(AppKeySet.ACCESS_TOKEN)?.get(0).toString()
                            )
                            localRepository.setCookie(
                                result.headers?.get(AppKeySet.SET_COOKIE)?.get(0).toString()
                            )
                            expired = result.data.expired
                            resultCode = result.data.resultCode
                            // tabletOtpPass가 True 면 otp 스킵
                            if (!result.data.tabletOtpPass)
                                loginState = LoginState.AuthCellPhone(result.data.handPhone)
                            else
                                checkFormVersion()
                        }
                        is ApiResult.Error -> {
                            loginState = when (result.code) {
                                ErrorCode.ERROR_CODE_UNREGISTERED_DEVICE,
                                ExceptionCode.EXCEPTION_CODE_INTERNAL_CONNECTION -> {
                                    // 특별히 코드로 분기를 해야 하는 로그인 실패 경우
                                    LoginState.LoginFail(result.code, result.message)
                                }
                                else -> LoginState.LoginCommonFail(result.message)
                            }
                        }
                    }
                }
        }

    fun registerDevice(contentResolver: ContentResolver) = viewModelScope.launch(Dispatchers.IO) {
        val androidId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
        loginRepository.registerDevice(androidId, id).collectLatest { result ->
            loginState = when (result) {
                is ApiResult.Loading -> LoginState.Loading
                is ApiResult.Success -> LoginState.RegisterSuccess
                is ApiResult.Error -> LoginState.RegisterFailed(result.message)
            }
        }
    }

    fun requestModifyPwd() = viewModelScope.launch(Dispatchers.IO) {
        loginState = LoginState.ModifyPwd.Loading

        loginRepository.modifyPwd(id, currentPwd, newPwd)
            .collectLatest { result ->
                when (result) {
                    is ApiResult.Loading -> loginState = LoginState.ModifyPwd.Loading
                    is ApiResult.Success -> {
                        QLog.t(TAG).d("${result.data}")
                        loginState = LoginState.ModifyPwd.ModifySuccess
                    }
                    is ApiResult.Error -> {
                        QLog.t(TAG).d("${result.code} : ${result.message}")
                        loginState = LoginState.ModifyPwd.Error(result.message)
                    }
                }
            }
    }

    fun checkInitial(): Boolean {
        return resultCode == "4"
    }

    fun logout() = viewModelScope.launch {
        localRepository.clear()
    }

    fun checkFormVersion() = viewModelScope.launch(Dispatchers.IO) {
        updateFormDataSource.checkVersion().collect { state ->
            when (state) {
                UpdateFormDataSource.UpdateFormState.OnReadyUpdate -> loginState =
                    LoginState.CheckFormVersionState.OnReadyUpdate
                UpdateFormDataSource.UpdateFormState.Loading -> loginState =
                    LoginState.CheckFormVersionState.CheckingFormVersion
                is UpdateFormDataSource.UpdateFormState.OnError -> loginState =
                    LoginState.CheckFormVersionState.Error(state.message)
                UpdateFormDataSource.UpdateFormState.OnUpdateComplete -> loginState =
                    LoginState.CheckFormVersionState.NoUpdateForm
                else -> {}
            }
        }
    }

    fun none() {
        loginState = LoginState.None
    }
}

sealed class LoginState {
    object None : LoginState()
    object Loading : LoginState()
    object LoginSuccess : LoginState()
    object RegisterSuccess : LoginState()

    data class RegisterFailed(val message: String) : LoginState()
    data class LoginCommonFail(val message: String) : LoginState()
    data class LoginFail(val code: Int, val message: String) : LoginState()

    data class AuthCellPhone(val cellphone: String) : LoginState()

    sealed class ModifyPwd : LoginState() {
        object None : ModifyPwd()
        object Loading : ModifyPwd()
        object ModifySuccess : ModifyPwd()
        data class Error(val message: String) : ModifyPwd()
    }

    sealed class CheckFormVersionState : LoginState() {
        object None : CheckFormVersionState()
        data class Error(val message: String) : CheckFormVersionState()
        object CheckingFormVersion : CheckFormVersionState()
        object OnReadyUpdate : CheckFormVersionState()
        object NoUpdateForm : CheckFormVersionState()
    }
}


