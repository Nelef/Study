package com.inzisoft.ibks.view.fragment

import android.os.Bundle
import android.view.View
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.inzisoft.ibks.R
import com.inzisoft.ibks.base.BaseFragment
import com.inzisoft.ibks.base.Right
import com.inzisoft.ibks.data.remote.model.ApplicationVersionResponse
import com.inzisoft.ibks.util.log.QLog
import com.inzisoft.ibks.view.compose.theme.background1Color
import com.inzisoft.ibks.view.compose.theme.mainColor
import com.inzisoft.ibks.view.compose.theme.sub1Color
import com.inzisoft.ibks.viewmodel.SplashCheckState
import com.inzisoft.ibks.viewmodel.SplashDialogState
import com.inzisoft.ibks.viewmodel.SplashViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SplashFragment : BaseFragment() {

    private val viewModel: SplashViewModel by viewModels()
    private var showProgressState by mutableStateOf(true)

    init {
        baseCompose.content = {
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
            SplashContentLayout()
        }
        baseCompose.surface = {
            when (viewModel.checkStepState) {
                SplashCheckState.NetworkCheck -> {
                    QLog.i("2. 네트워크 체크")

                    if (availableNetwork()) {
                        viewModel.checkApplicationVersion() // 앱버전 체크
                    } else {
                        ShowNetworkErrorPopup()
                    }
                }
                else -> {}
            }

            when (val dialogState = viewModel.dialogState) {
                is SplashDialogState.NeedApplicationUpdate -> {
                    NavigateApplicationUpdatePage(dialogState.applicationVersion)
                }
                is SplashDialogState.SplashCheckStatus -> {
                    SplashStateDialog(dialogState)
                }
                else -> {}
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.checkRootingStatus(requireContext())
        showProgressState = true
    }

    @Composable
    private fun SplashStateDialog(state: SplashDialogState) {
        when (state) {
            SplashDialogState.SplashCheckStatus.RootingCheckError -> ShowRootingErrorPopup()
            SplashDialogState.SplashCheckStatus.SplashWorkFinish -> NavigateLoginPage()
            else -> {

            }
        }
    }

    @Composable
    private fun NavigateLoginPage() {
        showProgressState = false
        // TODO 앱 업데이트 api 생성 후 작업
       navigate(SplashFragmentDirections.actionSplashFragmentToLoginFragment())
    }

    @Composable
    private fun NavigateApplicationUpdatePage(applicationVersion: ApplicationVersionResponse) {
        showProgressState = false
       navigate(
            SplashFragmentDirections.actionSplashFragmentToApplicationUpdateFragment(
                applicationVersion.version,
                applicationVersion.name,
                applicationVersion.fileRefNo,
                true
            )
        )
    }

    @Composable
    private fun ShowNetworkErrorPopup() {
        showBasicDialog(
            titleText = getString(R.string.alert),
            contentText = getString(R.string.alert_network_is_not_connected_message),
            rightBtnText = getString(R.string.retry),
            onDismissRequest = {
                if (availableNetwork()) {
                    viewModel.checkApplicationVersion() // 앱버전 체크
                } else {
                    viewModel.checkRootingStatus(requireContext())
                }
            }
        )
    }

    @Composable
    private fun ShowRootingErrorPopup() {
        showBasicDialog(
            titleText = getString(R.string.alert),
            contentText = getString(R.string.alert_detect_rooting_message),
            rightBtnText = getString(R.string.confirm),
            onDismissRequest = {
                terminateApplication()
            }
        )
    }

    @Preview(device = Devices.AUTOMOTIVE_1024p, widthDp = 1280, heightDp = 800)
    @Composable
    fun SplashContentLayout() {
        Box(
            modifier = Modifier
                .background(MaterialTheme.colors.background1Color)
                .fillMaxSize(),
        )
        {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            )
            {
                Image(
                    modifier = Modifier
                        .padding(top = 95.dp)
                        .size(width = 400.dp, height = 200.dp),
                    painter = painterResource(id = R.drawable.slogan_ibks),
                    contentDescription = null,
                    contentScale = ContentScale.FillWidth
                )

                Row(
                    modifier = Modifier
                        .padding(top = 48.dp)
                        .height(52.dp),
                    verticalAlignment = Alignment.Bottom,
                ) {
                    Image(
                        modifier = Modifier.size(width = 256.dp, height = 52.dp),
                        painter = painterResource(id = R.drawable.logo_ibks_c),
                        contentDescription = null,
                        contentScale = ContentScale.FillWidth
                    )

                    Text(
                        modifier = Modifier
                            .padding(start = 16.dp),
                        text = stringResource(R.string.ibks_title_message),
                        style = MaterialTheme.typography.h4,
                        color = MaterialTheme.colors.mainColor
                    )
                }

                Text(
                    modifier = Modifier.padding(top = 153.dp),
                    text = stringResource(R.string.splash_vaccine_processing_message),
                    style = MaterialTheme.typography.subtitle1,
                    color = MaterialTheme.colors.sub1Color,
                    textAlign = TextAlign.Center
                )

                if (showProgressState) {
                    CircularProgressIndicator(modifier = Modifier.padding(top = 48.dp))
                }
            }
        }
    }
}