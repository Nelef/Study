package com.inzisoft.ibks.viewmodel

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.inzisoft.ibks.BuildConfig
import com.inzisoft.ibks.PathManager
import com.inzisoft.ibks.R
import com.inzisoft.ibks.base.BaseViewModel
import com.inzisoft.ibks.data.internal.OpenSourceLicense
import com.inzisoft.ibks.data.internal.PenData
import com.inzisoft.ibks.data.remote.model.ApiResult
import com.inzisoft.ibks.data.repository.ApplicationUpdateRepository
import com.inzisoft.ibks.data.repository.LocalRepository
import com.inzisoft.ibks.util.VersionChecker
import com.inzisoft.ibks.util.log.QLog
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingViewModel @Inject constructor(
    private val applicationUpdateRepository: ApplicationUpdateRepository,
    @ApplicationContext private val context: Context
) : BaseViewModel() {

    var settingState: SettingState by mutableStateOf(SettingState.None)
    var versionData: ApplicationVersionData by mutableStateOf(ApplicationVersionData())
    var contentUiState: ContentUiState by mutableStateOf(ContentUiState.ShowVersionInfo)
    val openSourceLicenseList by lazy {
        val list = context.resources.assets.list("licenses/")
        val openSourceLicense = mutableListOf<OpenSourceLicense>()
        list?.forEach { licenseFileName ->
            val title = licenseFileName.substring(0, licenseFileName.indexOf('.')).replace('_', ' ')
            openSourceLicense.add(OpenSourceLicense(title = title, fileName = licenseFileName))
        }
        openSourceLicense
    }

    init {
        checkUpdate()
    }

    fun showAlert(message: String) {
        settingState = SettingState.ShowAlert(message)
    }

    private fun checkUpdate() = viewModelScope.launch(Dispatchers.IO) {
        applicationUpdateRepository.checkVersion().collectLatest { result ->
            val current = BuildConfig.VERSION_NAME
            var new: String

            when (result) {
                is ApiResult.Loading -> {}
                is ApiResult.Success -> {
                    new = result.data.version
                    QLog.i("현재 버전 : v$current 최신 버전 : v${new}")

                    versionData =
                        ApplicationVersionData(
                            currentVersion = current,
                            needUpdate = VersionChecker.compareVersion(current, new) == 1,
                            latestVersion = result.data.version,
                            apkName = result.data.name,
                            fileRefNo = result.data.fileRefNo
                        )
                }
                is ApiResult.Error -> {
                    showAlert(result.message)
                    new = "알 수 없음"
                    QLog.i("현재 버전 : v$current 최신 버전 : v${new}")

                    versionData =
                        ApplicationVersionData(
                            currentVersion = current,
                            needUpdate = false,
                            latestVersion = new,
                            apkName = "null",
                            fileRefNo = "null"
                        )
                }
            }
        }
    }

    fun versionInfo() {
        contentUiState = ContentUiState.ShowVersionInfo
    }

    fun openSourceLicense() {
        contentUiState = ContentUiState.ShowOpenSourceLicense
    }

    fun getOpenSourceLicenseItemList(): List<OpenSourceLicense> {
        return openSourceLicenseList
    }
}

sealed class SettingState {
    object None : SettingState()
    data class ShowAlert(val message: String) : SettingState()
}

sealed class ContentUiState {
    object ShowVersionInfo : ContentUiState()
    object ShowOpenSourceLicense : ContentUiState()
}

data class ApplicationVersionData(
    val needUpdate: Boolean = false,
    val currentVersion: String = "",
    val latestVersion: String = "",
    val apkName: String = "",
    val fileRefNo: String = ""
)