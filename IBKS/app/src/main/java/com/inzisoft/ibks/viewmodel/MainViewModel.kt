package com.inzisoft.ibks.viewmodel

import android.os.Message
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.inzisoft.ibks.PathManager
import com.inzisoft.ibks.TakeType
import com.inzisoft.ibks.base.BaseViewModel
import com.inzisoft.ibks.base.UiState
import com.inzisoft.ibks.data.internal.*
import com.inzisoft.ibks.data.remote.UpdateFormDataSource
import com.inzisoft.ibks.data.remote.model.ApiResult
import com.inzisoft.ibks.data.remote.model.DocsResponse
import com.inzisoft.ibks.data.repository.LocalRepository
import com.inzisoft.ibks.data.repository.MainRepository
import com.inzisoft.ibks.data.web.*
import com.inzisoft.ibks.util.FileUtils
import com.inzisoft.ibks.util.log.QLog
import com.inzisoft.paperless.model.ExternalFormData
import com.inzisoft.paperless.xml.builder.EmptyXmlBuilder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val pathManager: PathManager,
    private val localRepository: LocalRepository,
    private val mainRepository: MainRepository,
    private val updateFormDataSource: UpdateFormDataSource,
    private val emptyXmlBuilder: EmptyXmlBuilder?
) : BaseViewModel() {
    var webViewLoadInitData by mutableStateOf<UiState<WebViewLoadInitData>>(UiState.None)
    var webViewState by mutableStateOf<UiState<Unit>>(UiState.None)
    var dialogState by mutableStateOf<WebViewDialogState>(WebViewDialogState.None)
    var webViewControl by mutableStateOf<WebViewControl>(WebViewControl.None)
    var isFinish by mutableStateOf(false)
    var cookieData by mutableStateOf("")

    init {
        QLog.d("webViewViewModel init: $this")
        viewModelScope.launch {
            loadingWebView()

            // 웹페이지 로딩시 access-token, provId,
            // step(임시저장을 불러오기로 호출할시 로드할 웹페이지를 구분하는 값)을 전달함
            combine(
                localRepository.getUserInfo(),
                localRepository.getCookie()
            ) { userInfo, cookie ->
                cookieData = cookie
                WebViewLoadInitData(
                    cookie = cookie,
                    provId = userInfo.name,
                )
            }.catch { e ->
                val message = e.message ?: "unknown error"
                Firebase.crashlytics.recordException(e)
                QLog.e(message)
                loadedWebView()
                webViewLoadInitData = UiState.Error(message)
            }.collect {
                webViewLoadInitData = UiState.Success(it)
                loadedWebView()
                dialogState = WebViewDialogState.None
            }
        }
    }

    fun loadingWebView() {
        webViewState = UiState.Loading()
    }

    fun loadedWebView() {
        webViewState = UiState.None
    }

    fun showWebViewDialog(resultMessage: Message?) {
        dialogState = WebViewDialogState.WebViewDialog(resultMessage)
    }

//    fun logout(scriptFunName: String, json: String) =
//        viewModelScope.launch(Dispatchers.IO) {
//            dialogState = WebViewDialogState.PreviewDoc(scriptFunName, docCameraData, imageDir)
//        }


    fun notifyPage(json: String) = viewModelScope.launch(Dispatchers.IO) {
        val webPage = Gson().fromJson(json, NotifyWebPage::class.java)


        when (webPage.currentPage.lowercase()) {
            "main".lowercase() -> {
                FileUtils.delete(pathManager.getCacheDir())
                localRepository.clearAuthInfo()
                localRepository.clearElectronicSaveInfo()
            }
            "selectBusiness".lowercase() -> {
                val authInfo = localRepository.getAuthInfo().first()
                val entryId = authInfo.edocKey

                if (entryId.isNotEmpty()) {
                    FileUtils.delete(pathManager.getRecordDir(entryId))
                    FileUtils.delete(pathManager.getCacheDir(entryId))
                    localRepository.clearElectronicSaveInfo()
                }

                localRepository.setAuthInfo(authInfo.copy(edocKey = ""))
            }
        }
    }

    // 카메라 테스트
    fun launchAuthCamera(scriptFunName: String, json: String) =
        viewModelScope.launch(Dispatchers.Main) {
            val launchCameraData = Gson().fromJson(json, AuthCameraData::class.java)
            dialogState = when (launchCameraData.takeType) {
                TakeType.OCR.type -> WebViewDialogState.OcrCamera(scriptFunName, launchCameraData)
                else -> WebViewDialogState.NormalAuthCamera(scriptFunName, launchCameraData)
            }
        }

    fun launchDocCamera(scriptFunName: String, json: String) =
        viewModelScope.launch(Dispatchers.IO) {
            val docCameraData = Gson().fromJson(json, DocCameraData::class.java)
            dialogState = WebViewDialogState.DocCamera(scriptFunName, docCameraData)
        }

    fun previewDoc(scriptFunName: String, json: String) =
        viewModelScope.launch(Dispatchers.IO) {
            val entryId = localRepository.getAuthInfo().first().edocKey
            val docCameraData = Gson().fromJson(json, DocCameraData::class.java)
            val imageDir = pathManager.getEvidenceDocDir(entryId, docCameraData.docCode)
            dialogState = WebViewDialogState.PreviewDoc(scriptFunName, docCameraData, imageDir)
        }

    fun sendUserDataToWeb(scriptFunName: String) = viewModelScope.launch(Dispatchers.IO) {
        val userInfo = localRepository.getUserInfo().first()
        val json = Gson().toJson(userInfo)
        webViewControl = WebViewControl.JavaScript(WebViewResponse(scriptFunName, json))
    }

    fun openSetting() {
        dialogState = WebViewDialogState.OpenSetting
    }

    fun generateEntryId(scriptFunName: String, json: String) =
        viewModelScope.launch(Dispatchers.IO) {
            val entryInfo = Gson().fromJson(json, EntryInfo::class.java)
            mainRepository.generateEntryId(entryInfo).collect { result ->
                val entryId = when (result) {
                    is ApiResult.Error -> {
                        QLog.e("${result.message} : ${result.code}")
                        null
                    }

                    is ApiResult.Loading -> return@collect
                    is ApiResult.Success -> {
                        result.data.entryId
                    }
                }

                val resultEntryData = entryId?.run {
                    localRepository.setAuthInfo(AuthInfoDataSource.AuthInfo(entryId))
                    init(entryId)
                    ResultEntryData(true, this)
                } ?: ResultEntryData(false)

                webViewControl = WebViewControl.JavaScript(
                    WebViewResponse(
                        scriptFunName,
                        Gson().toJson(resultEntryData)
                    )
                )
            }
        }

    private fun init(entryId: String) {
        copyIdCardImage(entryId)
    }

    private fun copyIdCardImage(entryId: String) {
        val src = pathManager.getIdCardTempDir()
        val dest = pathManager.getEvidenceDocDir(entryId)
        FileUtils.copy(src, dest)
    }

    fun sendAuthDataToWeb(scriptFunName: String) = viewModelScope.launch(Dispatchers.Main) {
        val authInfo = localRepository.getAuthInfo().first()
        val json = Gson().toJson(authInfo)
        QLog.i(json)
        webViewControl = WebViewControl.JavaScript(WebViewResponse(scriptFunName, json))
    }

    fun sendNormalAuthDataToWeb(scriptFunName: String, launchCameraData: AuthCameraData) =
        viewModelScope.launch(Dispatchers.IO) {
            val authInfo = localRepository.getAuthInfo().first().copy(
                isAuthComplete = true,
                authType = launchCameraData.cameraType,
                takeType = launchCameraData.takeType
            )
            localRepository.setAuthInfo(authInfo)
            sendAuthDataToWeb(scriptFunName)
        }

//    fun showElectronicDoc(scriptFunName: String, json: String) =
//        viewModelScope.launch(Dispatchers.IO) {
//            QLog.json(json,"web 입력값")
//            dialogState = WebViewDialogState.ElectronicDoc(scriptFunName, json)
//        }

    fun showElectronicDocInputMode(scriptFunName: String, json: String, isSend: Boolean) =
        viewModelScope.launch(Dispatchers.IO) {
            QLog.json(json, "web 입력값")
            dialogState = WebViewDialogState.ElectronicInputDoc(scriptFunName, json, isSend)
        }

    fun showElectronicDocPreviewMode(scriptFunName: String, json: String) =
        viewModelScope.launch(Dispatchers.IO) {
            QLog.json(json, "web 입력값")
            dialogState = WebViewDialogState.ElectronicPreviewDoc(scriptFunName, json)
        }

    fun finish() {
        viewModelScope.launch {
            FileUtils.delete(
                File(
                    pathManager.getEvidenceDocDir(
                        localRepository.getAuthInfo().first().edocKey
                    )
                )
            )
            localRepository.clearAuthInfo()

            isFinish = true
        }
    }

    fun loadPen(scriptFunName: String, json: String) {
        loadingWebView()

        viewModelScope.launch(Dispatchers.IO) {
            val entryId = localRepository.getAuthInfo().first().edocKey
            dialogState = try {
                val writePenInfo = Gson().fromJson(json, WritePenInfo::class.java)
                val penDialogData = getPenDialogData(entryId, writePenInfo)
                if (penDialogData.type != PenDialogType.NONE) {
                    WebViewDialogState.WritePen(scriptFunName, penDialogData)
                } else {
                    WebViewDialogState.Error(IllegalArgumentException())
                }
            } catch (e: Exception) {
                Log.e("SW_DEBUG", "e: ${e.message}")
                WebViewDialogState.Error(e)
            }

            loadedWebView()
        }
    }

    private fun getPenDialogData(entryId: String, info: WritePenInfo): PenDialogData {
        return PenDialogData(
            type = getPenDialogType(info.type),
            title = info.title,
            penData = info.pen?.let { getPenData(entryId, it) },
            signPenData = info.signPen?.let { getPenData(entryId, it) },
            sealData = info.seal?.let { getPenData(entryId, it) }
        )
    }

    private fun getPenData(entryId: String?, webPenData: WebPenData): PenData {
        return PenData(
            id = webPenData.id,
            subtitle = webPenData.subtitle,
            placeholder = webPenData.placeholder,
            imagePath = pathManager.getPenImagePath(entryId, webPenData.id)
        )
    }

    fun completeWritePen(scriptFunName: String, result: String) =
        viewModelScope.launch(Dispatchers.IO) {
            val resultPenDataList = Gson().fromJson<List<ResultPenData>>(
                result,
                TypeToken.getParameterized(List::class.java, ResultPenData::class.java).type
            )

            val response = Gson().toJson(resultPenDataList)
            webViewControl = WebViewControl.JavaScript(WebViewResponse(scriptFunName, response))
            dialogState = WebViewDialogState.None
        }

    fun onBack() {
        webViewControl = WebViewControl.Back
    }

    fun onResultElectronicDoc(scriptFunName: String, json: String) {
        val jsonData = Gson().fromJson(json, CompleteTransmitState::class.java)

        webViewControl = WebViewControl.JavaScript(
            WebViewResponse(
                scriptFunName,
                Gson().toJson(CompleteTransmitState(jsonData.isComplete, jsonData.tFieldData))
            )
        )
    }

    fun showSecureKeyPad(scriptFunName: String, json: String) =
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                Gson().fromJson(json, SecureKeyPadInfo::class.java) ?: SecureKeyPadInfo()
            }.onSuccess {
                dialogState = WebViewDialogState.SecureKeyPad(scriptFunName, it)
            }.onFailure {

            }
        }

    fun onResultSecureKeypad(scriptFunName: String, str: String, enc: ByteArray, decPart: String) =
        viewModelScope.launch(Dispatchers.IO) {
            webViewControl = WebViewControl.JavaScript(
                WebViewResponse(
                    scriptFunName,
                    Gson().toJson(SecureKeyPadResult(str, String(enc), decPart))
                )
            )
        }

    fun showInstruction(scriptFunName: String, json: String) =
        viewModelScope.launch(Dispatchers.IO) {
            dialogState = WebViewDialogState.Instruction(scriptFunName, json)
        }

    fun onResultInstruction(scriptFunName: String, result: Boolean) {
        webViewControl =
            WebViewControl.JavaScript(WebViewResponse(scriptFunName, result.toString()))
    }

    fun getFormList(scriptFunName: String, productCode: String) =
        viewModelScope.launch(Dispatchers.IO) {
            val list = updateFormDataSource.getFormCode(productCode)
            webViewControl = WebViewControl.JavaScript(
                WebViewResponse(
                    scriptFunName,
                    Gson().toJson(AppendFormList(list))
                )
            )
        }

    fun clearFormList(scriptFunName: String, json: String) =
        viewModelScope.launch(Dispatchers.IO) {
            val jsonData = Gson().fromJson(json, ClearFormList::class.java) ?: null

            if (jsonData == null) {
                val authInfo = localRepository.getAuthInfo().first()
                val entryId = authInfo.edocKey

                if (entryId.isNotEmpty()) {
                    FileUtils.delete(pathManager.getTempRoot(entryId))
                    localRepository.clearElectronicSaveInfo()
                }
                localRepository.clearElectronicSaveInfo()
            }
            else {
                // TODO: 지울 파일 지정하여 삭제
            }

            webViewControl = WebViewControl.JavaScript(
                WebViewResponse(
                    scriptFunName,
                    Gson().toJson("clear")
                )
            )
        }

    fun logout(complete: () -> Unit) = viewModelScope.launch {
        localRepository.clear()
        complete()
    }

    // Web에서 펀드 상품코드 받아와서 docs 불러와서 확인.
    fun getDocsList(scriptFunName: String, json: String) = viewModelScope.launch(Dispatchers.IO) {
        val docsList = mutableListOf<String>()
        val showDocsList = mutableListOf<String>()
        val prdCd = Gson().fromJson(json, DocsProductCode::class.java)
        var isComplete = false

        mainRepository.getDocsList(prdCd).collect { result ->
            val docsData = when (result) {
                is ApiResult.Error -> {
                    QLog.e("${result.message} : ${result.code}")
                    null
                }

                is ApiResult.Loading -> return@collect
                is ApiResult.Success -> {
                    isComplete = true
                    result.data
                }
            }

            try {
                docsData?.let { docs ->
                    docs.forEach { doc ->
                        val docType = doc.docType
                        val docTypeNm = doc.docTypeNm
                        val formNm = doc.formNm
                        val formCd = doc.formCd
                        val formDir = doc.formDir

                        docsList.add(formCd)

                        if (docType != "FRM") {
                            QLog.i("다운로드! : docType:$docType / formCd:$formCd")
                            mainRepository.downloadEform("IS", formDir, "${formCd}.pdf")
                                .collect { result ->
                                    val file = when (result) {
                                        is ApiResult.Error -> {
                                            QLog.e("${result.message} : ${result.code}")
                                            null
                                        }
                                        is ApiResult.Loading -> {
                                            webViewState = UiState.Loading("$docTypeNm 다운로드..")
                                            return@collect
                                        }
                                        is ApiResult.Success -> {
                                            QLog.i("${formCd}.pdf 다운로드 시작.")
                                            showDocsList.add(formCd)
                                            result.data
                                        }
                                    }

                                    val getPath = pathManager.getPdfPath(formCd)

                                    FileUtils.delete(getPath)
                                    FileUtils.mkdir(getPath)

                                    saveFile(file, getPath)

                                    // xml 없는 pdf 빌더 실행.
                                    try {
                                        emptyXmlBuilder?.buildIfNeed(
                                            pathManager.getFormDir(),
                                            ExternalFormData(formCd, docTypeNm)
                                        ) ?: throw NullPointerException("emptyXmlBuilder is null")
                                    } catch (e: Exception) {
                                        throw IllegalStateException("fail create empty xml $doc")
                                    }
                                    webViewState = UiState.None
                                }
                        } else {
                            QLog.i("미 다운로드 : docType:$docType / formCd:$formCd")
                        }
                    }
                }
            } catch (e: Exception) {
                QLog.e("error: getDocsList [$e]")
                webViewState = UiState.Error("파일에 문제가 있어 진행이 불가능합니다.")
                isComplete = false
                docsList.clear()
                showDocsList.clear()
                FirebaseCrashlytics.getInstance().recordException(e)
            }

            webViewControl = WebViewControl.JavaScript(
                WebViewResponse(
                    scriptFunName,
                    Gson().toJson(AppendDocsList(isComplete, docsList, showDocsList, docsData))
                )
            )
        }
    }

    fun downloadForms(scriptFunName: String, json: String) = viewModelScope.launch(Dispatchers.IO) {
        runCatching {
            val downloadForms = Gson().fromJson(json, DownloadForms::class.java)
            downloadForms.docs.forEach { downloadForm(it) }
        }.onSuccess {
            webViewState = UiState.None
            webViewControl = WebViewControl.JavaScript(
                WebViewResponse(
                    scriptFunName,
                    Gson().toJson(DownloadFormsResult(true))
                )
            )
        }.onFailure {
            QLog.e(it)
            FirebaseCrashlytics.getInstance().recordException(it)

            webViewState = UiState.None
            webViewControl = WebViewControl.JavaScript(
                WebViewResponse(
                    scriptFunName,
                    Gson().toJson(DownloadFormsResult(false))
                )
            )
        }
    }

    private suspend fun downloadForm(doc: DocsResponse) {
        mainRepository.downloadForm(path = "${doc.formDir}/${doc.formCd}.pdf").collect { result ->
            when (result) {
                is ApiResult.Error -> {
                    result.exception?.let { throw it }
                        ?: throw Exception("${result.message} (${result.code})")
                }
                is ApiResult.Loading -> {
                    webViewState = UiState.Loading("${doc.docTypeNm} 다운로드..")
                    return@collect
                }
                is ApiResult.Success -> {
                    FileUtils.saveFile(pathManager.getPdfPath(doc.formCd), result.data)
                    QLog.d("${doc.formCd}.pdf 저장 완료.")

                    emptyXmlBuilder?.buildIfNeed(
                        pathManager.getFormDir(),
                        ExternalFormData(doc.formCd, doc.docTypeNm)
                    ) ?: throw NullPointerException("emptyXmlBuilder is null")
                    QLog.d("${doc.formCd}.xml 저장 완료.")
                }
            }
        }
    }

    private fun saveFile(body: ByteArray?, fileSavePath: String): String {
        if (body == null)
            return ""
        try {
            val fos = FileOutputStream(fileSavePath)
            fos.write(body);

            return fileSavePath
        } catch (e: Exception) {
            Log.e("saveFile", e.toString())
        }
        return ""
    }
}

sealed class WebViewDialogState {
    object None : WebViewDialogState()
    object OpenSetting : WebViewDialogState()
    data class Error(val error: Exception) : WebViewDialogState()
    data class SecureKeyPad(val scriptFunName: String, val secureKeyPadInfo: SecureKeyPadInfo) :
        WebViewDialogState()

    data class WritePen(val scriptFunName: String, val data: PenDialogData) : WebViewDialogState()
    data class WebViewDialog(val resultMessage: Message?) : WebViewDialogState()
    data class Instruction(val scriptFunName: String, val json: String) : WebViewDialogState()

    //    data class ElectronicDoc(val scriptFunName: String, val json: String) : WebViewDialogState()
    data class ElectronicInputDoc(
        val scriptFunName: String,
        val json: String,
        val isSend: Boolean
    ) : WebViewDialogState()

    data class ElectronicPreviewDoc(val scriptFunName: String, val json: String) :
        WebViewDialogState()

    object TestElectronicDoc : WebViewDialogState()

    // 카메라 테스트
    data class OcrCamera(val scriptFunName: String, val authCameraData: AuthCameraData) :
        WebViewDialogState()

    data class NormalAuthCamera(val scriptFunName: String, val authCameraData: AuthCameraData) :
        WebViewDialogState()

    data class DocCamera(val scriptFunName: String, val docCameraData: DocCameraData) :
        WebViewDialogState()

    data class PreviewDoc(
        val scriptFunName: String,
        val docCameraData: DocCameraData,
        val imageDirPath: String
    ) : WebViewDialogState()
}

sealed class WebViewControl {
    object None : WebViewControl()
    object Back : WebViewControl()
    object Forward : WebViewControl()
    object Refresh : WebViewControl()
    data class JavaScript(val response: WebViewResponse) : WebViewControl()
}