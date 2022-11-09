package com.inzisoft.ibks.base

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.webkit.WebView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.inzisoft.ibks.BuildConfig
import com.inzisoft.ibks.R
import com.inzisoft.ibks.data.internal.DialogData
import com.inzisoft.ibks.databinding.ActivityMainBinding
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
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

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
                        ShowBaseDialog(dialogData = it)
                    }

                    viewModel.alertDialogData?.let {
                        ShowAlertDialog(dialogData = it)
                    }

                    when (val vaccineState = viewModel.vaccineState) {
                        is UiState.Error -> {
                            if (vaccineState.exception is IxBackgroundRestrictedException) {
                                showVaccinePermissionRequiredPopup()
                            } else {
                                showDetectedMalwareErrorPopup(vaccineState.message)
                            }
                        }
                        else -> {}
                    }

                    // TODO 세션
//                    if (viewModel.expiredSession.value) {
//                        ShowAlertExpiredSession()
//                    }
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
        super.onDestroy()
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

    fun quitApplication() {
        this@MainActivity.finish()
        exitProcess(0)
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

    private fun hideSystemUI() {
        with(WindowCompat.getInsetsController(window, window.decorView)) {
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            hide(WindowInsetsCompat.Type.systemBars())
        }
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
            QLog.i("network available")
        }

        override fun onLost(network: Network) {
            viewModel.networkAvailable = false
            QLog.e("network unavailable")
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        if (requestCode == 1) {
            val isGranted: Boolean = checkUserAcceptPermissions(grantResults)
            if (isGranted) {
            } else {
                Toast.makeText(
                    this,
                    R.string.toast_msg_splash_permission_denied,
                    Toast.LENGTH_SHORT
                ).show()
                // TODO android 13에서 write 권한 사라짐.(write 권한은 안써도 사용 가능..)
//                finish()
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
                contentText = String.format(
                    getString(R.string.alert_vaccine_permission_restricted_message),
                    appName
                ),
                rightBtnText = getString(R.string.confirm),
                onDismissRequest = {
                    quitApplication()
                }
            )
        )
    }

    private fun showDetectedMalwareErrorPopup(message: String) {
        showBasicDialog(
            DialogData(
                titleText = getString(R.string.alert),
                contentText = getString(R.string.alert_detect_spyware_message, message),
                rightBtnText = getString(R.string.confirm),
                onDismissRequest = {
                    quitApplication()
                }
            )
        )
    }
}