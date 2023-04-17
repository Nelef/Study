package com.inzisoft.ibks.view.fragment.paperelss

import android.graphics.Bitmap
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.*
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.inzisoft.ibks.R
import com.inzisoft.ibks.base.UiState
import com.inzisoft.ibks.data.internal.*
import com.inzisoft.ibks.databinding.FragmentPaperlessBinding
import com.inzisoft.ibks.util.log.QLog
import com.inzisoft.ibks.view.compose.*
import com.inzisoft.ibks.view.compose.theme.IBKSTheme
import com.inzisoft.ibks.viewmodel.PaperlessState
import com.inzisoft.ibks.viewmodel.PaperlessViewModel
import com.inzisoft.ibks.viewmodel.ViewMode
import com.inzisoft.paperless.Constants
import com.inzisoft.paperless.callback.PaperlessInterface.SealScanPopupEventListener
import com.inzisoft.paperless.data.PaperlessSaveData
import com.inzisoft.paperless.data.PaperlessSaveOptions
import com.inzisoft.paperless.data.ResultXmlData
import com.inzisoft.paperless.model.ExternalFormData
import com.inzisoft.paperless.ods.data.TFieldInfo
import com.inzisoft.paperless.ods.listener.PaperlessODSInterface
import com.inzisoft.paperless.ods.listener.PaperlessODSListener
import com.inzisoft.paperless.ods.view.PaperlessODSOptions
import com.inzisoft.paperless.util.MsgLog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

private const val ARG_DATA = "paperless_data"

@AndroidEntryPoint
class PaperlessFragment : Fragment(), PaperlessInterface, DocInterface {

    private var _binding: FragmentPaperlessBinding? = null
    private val paperlessODSInterface get() = _binding?.paperlessOdsView as? PaperlessODSInterface

    private val viewModel: PaperlessViewModel by viewModels()

    var listener: PaperlessInterface.Listener? = null
    var saveResultListener: PaperlessInterface.SaveResultListener? = null

    companion object {
        @JvmStatic
        fun newInstance(paperlessData: PaperlessData) =
            PaperlessFragment().apply {
                arguments = bundleOf(ARG_DATA to paperlessData)
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        runCatching {
            with(requireNotNull(arguments)) {
                @Suppress("DEPRECATION")
                requireNotNull(
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                        getParcelable(ARG_DATA)
                    } else {
                        getParcelable(ARG_DATA, PaperlessData::class.java)
                    }
                )
            }
        }.onSuccess {
            viewModel.paperlessData = it
            viewModel.init()
        }.onFailure {
            viewModel.loadFail(it)
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPaperlessBinding.inflate(inflater, container, false)
        _binding?.containerCompose?.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)

            setContent {
                IBKSTheme {
                    PaperlessViewScreen(
                        isPreview = viewModel.viewMode.value == ViewMode.Preview,
                        pageText = "${viewModel.curPage.value}/${viewModel.totalPage.value}",
                        showPrevBtn = viewModel.isShowPrevBtn.value,
                        onPrevPage = { paperlessODSInterface?.goToPrevPage() },
                        showNextBtn = viewModel.isShowNextBtn.value,
                        onNextPage = { paperlessODSInterface?.goToNextPage() },
                        showThumbnail = viewModel.isShowThumbnail.value,
                        currentPage = viewModel.curPage.value,
                        thumbnailList = viewModel.thumbnails,
                        onClickThumbnail = { paperlessODSInterface?.goToPage(it + 1) }
                    )

                    when (val loadingState = viewModel.loading.value) {
                        is UiState.Loading -> LoadingPopup(message = stringResource(id = loadingState.strResId))
                        else -> {}
                    }
                }
            }
        }

        return _binding?.root
    }

    @Preview
    @Composable
    fun PreviewPaperlessViewScreen() {
        IBKSTheme {

            val showThumbnail = remember { mutableStateOf(false) }

            PaperlessViewScreen(
                pageText = "1/2",
                showPrevBtn = true,
                onPrevPage = { showThumbnail.value = true },
                showNextBtn = true,
                onNextPage = { showThumbnail.value = false },
                showThumbnail = showThumbnail.value,
                currentPage = 0,
                thumbnailList = listOf(),
                onClickThumbnail = { }
            )
        }
    }

    @Composable
    fun PaperlessViewScreen(
        isPreview: Boolean = false,
        pageText: String,
        showPrevBtn: Boolean,
        onPrevPage: () -> Unit,
        showNextBtn: Boolean,
        onNextPage: () -> Unit,
        showThumbnail: Boolean = false,
        currentPage: Int,
        thumbnailList: List<Thumbnail>,
        onClickThumbnail: (index: Int) -> Unit
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .onSizeChanged {
                    viewModel.containerSize = it
                }
                .padding(8.dp)
        ) {
            if (isPreview) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 12.dp)
                        .size(80.dp, 48.dp)
                        .background(Color(0x66000000), RoundedCornerShape(16.dp))
                        .clip(RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = pageText,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.subtitle1
                    )
                }

                PreviewInfo()
            }

            if (showPrevBtn) {
                IconButton(
                    modifier = Modifier
                        .size(96.dp)
                        .align(Alignment.CenterStart),
                    onClick = onPrevPage,
                    shape = CircleShape,
                    backgroundColor = Color(0x33000000),
                    icon = R.drawable.view_back,
                    pressedIcon = R.drawable.view_back_on
                )
            }

            if (showNextBtn) {
                IconButton(
                    modifier = Modifier
                        .size(96.dp)
                        .align(Alignment.CenterEnd),
                    onClick = onNextPage,
                    shape = CircleShape,
                    backgroundColor = Color(0x33000000),
                    icon = R.drawable.view_next,
                    pressedIcon = R.drawable.view_next_on
                )
            }

            AnimatedVisibility(
                visible = showThumbnail,
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                LeftMenuBubble(
                    thumbnailList = thumbnailList,
                    currentPage = currentPage,
                    onClickThumbnail = onClickThumbnail
                )
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.i("jhkim", "onViewCreated")


        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.paperlessState.collect { applyPaperlessState(it) }
            }
        }
    }

    private fun init(containerWidth: Int, containerHeight: Int) {
        paperlessODSInterface?.run {
            MsgLog.setDebug(true)
            setPaperlessODSListener(paperlessOdsListener)
            setSealScanPopupEventListener(paperlessOdsListener)

            initPaperlessView(
                PaperlessODSOptions.Builder()
                    .setRootDirPath(viewModel.getBizRootPath())
                    .setInitRect(
                        Rect(
                            0,
                            0,
                            containerWidth,
                            containerHeight
                        )
                    )
                    .build()
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding?.run {
            paperlessOdsView.destroy()
        }

        _binding = null
    }

    private fun applyPaperlessState(state: PaperlessState) {
        paperlessODSInterface?.run {
            when (state) {
                is PaperlessState.Init -> init(state.containerWidth, state.containerHeight)
                is PaperlessState.GoToPdf -> movePdfPage(state.formCode)
                is PaperlessState.SetData -> setTFieldData(state.tFieldInfo)
                is PaperlessState.LoadComplete -> {
                    listener?.onLoadComplete(
                        state.businessLogic,
                        true,
                        state.formCount
                    )
                }
                is PaperlessState.LoadFail -> listener?.onLoadComplete(
                    viewModel.businessLogic,
                    false,
                    viewModel.formList.size,
                    state.throwable
                )
                PaperlessState.Destroy -> {
                    paperlessODSInterface?.destroy()
                    _binding = null
                }
                PaperlessState.None -> {}
            }
        }
    }


    /***********************************************************************
     *
     * PaperlessInterface
     *
     ***********************************************************************/


    override fun onWriteMode() {
        paperlessODSInterface?.runCatching { setViewMode(false) }
            ?.onFailure { onError(it) }
    }

    override fun onPreviewMode() {
        paperlessODSInterface?.runCatching { setViewMode(true) }
            ?.onFailure { onError(it) }
    }

    override fun goPage(index: Int) {
        paperlessODSInterface?.runCatching { goToPage(index + 1) }
            ?.onFailure { onError(it) }
    }

    override fun goPage(formCode: String) {
        paperlessODSInterface?.runCatching { movePdfPage(formCode) }
            ?.onFailure { onError(it) }
    }

    override fun getPageInfo() {
        listener?.updatePage(
            viewModel.businessLogic,
            viewModel.curPage.value,
            viewModel.totalPage.value
        )
    }

    override fun onHighlighter() {
        paperlessODSInterface?.runCatching { drawSupportPen() }
            ?.onFailure { onError(it) }
    }

    override fun onEraser() {
        paperlessODSInterface?.runCatching { drawSupportEraser() }
            ?.onFailure { onError(it) }
    }

    override fun turnOffHighlighter() {
        paperlessODSInterface?.runCatching { releaseSupportPen() }
            ?.onFailure { onError(it) }
    }

    override fun setSealImage(sealImage: Bitmap?) {
        paperlessODSInterface?.setSealImage(sealImage)
    }

    override fun loadThumbnail(result: (thumbnailList: List<Thumbnail>) -> Unit) {
        lifecycleScope.launch(Dispatchers.Default) {
            paperlessODSInterface?.runCatching {
                thumbnailDataList.map {
                    Thumbnail(
                        title = it.formName,
                        image = Image(
                            it.pageImageFilePath,
                            it.width,
                            it.height
                        ),
                        isFirst = it.isFirstIndex,
                        isShowDivider = it.isShowDivider,
                        isComplete = it.isInputMustEntry
                    )
                }
            }?.onSuccess {
                result(it)
            }?.onFailure { onError(it) }
        }
    }

    override fun openThumbnail() {
        loadThumbnail {
            viewModel.onLoadThumbnail(it)
            listener?.updateThumbnailState(true)
        }
    }

    override fun closeThumbnail() {
        viewModel.closeThumbnail()
        listener?.updateThumbnailState(false)
    }

    override fun isOpenThumbnail(): Boolean {
        return viewModel.isShowThumbnail.value
    }

    override fun isFillOutComplete(result: (isComplete: Boolean) -> Unit) {
        lifecycleScope.launch(Dispatchers.Default) {
            result(
                paperlessODSInterface?.runCatching { allMustEntryFieldsCompleted() }
                    ?.onFailure { onError(it) }?.getOrDefault(false) ?: false
            )
        }
    }

    override fun fillOutComplete() = callbackFlow {
        viewModel.fillOutComplete(this@callbackFlow)

        lifecycleScope.launch(Dispatchers.Default) {

            paperlessODSInterface?.runCatching {
                fillOutComplete()
            }?.onFailure {
                close(it)
            } ?: close(NullPointerException("interface is null"))

        }

        awaitClose {}
    }

    override fun fieldCallUpAtEmptyMustEntry() {
        paperlessODSInterface?.fieldCallUpAtEmptyMustEntry()
    }

    override fun setTFieldData(tFieldInfo: TFieldInfo) {
        lifecycleScope.launch(Dispatchers.Main) {
            paperlessODSInterface?.runCatching {
                setTfieldInfo(tFieldInfo)
            }?.onFailure { onError(it) }
        }
    }

    override fun getTerminalInfo(result: (Map<String, String>) -> Unit) {
        lifecycleScope.launch(Dispatchers.Default) {
            paperlessODSInterface?.runCatching {
                getAllTFieldInfo {
                    result(it?.toMap() ?: hashMapOf())
                }
            }?.onFailure { onError(it) }
        }
    }

    override fun getTerminalInfo(
        context: CoroutineContext,
        tFieldIdList: List<String>,
        result: (Map<String, String>) -> Unit
    ) {
        lifecycleScope.launch(context) {
            paperlessODSInterface?.runCatching {
                getTFieldInfo(TFieldInfo().apply {
                    putTFieldValue(tFieldIdList.associateWith { "" })
                }) {
                    result(it?.toMap() ?: hashMapOf())
                }
            }?.onFailure { onError(it) }
        }
    }

    override fun saveResultXml(saveDirPath: String) {
        paperlessODSInterface?.runCatching {
            saveResultXML(
                PaperlessSaveOptions.Builder()
                    .setCombine(false)
                    .setSaveRootPath(saveDirPath)
                    .build()
            )
        }?.onFailure {
            saveResultListener?.onSaveResultXml(
                viewModel.businessLogic,
                false,
                throwable = it
            )
        }
    }


    /***********************************************************************
     *
     * PaperlessODSListener Listener
     *
     ***********************************************************************/

    private val paperlessOdsListener = object : PaperlessODSListener, SealScanPopupEventListener {

        override fun onReady(isSuccess: Boolean, message: String?) {

            if (isSuccess) {
                paperlessODSInterface?.runCatching {
                    viewModel.loadBusinessLogic { businessLogic, options ->
                        when (options) {

                            is LoadOptions.Restore -> loadResultPaperless(
                                businessLogic,
                                ResultXmlData().apply {
                                    resultFolderPathList = options.resultXmlDirPathList
                                })

                            is LoadOptions.Write -> loadPaperless(
                                businessLogic,
                                options.formList?.map { ExternalFormData(it.code, it.name) },
                                null
                            )
                        }
                    }
                }?.onFailure {
                    listener?.onLoadComplete(
                        viewModel.businessLogic,
                        false,
                        viewModel.formList.size,
                        it
                    )
                }
            } else {
                QLog.e("Paperless init fail.")
            }
        }

        override fun onViewModeChanged(isPreview: Boolean) {
            QLog.d("onViewModeChanged $isPreview")

            viewModel.onChangeViewMode(isPreview)
        }

        override fun onPageChanged(total: Int, current: Int) {
            QLog.d("onPageChanged $current/$total")
            listener?.updatePage(
                viewModel.businessLogic,
                current,
                total
            )
            viewModel.updatePage(current, total)
        }

        override fun onBusinessLogicLoaded(isSuccess: Boolean, message: String?) {
            QLog.d("onBusinessLogicLoaded $isSuccess : $message")
        }

        override fun onEmptyDocumentLoaded(documentId: String?) {
            viewModel.onEmptyDocumentLoaded()
        }

        override fun beforeToLoadPDF(mode: Constants.PDFLoadMode, formId: String) {
            if (mode != Constants.PDFLoadMode.PDF_LOAD_DELETE) {
                if (isOpenThumbnail()) closeThumbnail()
            }
        }

        override fun onDocumentLoaded(formData: ExternalFormData) {
            QLog.d("onDocumentLoaded $formData")

            viewModel.onDocumentLoaded(formData)
        }

        override fun onDocumentLoadComplete(
            isSuccess: Boolean,
            formId: String?,
            errorCode: Constants.PaperlessErrorCode?
        ) {
            QLog.d("onDocumentLoadComplete $isSuccess $formId $errorCode")
            if (isSuccess) {
                if (isOpenThumbnail()) openThumbnail()
                viewModel.onDocumentLoadComplete()
            } else {
                listener?.onLoadComplete(
                    viewModel.businessLogic,
                    false,
                    viewModel.formList.size,
                    IllegalArgumentException("$errorCode $formId")
                )
            }

        }

        override fun onRestoreComplete(
            isSuccess: Boolean,
            message: String?,
            throwable: Throwable?
        ) {
            QLog.d("onRestoreComplete $isSuccess")
            if (isSuccess) {
                viewModel.onDocumentLoadComplete()
            } else {
                FirebaseCrashlytics.getInstance()
                    .recordException(
                        IllegalStateException(
                            "[onRestoreComplete] ${message ?: "unknown"}",
                            throwable
                        )
                    )
                listener?.onLoadComplete(
                    viewModel.businessLogic,
                    false,
                    viewModel.formList.size,
                    throwable
                )
            }
        }

        override fun onDocumentDeleteComplete(
            isSuccess: Boolean,
            formId: String?,
            errorCode: Constants.PaperlessErrorCode
        ) {
            if (isOpenThumbnail()) openThumbnail()
        }

        override fun onAttachImageLoaded(isSuccess: Boolean) {

        }

        override fun onSaveEachResult(paperlessSaveData: PaperlessSaveData?) {
            QLog.d("onSaveEachResult $paperlessSaveData")
        }

        override fun onSaveResult(
            isSuccess: Boolean,
            paperlessSaveDataList: ArrayList<PaperlessSaveData>,
            errorCode: Constants.PaperlessErrorCode?,
            errMsg: String?
        ) {
            QLog.d("onSaveResult $isSuccess $paperlessSaveDataList $errorCode $errMsg")

            if (isSuccess) {
                saveResultListener?.onSaveResultXml(
                    viewModel.businessLogic,
                    true,
                    paperlessSaveDataList
                )
            } else {
                saveResultListener?.onSaveResultXml(
                    viewModel.businessLogic,
                    false,
                    throwable = IllegalStateException("code: $errorCode message : $errMsg")
                )
            }
        }

        override fun onSaveImsiXmlResult(
            isSuccess: Boolean,
            imsiXmlFilePath: String?,
            paperlessSaveDataList: ArrayList<PaperlessSaveData>?
        ) {
            QLog.d("onSaveImsiXmlResult $isSuccess $imsiXmlFilePath $paperlessSaveDataList")
        }

        override fun onFillOutComplete(
            result: PaperlessODSListener.ResultFillOutComplete,
            message: String
        ) {
            when (result) {
                PaperlessODSListener.ResultFillOutComplete.SUCCESS -> {
                    viewModel.onFillOutComplete(true, message)
                }
                PaperlessODSListener.ResultFillOutComplete.ERROR_MESSAGE -> {
                    viewModel.onFillOutComplete(false, message)
                }
            }
        }

        override fun onCallSetValueToTerminal(tFieldInfo: TFieldInfo?) {
            QLog.d("onCallSetValueToTerminal $tFieldInfo")
        }

        override fun onShowAlertDialog(msg: String?) {
            msg?.let {
                listener?.alert(
                    viewModel.businessLogic,
                    AlertData(
                        contentText = it.replace("\\n", "\n"),
                        rightBtnText = getString(R.string.confirm),
                        onDismissRequest = { viewModel.dismissAlert() })
                )
            }
            QLog.d("onShowAlertDialog $msg")
        }

        override fun onEssentialFieldChanged(formName: String?, remainCnt: Int, totalCnt: Int) {
            QLog.d("onEssentialFieldChanged $formName $remainCnt/$totalCnt")
        }

        override fun onEssentialFieldChanged(remainCnt: Int, totalCnt: Int) {
            QLog.d("onEssentialFieldChanged $remainCnt/$totalCnt")
        }

        override fun onDataSetComplete() {
            QLog.d("onDataSetComplete")
            viewModel.onDataSetComplete()
        }

        override fun showAddressPopup(formId: String?, fieldId: String?, value: String?) {
        }

        override fun onReactionRuleStart() {
            QLog.d("onReactionRuleStart")
        }

        override fun onReactionRuleEnd() {
            QLog.d("onReactionRuleEnd")
        }

        override fun onSealScanPopupOpen() {
            QLog.d("onSealScanPopupOpen")
            listener?.showSealCamera()
        }

        override fun onSealScanPopupCancel() {
            QLog.d("onSealScanPopupCancel")
        }
    }

    private fun onError(throwable: Throwable) {
        listener?.onError(viewModel.businessLogic, throwable)
    }
}
