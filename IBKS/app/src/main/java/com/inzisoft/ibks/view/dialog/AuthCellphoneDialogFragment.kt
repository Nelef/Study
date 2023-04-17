package com.inzisoft.ibks.view.dialog

import android.os.Bundle
import android.view.View
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.inzisoft.ibks.*
import com.inzisoft.ibks.base.BaseDialogFragment
import com.inzisoft.ibks.base.Right
import com.inzisoft.ibks.base.UiState
import com.inzisoft.ibks.data.internal.AlertData
import com.inzisoft.ibks.view.compose.AuthCellPhoneDialog
import com.inzisoft.ibks.view.compose.Loading
import com.inzisoft.ibks.viewmodel.AuthCellPhoneViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AuthCellphoneDialogFragment : BaseDialogFragment() {

    val viewModel: AuthCellPhoneViewModel by viewModels()

    init {
        baseCompose.content = {
            AuthCellPhoneDialog(
                visible = true,
                cellPhone = viewModel.cellphone,
                onClose = {
                    viewModel.alert(
                        AlertData(
                            contentText = getString(R.string.close_popup_auth_applicant_cell_phone),
                            leftBtnText = getString(R.string.cancel),
                            rightBtnText = getString(R.string.confirm),
                            onDismissRequest = { state ->
                                if (state == Right) {
                                    cancel()
                                }
                                viewModel.dismissAlert()
                            }
                        )
                    )
                },
                onRequestCellphoneAuth = {
                    hideKeyboard()
                    viewModel.issueOTP()
                },
                authNumber = viewModel.authNumber,
                onAuthNumberChange = { viewModel.onAuthNumChange(it) },
                validTime = viewModel.validTime,
                onConfirm = {
                    hideKeyboard()
                    viewModel.confirmOTP {
                        complete()
                    }
                },
                onRequestCellphoneAuthResend = {
                    hideKeyboard()
                    viewModel.issueOTP()
                },
                onTimeExtension = {
                    hideKeyboard()
                    viewModel.extensionOTP()
                },
            )
        }

        baseCompose.surface = {
            if (BuildConfig.DEBUG || BuildConfig.TEST_BUTTON) {
                Row {
                    Box(modifier = Modifier.weight(0.8f)) { }
                    Button(
                        modifier = Modifier.weight(0.2f),
                        colors = ButtonDefaults.buttonColors(Color.Black),
                        onClick = { complete() }) { Text("다음화면") }
                }
            }

            when (val state = viewModel.uiState) {
                is UiState.Error -> {
                    if (state.code == ExceptionCode.EXCEPTION_CODE_OTP_TIMEOUT) {
                        ShowAlert(getString(R.string.auth_number_timeout))
                    } else {
                        ShowAlert(state.message)
                    }
                }
                is UiState.Loading -> Loading()
                else -> {}
            }

            when (val popupState = viewModel.popupState.value) {
                is UiState.Success -> {
                    val popupData = popupState.data

                    ShowAlertDialog(
                        contentText = popupData.contentText,
                        leftBtnText = popupData.leftBtnText,
                        rightBtnText = popupData.rightBtnText,
                        onDismissRequest = popupData.onDismissRequest
                    )
                }
                else -> {}
            }
        }
    }

    @Composable
    private fun ShowAlert(message: String) {
        ShowAlertDialog(
            contentText = message,
            rightBtnText = getString(R.string.confirm),
            onDismissRequest = { viewModel.uiState = UiState.None })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.let {
            val arguments = AuthCellphoneDialogFragmentArgs.fromBundle(it)
            viewModel.load(arguments.cellphone)
        }
    }

    private fun complete() {
        finish()
        setFragmentResult(FragmentRequest.AuthCellPhone, FragmentResult.OK(Unit))
    }

    private fun cancel() {
        finish()
        setFragmentResult(FragmentRequest.AuthCellPhone, FragmentResult.Cancel())
    }

    private fun finish() {
        findNavController().navigateUp()
    }
}