package com.inzisoft.ibks.view.dialog

import android.view.View
import android.widget.Toast
import androidx.fragment.app.clearFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.inzisoft.ibks.*
import com.inzisoft.ibks.base.BaseDialogFragmentViewModel
import com.inzisoft.mobile.data.MIDReaderProfile
import com.inzisoft.ibks.viewmodel.CameraViewModel
import com.inzisoft.ibks.viewmodel.NormalAuthCameraViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NormalAuthCameraDialogFragment: BaseNormalCameraDialogFragment() {
    private val normalAuthCameraViewModel by viewModels<NormalAuthCameraViewModel>()

    init {
        MIDReaderProfile.getInstance().SET_USER_SCREEN_PORTRAIT = false
    }

    override fun getViewModel(): CameraViewModel {
        return normalAuthCameraViewModel
    }

    override fun initComposeLayout() {
        super.initComposeLayout()
        binding.btnGallery.visibility = View.GONE
    }

    override fun showPreviewDocDialogFragment() {
        setFragmentResultListener(FragmentRequest.PreviewDoc) { scriptFuncName, fragmentResult ->
            clearFragmentResultListener(FragmentRequest.PreviewDoc.key)
            when (fragmentResult) {
                is FragmentResult.Cancel -> {
                    normalAuthCameraViewModel.restartCamera()
                }
                is FragmentResult.Error -> {}
                is FragmentResult.OK -> {
                    setFragmentResult(FragmentRequest.NormalAuthCamera, FragmentResult.OK(null))
                    findNavController().navigateUp()
                }
            }
        }

        findNavController().navigate(
            NormalAuthCameraDialogFragmentDirections.actionNormalAuthCameraDialogFragmentToPreviewDocDialogFragment(
                normalAuthCameraViewModel.scriptFunName!!,
                PreviewDocType.NORMAL_AUTH,
                normalAuthCameraViewModel.docInfoData
            )
        )
    }

    override fun showInfoToast() {
        Toast.makeText(requireContext(), R.string.normal_auth_camera_toast, Toast.LENGTH_SHORT).show()
    }

    override fun cancel() {
        normalAuthCameraViewModel.clearAllImage()
        setFragmentResult(FragmentRequest.NormalAuthCamera, FragmentResult.Cancel())
        findNavController().navigateUp()
    }

    override fun getBaseViewModel(): BaseDialogFragmentViewModel? {
        return normalAuthCameraViewModel
    }
}