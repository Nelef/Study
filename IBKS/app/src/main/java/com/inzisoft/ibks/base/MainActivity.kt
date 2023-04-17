package com.inzisoft.ibks.base

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.webkit.WebView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.inzisoft.ibks.BuildConfig
import com.inzisoft.ibks.R
import com.inzisoft.ibks.data.internal.DialogData
import com.inzisoft.ibks.data.internal.RecordState
import com.inzisoft.ibks.databinding.ActivityMainBinding
import com.inzisoft.ibks.util.RecordService
import com.inzisoft.ibks.util.log.QLog
import com.inzisoft.ibks.view.compose.AlertDialog
import com.inzisoft.ibks.view.compose.BasicDialog
import com.inzisoft.ibks.view.compose.theme.IBKSTheme
import com.nprotect.security.inapp.IxBackgroundRestrictedException
import com.nprotect.security.inapp.IxSecureManagerHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlin.system.exitProcess


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    val viewModel: MainActivityViewModel by viewModels()
    private lateinit var binding: ActivityMainBinding

    private val PERMISSIONS = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        // SIM 전화번호 추출 : 사용자의 안드로이드 버전에 따라 권한을 다르게 요청
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Manifest.permission.READ_PHONE_NUMBERS
        } else {
            Manifest.permission.READ_PHONE_STATE
        }
    )

    // bindService
    private var recordService: RecordService? = null
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            QLog.i("recordService 연결")
            this?.run {
                val b = service as RecordService.RecordServiceBinder
                recordService = b.getService().apply {
                    startRecording(this)
                }
                viewModel.onRecordStart()
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            QLog.i("recordService 연결끊김")
            viewModel.onRecordStop()
            recordService = null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        if (BuildConfig.DEBUG) {
            WebView.setWebContentsDebuggingEnabled(true)
        }

        hideSystemUI()
        binding = ActivityMainBinding.inflate(layoutInflater)

        binding.composeView.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                IBKSTheme {
                    viewModel.basicDialogData?.let {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0x33000000))
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) { },
                            contentAlignment = Alignment.Center
                        ) {
                            ShowBaseDialog(dialogData = it)
                        }
                    }

                    viewModel.alertDialogData?.let {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0x33000000))
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) { },
                            contentAlignment = Alignment.Center
                        ) {
                            ShowAlertDialog(dialogData = it)
                        }
                    }

                    when (val vaccineState = viewModel.vaccineState) {
                        is UiState.Error -> {
                            if (vaccineState.throwable is IxBackgroundRestrictedException) {
                                showVaccinePermissionRequiredPopup()
                            } else {
                                showDetectedMalwareErrorPopup(vaccineState.message)
                            }
                        }
                        else -> {}
                    }

                    if (viewModel.expiredSession.value) {
                        ShowAlertExpiredSession()
                    }
                }
            }
        }

        setContentView(binding.root)
        registerNetworkCallback()
        checkPermissions(PERMISSIONS, 1)
        viewModel.runVaccineModule(applicationContext)
    }

    override fun onDestroy() {
        unRegisterNetworkCallback()
        IxSecureManagerHelper.getInstance().stop(applicationContext)
        recordCancel()
        super.onDestroy()
    }

    private fun registerNetworkCallback() {
        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return

        connectivityManager.activeNetwork?.let { network ->
            connectivityManager.getNetworkCapabilities(network)?.apply {
                if (hasTransport(NetworkCapabilities.TRANSPORT_WIFI) || hasTransport(
                        NetworkCapabilities.TRANSPORT_CELLULAR
                    )
                ) {
                    viewModel.networkAvailable = true
                }
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            connectivityManager.registerDefaultNetworkCallback(networkCallback)
        }
    }

    private fun unRegisterNetworkCallback() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val connectivityManager =
                getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return
            connectivityManager.unregisterNetworkCallback(networkCallback)
        }
    }

    private fun hideSystemUI() {
        with(WindowCompat.getInsetsController(window, window.decorView)) {
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            hide(WindowInsetsCompat.Type.systemBars())
        }
    }

    fun networkAvailable(): Boolean {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
            val connectivityManager =
                getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
                    ?: return false

            connectivityManager.activeNetwork?.let { network ->
                connectivityManager.getNetworkCapabilities(network)?.apply {
                    if (hasTransport(NetworkCapabilities.TRANSPORT_WIFI) || hasTransport(
                            NetworkCapabilities.TRANSPORT_CELLULAR
                        )
                    ) {
                        viewModel.networkAvailable = true
                    }
                }
            }
        }


        return viewModel.networkAvailable
    }

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            viewModel.networkAvailable = true
            Log.i("NetworkCallback", "network available")
        }

        override fun onLost(network: Network) {
            viewModel.networkAvailable = false
            Log.e("NetworkCallback", "network unavailable")
        }
    }

    fun hideKeyboard() {
        this.currentFocus?.apply {
            val inputMethodManager =
                getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(windowToken, 0)
        }
    }

    fun showBasicDialog(dialogData: DialogData) {
        viewModel.basicDialogData = dialogData
    }

    fun dismissBasicDialog() {
        viewModel.requestDismissBaseDialog(Cancel)
    }

    fun showAlertDialog(dialogData: DialogData) {
        viewModel.alertDialogData = dialogData
    }

    fun dismissAlertDialog() {
        viewModel.requestDismissAlertDialog(Cancel)
    }

    @Composable
    fun ShowBaseDialog(dialogData: DialogData) {
        BasicDialog(
            titleText = dialogData.titleText,
            contentText = dialogData.contentText,
            onClosed = { viewModel.requestDismissBaseDialog(Cancel) },
            leftBtnText = dialogData.leftBtnText,
            onLeftBtnClick = { viewModel.requestDismissBaseDialog(Left) },
            rightBtnText = dialogData.rightBtnText,
            onRightBtnClick = { viewModel.requestDismissBaseDialog(Right) }
        )
    }

    @Composable
    fun ShowAlertDialog(dialogData: DialogData) {
        AlertDialog(
            contentText = dialogData.contentText,
            leftBtnText = dialogData.leftBtnText,
            onLeftBtnClick = { viewModel.requestDismissAlertDialog(Left) },
            rightBtnText = dialogData.rightBtnText,
            onRightBtnClick = { viewModel.requestDismissAlertDialog(Right) }
        )
    }

    @Composable
    fun ShowAlertExpiredSession() {
        viewModel.alertDialogData = DialogData(
            titleText = "",
            contentText = buildAnnotatedString {
                append(stringResource(id = R.string.expired_session))
            },
            rightBtnText = stringResource(id = R.string.confirm),
            onDismissRequest = {
                restartApplication()
            }
        )
    }

    fun showSurface(test: @Composable (() -> Unit)?) {
        binding.composeView.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                IBKSTheme {
                    test?.invoke()
                }
            }
        }
    }

    private fun restartApplication() {
        startActivity(
            Intent.makeRestartActivityTask(
                packageManager.getLaunchIntentForPackage(
                    packageName
                )?.component
            )
        )
        exitProcess(0)
    }

    fun quitApplication() {
        this@MainActivity.finish()
        exitProcess(0)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        if (requestCode == 1) {
            // 안드로이드 13이상일 시, 그 권한이 WRITE_EXTERNAL_STORAGE 이라면 스킵.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                for (permissionIdx in permissions.indices) {
                    if (permissions[permissionIdx] == Manifest.permission.WRITE_EXTERNAL_STORAGE) {
                        grantResults[permissionIdx] = PackageManager.PERMISSION_GRANTED
                    }
                }
            }
            val isGranted: Boolean = checkUserAcceptPermissions(grantResults)
            if (!isGranted) {
                showAlertDialog(
                    DialogData(
                        titleText = getString(R.string.toast_msg_splash_permission_need_title),
                        contentText = buildAnnotatedString {
                            append(getString(R.string.toast_msg_splash_permission_denied))
                        },
                        rightBtnText = getString(R.string.move_to_app_setting),
                        onDismissRequest = {
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                .setData(Uri.parse("package:" + BuildConfig.APPLICATION_ID))
                            startActivity(intent)
                            finish()
                        }
                    )
                )
            }
        }
    }

    private fun checkPermissions(
        permissions: Array<String>,
        requestCode: Int
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            var result: Int
            val permissionList: MutableList<String> = ArrayList()
            for (pm in permissions) {
                result = ContextCompat.checkSelfPermission(this, pm)
                if (result != PackageManager.PERMISSION_GRANTED) {
                    permissionList.add(pm)
                }
            }
            if (permissionList.isNotEmpty()) {
                requestPermissions(permissionList.toTypedArray(), requestCode)
            }
        }
    }

    private fun checkUserAcceptPermissions(grantResults: IntArray): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true
        }
        if (grantResults.isEmpty()) {
            return false
        }
        for (result in grantResults) {
            if (result == PackageManager.PERMISSION_DENIED) {
                return false
            }
        }
        return true
    }

    private fun showVaccinePermissionRequiredPopup() {
        val appName = applicationContext.applicationInfo.loadLabel(packageManager).toString()
        showBasicDialog(
            DialogData(
                titleText = getString(R.string.alert_vaccine_permission_need_title),
                contentText = buildAnnotatedString {
                    append(
                        String.format(
                            getString(R.string.alert_vaccine_permission_restricted_message),
                            appName
                        )
                    )
                },
                rightBtnText = getString(R.string.move_to_app_setting),
                onDismissRequest = {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        .setData(Uri.parse("package:" + BuildConfig.APPLICATION_ID))
                    startActivity(intent)
                    finish()
                }
            )
        )
    }

    private fun showDetectedMalwareErrorPopup(message: String) {
        showBasicDialog(
            DialogData(
                titleText = getString(R.string.alert),
                contentText = buildAnnotatedString {
                    append(getString(R.string.alert_detect_spyware_message, message))
                },
                rightBtnText = getString(R.string.confirm),
                onDismissRequest = {
                    quitApplication()
                }
            )
        )
    }

    fun recordStart() {
        val filePath = viewModel.generateRecordFilePath()

        if (filePath.isEmpty()) {
            QLog.e("fail start recording. record path is empty")
            Toast.makeText(this, "녹취 시작 실패(record 정보 부족)", Toast.LENGTH_SHORT).show()
        } else {
            this?.run {
                val intent = Intent(this, RecordService::class.java)
                    .putExtra(RecordService.INTENT_PATH, filePath)
                bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
            }
        }
    }

    fun recordResume() {
        recordService?.resumeRecording()
        viewModel.onRecordStart()
    }

    fun recordPause() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            recordService?.pauseRecording()
            viewModel.onRecordPause()
        } else {
            Toast.makeText(this, "안드로이드 7 이상 지원 기능입니다.", Toast.LENGTH_SHORT).show()
        }
    }

    fun recordStop() {
        if (viewModel.recordState.value != RecordState.None) {
            this?.unbindService(serviceConnection)
            viewModel.onRecordStop()
        }
        Toast.makeText(
            this,
            R.string.finish_record_message,
            Toast.LENGTH_LONG
        ).show()
    }

    fun recordCancel() {
        if (viewModel.showFloatingRecordButton) {
            if (viewModel.recordState.value != RecordState.None) {
                this?.unbindService(serviceConnection)
                viewModel.onRecordCancel()
            } else {
                viewModel.showFloatingRecordButton = false
            }
            Toast.makeText(
                this,
                R.string.cancel_record_message,
                Toast.LENGTH_LONG
            ).show()
        }
    }
}
