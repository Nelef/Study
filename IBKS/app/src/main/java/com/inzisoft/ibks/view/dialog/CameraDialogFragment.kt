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
//    private fun initSecKeypad() {
//        // 핸들러 생성
//        //progressHandler = ProgressHandler(this)
//
//        /////////////////////////////////////////////////////////////////////////////////////////////////////
//        /////////////////////////////////////////////////////////////////////////////////////////////////////
//        // 보안 키패드 설정
//        keypadMngHelper = IxKeypadManageHelper(requireContext(), 0x2231)// 키패드 핼퍼 객체 생성
//        // 키패드 입력 시 화면 설정
//        keypadMngHelper.setUiVisibility(Defines.FLAG_INHERIT_UI_VISIBILITY)
//
//        keypadMngHelper.setEnableViewMode(true)
//        // 키패드 콜백 리스너 등록
//        keypadMngHelper.setSecureKeypadEventListener(object : IxKeypadManageHelper.SecureKeypadEventListener {
//            /** 키패드 높이 반환  */
//            override fun onKeypadChangeHeight(height: Int) {}
//
//            /** 입력 제한 정책 위반 콜백  */
//            override fun onInputViolationOccured(errorCode: Int) {}
//            override fun onKeypadCreate() {
//                TODO("Not yet implemented")
//            }
//
//            override fun onKeypadFinish() {
//            }
//
//            override fun onKeypadFinish(resultCode: Int) {
//            }
//
//            /** 포커싱 변환 콜백  */
//            override fun onChangeEditText(edittext: EditText) {
//                //focusingInputBox(edittext, true) // 포커싱 되어 있는 입력창을 포커싱 입력창 이미지로 변경
//            }
//
//            override fun onInputChanged(edittext: EditText, count: Int) {}
//            override fun onInitializing(onloading: Boolean) {}
//        })
//
//        val inputConfig = IxConfigureInputItem(binding.pwText, Defines.KEYPAD_TYPE_QWERTY, Defines.SHUFFLE_TYPE_GAPKEY)
//        inputConfig.minLength = 6
//        inputConfig.maxLength = 12
//
//        keypadMngHelper.configureInputBox(binding.pwText, inputConfig)
//    }
}