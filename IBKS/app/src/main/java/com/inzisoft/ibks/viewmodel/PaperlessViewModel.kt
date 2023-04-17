package com.inzisoft.ibks.viewmodel

import android.graphics.BitmapFactory
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.unit.IntSize
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inzisoft.ibks.PathManager
import com.inzisoft.ibks.R
import com.inzisoft.ibks.base.UiState
import com.inzisoft.ibks.data.internal.AlertData
import com.inzisoft.ibks.data.internal.LoadOptions
import com.inzisoft.ibks.data.internal.PaperlessData
import com.inzisoft.ibks.data.internal.Thumbnail
import com.inzisoft.ibks.data.repository.LocalRepository
import com.inzisoft.ibks.util.log.QLog
import com.inzisoft.paperless.model.ExternalFormData
import com.inzisoft.paperless.ods.data.TFieldInfo
import com.inzisoft.paperless.util.ImageController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject

@HiltViewModel
class PaperlessViewModel @Inject constructor(
    private val pathManager: PathManager,
    private val localRepository: LocalRepository,
) : ViewModel() {

    private val _loading = mutableStateOf<UiState<Unit>>(UiState.None)
    val loading: State<UiState<Unit>> get() = _loading

    private val _viewMode = mutableStateOf<ViewMode>(ViewMode.Write)
    val viewMode: State<ViewMode> get() = _viewMode

    private val _paperlessState = MutableStateFlow<PaperlessState>(PaperlessState.None)
    val paperlessState: StateFlow<PaperlessState> = _paperlessState

    private val _totalPage = mutableStateOf(0)
    val totalPage: State<Int> get() = _totalPage

    private val _curPage = mutableStateOf(0)
    val curPage: State<Int> get() = _curPage

    private val _isShowPrevBtn = mutableStateOf(false)
    val isShowPrevBtn: State<Boolean> get() = _isShowPrevBtn

    private val _isShowNextBtn = mutableStateOf(false)
    val isShowNextBtn: State<Boolean> get() = _isShowNextBtn

    private val _isShowThumbnail = mutableStateOf(false)
    val isShowThumbnail: State<Boolean> get() = _isShowThumbnail

    var thumbnails = listOf<Thumbnail>()

    private lateinit var entryId: String
    lateinit var paperlessData: PaperlessData
    private var isInit = false

    val businessLogic: String get() = if (this::paperlessData.isInitialized) paperlessData.businessLogic else ""

    private var initialTFieldInfo = TFieldInfo()

    var formList = mutableListOf<ExternalFormData>()

    private var fillOutComplete: ProducerScope<Pair<Boolean, String>>? = null

    private val initMutex = Mutex()

    var containerSize = IntSize(0, 0)
        set(value) {
            if (value.width == 0 || value.height == 0) return

            if (containerSize.width == value.width && containerSize.height == value.height) return

            field = value

            init()
        }

    fun init() = viewModelScope.launch(Dispatchers.IO) {
        initMutex.withLock {
            if (isInit) return@withLock

            if (containerSize == IntSize.Zero) return@withLock

            if (!this@PaperlessViewModel::paperlessData.isInitialized) return@withLock

            _loading.value = UiState.Loading(strResId = R.string.load_application)

            runCatching {
                entryId = localRepository.getAuthInfo().first().edocKey

                if (entryId.isEmpty()) {
                    throw IllegalArgumentException("entryId is empty.")
                }

                validatePaperlessData(paperlessData)

                paperlessData
            }.onSuccess {
                when (val options = it.loadOptions) {
                    is LoadOptions.Write -> options.data?.let { initialTFieldInfo.putTFieldValue(it) }
                    else -> {}
                }

                isInit = true
                _paperlessState.update {
                    PaperlessState.Init(
                        containerSize.width,
                        containerSize.height
                    )
                }
            }.onFailure {
                loadFail(it)
            }
        }
    }

    private fun validatePaperlessData(paperlessData: PaperlessData) {
        with(paperlessData) {
            require(businessLogic != PaperlessData.DEFAULT_ID) { "PaperlessData is init." }
            require(businessLogic.isNotEmpty()) { "businessLogic is empty." }
            when (loadOptions) {
                is LoadOptions.Restore -> require(loadOptions.resultXmlDirPathList.isNotEmpty()) { "resultXmlRootPathList is empty." }
                else -> {}
            }
        }
    }

    fun getBizRootPath(): String {
        return pathManager.getBizRootDir()
    }

    fun loadBusinessLogic(callback: (businessLogic: String, options: LoadOptions) -> Unit) {
        callback(paperlessData.businessLogic, paperlessData.loadOptions)
    }

    fun onEmptyDocumentLoaded() {
        loadFail(IllegalStateException("no open forms"))
    }

    fun onChangeViewMode(preview: Boolean) {
        _viewMode.value = if (preview) ViewMode.Preview else ViewMode.Write
    }

    fun updatePage(curPage: Int, totalPage: Int) {
        _isShowPrevBtn.value = curPage > 1
        _isShowNextBtn.value = curPage < totalPage

        _curPage.value = curPage
        _totalPage.value = totalPage
    }

    fun onDocumentLoaded(formData: ExternalFormData) {
        formList.add(formData)
    }

    fun onDocumentLoadComplete() = viewModelScope.launch(Dispatchers.IO) {
        when (val options = paperlessData.loadOptions) {
            is LoadOptions.Restore -> loadComplete()
            is LoadOptions.Write -> setData(options.image)
        }
    }

    private fun setData(pen: List<String>?) {
        with(initialTFieldInfo) {
            if (size() > 0) {
                runCatching {
                    pen?.forEach { id ->
                        val path = pathManager.getPenImagePath(entryId, id)
                        val image = BitmapFactory.decodeFile(path)?.run {
                            ImageController.convertBitmapToBase64PngString(this).apply {
                                recycle()
                            }
                        } ?: throw NullPointerException("$id image is empty.")

                        initialTFieldInfo.putTFieldValue(id, image)
                    }
                }.onSuccess {
                    _paperlessState.update { PaperlessState.SetData(this) }
                }.onFailure {
                    loadFail(it)
                }
            } else {
                loadComplete()
            }
        }
    }

    fun onDataSetComplete() {
        if (initialTFieldInfo.size() > 0) {
            initialTFieldInfo = TFieldInfo()
        }

        loadComplete()
    }

    private fun loadComplete() {
        _loading.value = UiState.Success(Unit)
        _paperlessState.update { PaperlessState.LoadComplete(businessLogic, formList.size) }
    }

    fun loadFail(throwable: Throwable) {
        QLog.e(throwable)
        _loading.value = UiState.Error(throwable = throwable)
        _paperlessState.update { PaperlessState.LoadFail(throwable = throwable) }
    }

    fun alert(alertData: AlertData) {

    }

    fun dismissAlert() {
        TODO("Not yet implemented")
    }

    fun onLoadThumbnail(thumbnails: List<Thumbnail>) {
        this.thumbnails = thumbnails
        _isShowThumbnail.value = true
    }

    fun closeThumbnail() {
        _isShowThumbnail.value = false
        this.thumbnails = listOf()
    }

    fun fillOutComplete(producerScope: ProducerScope<Pair<Boolean, String>>) {
        fillOutComplete = producerScope
    }

    fun onFillOutComplete(isSuccess: Boolean, message: String) {
        fillOutComplete?.run {
            trySend(isSuccess to message)
            close()
        }
    }
}

sealed class ViewMode {
    object Write : ViewMode()
    object Preview : ViewMode()
}

sealed class PaperlessState {
    object None : PaperlessState()
    data class Init(val containerWidth: Int, val containerHeight: Int) : PaperlessState()
    data class LoadComplete(val businessLogic: String, val formCount: Int) : PaperlessState()
    data class LoadFail(val throwable: Throwable) : PaperlessState()
    data class GoToPdf(val formCode: String) : PaperlessState()
    data class SetData(val tFieldInfo: TFieldInfo) : PaperlessState()
    object Destroy : PaperlessState()
}