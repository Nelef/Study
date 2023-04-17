package com.inzisoft.ibks.view.dialog

import android.graphics.Bitmap
import android.view.View
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.stringResource
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.inzisoft.ibks.FragmentRequest
import com.inzisoft.ibks.FragmentResult
import com.inzisoft.ibks.R
import com.inzisoft.ibks.setFragmentResult
import com.inzisoft.ibks.view.compose.Loading
import com.inzisoft.ibks.view.compose.OcrCameraTopBar
import com.inzisoft.ibks.view.compose.theme.IBKSTheme
import com.inzisoft.ibks.viewmodel.CameraViewModel
import com.inzisoft.ibks.viewmodel.SealCameraViewModel
import com.inzisoft.ibks.viewmodel.SealScanState

class SealCameraDialogFragment: OcrCameraDialogFragment() {
    val viewModel: SealCameraViewModel by viewModels()

    override fun initComposeLayout() {
//        super.initComposeLayout()
        SealCameraScreen(
            binding.topComposeView,
            binding.leftComposeView,
            binding.rightComposeView,
            binding.popupComposeView
        )

        binding.tvOcrGuide.visibility = View.GONE
        binding.lyCameraOptions.visibility = View.VISIBLE
        binding.btnGallery.visibility = View.GONE
        binding.ivThumbnail.visibility = View.GONE
        binding.tvThumbcount.visibility = View.GONE
        binding.guideline.visibility = View.GONE
    }

    private fun SealCameraScreen(
        topComposeView: ComposeView,
        leftComposeView: ComposeView,
        rightComposeView: ComposeView,
        popupComposeView: ComposeView
    ) {
        topComposeView.apply {
            setContent {
                IBKSTheme {
                    val cameraState = viewModel.cameraState

                    OcrCameraTopBar(
                        title = viewModel.config.title,
                        cameraState = cameraState,
                        showRetake = false,
                        showAuth = false,
                        onCancel = {
                            setFragmentResult(FragmentRequest.SealCamera, FragmentResult.Cancel())
                            findNavController().navigateUp()
                        }
                    )
                }
            }
        }

        popupComposeView.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                IBKSTheme {
                    when(val sealScanResultState = viewModel.sealScanResultState) {
                        is SealScanState.SealScanComplete -> { complete(sealScanResultState.bitmap) }
                        is SealScanState.Loading -> { Loading() }
                        is SealScanState.Error -> {
                            ShowAlertDialog(
                                contentText = stringResource(id = R.string.seal_scan_failed),
                                onDismissRequest = {
                                    viewModel.cameraResume()
                                    viewModel.sealScanResultState = SealScanState.None
                                }
                            )
                        }
                        is SealScanState.None -> {}
                    }
                }
            }
        }

        leftComposeView.visibility = View.GONE
        rightComposeView.visibility = View.GONE
    }

    override fun getViewModel(): CameraViewModel {
        return viewModel
    }

    private fun complete(bitmap: Bitmap) {
        viewModel.sealScanResultState = SealScanState.None
        setFragmentResult(FragmentRequest.SealCamera, FragmentResult.OK(bitmap))
        findNavController().navigateUp()
    }
}