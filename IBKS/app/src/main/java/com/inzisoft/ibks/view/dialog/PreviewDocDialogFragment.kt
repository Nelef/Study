package com.inzisoft.ibks.view.dialog

import android.graphics.*
import android.util.Log
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.request.RequestOptions
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import com.inzisoft.ibks.*
import com.inzisoft.ibks.R
import com.inzisoft.ibks.base.BaseDialogFragment
import com.inzisoft.ibks.base.BaseDialogFragmentViewModel
import com.inzisoft.ibks.view.compose.*
import com.inzisoft.ibks.view.compose.theme.*
import com.inzisoft.ibks.view.overlayviews.CustomMaskingCanvasView
import com.inzisoft.ibks.viewmodel.DialogUiState
import com.inzisoft.ibks.viewmodel.IndexLocationState
import com.inzisoft.ibks.viewmodel.PreviewDocState
import com.inzisoft.ibks.viewmodel.PreviewDocViewModel
import com.inzisoft.mobile.util.CommonUtils
import com.inzisoft.mobile.view.MaskingCanvasView
import com.inzisoft.mobile.view.MaskingCanvasView.RectData
import com.skydoves.landscapist.glide.GlideImage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@OptIn(ExperimentalPagerApi::class)
@AndroidEntryPoint
class PreviewDocDialogFragment : BaseDialogFragment() {

    val viewModel: PreviewDocViewModel by viewModels()
    lateinit var maskingCanvasView: MaskingCanvasView

    init {
        baseCompose.topBar = {
            PreviewDocTopBar(
                title = viewModel.docInfoData?.docName?: "",
                showBack = true,
                onBack = {
                    cancel()
                },
                showCompleteBtn = viewModel.previewDocType != PreviewDocType.PREVIEW_DOC
            ) {
                viewModel.sendDocImageToServer()
            }
        }

        baseCompose.bottomBar = {
            if (viewModel.previewDocType != PreviewDocType.PREVIEW_DOC) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(68.dp)
                        .background(
                            color = MaterialTheme.colors.background1Color,
                            shape = RectangleShape
                        ),
                    contentAlignment = Alignment.BottomStart
                ) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .offset(x = 24.dp)
                            .size(80.dp, 48.dp)
                            .background(Color(0xFFEFEFEF), RoundedCornerShape(16.dp))
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = viewModel.positionString,
                            color = MaterialTheme.colors.sub1Color,
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.subtitle1
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .selectableGroup(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        ColorButton(
                            enabled = viewModel.docInfoData!!.maskingYn,
                            onClick = { viewModel.clickMaskingBtn() } ,
                            text = stringResource(id = R.string.add_masking),
                            modifier = Modifier
                                .padding(start = 24.dp)
                                .width(240.dp),
                            buttonStyle = ButtonStyle.Basic
                        )

                        Spacer(modifier = Modifier.width(20.dp))

                        ColorButton(
                            enabled = viewModel.docInfoData!!.maskingYn,
                            onClick = {
                                maskingCanvasView.resetView()
                                viewModel.clickUnmaskingBtn()
                            },
                            text = stringResource(id = R.string.remove_masking),
                            modifier = Modifier
                                .padding(start = 24.dp)
                                .width(240.dp),
                            buttonStyle = ButtonStyle.Basic
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .selectableGroup(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End
                    ) {

                        TopBarDivider()

                        IconButton(
                            onClick = {
                                viewModel.clickDeleteBtn()
                            },
                            modifier = Modifier.padding(horizontal = 24.dp),
                            icon = R.drawable.icon_trash,
                            pressedIcon = R.drawable.icon_trash_on
                        )
                    }
                }
            }
        }

        baseCompose.content = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFD7D7D7))
            ) {
                var pagerState = rememberPagerState()
                val coroutineScope = rememberCoroutineScope()

                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    when(viewModel.previewDocType) {
                        PreviewDocType.PREVIEW_DOC -> {
                            viewModel.docInfoData?.apply {
                                HorizontalPager(
                                    modifier = Modifier.fillMaxSize(),
                                    count = docImageDataList.size,
                                    state = pagerState
                                ) { page ->
                                    viewModel.changePosition(pagerState.currentPage)
                                    ZoomImageView(
                                        modifier = Modifier.fillMaxSize(),
                                        imagePath = docImageDataList[page].realImagePath
                                    )
                                }
                            }

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
                                    text = viewModel.positionString,
                                    color = MaterialTheme.colors.background1Color,
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.subtitle1
                                )
                            }
                        }
                        else -> {
                            MaskingImageView()
                        }
                    }
                }

                val indexLocationState = viewModel.indexLocationState
                if(indexLocationState != IndexLocationState.UnderOneSize) {
                    IconButton(
                        enabled = (indexLocationState != IndexLocationState.FirstIndex),
                        modifier = Modifier
                            .size(96.dp)
                            .align(Alignment.CenterStart),
                        onClick = {
                            when (viewModel.previewDocType) {
                                PreviewDocType.PREVIEW_DOC -> coroutineScope.launch {
                                    pagerState.scrollToPage(pagerState.currentPage - 1)
                                }
                                else -> viewModel.clickPrevBtn()
                            }
                        },
                        shape = CircleShape,
                        backgroundColor = Color(0x33000000),
                        icon = R.drawable.view_back,
                        pressedIcon = R.drawable.view_back_on
                    )
                    IconButton(
                        enabled = (indexLocationState != IndexLocationState.LastIndex),
                        modifier = Modifier
                            .size(96.dp)
                            .align(Alignment.CenterEnd),
                        onClick = {
                            when (viewModel.previewDocType) {
                                PreviewDocType.PREVIEW_DOC -> coroutineScope.launch {
                                    pagerState.scrollToPage(pagerState.currentPage + 1)
                                }
                                else -> viewModel.clickNextBtn()
                            }
                        },
                        shape = CircleShape,
                        backgroundColor = Color(0x33000000),
                        icon = R.drawable.view_next,
                        pressedIcon = R.drawable.view_next_on
                    )
                }
            }
        }

        baseCompose.surface = {
            when (val dialogUiState = viewModel.dialogUiState) {
                is DialogUiState.DeleteImageAlertDialog -> {
                    AlertDialog(
                        contentText = stringResource(id = R.string.delete_image_popup),
                        leftBtnText = stringResource(id = R.string.no),
                        onLeftBtnClick = { viewModel.dialogUiState = DialogUiState.None },
                        rightBtnText = stringResource(id = R.string.yes),
                        onRightBtnClick = {
                            viewModel.deleteCurrentImage()
                        })
                }
                is DialogUiState.SendSuccessDialog -> {
                    AlertDialog(
                        contentText = stringResource(id = R.string.camera_complete),
                        onRightBtnClick = {
                            viewModel.dialogUiState = DialogUiState.None
                            setFragmentResult(
                                FragmentRequest.PreviewDoc,
                                FragmentResult.OK(dialogUiState.imageCount)
                            )
                            findNavController().navigateUp()
                        }
                    )
                }
                is DialogUiState.ImageAllRemovedDialog -> {
                    Log.e("SW_DEBUG", "PreviewDocState.ImageAllRemoved")
                    AlertDialog(
                        contentText = stringResource(id = R.string.empty_image),
                        onRightBtnClick = {
                            viewModel.dialogUiState = DialogUiState.None
                            cancel()
                        }
                    )
                }
                is DialogUiState.SendFailedDialog -> {
                    BasicDialog(
                        titleText = stringResource(id = R.string.send_doc_fail_popup_title),
                        contentText = buildAnnotatedString {
                            append(dialogUiState.message)
                        },
                        rightBtnText = stringResource(id = R.string.confirm),
                        onRightBtnClick = {
                            viewModel.dialogUiState = DialogUiState.None
                        },
                        onClosed = {
                            viewModel.dialogUiState = DialogUiState.None
                        }
                    )
                }
                is DialogUiState.Loading -> {
                    Loading()
                }
                is DialogUiState.MaskingAlertDialog,
                is DialogUiState.MaskingAddCompleteDialog,
                is DialogUiState.MaskingRemoveCompleteDialog -> {
                    val contentText = when(dialogUiState) {
                        is DialogUiState.MaskingAlertDialog -> R.string.nomasking_alert
                        is DialogUiState.MaskingAddCompleteDialog -> R.string.masking_add_compelete
                        else -> R.string.masking_remove_compelete
                    }
                    AlertDialog(
                        contentText = stringResource(id = contentText),
                        onRightBtnClick = {
                            viewModel.dialogUiState = DialogUiState.None
                        }
                    )
                }
            }

            when(val previewDocState = viewModel.previewDocState) {
                is PreviewDocState.Masking -> {
                    val maskedBitmap = applyMasks(previewDocState.bitmap)
                    previewDocState.docImageData.saveMaskedImageFile(
                        context = requireContext(),
                        maskedBitmap = maskedBitmap
                    )
                    viewModel.previewDocState = PreviewDocState.None
                    viewModel.dialogUiState = DialogUiState.MaskingAddCompleteDialog
                }
                is PreviewDocState.None -> {}
            }
        }
    }

    @Composable
    fun ZoomImageView(modifier: Modifier, imagePath: String) {
        var scale by remember { mutableStateOf(1f) }
        scale = 1f
        val transformableState =
            rememberTransformableState { zoomChange, offsetChange, rotationChange ->
                if(scale * zoomChange < 1f) {
                    scale = 1f
                } else {
                    scale *= zoomChange
                }
            }


        GlideImage(
            imageModel = imagePath,
            modifier = Modifier
                .then(modifier)
                .transformable(state = transformableState)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                },
            requestOptions = {
                RequestOptions().skipMemoryCache(true)
            }
        )
    }

    @Composable
    fun MaskingImageView() {
        AndroidView(
            factory = {
                Log.e("SW_DEBUG", "new MaskingCanvasView factory")
                val container = RelativeLayout(context).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }

                val layoutParams = RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE)
                maskingCanvasView = CustomMaskingCanvasView(
                    requireContext(),
                    null,
                    viewModel.docInfoData!!.maskingYn
                ).apply {
                    Log.e("SW_DEBUG", "new MaskingCanvasView: $this")

                    adjustViewBounds = true

                    if (usableMasking) {
                        //마스킹 시 터치되는 그림자 색깔 설정
                        setShadeColor(android.graphics.Color.DKGRAY)

                        //마스킹 시 그려지는 rect 색깔 설정
                        setRectColor(android.graphics.Color.BLACK)
                    }
                }

                container.addView(maskingCanvasView, layoutParams)
                container
            },
            update = {
                viewModel.image?.apply {
                    Log.e("SW_DEBUG", "new MaskingCanvasView image")
                    maskingCanvasView.resetView()
                    maskingCanvasView.setImageBitmap(null)
                    maskingCanvasView.setImageBitmap(
                        this,
                        this.width,
                        this.height
                    )
                }
            }
        )
    }

    //마스킹 적용
    private fun applyMasks(bmp: Bitmap): Bitmap {
        var bitmap: Bitmap = bmp
        val dataList: ArrayList<RectData> = maskingCanvasView.maskedList
        val bitmapWidth = bitmap!!.width
        val bitmapHeight = bitmap.height
        val size: Point = maskingCanvasView.screenSize
        for (data in dataList) {
            val rect = CommonUtils.convertDisplayROIToPreviewROI(
                size,
                Point(bitmapWidth, bitmapHeight),
                data.maskedRect
            )
            bitmap = addMasking(bitmap, rect, android.graphics.Color.BLACK)
        }
        return bitmap
    }

    //라인별 마스킹
    private fun addMasking(originBitmap: Bitmap, maskingRect: Rect, color: Int): Bitmap {
        var bitmap = originBitmap
        val maskingBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, bitmap.config)
        val maskingCanvas = Canvas(maskingBitmap)
        val paint = Paint()
        paint.color = color
        maskingCanvas.drawBitmap(bitmap, 0f, 0f, null)
        maskingCanvas.drawRect(maskingRect, paint)
        return maskingBitmap
        return bitmap
    }

    override fun getBaseViewModel(): BaseDialogFragmentViewModel? {
        return viewModel
    }

    private fun cancel() {
        setFragmentResult(FragmentRequest.PreviewDoc, FragmentResult.Cancel())
        findNavController().navigateUp()
    }
}