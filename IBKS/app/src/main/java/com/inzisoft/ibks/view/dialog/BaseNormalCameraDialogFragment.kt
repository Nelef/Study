package com.inzisoft.ibks.view.dialog

import android.os.Handler
import android.os.Looper
import android.view.View
import com.inzisoft.ibks.data.internal.AuthDialogData
import com.inzisoft.ibks.view.compose.AlertDialog
import com.inzisoft.ibks.view.compose.NormalCameraTopBar
import com.inzisoft.ibks.view.compose.theme.IBKSTheme
import com.inzisoft.ibks.viewmodel.BaseNormalCameraViewModel
import com.inzisoft.ibks.viewmodel.CameraState

abstract class BaseNormalCameraDialogFragment : CameraDialogFragment() {
    override fun initComposeLayout() {
        binding.leftComposeView.visibility = View.GONE
        binding.rightComposeView.visibility = View.GONE
        binding.aboveComposeView.visibility = View.GONE
        binding.lyCameraOptions.visibility = View.VISIBLE
        binding.tvOcrGuide.visibility = View.GONE
        binding.topComposeView.apply {
            setContent {
                IBKSTheme {
                    val viewModel = getViewModel() as BaseNormalCameraViewModel

                    when (getViewModel().cameraState) {
                        CameraState.CameraMaxTake -> {
                            binding.btnTakeCamera.isEnabled = false
                            binding.btnGallery.isEnabled = false
                            showPreviewDocDialogFragment()
                        }
                        else -> {
                            binding.btnTakeCamera.isEnabled = true
                            binding.btnGallery.isEnabled = true
                        }
                    }

                    binding.ivThumbnail.setImageBitmap(viewModel.imageData)
                    if (viewModel.imageData != null) {
                        binding.ivThumbnail.setOnClickListener {
                            showPreviewDocDialogFragment()
                        }
                    } else {
                        binding.ivThumbnail.setOnClickListener(null)
                    }

                    binding.tvThumbcount.text = if (viewModel.imageIndex == 0) {
                        binding.tvThumbcount.visibility = View.GONE
                        "0"
                    } else {
                        binding.tvThumbcount.visibility = View.VISIBLE
                        "${viewModel.imageIndex}"
                    }

                    NormalCameraTopBar(
                        title = viewModel.docInfoData.docName,
                        canComplete = viewModel.imageIndex != 0,
                        onCancel = { cancel() }) {
                        showPreviewDocDialogFragment()
                    }
                }
            }
        }


        val runnable = Runnable {
            showInfoToast()
        }
        Handler(Looper.getMainLooper()).postDelayed(runnable, 1000)
    }

    abstract fun cancel()

    abstract fun showPreviewDocDialogFragment()

    abstract fun showInfoToast()
}