package com.inzisoft.ibks.view.dialog

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.*
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.inzisoft.ibks.*
import com.inzisoft.ibks.R
import com.inzisoft.ibks.base.*
import com.inzisoft.ibks.data.internal.AlertData
import com.inzisoft.ibks.data.internal.PaperlessData
import com.inzisoft.ibks.data.web.AuthCameraData
import com.inzisoft.ibks.util.log.QLog
import com.inzisoft.ibks.view.compose.*
import com.inzisoft.ibks.view.compose.theme.mainColor
import com.inzisoft.ibks.view.compose.theme.point4Color
import com.inzisoft.ibks.view.fragment.EvidenceDocumentViewFragment
import com.inzisoft.ibks.view.fragment.paperelss.DocInterface
import com.inzisoft.ibks.view.fragment.paperelss.PaperlessFragment
import com.inzisoft.ibks.view.fragment.paperelss.PaperlessInterface
import com.inzisoft.ibks.viewmodel.*
import com.inzisoft.paperless.data.PaperlessSaveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.CountDownLatch

abstract class BaseElectronicDocFragment : BaseDialogFragment() {
    //    val viewModel: BaseElectronicDocViewModel by viewModels()
    val viewModel: BaseElectronicDocViewModel by lazy { getChildViewModel() }
    lateinit var fragmentContainerView: FragmentContainerView
    val paperlessFragmentMap = mutableMapOf<String, PaperlessInterface>()
    var evidenceDocFragment: EvidenceDocumentViewFragment? = null
    var currentFragment: Fragment? = null
    var isInputMode: Boolean = false

    val paperlessTable = mutableSetOf<String>()

    private var penPopupControl by mutableStateOf(false)
    var showThumbnailState by mutableStateOf(false)
    var totalPaperlessCount = 0
    var savedPaperlessCount = 0
    var saveResultXmlRootPath = ""

    private val saveResultListener: PaperlessInterface.SaveResultListener =
        object : PaperlessInterface.SaveResultListener {
            override fun onSaveResultXml(
                businessLogic: String,
                isSuccess: Boolean,
                saveDataList: List<PaperlessSaveData>,
                throwable: Throwable?
            ) {
                if (isSuccess) {
                    paperlessTable.remove(businessLogic)
                    savedPaperlessCount++
                    viewModel.savedEachPaperlessComplete(
                        savedPaperlessCount,
                        totalPaperlessCount,
                        businessLogic,
                        saveDataList
                    ) {
                        if (paperlessTable.size > 0) {
                            val businessLogic = paperlessTable.first()

                            val paperless =
                                paperlessFragmentMap[businessLogic] as PaperlessFragment
                            saveResultXml(businessLogic, paperless)
                        } else {
                            viewModel.saveResultXmlComplete()
                        }
                    }
                } else {
                    viewModel.alert(
                        AlertData(
                            contentText = getString(R.string.save_application_fail),
                            rightBtnText = getString(R.string.confirm),
                            onDismissRequest = {
                                viewModel.dismissAlert()
                                viewModel.noneState()
                            }
                        )
                    )
                }
            }
        }

    private val paperlessListener: PaperlessInterface.Listener =
        object : PaperlessInterface.Listener {
            override fun updateThumbnailState(isShow: Boolean) {
                showThumbnailState = isShow
            }

            override fun onLoadComplete(
                businessLogic: String,
                isSuccess: Boolean,
                formCount: Int,
                throwable: Throwable?
            ) {
                if (isSuccess) {
                    paperlessTable.remove(businessLogic)
                    totalPaperlessCount++
                    if (paperlessTable.size > 0) {
                        val electronicTabData =
                            viewModel.getElectronicTabData(paperlessTable.first())
                        electronicTabData?.let {
                            moveTab(it)
                        }
                    } else {
                        if (!viewModel.loadComplete) {
                            moveTab(viewModel.getFirstElectronicDocInfo())
                        }

                        viewModel.loadComplete = true
                    }
                } else {
                    viewModel.alert(
                        AlertData(
                            contentText = getString(R.string.loading_fail),
                            rightBtnText = getString(R.string.confirm),
                            onDismissRequest = {
                                viewModel.dismissAlert()
                                cancel(viewModel.onCancel())
                            }
                        )
                    )
                }
            }

            override fun updatePage(businessLogic: String, current: Int, total: Int) {
                viewModel.pageInfo = Pair(current, total)
            }

            override fun showSealCamera() {
                setFragmentResultListener(FragmentRequest.SealCamera) { _, result ->
                    clearFragmentResultListener(FragmentRequest.SealCamera.key)
                    when (result) {
                        is FragmentResult.Cancel -> {}
                        is FragmentResult.Error -> {}
                        is FragmentResult.OK -> {

                            val sealImage = result.data
                            viewModel.setSealImage(sealImage)
                        }
                    }
                }
                val authCameraData = AuthCameraData("seal", "ocr", "N", "N")

                navigate(
                    NavGraphDirections.actionGlobalSealCameraDialogFragment(
                        authCameraData
                    )
                )
            }

            override fun alert(businessLogic: String, alertData: AlertData) {
                viewModel.alert(alertData.copy(onDismissRequest = { viewModel.dismissAlert() }))
            }

            override fun onError(businessLogic: String, throwable: Throwable) {
            }
        }

    init {
        baseCompose.topBar = {
            val showTopBar = viewModel.paperlessTransmitState == PaperlessTransmitState.None
            if (showTopBar) {
                when (val paperlessTopBarState = viewModel.paperlessTopBarState) {
                    is PaperlessTopBarState.None -> {}
                    is PaperlessTopBarState.InputMode -> {
                        isInputMode = true
                        PaperlessInputTopBar(
                            title = viewModel.getTitle(),
                            electronicTabDataList = paperlessTopBarState.electronicTabDataList,
                            onClickThumbnail = { showThumbnail ->
                                currentFragment?.let {
                                    showThumbnail(it as DocInterface, showThumbnail)
                                }
                            },
                            pageInfo = "${viewModel.pageInfo.first}/${viewModel.pageInfo.second}",
                            onTabClick = { electronicDocInfo ->
                                moveTab(electronicDocInfo)
                            },
                            selectedTab = viewModel.selectedElectronicTabData.value,
                            showThumbnail = showThumbnailState,
                            showHighlighter = penPopupControl,
                            onClickHighlighter = { penPopupControl = !it },
                            onReleaseHighlighter = { currentFragment?.apply { if (this is PaperlessFragment) turnOffHighlighter() } },
                            onHighlighter = { currentFragment?.apply { if (this is PaperlessFragment) onHighlighter() } },
                            onEraser = { currentFragment?.apply { if (this is PaperlessFragment) onEraser() } },
                            onCancel = { cancel(viewModel.onCancel()) },
                            enabledTransmit = viewModel.loadComplete,
                            onAlert = { alertData ->
                                viewModel.alert(alertData.copy(onDismissRequest = {
                                    alertData.onDismissRequest(it)
                                    viewModel.dismissAlert()
                                }))
                            },
                            onFillOutComplete = {
                                viewModel.clickInputComplete()
                            },
                            recordState = (activity as MainActivity).viewModel.recordState.value
                        )
                    }
                    is PaperlessTopBarState.PreviewMode -> {
                        isInputMode = false
                        PaperlessPreviewTopBar(
                            title = viewModel.getTitle(),
                            electronicTabDataList = paperlessTopBarState.electronicTabDataList,
                            onClickThumbnail = { showThumbnail ->
                                currentFragment?.let {
                                    showThumbnail(it as DocInterface, showThumbnail)
                                }
                            },
                            showThumbnail = showThumbnailState,
                            pageInfo = "${viewModel.pageInfo.first}/${viewModel.pageInfo.second}",
                            onTabClick = { electronicDocInfo ->
                                moveTab(electronicDocInfo)
                            },
                            selectedTab = viewModel.selectedElectronicTabData.value,
                            onEdit = { viewModel.changeMode(false) },
                            onCancel = { cancel(viewModel.onCancel()) },
                            enabledTransmit = viewModel.loadComplete,
                            onTransmit = {
                                viewModel.onTransmit()
                            },
                            onAlert = { alertData ->
                                viewModel.alert(alertData.copy(onDismissRequest = {
                                    alertData.onDismissRequest(it)
                                    viewModel.dismissAlert()
                                }))
                            },
                            recordState = (activity as MainActivity).viewModel.recordState.value,
                            tabletSndAuth = viewModel.tabletSndAuth
                        )
                    }
                }
            }
        }

        baseCompose.content = {
            Box(modifier = Modifier.fillMaxSize()) {
                FragmentContainer(modifier = Modifier.fillMaxSize())

                when (val paperlessTransmitState = viewModel.paperlessTransmitState) {
                    is PaperlessTransmitState.None -> {}
                    else -> {
                        Log.e("SW_DEBUG", "")
                        Transmit(state = paperlessTransmitState)
                    }
                }
            }

            FloatingRecordButton(
                showFloatingRecordButton = (activity as MainActivity).viewModel.showFloatingRecordButton,
                recordState = (activity as MainActivity).viewModel.recordState.value,
                recordTime = (activity as MainActivity).viewModel.recordTime.value,
                recordFileList = (activity as MainActivity).viewModel.recordList,
                onRecord = { (activity as MainActivity).recordStart() },
                onResume = { (activity as MainActivity).recordResume() },
                onPause = { (activity as MainActivity).recordPause() },
                onStop = {
                    viewModel.alert(
                        AlertData(
                            contentText = getString(R.string.alert_finish_record),
                            leftBtnText = getString(R.string.cancel),
                            rightBtnText = getString(R.string.finish),
                            onDismissRequest = { state ->
                                if (state == Right) {
                                    (activity as MainActivity).recordStop()
                                }
                                viewModel.dismissAlert()
                            }
                        )
                    )
                },
                onRecordList = { (activity as MainActivity).viewModel.updateRecordList() }
            )
        }

        baseCompose.surface = {
            MemoBottomDialog(
                visible = viewModel.showMemoDialog.value,
                memo = viewModel.memo.value,
                currentBytes = viewModel.memoBytes.value,
                totalBytes = BaseElectronicDocViewModel.MAX_BYTES_MEMO,
                onMemoChange = { viewModel.onMemoChange(it) },
                onCancel = { viewModel.onResultMemo(false) },
                onConfirm = {
                    // TODO 녹취 종료 때 녹취중이라면 alert 창 띄우기
                    QLog.i("${(activity as MainActivity).viewModel.recordState.value}")
                    (activity as MainActivity).recordStop()
                    (activity as MainActivity).viewModel.showFloatingRecordButton = false
                    viewModel.onResultMemo(true)
                }
            )

            when (val popupState = viewModel.popupState.value) {
                is UiState.Success -> {
                    val popupData = popupState.data

                    ShowAlertDialog(
                        contentText = popupData.contentText,
                        leftBtnText = popupData.leftBtnText,
                        rightBtnText = popupData.rightBtnText,
                        onDismissRequest = popupData.onDismissRequest
                    )
                }
                else -> {}
            }
        }
    }

    @Composable
    fun FragmentContainer(
        modifier: Modifier = Modifier
    ) {
        val localView = LocalView.current
        val parentFragment = remember(localView) {
            try {
                localView.findFragment<Fragment>()
            } catch (e: IllegalStateException) {
                // findFragment throws if no parent fragment is found
                null
            }
        }
        val containerId by rememberSaveable {
            mutableStateOf(View.generateViewId())
        }
        val container = remember {
            mutableStateOf<FragmentContainerView?>(null)
        }
        val viewBlock: (Context) -> View = remember(localView) {
            { context ->
                FragmentContainerView(context)
                    .apply { id = containerId }
                    .also {
                        container.value = it
                        fragmentContainerView = it
                    }
            }
        }
        AndroidView(
            modifier = modifier,
            factory = viewBlock,
            update = {}
        )

        val localContext = LocalContext.current
        DisposableEffect(localView, localContext, container) {
            onDispose {
                val fragmentManager =
                    parentFragment?.childFragmentManager
                        ?: (localContext as?
                                FragmentActivity)?.supportFragmentManager
                val existingFragment = fragmentManager?.findFragmentById(container.value?.id ?: 0)
                if (existingFragment != null && !fragmentManager.isStateSaved) {
                    fragmentManager.commit { remove(existingFragment) }
                }
            }
        }
    }

    @Composable
    private fun Transmit(state: PaperlessTransmitState) {
        val defaultTitle = stringResource(id = R.string.transmit_title)
        val defaultColor = MaterialTheme.colors.mainColor
        val failColor = MaterialTheme.colors.point4Color
        val defaultSubtitle = stringResource(id = R.string.transmit_subtitle)

        var title by remember { mutableStateOf(defaultTitle) }
        var color by remember { mutableStateOf(defaultColor) }
        var message by remember { mutableStateOf(defaultSubtitle) }
        var message2 by remember { mutableStateOf<AnnotatedString?>(null) }
        var progressMessage by remember { mutableStateOf("") }
        var progress by remember { mutableStateOf(1f) }

        when (state) {
            is PaperlessTransmitState.None -> {}
            is PaperlessTransmitState.Complete -> {
                title = stringResource(id = R.string.transmit_title_complete)
                color = defaultColor
                message = ""
            }
            is PaperlessTransmitState.Error -> {
                color = MaterialTheme.colors.point4Color
                if (state.code == ExceptionCode.EXCEPTION_CODE_INTERNAL_CONNECTION) {
                    title = stringResource(id = R.string.transmit_title_error_connection)
                    color = failColor
                    message = stringResource(id = R.string.transmit_message_error_connection)
                    message2 = state.message2
                } else {
                    title = stringResource(id = R.string.transmit_title_error)
                    color = failColor
                    message = state.message
                    message2 = state.message2
                }
            }
            is PaperlessTransmitState.MakeResult -> {
                message = defaultSubtitle
                message2 = null
                progressMessage = stringResource(
                    id = R.string.transmit_step_1,
                    state.current,
                    state.total
                )
                progress = state.progress * 0.25f
            }
            is PaperlessTransmitState.Transmit -> {
                title = stringResource(id = R.string.transmit_title)
                color = defaultColor
                message = defaultSubtitle
                message2 = null
                progressMessage = stringResource(id = R.string.transmit_step_3)
                progress = 0.75f + state.progress * 0.25f
            }
            is PaperlessTransmitState.ZipFile -> {
                title = stringResource(id = R.string.transmit_title)
                color = defaultColor
                message = defaultSubtitle
                message2 = null
                progressMessage = stringResource(id = R.string.transmit_step_2)
                progress = 0.5f + state.progress * 0.25f
            }
        }

        TransmitScreen(title = title, titleColor = color, message = message, message2 = message2) {
            when (state) {
                PaperlessTransmitState.None -> {}
                PaperlessTransmitState.Complete -> {
                    CompleteArea {
                        complete(viewModel.onComplete())
                    }
                }
                is PaperlessTransmitState.Error -> {
                    ErrorArea(onSaveTemp = {
                        viewModel.alert(
                            AlertData(
                                contentText = getString(R.string.do_save_temp_message),
                                leftBtnText = getString(R.string.cancel),
                                rightBtnText = getString(R.string.confirm),
                                onDismissRequest = { state ->
                                    if (state == Right) {
                                        complete(viewModel.onComplete())
                                    }
                                }
                            )
                        )
                    }, onRetransmit = {
                        sendPaperless()
                    })
                }
                is PaperlessTransmitState.MakeResult,
                is PaperlessTransmitState.Transmit,
                is PaperlessTransmitState.ZipFile -> {
                    ProgressArea(message = progressMessage, progress = progress)
                }
            }
        }
    }

    abstract fun getChildViewModel(): BaseElectronicDocViewModel
    abstract fun makePaperlessData(electronicTabData: ElectronicTabData): PaperlessData
    abstract fun complete(json: String)
    abstract fun cancel(json: String)

    override fun getBaseViewModel(): BaseDialogFragmentViewModel {
        return viewModel
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.paperlessState.observe(viewLifecycleOwner, electronicDocStateObserver)
    }

    private val electronicDocStateObserver = Observer<PaperlessUiState> { paperlessState ->
        when (paperlessState) {
            is PaperlessUiState.Init -> {
                Log.e("SW_DEBUG", "PaperlessInitState.Init")
                totalPaperlessCount = 0
                initPaperlessTable(paperlessState.electronicTabDataList)
                moveTab(paperlessState.electronicTabDataList.first())
            }
            is PaperlessUiState.Load -> {
                switchTab(electronicTabData = paperlessState.electronicTabData)
            }
            is PaperlessUiState.SetSeal -> {
                (currentFragment as? PaperlessFragment)?.setSealImage(paperlessState.sealImage)
            }
            is PaperlessUiState.SaveResultXml -> {
                initPaperlessTable(paperlessState.electronicTabDataList)
                saveResultXmlRootPath = paperlessState.saveResultRootPath
                savedPaperlessCount = 0
                Log.e("SW_DEBUG", "saveResultXmlRootPath: $saveResultXmlRootPath")
                val businessLogic = paperlessTable.first()
                val paperless = paperlessFragmentMap[businessLogic] as PaperlessFragment
                viewModel.savedEachPaperlessComplete(savedPaperlessCount, totalPaperlessCount)
                saveResultXml(businessLogic, paperless)
            }
            is PaperlessUiState.FillOutComplete -> {
                initPaperlessTable(paperlessState.electronicTabDataList)
                Log.e("SW_DEBUG", "paperlessTable.size: ${paperlessTable.size}")
                checkMustEntry(0, paperlessTable.size)

            }
            is PaperlessUiState.PreviewMode -> {
                isInputMode = false
                currentFragment?.apply { if (this is PaperlessFragment) onPreviewMode() }
            }
            is PaperlessUiState.InputMode -> {
                isInputMode = true
                currentFragment?.apply { if (this is PaperlessFragment) onWriteMode() }
            }
            is PaperlessUiState.PaperlessSend -> {
                sendPaperless()
            }
            is PaperlessUiState.PaperlessComplete -> {
                sendTFieldDataToWeb {
                    complete(viewModel.onComplete(it))
                }
            }
            PaperlessUiState.None -> {}
        }
    }

    private fun moveTab(electronicTabData: ElectronicTabData) {
        viewModel.moveTab(electronicTabData)
    }

    private fun initPaperlessTable(electronicTabDataList: List<ElectronicTabData>) {
        viewModel.paperlessState.postValue(PaperlessUiState.None)
        paperlessTable.clear()
        electronicTabDataList.forEach { electronicTabData ->
            if (electronicTabData.docType == DocType.NORMAL
                || electronicTabData.docType == DocType.USE_VERSION_CHECK_XML
            ) {
                Log.e("SW_DEBUG", "add ${electronicTabData.businessLogic}")
                paperlessTable.add(electronicTabData.businessLogic)
            }
        }
    }

    private fun switchTab(electronicTabData: ElectronicTabData) {
        if (electronicTabData.docType == DocType.NORMAL || electronicTabData.docType == DocType.USE_VERSION_CHECK_XML) {
            movePaperlessTab(electronicTabData)
        } else if (electronicTabData.docType == DocType.ADDED_DOC) {
            moveEvidenceDocumentTab()
        }
    }

    private fun moveEvidenceDocumentTab() {
        val ft = childFragmentManager.beginTransaction()
        if (evidenceDocFragment == null) {
            evidenceDocFragment = EvidenceDocumentViewFragment.newInstance(viewModel.getDocsList())
            ft.add(fragmentContainerView.id, evidenceDocFragment!!)
        }

        currentFragment?.let {
            if (viewModel.loadComplete && it is PaperlessFragment) {
                showHighliter(it, false)
                showThumbnail(it, false)
            }
            if (it is PaperlessFragment) {
                it.listener = null
            } else if (it is EvidenceDocumentViewFragment) {
                it.listener = null
            }
            ft.hide(it)
        }

        evidenceDocFragment?.let {
            ft.show(it)
            it.listener = paperlessListener
            currentFragment = it
        }

        ft.commitAllowingStateLoss()

        if (viewModel.loadComplete) {
            evidenceDocFragment?.getPageInfo()
        }
    }

    private fun movePaperlessTab(electronicTabData: ElectronicTabData) {
        val ft = childFragmentManager.beginTransaction()
        var paperlessFragment = paperlessFragmentMap[electronicTabData.businessLogic]
        if (paperlessFragment == null) {
            val paperlessData = makePaperlessData(electronicTabData)
            paperlessFragment = PaperlessFragment.newInstance(paperlessData = paperlessData)
            paperlessFragmentMap[electronicTabData.businessLogic] = paperlessFragment
            ft.add(fragmentContainerView.id, paperlessFragment)
        }

        currentFragment?.let {
            if (viewModel.loadComplete && it is PaperlessFragment) {
                showHighliter(it, false)
                showThumbnail(it, false)
            }
            if (it is PaperlessFragment) {
                it.listener = null
            } else if (it is EvidenceDocumentViewFragment) {
                it.listener = null
            }
            ft.hide(it)
        }

        paperlessFragment?.let {
            ft.show(it as PaperlessFragment)
            it.listener = paperlessListener
            currentFragment = it

            if (isInputMode) {
                (currentFragment as PaperlessFragment).onWriteMode()
            } else {
                (currentFragment as PaperlessFragment).onPreviewMode()
            }
            if (viewModel.loadComplete) {
                it.getPageInfo()
            }
        }

        ft.commitAllowingStateLoss()
    }

    private fun showThumbnail(fragment: DocInterface, show: Boolean) {
        fragment?.apply {
            showThumbnailState = show
            if (show) {
                openThumbnail()
            } else {
                closeThumbnail()
            }
        }
    }

    private fun showHighliter(paperlessInterface: PaperlessInterface, show: Boolean) {
        paperlessInterface?.apply {
            penPopupControl = show
            turnOffHighlighter()
        }
    }

    private fun checkMustEntry(index: Int, total: Int) {
        Log.e("SW_DEBUG", "checkMustEntry: $index // total: $total")
        val businessLogic = paperlessTable.elementAt(index)
        paperlessFragmentMap[businessLogic]?.run {
            isFillOutComplete { isComplete ->
                if (isComplete) {
                    if (index + 1 < total) {
                        checkMustEntry(index + 1, total)
                    } else {
                        checkFillOutComplete()
                    }
                } else {
                    viewModel.alert(
                        AlertData(
                            contentText = getString(R.string.alert_not_fill_out),
                            rightBtnText = getString(R.string.confirm),
                            onDismissRequest = {
                                viewModel.checkMustEntryComplete(false)
                                viewModel.getElectronicTabData(businessLogic)?.let {
                                    moveTab(it)
                                    fieldCallUpAtEmptyMustEntry()
                                }
                                viewModel.dismissAlert()
                            }
                        )
                    )
                }

            }
        }
    }

    private fun checkFillOutComplete() {
        lifecycleScope.launch {
            var complete = true

            run loop@{
                paperlessTable.forEach { businessLogic ->
                    paperlessFragmentMap[businessLogic]?.run {
                        fillOutComplete().collect {
                            val success = it.first
                            val message = it.second

                            complete = complete and success

                            if (!success) {
                                viewModel.alert(
                                    AlertData(
                                        contentText = message,
                                        rightBtnText = getString(R.string.confirm),
                                        onDismissRequest = {
                                            viewModel.checkMustEntryComplete(false)
                                            viewModel.getElectronicTabData(businessLogic)?.let {
                                                moveTab(it)
                                                fieldCallUpAtEmptyMustEntry()
                                            }
                                            viewModel.dismissAlert()
                                        }
                                    )
                                )
                            }
                        }

                        if (!complete) {
                            return@loop
                        }
                    }
                }
            }


            viewModel.checkMustEntryComplete(complete)
        }
    }

    private fun sendTFieldDataToWeb(result: (TFieldData) -> Unit) =
        lifecycleScope.launch(Dispatchers.IO) {
            val latch = CountDownLatch(viewModel.electronicTabDataList.size)
            val terminalData = mutableMapOf<String, Map<String, String>>()

            viewModel.electronicTabDataList.forEach {
                if (it.tFieldsAfterComplete.isEmpty()) {
                    latch.countDown()
                    return@forEach
                }

                paperlessFragmentMap[it.businessLogic]?.getTerminalInfo(
                    context = this.coroutineContext,
                    tFieldIdList = it.tFieldsAfterComplete
                ) { infoMap ->
                    terminalData[it.businessLogic] = infoMap
                    latch.countDown()
                } ?: latch.countDown()
            }

            latch.await()

            withContext(Dispatchers.Main) {
                result(TFieldData(terminalData))
            }
        }

    private fun sendPaperless() = lifecycleScope.launch(Dispatchers.IO) {
        val latch = CountDownLatch(paperlessFragmentMap.size)
        val terminalInfoMap = mutableMapOf<String, String>()
        val sendImageDataInfoList = viewModel.getSendImageDataInfos()

        paperlessFragmentMap.forEach { mapEntry ->
            mapEntry.value.getTerminalInfo { infoMap ->
                terminalInfoMap.putAll(infoMap)
                sendImageDataInfoList.forEach { sendImageDataInfo ->
                    infoMap[sendImageDataInfo.tFieldId]?.let { imageData ->
                        sendImageDataInfo.base64Image = imageData
                    }

                    terminalInfoMap.remove(sendImageDataInfo.tFieldId)
                }

                latch.countDown()
            }
        }

        latch.await()

        viewModel.sendPaperless(terminalInfoMap)
    }

    private fun saveResultXml(businessLogic: String, paperlessFragment: PaperlessFragment) {
        paperlessFragment.saveResultListener = saveResultListener
        val saveResultXmlPath = "$saveResultXmlRootPath/$businessLogic"
        paperlessFragment.saveResultXml(saveResultXmlPath)
    }

    fun finish() {
        findNavController().navigateUp()
    }
}

