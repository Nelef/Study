package com.inzisoft.ibks.view.dialog

import android.icu.text.SimpleDateFormat
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.inzisoft.ibks.FragmentRequest
import com.inzisoft.ibks.FragmentResult
import com.inzisoft.ibks.TakeType
import com.inzisoft.ibks.base.BaseDialogFragment
import com.inzisoft.ibks.data.internal.AuthDialogData
import com.inzisoft.ibks.setFragmentResult
import com.inzisoft.ibks.view.compose.*
import com.inzisoft.ibks.view.compose.theme.*
import com.inzisoft.ibks.viewmodel.DirectInputUiState
import com.inzisoft.ibks.viewmodel.DirectInputViewModel
import dagger.hilt.android.AndroidEntryPoint
import com.inzisoft.ibks.R
import com.inzisoft.ibks.base.BaseDialogFragmentViewModel

@AndroidEntryPoint
class DirectInputDialogFragment: BaseDialogFragment() {
    private val viewModel: DirectInputViewModel by viewModels()

    init {
        baseCompose.content = {

            BasicBottomDialog(
                title = stringResource(id = R.string.direct_idcard_input),
                modifier = Modifier.width(1232.dp),
                onClose = { findNavController().navigateUp() },
                visible = true
            ) {
                val isIdCard = when (viewModel.directInputUiState) {
                    is DirectInputUiState.OVERSEA,
                    is DirectInputUiState.IDCARD -> true
                    else -> false
                }

                Spacer(modifier = Modifier.height(23.dp))

                Column(Modifier.fillMaxWidth()) {
                    if(viewModel.directInputUiState != DirectInputUiState.OVERSEA) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectableGroup(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            ChoiceButton(
                                selected = isIdCard,
                                onClick = { viewModel.selectChoiceButton(DirectInputUiState.IDCARD) },
                                text = stringResource(id = R.string.jumin_card)
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            ChoiceButton(
                                selected = !isIdCard,
                                onClick = { viewModel.selectChoiceButton(DirectInputUiState.DRIVE_LICENSE) },
                                text = stringResource(id = R.string.drive_license_card)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(26.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier.weight(1.0f),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Image(
                                painter = painterResource(
                                    id = if (isIdCard) {
                                        R.drawable.img_guide_resident
                                    } else {
                                        R.drawable.img_guide_driver
                                    }
                                ),
                                contentDescription = ""
                            )
                        }

                        Box(
                            modifier = Modifier.weight(1.0f),
                            contentAlignment = Alignment.Center
                        ) {

                            Column {
                                Text(
                                    text = buildAnnotatedString {
                                        append("(")
                                        withStyle(style = SpanStyle(color = MaterialTheme.colors.point4Color)) {
                                            append("*")
                                        }
                                        append(")")
                                        append(stringResource(id = R.string.direct_input_guide))
                                    },
                                    style = MaterialTheme.typography.subtitle1,
                                    color = MaterialTheme.colors.sub1Color
                                )
                                Spacer(modifier = Modifier.height(20.dp))
                                val authData = viewModel.authDataState
                                IdCardDetail(
                                    authData = authData,
                                    authCameraData = viewModel.authCameraData,
                                    onDatePickerShow = { key ->
                                        showDatePicker { date ->
                                            authData.dataMap[key] =
                                                SimpleDateFormat("yyyyMMdd").format(date)
                                        }
                                    }
                                )

                                if(isIdCard) {
                                    Spacer(modifier = Modifier.height(73.dp))

                                    Row {
                                        Spacer(modifier = Modifier.width(132.dp))
                                        RoundImageButton(
                                            onClick = { viewModel.showAuthGuideDialog() },
                                            text = stringResource(R.string.button_text_show_auth_guide_popup)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(90.dp))

                    Row(
                        modifier = Modifier
                            .padding(top = 40.dp)
                            .fillMaxWidth()
                            .wrapContentWidth(align = Alignment.CenterHorizontally),
                        horizontalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        GrayColorButton(
                            modifier = Modifier.size(240.dp, 56.dp),
                            onClick = { findNavController().navigateUp() },
                            text = stringResource(id = R.string.cancel),
                            buttonStyle = ButtonStyle.Dialog
                        )

                        ColorButton(
                            modifier = Modifier.size(240.dp, 56.dp),
                            onClick = { viewModel.auth(TakeType.DIRECT_INPUT) {
                                    result ->
                                if (result) {
                                    setFragmentResult(FragmentRequest.DirectInput, FragmentResult.OK(null))
                                    findNavController().navigateUp()
                                }
                            } },
                            text = stringResource(id = R.string.camera_auth),
                            buttonStyle = ButtonStyle.Dialog
                        )
                    }

                    Spacer(modifier = Modifier.height(48.dp))
                }
            }
        }

        baseCompose.surface = {
            when(val dialogState = viewModel.dialogState) {
                is AuthDialogData.AuthFailedPopup -> {
                    AlertDialog(
                        contentText = buildAnnotatedString {
                            append(dialogState.message)
                        },
                        onRightBtnClick = {
                            viewModel.dialogState = AuthDialogData.None
                        })
                }
                is AuthDialogData.Loading -> { Loading() }

                is AuthDialogData.ShowAuthGuidePopup -> {
                    CameraAuthGuidePopup {
                        viewModel.closeAuthGuideDialog()
                    }
                }
                is AuthDialogData.None -> {}
            }
        }
    }

    override fun getBaseViewModel(): BaseDialogFragmentViewModel? {
        return viewModel
    }
}
