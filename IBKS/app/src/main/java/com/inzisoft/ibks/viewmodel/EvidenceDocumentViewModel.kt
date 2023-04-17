package com.inzisoft.ibks.viewmodel

import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.inzisoft.ibks.PathManager
import com.inzisoft.ibks.base.BaseViewModel
import com.inzisoft.ibks.data.internal.Image
import com.inzisoft.ibks.data.internal.Thumbnail
import com.inzisoft.ibks.data.repository.LocalRepository
import com.inzisoft.ibks.data.web.EvidenceDocument
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EvidenceDocumentViewModel @Inject constructor(
    private val pathManager: PathManager,
    private val localRepository: LocalRepository
) : BaseViewModel() {
    var docImage: Image? by mutableStateOf(null)
    private val docsThumbnails = mutableListOf<Thumbnail>()
    private val _thumbnailList = mutableStateOf<List<Thumbnail>>(listOf())
    val thumbnailList: State<List<Thumbnail>> = _thumbnailList
    lateinit var entryId: String

    private val _totalPage = MutableStateFlow(0)
    private val _curPage = MutableStateFlow(0)
    val totalPage: StateFlow<Int> = _totalPage
    val curPage: StateFlow<Int> = _curPage

    var isShowPrevBtn by mutableStateOf(false)
    var isShowNextBtn by mutableStateOf(false)

    var isShowThumbnail by mutableStateOf(false)

    fun makeDocsThumbnail(docs: List<EvidenceDocument>) {
        viewModelScope.launch(Dispatchers.IO) {
            entryId = localRepository.getAuthInfo().first().edocKey
            Log.e("SW_DEBUG", "entryId: $entryId")

            docs.forEach { evidenceDocument ->
                val imageCount = pathManager.getEvidenceDocImageCount(entryId, evidenceDocument.code)

                (0 until imageCount).forEach { index ->
                    val path = pathManager.getEvidenceJpgImage(entryId, evidenceDocument.code, index)

                    val options = BitmapFactory.Options().apply {
                        inJustDecodeBounds = true
                    }.also {
                        BitmapFactory.decodeFile(path, it)
                    }

                    docsThumbnails.add(
                        Thumbnail(
                            title = evidenceDocument.name,
                            image = Image(path, options.outWidth, options.outHeight),
                            isFirst = index == 0,
                            isShowDivider = imageCount == index + 1
                        )
                    )
                }
            }

            if (docsThumbnails.isNotEmpty()) {
                loadThumbnail()
                docImage = docsThumbnails[0].image
                updateDocPageInfo(1, docsThumbnails.size)
            }
        }
    }

    private fun updateDocPageInfo(curPage: Int, totalPage: Int) {
        isShowPrevBtn = curPage > 1
        isShowNextBtn = curPage < totalPage

        _curPage.value = curPage
        _totalPage.value = totalPage
    }

    fun goDocsPage(index: Int) {
        docImage = docsThumbnails[index].image
        updateDocPageInfo(index + 1, _totalPage.value)
    }

    private fun loadThumbnail() {
        if (docsThumbnails.isEmpty()) {
            _thumbnailList.value = listOf()
        } else {
            _thumbnailList.value = docsThumbnails
        }
    }

    fun showThumbnail() {
        isShowThumbnail = true
    }

    fun closeThumbnail() {
        isShowThumbnail = false
    }

    fun goNextPage() {
        goDocsPage(_curPage.value)
    }

    fun goPrevPage() {
        goDocsPage(_curPage.value - 2)
    }
}