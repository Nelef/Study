package com.inzisoft.ibks.view.fragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.activity.compose.BackHandler
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.inzisoft.ibks.R
import com.inzisoft.ibks.base.BaseFragment
import com.inzisoft.ibks.base.PopupState
import com.inzisoft.ibks.base.Right
import com.inzisoft.ibks.util.FileUtils
import com.inzisoft.ibks.util.log.QLog
import com.inzisoft.ibks.view.compose.ButtonStyle
import com.inzisoft.ibks.view.compose.ColorButton
import com.inzisoft.ibks.view.compose.theme.IBKSTheme
import com.inzisoft.ibks.view.compose.theme.disableColor
import com.inzisoft.ibks.view.compose.theme.mainColor
import com.inzisoft.ibks.view.compose.theme.point4Color
import com.inzisoft.ibks.viewmodel.ApplicationState
import com.inzisoft.ibks.viewmodel.ApplicationUpdateViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ApplicationUpdateFragment : BaseFragment() {

    private val viewModel: ApplicationUpdateViewModel by viewModels()

    init {

        baseCompose.content = {
            when (val state = viewModel.uiState) {
                is ApplicationState.Complete -> install(state.uri)
                is ApplicationState.Error -> {
                    showAlert(getString(R.string.application_update_fail, state.code))
                }
                else -> {}
            }

            ApplicationUpdateScreen(
                onInstall = {
                    val state =
                        viewModel.uiState as? ApplicationState.Complete
                            ?: return@ApplicationUpdateScreen
                    install(state.uri)
                },
                onStartUpdate = {
                    startUpdate()
                },
                current = viewModel.progress.downloaded,
                total = viewModel.progress.total,
                progress = viewModel.progress.progress
            )
        }
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
            viewModel.startUpdate(requireContext())
        }
    }

    private fun showAlert(
        message: String,
        rightBtnText: String = getString(R.string.confirm),
        onDismissRequest: (state: PopupState) -> Unit = { viewModel.init() }
    ) {
        showAlertDialog(
            contentText = message,
            rightBtnText = rightBtnText,
            onDismissRequest = onDismissRequest
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let {
            val args = ApplicationUpdateFragmentArgs.fromBundle(it)
            viewModel.load(args.version, args.name, args.fileRefNo, args.fromSplash)
        }
    }

    private fun install(uri: Uri) {
        QLog.i("start install")

        startActivity(
            Intent(Intent.ACTION_VIEW)
                .setDataAndType(uri, "application/vnd.android.package-archive")
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        )
    }

}

@Preview(widthDp = 1280, heightDp = 800)
@Composable
fun ApplicationUpdateScreen1() {
    IBKSTheme {
        ApplicationUpdateScreen(onInstall = {}, onStartUpdate = {})
    }
}

@Preview(widthDp = 1280, heightDp = 800)
@Composable
fun ApplicationUpdateScreen2() {
    IBKSTheme {
        ApplicationUpdateScreen(
            onInstall = {},
            onStartUpdate = {},
            current = 40.0f,
            total = 80f,
            progress = 0.5f
        )
    }
}

@Preview(widthDp = 1280, heightDp = 800)
@Composable
fun ApplicationUpdateScreen3() {
    IBKSTheme {
        ApplicationUpdateScreen(
            onInstall = {},
            onStartUpdate = {},
            current = 80f,
            total = 80f,
            progress = 1f
        )
    }
}

@Composable
fun ApplicationUpdateScreen(
    onInstall: () -> Unit,
    onStartUpdate: () -> Unit,
    current: Float = 0f,
    total: Float = 0f,
    progress: Float = 0f,
) {

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {

        Column(
            modifier = Modifier
                .fillMaxHeight()
                .align(Alignment.Center),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = stringResource(id = R.string.application_update_title),
                color = MaterialTheme.colors.mainColor,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.h1
            )

            Text(
                text = stringResource(id = R.string.application_update_subtitle),
                modifier = Modifier.padding(top = 24.dp, bottom = 96.dp),
                fontWeight = FontWeight.Medium,
                style = MaterialTheme.typography.subtitle1,
                textAlign = TextAlign.Center
            )

            Column(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colors.disableColor,
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(top = 16.dp, start = 48.dp, end = 48.dp, bottom = 24.dp)
                    .align(Alignment.CenterHorizontally),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(id = R.string.application_update_notice_title),
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    color = MaterialTheme.colors.point4Color,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.h6
                )

                Text(
                    text = stringResource(id = R.string.application_update_notice_item_1),
                    modifier = Modifier.padding(top = 16.dp),
                    fontWeight = FontWeight.Light,
                    style = MaterialTheme.typography.subtitle1
                )

                Text(
                    text = stringResource(id = R.string.application_update_notice_item_2),
                    modifier = Modifier.padding(top = 12.dp),
                    fontWeight = FontWeight.Light,
                    style = MaterialTheme.typography.subtitle1
                )
            }

            if (progress == 1f) {
                ColorButton(
                    onClick = onInstall,
                    modifier = Modifier
                        .width(320.dp)
                        .padding(top = 64.dp),
                    text = stringResource(id = R.string.application_update_install),
                    buttonStyle = ButtonStyle.Big
                )
            } else if (current > 0 && total > 0) {
                Column(
                    modifier = Modifier
                        .padding(top = 64.dp)
                        .fillMaxWidth(0.375f)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = stringResource(
                                id = R.string.application_update_ing,
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
                        .width(320.dp)
                        .padding(top = 64.dp),
                    text = stringResource(id = R.string.application_update_start),
                    buttonStyle = ButtonStyle.Big
                )
            }
        }
    }
}