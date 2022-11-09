package com.inzisoft.ibks.viewmodel

import android.content.ContentResolver
import android.provider.Settings
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.inzisoft.ibks.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
//    private val loginRepository: LoginRepository,
//    private val localRepository: LocalRepository,
//    private val formUpdateRepository: FormUpdateRepository
) : BaseViewModel() {

    companion object {
        private const val TAG = "LoginViewModel"
    }

    init {
        viewModelScope.launch {
            // 로그인 화면에서 UserInfo를 삭제함.
//            localRepository.clearUserInfo()
//            localRepository.clearAccessToken()
        }
    }

    var loginState by mutableStateOf<LoginState>(LoginState.None)
    var checkFormVersionState: CheckFormVersionState by mutableStateOf(CheckFormVersionState.None)

    var id by mutableStateOf("")
    var password by mutableStateOf("")

    fun onIdChange(id: String) {
        this.id = id
    }

    fun onPassChange(password: String) {
        this.password = password
    }

    fun requestLogin(contentResolver: ContentResolver) = viewModelScope.launch(Dispatchers.IO) {
        loginState = LoginState.Loading

        val androidId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)

        // TEST
//        checkFormVersionState = CheckFormVersionState.OnReadyUpdate
        checkFormVersionState = CheckFormVersionState.AuthCellPhone

        // TODO 로그인 api 생성 후 작업
//        localRepository.setAccessToken("")
//        loginRepository.requestLogin(androidId, id, password)
//            .collect { result ->
//                when (result) {
//                    is ApiResult.Loading -> loginState = LoginState.Loading
//                    is ApiResult.Success -> {
//                        QLog.t(TAG).d("${result.data}")
//                        localRepository.setUserInfo(result.data)
//                        localRepository.setAccessToken(
//                            result.headers?.get(AppKeySet.ACCESS_TOKEN)?.get(0).toString()
//                        )
//                        loginState = LoginState.LoginSuccess
//
//                        checkFormVersion()
//                    }
//                    is ApiResult.Error -> {
//                        loginState = LoginState.LoginFail(result.message)
//                    }
//                }
//            }
    }

    private fun checkFormVersion() = viewModelScope.launch(Dispatchers.IO) {
        // TODO 폼 버전 api 생성 후 작업
//        formUpdateRepository.checkVersion().collect { state ->
//            when (state) {
//                FormUpdateRepository.UpdateFormState.OnReadyUpdate -> checkFormVersionState =
//                    CheckFormVersionState.OnReadyUpdate
//                FormUpdateRepository.UpdateFormState.Loading -> checkFormVersionState =
//                    CheckFormVersionState.CheckingFormVersion
//                is FormUpdateRepository.UpdateFormState.OnError -> checkFormVersionState =
//                    CheckFormVersionState.Error(state.message)
//                FormUpdateRepository.UpdateFormState.OnUpdateComplete -> checkFormVersionState =
//                    CheckFormVersionState.NoUpdateForm
//                else -> {}
//            }
//        }
    }
}

sealed class LoginState {
    object None : LoginState()
    object Loading : LoginState()
    object LoginSuccess : LoginState()
    data class LoginFail(val message: String) : LoginState()
}

sealed class CheckFormVersionState {
    object None : CheckFormVersionState()
    data class Error(val message: String) : CheckFormVersionState()
    object CheckingFormVersion : CheckFormVersionState()
    object OnReadyUpdate : CheckFormVersionState()
    object NoUpdateForm : CheckFormVersionState()
    //otp
    object AuthCellPhone : CheckFormVersionState()
}
