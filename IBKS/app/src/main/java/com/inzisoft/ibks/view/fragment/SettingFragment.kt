package com.inzisoft.ibks.view.fragment

import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.inzisoft.ibks.R
import com.inzisoft.ibks.base.BaseFragment
import com.inzisoft.ibks.base.Right
import com.inzisoft.ibks.data.internal.OpenSourceLicense
import com.inzisoft.ibks.data.internal.Setting
import com.inzisoft.ibks.view.compose.BasicTopBar
import com.inzisoft.ibks.view.compose.ButtonStyle
import com.inzisoft.ibks.view.compose.ColorButton
import com.inzisoft.ibks.view.compose.theme.*
import com.inzisoft.ibks.viewmodel.ApplicationVersionData
import com.inzisoft.ibks.viewmodel.ContentUiState
import com.inzisoft.ibks.viewmodel.SettingState
import com.inzisoft.ibks.viewmodel.SettingViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingFragment : BaseFragment() {

    private val viewModel: SettingViewModel by viewModels()

    init {
        baseCompose.topBar = {
            BasicTopBar(title = stringResource(id = R.string.setting), onBack = {
                findNavController().navigateUp()
            })
        }

        baseCompose.content = {
            SettingScreen(
                listOf(
                    Setting(R.string.settting_title_1, R.drawable.micon_update, R.drawable.micon_update_on) {
                        viewModel.versionInfo()
                    },
                    Setting(R.string.settting_title_5, R.drawable.micon_opens, R.drawable.micon_opens_on) {
                        viewModel.openSourceLicense()
                    }
                ),
                viewModel.getOpenSourceLicenseItemList(),
                viewModel.versionData
            )
        }

        baseCompose.surface = {
            when (val state = viewModel.settingState) {
                is SettingState.ShowAlert -> showAlertDialog(
                    contentText = state.message,
                    rightBtnText = stringResource(
                        id = R.string.confirm
                    ),
                    onDismissRequest = {
                        viewModel.settingState = SettingState.None
                    })
                is SettingState.None -> {}
            }
        }
    }

    @Composable
    fun SettingScreen(
        settingMenuList: List<Setting>,
        openSourceLicenseList: List<OpenSourceLicense>,
        applicationVersion: ApplicationVersionData
    ) {
        Row {
            SettingMenuScreen(list = settingMenuList)
            SettingContentScreen(
                applicationVersion = applicationVersion,
                onQuitApp = {
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
                },
                openSourceLicenseList = openSourceLicenseList
            )
        }
    }

    @Composable
    fun SettingMenuScreen(list: List<Setting>) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .width(320.dp)
                .background(MaterialTheme.colors.unfocusedColor)
                .padding(PaddingValues(vertical = 48.dp))
        ) {
            val modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
            LazyColumn {
                items(list) { item ->
                    val interactionSource = remember { MutableInteractionSource() }
                    val isPressed by interactionSource.collectIsPressedAsState()
                    val icon = if (isPressed) item.pressedIcon else item.defaultIcon
                    val backgroundColor =
                        if (isPressed) MaterialTheme.colors.point1Color else Color.Transparent
                    Row(
                        modifier = Modifier
                            .clickable(
                                interactionSource = interactionSource,
                                indication = LocalIndication.current
                            ) { item.onSelected() }
                            .then(modifier)
                            .background(backgroundColor),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 36.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                modifier = Modifier.padding(end = 12.dp),
                                painter = painterResource(id = icon),
                                contentDescription = "",
                            )

                            Text(
                                text = stringResource(id = item.titleResId),
                                style = MaterialTheme.typography.h6,
                                color = if (isPressed) MaterialTheme.colors.background3Color
                                else MaterialTheme.colors.sub1Color
                            )
                        }
                    }

                    Divider(color = MaterialTheme.colors.sub2Color, thickness = 1.dp)
                }
            }
        }
    }

    @Composable
    fun SettingContentScreen(
        applicationVersion: ApplicationVersionData,
        openSourceLicenseList: List<OpenSourceLicense>,
        onQuitApp: () -> Unit
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background3Color),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (viewModel.contentUiState) {
                is ContentUiState.ShowVersionInfo -> {
                    AppVersionInfo(
                        currentVersion = applicationVersion.currentVersion,
                        latestVersion = applicationVersion.latestVersion,
                        onQuitApp = onQuitApp,
                        enabledUpdateBtn = applicationVersion.needUpdate
                    )
                }
                is ContentUiState.ShowOpenSourceLicense -> {
                    OpensourceLicenseContent(list = openSourceLicenseList)
                }
            }
        }
    }

    @Composable
    fun OpensourceLicenseContent(list: List<OpenSourceLicense>) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(PaddingValues(vertical = 48.dp)),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
            val listState = rememberLazyListState()
            var licenseFileName by remember { mutableStateOf("") }

            Text(
                modifier = Modifier
                    .padding(top = 48.dp),
                text = stringResource(id = R.string.settting_title_5),
                color = MaterialTheme.colors.point1Color,
                style = MaterialTheme.typography.h1
            )

            LazyColumn(
                modifier = Modifier
                    .width(704.dp)
                    .padding(top = 36.dp),
                state = listState
            ) {
                items(list) { item ->
                    val isSelected = licenseFileName.isNotEmpty() && licenseFileName == item.fileName
                    val icon = if(isSelected) R.drawable.micon_close else R.drawable.micon_open
                    Divider(color = MaterialTheme.colors.sub2Color, thickness = 1.dp)

                    Row(
                        modifier = Modifier
                            .selectable(
                                selected = item.fileName == licenseFileName,
                                onClick = {
                                    licenseFileName = if (licenseFileName != item.fileName)
                                        item.fileName else ""
                                })
                            .then(modifier),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.h6,
                                color = MaterialTheme.colors.textColor
                            )

                            Image(
                                modifier = Modifier.padding(end = 12.dp),
                                painter = painterResource(id = icon),
                                contentDescription = "",
                            )
                        }
                    }

                    AnimatedVisibility(isSelected) {
                        LicenseWebView(licenseFileName = licenseFileName)
                    }
                }
            }
        }
    }

    @Composable
    private fun LicenseWebView(licenseFileName: String) {
        var onDestroy by remember { mutableStateOf(false) }

        DisposableEffect(key1 = onDestroy) {
            onDispose {
                onDestroy = true
            }
        }

        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                WebView(context).apply {

                    settings.apply {
                        cacheMode = WebSettings.LOAD_NO_CACHE
                    }

                    webViewClient = WebViewClient()
                    webChromeClient = WebChromeClient()
                }
            }, update = { webView ->
                if (onDestroy) {
                    webView.destroy()
                    return@AndroidView
                }

                webView.loadUrl(
                    "file:///android_asset/licenses/$licenseFileName"
                )
            })
    }

    @Composable
    fun AppVersionInfo(
        currentVersion: String,
        latestVersion: String,
        onQuitApp: () -> Unit,
        enabledUpdateBtn: Boolean
    ) {
        Column(
            modifier = Modifier.padding(top = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                modifier = Modifier.size(224.dp, 56.dp),
                painter = painterResource(id = R.drawable.logo_ibks_c),
                contentDescription = ""
            )

            Text(
                modifier = Modifier.padding(top = 16.dp),
                text = stringResource(id = R.string.ibks_title_message),
                color = MaterialTheme.colors.grayPressedColor,
                style = MaterialTheme.typography.h1
            )

            Box(
                modifier = Modifier
                    .size(width = 520.dp, height = 168.dp)
                    .padding(top = 48.dp)
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colors.sub2Color,
                        shape = RectangleShape
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 36.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            modifier = Modifier.padding(end = 66.dp),
                            text = stringResource(id = R.string.current_version),
                            style = MaterialTheme.typography.subtitle1,
                            color = MaterialTheme.colors.sub1Color
                        )

                        Text(
                            modifier = Modifier.padding(end = 66.dp),
                            text = "v$currentVersion",
                            style = MaterialTheme.typography.h6,
                            color = MaterialTheme.colors.grayPressedColor
                        )
                    }

                    Divider(
                        modifier = Modifier.padding(vertical = 19.dp),
                        color = MaterialTheme.colors.sub2Color, thickness = 1.dp
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            modifier = Modifier.padding(end = 66.dp),
                            text = stringResource(id = R.string.latest_version),
                            style = MaterialTheme.typography.subtitle1,
                            color = MaterialTheme.colors.sub1Color
                        )

                        Text(
                            modifier = Modifier.padding(end = 66.dp),
                            text = "v$latestVersion",
                            style = MaterialTheme.typography.h6,
                            color = MaterialTheme.colors.point1Color
                        )
                    }
                }
            }
            if(enabledUpdateBtn) {
                Text(
                    modifier = Modifier
                        .padding(top = 48.dp),
                    textAlign = TextAlign.Center,
                    text = stringResource(id = R.string.setting_need_update_text),
                    style = MaterialTheme.typography.h6,
                    color = MaterialTheme.colors.point1Color
                )
            }
            ColorButton(
                enabled = enabledUpdateBtn,
                onClick = onQuitApp,
                modifier = Modifier.padding(top = 64.dp),
                text =
                if (enabledUpdateBtn)
                    stringResource(id = R.string.quit_app)
                else stringResource(
                    id = R.string.setting_need_not_update_text
                ),
                buttonStyle = ButtonStyle.Big
            )
        }
    }
}