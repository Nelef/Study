package com.inzisoft.ibks.base

import android.content.Context
import android.media.MediaMetadataRetriever
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inzisoft.ibks.PathManager
import com.inzisoft.ibks.data.internal.DialogData
import com.inzisoft.ibks.data.internal.RecordData
import com.inzisoft.ibks.data.internal.RecordState
import com.inzisoft.ibks.data.repository.LocalRepository
import com.inzisoft.ibks.data.web.RecordOfVoice
import com.inzisoft.ibks.util.log.QLog
import com.nprotect.security.inapp.IxBackgroundRestrictedException
import com.nprotect.security.inapp.IxSecureManagerHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File
import java.util.*
import javax.inject.Inject
import kotlin.concurrent.timer

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    private val pathManager: PathManager,
    private val localRepository: LocalRepository
) : ViewModel() {

    var networkAvailable = false

    var vaccineState by mutableStateOf<UiState<Unit>>(UiState.None)
    var basicDialogData by mutableStateOf<DialogData?>(null)
    var alertDialogData by mutableStateOf<DialogData?>(null)

    val expiredSession = localRepository.expiredSession

    // 녹취
    var showFloatingRecordButton by mutableStateOf(false)
    var selectedRecordCode by mutableStateOf("")
    val records = mutableStateMapOf<String, List<RecordData>>()
    private val _recordState = mutableStateOf<RecordState>(RecordState.None)
    val recordState: State<RecordState> = _recordState
    private val _recordTime = mutableStateOf("00:00")
    val recordTime: State<String> = _recordTime
    private var _recordList = mutableStateListOf<RecordData>()
    val recordList: List<RecordData> = _recordList
    private lateinit var entryId: String
    var webRecords by mutableStateOf<List<RecordOfVoice>>(listOf())

    // timer
    private var time = 0
    private var timerTask: Timer? = null

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

    fun getEntryId() {
        viewModelScope.launch {
            entryId = localRepository.getAuthInfo().first().edocKey
        }
    }

    fun onRecordCreate() {
        records.clear()
        showFloatingRecordButton = true
    }

    fun onRecordStart() = viewModelScope.launch(Dispatchers.IO) {
        // 타이머 실행
        timerTask = timer(period = 1000) {
            time++

            val sec = "%02d".format(time % 60)
            val min = "%02d".format(time / 60)

            _recordTime.value = "$min : $sec"
        }

        _recordState.value = RecordState.Recoding
    }

    fun onRecordPause() {
        timerTask?.cancel()

        _recordState.value = RecordState.Paused
    }

    fun onRecordStop() {
        timerTask?.cancel()
        time = 0
        _recordTime.value = "00:00"

        updateRecordList()
        _recordState.value = RecordState.None
    }

    fun onRecordCancel() {
        timerTask?.cancel()
        time = 0
        _recordTime.value = "00:00"

        records.clear()
        _recordState.value = RecordState.None
        showFloatingRecordButton = false
    }

    fun generateRecordFilePath(): String {
        // TODO: 차후 code 여러개일 경우 처리필요
        return try {
            selectedRecordCode = webRecords[0].code
            val index = records[selectedRecordCode]?.run { size } ?: 0
            pathManager.getRecordPath(entryId, selectedRecordCode, index).apply {
                val generateFile = File(this)
                generateFile.parentFile?.mkdirs()
            }
        } catch (e: Exception) {
            QLog.e(e)
            return ""
        }
    }

    fun updateRecordList() = viewModelScope.launch(Dispatchers.IO) {
        // TODO: recode code 여러개에 대한 처리가 필요함

        records.apply {
            clear()

            webRecords.forEach {
                val records = pathManager.getRecordFiles(entryId, it.code).map { recordFile ->
                    val duration = MediaMetadataRetriever().run {
                        setDataSource(recordFile.absolutePath)
                        java.lang.Long.parseLong(
                            extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION) ?: "0"
                        )
                    }

                    RecordData(
                        it.name,
                        recordFile.absolutePath,
                        recordFile.name,
                        recordFile.length(),
                        duration
                    )
                }

                if (records.isNotEmpty()) {
                    this[it.code] = records
                }
            }
        }

        records[selectedRecordCode]?.let {
            _recordList.apply {
                clear()
                addAll(it)
            }
        }
    }
}