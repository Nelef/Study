package com.inzisoft.ibks.viewmodel

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.inzisoft.ibks.BuildConfig
import com.inzisoft.ibks.PathManager
import com.inzisoft.ibks.base.BaseViewModel
import com.inzisoft.ibks.data.remote.model.ApplicationVersionResponse
import com.inzisoft.ibks.util.ValidChecker
import com.inzisoft.ibks.util.log.QLog
import com.nprotect.security.inapp.IxSecureManagerHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
//    private val applicationUpdateRepository: ApplicationUpdateRepository,
    pathManager: PathManager
) : BaseViewModel() {

    var dialogState by mutableStateOf<SplashDialogState>(SplashDialogState.None)
    var checkStepState by mutableStateOf<SplashCheckState>(SplashCheckState.None)


    init {
        QLog.init(pathManager.getLogDir())
    }

    fun checkRootingStatus(context: Context) = viewModelScope.launch(Dispatchers.IO) {
        checkStepState = SplashCheckState.RootingCheck
        QLog.i("1. 루팅 체크 시작")
        val isRooting = IxSecureManagerHelper.getInstance().checkRooting(context)
        if (isRooting) {
            QLog.i("=> 루팅됨")
            dialogState = SplashDialogState.SplashCheckStatus.RootingCheckError
        } else {
            QLog.i("=> 루팅 되지 않음")
            dialogState = SplashDialogState.None
            checkStepState = SplashCheckState.NetworkCheck
        }
    }

    fun checkApplicationVersion() = viewModelScope.launch(Dispatchers.IO) {
        checkStepState = SplashCheckState.AppVersionCheck
        QLog.i("3. 앱 버전 체크")

        // TODO 앱 업데이트 api 생성 후 삭제
        QLog.i("최신버전입니다. (v${BuildConfig.VERSION_NAME})")
        dialogState = SplashDialogState.SplashCheckStatus.SplashWorkFinish

        // TODO 앱 업데이트 api 생성 후 작업
//        applicationUpdateRepository.checkVersion().collect { result ->
//            try {
//                when (result) {
//                    is ApiResult.Loading -> {}
//                    is ApiResult.Success -> {
//
//                        val current = BuildConfig.VERSION_NAME
//                        val new = result.data.version
//                        QLog.i("현재 버전 : v$current 최신 버전 : v${result.data.version}")
//
//                        dialogState = if (compareVersion(current, new) == 1) {
//                            QLog.i("앱 버전 업데이트 필요 (v$current -> v$new)")
//                            SplashDialogState.NeedApplicationUpdate(result.data)
//                        } else {
//                            QLog.i("최신버전입니다. (v$current)")
//                            SplashDialogState.SplashCheckStatus.SplashWorkFinish
//                        }
//
//                    }
//                    is ApiResult.Error -> throw Exception(result.message)
//                }
//            } catch (e: Exception) {
//                // TODO: 업데이트 실패 알림 필요한지 여부확인
//                Firebase.crashlytics.recordException(e)
//                QLog.e("최신버전 확인 실패", e)
//                dialogState = SplashDialogState.SplashCheckStatus.SplashWorkFinish
//            }
//        }
    }

    fun compareVersion(current: String, new: String): Int {
        if (!ValidChecker.isValidVersion(current) || !ValidChecker.isValidVersion(new))
            throw IllegalArgumentException("invalid version (current : $current, new : $new)")

        if (current == new) return 0

        val currentVersions = current.split('.').map { it.toInt() }
        val newVersions = new.split('.').map { it.toInt() }

        if (currentVersions[0] < newVersions[0]) return 1

        if (currentVersions[1] < newVersions[1]) return 1

        if (currentVersions[2] < newVersions[2]) return 1

        return -1
    }
}

sealed class SplashDialogState {

    object None : SplashDialogState()
    data class Error(val code: Int) : SplashDialogState()
    data class NeedApplicationUpdate(val applicationVersion: ApplicationVersionResponse) :
        SplashDialogState()

    sealed class SplashCheckStatus : SplashDialogState() {
        object RootingCheckError : SplashCheckStatus()
        object SplashWorkFinish : SplashCheckStatus()
    }
}

sealed class SplashCheckState {
    object None : SplashCheckState()
    object RootingCheck : SplashCheckState()
    object NetworkCheck : SplashCheckState()
    object AppVersionCheck : SplashCheckState()
}