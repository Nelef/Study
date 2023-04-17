package com.inzisoft.ibks.view.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import com.inzisoft.ibks.base.BaseDialogFragment
import com.inzisoft.ibks.databinding.FragmentCameraBinding
import com.inzisoft.ibks.view.overlayviews.OverlayView
import com.inzisoft.ibks.viewmodel.CameraState
import com.inzisoft.ibks.viewmodel.CameraViewModel

abstract class CameraDialogFragment : BaseDialogFragment() {
    lateinit var binding: FragmentCameraBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCameraBinding.inflate(inflater, container, false)
        binding.cameraPreview.apply {
            viewTreeObserver.addOnGlobalLayoutListener(object :
                ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    viewTreeObserver.removeOnGlobalLayoutListener(this)
                    initCamera()
                }
            })
        }

        initComposeLayout()

        return binding.root
    }

    abstract fun initComposeLayout()
    abstract fun getViewModel(): CameraViewModel

    // 카메라 초기화
    private fun initCamera() {
        val overlayView = OverlayView(requireContext(), getViewModel().config.recogType, binding.rightComposeView.width)
        getViewModel().initCamera(requireActivity(), binding.btnTakeCamera, overlayView, binding.cameraPreview)
    }

    override fun onResume() {
        super.onResume()
        if(getViewModel().cameraState == CameraState.CameraPreviewState) {
            getViewModel().cameraResume()
        }
    }

    override fun onPause() {
        super.onPause()
        getViewModel().cameraPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        getViewModel().cameraRelease()
    }
}