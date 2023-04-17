package com.inzisoft.ibks.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inzisoft.ibks.ErrorCode
import com.inzisoft.ibks.ExceptionCode
import com.inzisoft.ibks.R
import com.inzisoft.ibks.base.UiState
import com.inzisoft.ibks.data.internal.AlertData
import com.inzisoft.ibks.data.remote.ApplicationRemoteDataSource
import com.inzisoft.ibks.data.remote.model.ApiResult
import com.inzisoft.ibks.util.DownCounter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthCellPhoneViewModel @Inject constructor(
    private val applicationDataSource: ApplicationRemoteDataSource,
) : ViewModel() {

    companion object {
        private const val MASK_CHAR = '*'
        private const val DIVIDER_CELLPHONE = '-'
    }

    var cellphone by mutableStateOf("")
    var uiState by mutableStateOf<UiState<Unit>>(UiState.None)
    var authNumber by mutableStateOf("")
    var invalidAuthNumMsg: Int? by mutableStateOf(null)
    var validTime: String? by mutableStateOf(null)
    var IssueValidMin: String? by mutableStateOf(null)
    var IssueValidSec: String? by mutableStateOf(null)

    private val _popupState = mutableStateOf<UiState<AlertData>>(UiState.None)
    val popupState: State<UiState<AlertData>> = _popupState

    private var otpDownCounter = DownCounter(viewModelScope)
    private var issueOtpDownCounter = DownCounter(viewModelScope)

    fun load(cellphone: String) {
        uiState = UiState.Loading()

        viewModelScope.launch(Dispatchers.IO) {

            delay(300)

            this@AuthCellPhoneViewModel.cellphone = cellphone

            uiState = UiState.None
        }
    }

    fun alert(data: AlertData) {
        _popupState.value = UiState.Success(data)
    }

    fun dismissAlert() {
        _popupState.value = UiState.None
    }

    fun onAuthNumChange(authNum: String) {
        invalidAuthNumMsg = if (authNum.length == 6) null else R.string.invalid_auth_number

        if (authNum.length < 7) {
            this.authNumber = authNum
        }
    }

    fun issueOTP() = viewModelScope.launch(Dispatchers.IO) {
        if (issueOtpDownCounter.isRun()) {
            uiState = UiState.Error("${IssueValidMin}분 ${IssueValidSec}초 후에 다시 시도해 주세요.")
        } else {
            issueOtpDownCounter.start { time ->
                if (time == 0L) {
                    issueOtpDownCounter.stop()
                    return@start
                }
                IssueValidMin = (time / 60).toString()
                IssueValidSec = (time % 60).toString()
            }
            applicationDataSource.issueOTP(cellphone).collectLatest { result ->
                when (result) {
                    is ApiResult.Error -> {
                        validTime = null
                        uiState = UiState.Error(result.message)
                    }
                    is ApiResult.Loading -> uiState = UiState.Loading()
                    is ApiResult.Success -> {
                        otpDownCounter.start { time ->
                            if (time == 0L) {
                                authTimeout()
                                return@start
                            }

                            val min = (time / 60).toString().padStart(2, '0')
                            val sec = (time % 60).toString().padStart(2, '0')
                            validTime = "$min : $sec"
                        }
                        uiState = UiState.None
                        if(validTime != null) {
                            uiState = UiState.Error("인증번호가 재전송되었습니다.")
                        }
                    }
                }
            }
        }
    }


    fun confirmOTP(completeCallback: () -> Unit) = viewModelScope.launch(Dispatchers.IO) {
        applicationDataSource.confirmOTP(cellphone, authNumber).collectLatest { result ->
            when (result) {
                is ApiResult.Error -> {
                    if (result.code == ErrorCode.ERROR_CODE_OTP_TIMEOUT) {
                        authTimeout()
                    } else {
                        uiState = UiState.Error(message = result.message, code = result.code)
                    }
                }
                is ApiResult.Loading -> uiState = UiState.Loading()
                is ApiResult.Success -> {
                    otpDownCounter.stop()
                    validTime = null

                    viewModelScope.launch(Dispatchers.Main) {
                        completeCallback()
                    }
                }
            }
        }
    }

    fun extensionOTP() {
        otpDownCounter.start { time ->
            if (time == 0L) {
                authTimeout()
                return@start
            }

            val min = (time / 60).toString().padStart(2, '0')
            val sec = (time % 60).toString().padStart(2, '0')
            validTime = "$min : $sec"
        }
    }

    private fun authTimeout() {
        if (otpDownCounter.isRun()) otpDownCounter.stop()
        uiState = UiState.Error(code = ExceptionCode.EXCEPTION_CODE_OTP_TIMEOUT)
        authNumber = ""
        validTime = "00 : 00"
    }
}