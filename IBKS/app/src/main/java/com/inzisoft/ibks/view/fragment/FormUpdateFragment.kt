package com.inzisoft.ibks.view.fragment

import android.os.Handler
import android.os.Looper
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.inzisoft.ibks.R
import com.inzisoft.ibks.base.BaseFragment
import com.inzisoft.ibks.base.PopupState
import com.inzisoft.ibks.util.FileUtils
import com.inzisoft.ibks.view.compose.ButtonStyle
import com.inzisoft.ibks.view.compose.ColorButton
import com.inzisoft.ibks.view.compose.theme.*
import com.inzisoft.ibks.viewmodel.FormUpdateViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FormUpdateFragment : BaseFragment() {

    private val viewModel: FormUpdateViewModel by viewModels()

    init {

        baseCompose.content = {

            FormUpdateScreen(
                onStartUpdate = {
                    startUpdate()
                },
                current = viewModel.current,
                total = viewModel.total,
                progress = viewModel.progress
            )

            if (viewModel.updateComplete) {
               navigate(FormUpdateFragmentDirections.actionFormUpdateFragmentToMainFragment())
            }
        }
//        baseCompose.surface = {
//            if (BuildConfig.DEBUG) {
//                Row {
//                    FloatingActionButton(onClick = {
//                        viewModel.updateComplete = true // 임시 테스트
//                    }) {
//                        Text("다음화면")
//                    }
//                }
//            }
//        }
    }

    private fun startUpdate() {
        if (!availableNetwork()) {
            showAlert(getString(R.string.form_update_error_network), getString(R.string.retry)) {
                Handler(Looper.getMainLooper()).postDelayed({ startUpdate() }, 100)
            }
        } else if (!FileUtils.availableStorage(requireContext(), 200 * 1024 * 1024)) {
            showAlert(getString(R.string.form_update_error_storage)) {
                terminateApplication()
            }
        } else {
            viewModel.startUpdate()
        }
    }

    private fun showAlert(
        message: String,
        rightBtnText: String = getString(R.string.confirm),
        onDismissRequest: (state: PopupState) -> Unit = {}
    ) {
        showAlertDialog(
            contentText = message,
            rightBtnText = rightBtnText,
            onDismissRequest = onDismissRequest
        )
    }

    @Preview(widthDp = 1280, heightDp = 800)
    @Composable
    fun PreviewFormUpdateScreen2() {
        IBKSTheme {
            FormUpdateScreen(onStartUpdate = {})
        }
    }

    @Preview(widthDp = 1280, heightDp = 800)
    @Composable
    fun PreviewFormUpdateScreen3() {
        IBKSTheme {
            FormUpdateScreen(onStartUpdate = {}, current = 1, total = 10, progress = 0.5f)
        }
    }

    @Composable
    fun FormUpdateScreen(
        onStartUpdate: () -> Unit,
        current: Int = 0,
        total: Int = 0,
        progress: Float = 0f,
    ) {

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
                    text = stringResource(id = R.string.form_update_title),
                    color = MaterialTheme.colors.point1Color,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.h4
                )

                Text(
                    text = stringResource(id = R.string.form_update_subtitle),
                    modifier = Modifier.padding(top = 24.dp, bottom = 48.dp),
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    lineHeight = 1.5.em,
                    style = MaterialTheme.typography.subtitle1
                )

                Column(
                    modifier = Modifier
                        .padding(horizontal = 20.dp)
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colors.disableColor,
                            shape = RoundedCornerShape(0.dp)
                        )
                        .padding(top = 16.dp, start = 48.dp, end = 48.dp, bottom = 24.dp)
                        .align(Alignment.CenterHorizontally),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = stringResource(id = R.string.form_update_notice_title),
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        color = MaterialTheme.colors.point4Color,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.h6
                    )

                    Text(
                        text = stringResource(id = R.string.form_update_notice_item_1),
                        modifier = Modifier.padding(top = 16.dp),
                        fontWeight = FontWeight.Light,
                        lineHeight = 1.5.em,
                        style = MaterialTheme.typography.subtitle1
                    )

                    Text(
                        text = stringResource(id = R.string.form_update_notice_item_2),
                        modifier = Modifier.padding(top = 12.dp),
                        fontWeight = FontWeight.Light,
                        lineHeight = 1.5.em,
                        style = MaterialTheme.typography.subtitle1
                    )
                }

                if (current > 0 && total > 0) {
                    Column(
                        modifier = Modifier
                            .padding(top = 84.dp)
                            .height(56.dp)
                            .fillMaxWidth(0.375f)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = stringResource(
                                    id = R.string.form_update_ing,
                                    current,
                                    total
                                ),
                                style = MaterialTheme.typography.body1
                            )

                            Text(
                                text = "${(progress * 100).toInt()}%",
                                style = MaterialTheme.typography.body1
                            )
                        }


                        LinearProgressIndicator(
                            progress = progress, modifier = Modifier
                                .padding(top = 8.dp)
                                .height(8.dp)
                                .fillMaxWidth()
                        )
                    }
                } else {
                    ColorButton(
                        onClick = onStartUpdate,
                        modifier = Modifier
                            .padding(top = 84.dp)
                            .size(240.dp, 56.dp),
                        text = stringResource(id = R.string.form_update_start),
                        buttonStyle = ButtonStyle.Big
                    )
                }
            }
        }
    }

}