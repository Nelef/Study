package com.inzisoft.ibks.view.fragment

import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.inzisoft.ibks.R
import com.inzisoft.ibks.base.BaseFragment
import com.inzisoft.ibks.data.internal.Thumbnail
import com.inzisoft.ibks.data.web.EvidenceDocument
import com.inzisoft.ibks.view.compose.*
import com.inzisoft.ibks.view.compose.theme.h7
import com.inzisoft.ibks.view.fragment.paperelss.*
import com.inzisoft.ibks.viewmodel.EvidenceDocumentViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

private const val ARG_DATA = "document_data"

@AndroidEntryPoint
class EvidenceDocumentViewFragment : BaseFragment(), DocInterface {
    private val viewModel by viewModels<EvidenceDocumentViewModel>()
    var listener: PaperlessInterface.Listener? = null

    companion object {
        @JvmStatic
        fun newInstance(
            docList: List<EvidenceDocument>
        ) = EvidenceDocumentViewFragment().apply {
//            arguments?.putParcelableArray(ARG_DATA, docList.toTypedArray())
            arguments = bundleOf(
                ARG_DATA to docList.toTypedArray()
            )
        }
    }

    init {
        baseCompose.content = {
            EvidenceDocumentViewScreen(
                showPrevBtn = viewModel.isShowPrevBtn,
                onPrevPage = { viewModel.goPrevPage() },
                showNextBtn = viewModel.isShowNextBtn,
                onNextPage = { viewModel.goNextPage() },
                currentPage = viewModel.curPage.value,
                showThumbnail = viewModel.isShowThumbnail,
                thumbnailList = viewModel.thumbnailList.value,
                onClickThumbnail = { index -> viewModel.goDocsPage(index) }
            )
        }
    }

    @Composable
    fun EvidenceDocumentViewScreen(
        showPrevBtn: Boolean,
        onPrevPage: () -> Unit,
        showNextBtn: Boolean,
        onNextPage: () -> Unit,
        showThumbnail: Boolean,
        currentPage: Int,
        thumbnailList: List<Thumbnail>,
        onClickThumbnail: (index: Int) -> Unit
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
                .clickable(enabled = false) { }
        ) {
            val image = viewModel.docImage

            if (image == null || image.path.isEmpty()) {
                Text(
                    text = stringResource(id = R.string.message_empty_doc),
                    modifier = Modifier.align(Alignment.Center),
                    style = MaterialTheme.typography.h7
                )
            } else {
                ImageViewer(
                    modifier = Modifier.fillMaxSize(),
                    imagePaths = listOf(image.path),
                    onScrollBottom = {},
                    onError = {}
                )
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.run {
            @Suppress("DEPRECATION")
            val evidenceDocumentList = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                getParcelableArray(ARG_DATA)
            } else {
                getParcelableArray(ARG_DATA, EvidenceDocument::class.java)
            } ?: arrayOf<EvidenceDocument>()
            viewModel.makeDocsThumbnail(evidenceDocumentList.toList() as List<EvidenceDocument>)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        lifecycleScope.launch(Dispatchers.Main) {
            combine(viewModel.curPage, viewModel.totalPage) { curPage, totalPage ->
                Pair(curPage, totalPage)
            }.collect {
                listener?.updatePage("", it.first, it.second)
            }
        }
    }

    override fun loadThumbnail(result: (thumbnailList: List<Thumbnail>) -> Unit) {
        TODO("Not yet implemented")
    }

    override fun openThumbnail() {
        viewModel.showThumbnail()
    }

    override fun closeThumbnail() {
        viewModel.closeThumbnail()
    }

    override fun isOpenThumbnail(): Boolean {
        TODO("Not yet implemented")
    }

    override fun getPageInfo() {
        runCatching {
            listener?.updatePage("", viewModel.curPage.value, viewModel.totalPage.value)
        }
    }

    override fun goPage(index: Int) {
        viewModel.goDocsPage(index)
    }
}