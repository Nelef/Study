package com.inzisoft.ibks.base

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inzisoft.ibks.data.internal.DialogData
import com.inzisoft.ibks.util.log.QLog
import com.nprotect.security.inapp.IxBackgroundRestrictedException
import com.nprotect.security.inapp.IxSecureManagerHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

class MainActivityViewModel : ViewModel() {

    var networkAvailable = false

    var vaccineState by mutableStateOf<UiState<Unit>>(UiState.None)
    var basicDialogData by mutableStateOf<DialogData?>(null)
    var alertDialogData by mutableStateOf<DialogData?>(null)

    // TODO 로그인 api 생성 후 작업
//    val expiredSession = localRepository.expiredSession

    fun requestDismissBaseDialog(state: PopupState) {
        basicDialogData?.onDismissRequest?.invoke(state)
        basicDialogData = null
    }

    fun requestDismissAlertDialog(state: PopupState) {
        alertDialogData?.onDismissRequest?.invoke(state)
        alertDialogData = null
    }

    fun runVaccineModule(context: Context) = viewModelScope.launch(Dispatchers.IO) {
        try {
            IxSecureManagerHelper.getInstance().apply {
                setShowSplash(false)
                setScannerType(IxSecureManagerHelper.TYPE_SCANNER_PATTERN)
                setEventListener(object : IxSecureManagerHelper.IxSecureEventListener {
                    override fun onStatusChanged(status: Int) {
                        checkSecureServiceStatus(status)
                    }

                    override fun onMalwareFound(malwareCount: Int) {
                        QLog.w("malware count : $malwareCount", "Vaccine")
                    }

                    override fun onRealtimeMalwareFound(pkgName: String?) {
                        QLog.w("malware found : $pkgName", "Vaccine")
                        vaccineState = UiState.Error("malware found : $pkgName")
                    }

                    override fun onRemainMalware(remained: Boolean) {
                        if (remained) {
                            QLog.e("malware remained", "Vaccine")
                        }
                    }
                })
            }.start(context)
        } catch (e: IxBackgroundRestrictedException) {
            QLog.e("[Background Battery Permission] is required.", "Vaccine")
            vaccineState = UiState.Error("[Background Battery Permission] is required.", e)
        }
    }

    private fun checkSecureServiceStatus(status: Int) {
        var statusString: String? = null
        when (status) {
            IxSecureManagerHelper.STATUS_SERVICE_END -> statusString = "백신 서비스 종료"

            IxSecureManagerHelper.STATUS_SERVICE_START -> {
                vaccineState = UiState.Loading()
                statusString = "백신 서비스 시작"
            }

            IxSecureManagerHelper.STATUS_UPDATE_START -> statusString = "업데이트 시작"

            IxSecureManagerHelper.STATUS_UPDATE_FINISH -> statusString = "업데이트 종료"

            IxSecureManagerHelper.STATUS_SCAN_START -> statusString = "악성코드 검사 시작"

            IxSecureManagerHelper.STATUS_SCAN_FINISH -> {
                vaccineState = UiState.Success(Unit)
                statusString = "악성코드 검사 종료"
            }

            IxSecureManagerHelper.STATUS_UPDATE_TIMEOUT -> statusString = "네트워크가 불안정합니다."

            IxSecureManagerHelper.STATUS_UNSTABLE_CLOUD -> statusString = "클라우드 접속이 불안정합니다."

            IxSecureManagerHelper.STATUS_NO_PATTERN_DB -> statusString = "업데이트가 필요합니다."
        }

        QLog.i("$statusString", "Vaccine")
    }
}