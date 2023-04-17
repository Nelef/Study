package com.inzisoft.ibks.view.compose

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.inzisoft.ibks.R
import com.inzisoft.ibks.view.compose.theme.IBKSTheme
import com.inzisoft.ibks.view.compose.theme.disableColor
import com.inzisoft.ibks.view.compose.theme.point1Color
import com.inzisoft.ibks.view.compose.theme.point4Color

@Preview(widthDp = 1280, heightDp = 760)
@Composable
fun PreviewAuthCellPhoneDialog() {
    IBKSTheme {
        AuthCellPhoneDialog(
            visible = true,
            cellPhone = "010-1234-5678",
            onClose = { },
            onRequestCellphoneAuth = {},
            authNumber = "",
            onAuthNumberChange = {},
            validTime = "06:30",
            onConfirm = {},
            onRequestCellphoneAuthResend = {},
            onTimeExtension = {}
        )
    }
}

@Composable
fun AuthCellPhoneDialog(
    visible: Boolean,
    cellPhone: String,
    onClose: () -> Unit,
    onRequestCellphoneAuth: () -> Unit,
    onRequestCellphoneAuthResend: () -> Unit,
    authNumber: String,
    onAuthNumberChange: (String) -> Unit,
    invalidAuthNumMsg: String? = null,
    validTime: String?,
    onTimeExtension: () -> Unit,
    onConfirm: () -> Unit
) {
    val focusManager = LocalFocusManager.current

    BasicBottomDialog(
        title = stringResource(R.string.popup_auth_applicant_cell_phone_title),
        modifier = Modifier.size(840.dp, 639.dp),
        onClose = onClose,
        visible = visible
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(36.dp))

                Text(
                    textAlign = TextAlign.Center,
                    text = stringResource(
                        R.string.popup_auth_applicant_cell_phone_subtitle,
                        cellPhone
                    ), style = MaterialTheme.typography.h6
                )

                Spacer(modifier = Modifier.height(24.dp))

                ColorButton(
                    onClick = onRequestCellphoneAuth,
                    enabled = invalidAuthNumMsg == null && validTime == null,
                    text = stringResource(id = R.string.send_auth_number)
                )

                Spacer(modifier = Modifier.height(48.dp))

                Column(
                    modifier = Modifier
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colors.disableColor,
                            shape = RoundedCornerShape(0.dp)
                        )
                        .padding(top = 38.dp, start = 108.dp, end = 108.dp, bottom = 24.dp)
                        .align(Alignment.CenterHorizontally),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Input(
                        value = authNumber,
                        onValueChange = onAuthNumberChange,
                        enabled = validTime != null && validTime != "00 : 00",
                        placeholder = stringResource(R.string.request_write_auth_number_hint),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        keyboardActions = KeyboardActions {
                            focusManager.clearFocus()
                        },
                        errorMessage = invalidAuthNumMsg
                    )

                    Spacer(modifier = Modifier.height(15.dp))

                    validTime?.let {
                        Text(
                            text = stringResource(R.string.valid_time, it),
                            color = MaterialTheme.colors.point1Color,
                            style = MaterialTheme.typography.subtitle2
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RoundButton(
                            modifier = Modifier.size(76.dp, 28.dp),
                            onClick = onTimeExtension,
                            enabled = validTime == "00 : 00",
                            text = stringResource(R.string.valid_time_extension),
                            buttonStyle = ButtonStyle.Small
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        RoundButton(
                            modifier = Modifier.size(76.dp, 28.dp),
                            onClick = onRequestCellphoneAuthResend,
                            enabled = validTime != null,
                            text = stringResource(R.string.re_send_auth_number),
                            buttonStyle = ButtonStyle.Small
                        )
                    }
                }
                Spacer(modifier = Modifier.height(56.dp))

                Text(
                    text = stringResource(R.string.changed_mobile_number_hint),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colors.point4Color,
                    style = MaterialTheme.typography.body1
                )

                Spacer(modifier = Modifier.weight(1f))
            }

            Row(
                Modifier.height(84.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ColorDialogButton(
                    onClick = onConfirm,
                    modifier = Modifier.weight(1f),
                    enabled = authNumber.isNotEmpty() && validTime != "00 : 00",
                    text = stringResource(id = R.string.auth_number_confirm),
                    buttonStyle = ButtonStyle.Big
                )
            }
        }
    }
}
