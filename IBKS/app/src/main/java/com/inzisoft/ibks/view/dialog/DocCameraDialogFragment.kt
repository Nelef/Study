package com.inzisoft.ibks.view.dialog

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.clearFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.inzisoft.ibks.*
import com.inzisoft.ibks.base.BaseDialogFragmentViewModel
import com.inzisoft.ibks.data.web.DocCameraResultData
import com.inzisoft.mobile.data.MIDReaderProfile
import com.inzisoft.ibks.viewmodel.CameraViewModel
import com.inzisoft.ibks.viewmodel.DocCameraViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
open class DocCameraDialogFragment: BaseNormalCameraDialogFragment() {
    private val docCameraViewModel by viewModels<DocCameraViewModel>()

    private val galleryActivity: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { activityResult ->
        when (activityResult.resultCode) {
            Activity.RESULT_OK -> {
                context?.let { context ->
                    activityResult.data?.data?.let { uri ->
                        val bitmap = BitmapFactory.decodeStream(
                            context.contentResolver?.openInputStream(uri)
                        )
                        docCameraViewModel.saveExternalImageBitmap(
                            context = context,
                            bitmap = bitmap
                        )
                    }
                }
            }
            else -> {

            }
        }
    }

    init {
        MIDReaderProfile.getInstance().SET_USER_SCREEN_PORTRAIT = true
    }

    override fun getViewModel(): CameraViewModel {
        return docCameraViewModel
    }

    override fun initComposeLayout() {
        super.initComposeLayout()
        binding.btnGallery.setOnClickListener {
            getGalleryImage()
        }
    }

    override fun showPreviewDocDialogFragment() {
        setFragmentResultListener(FragmentRequest.PreviewDoc) { scriptFuncName, fragmentResult ->
            clearFragmentResultListener(FragmentRequest.PreviewDoc.key)
            when (fragmentResult) {
                is FragmentResult.Cancel -> {
                    docCameraViewModel.restartCamera()
                }
                is FragmentResult.Error -> {}
                is FragmentResult.OK -> {
                    activity?.apply {
                        setResult(
                            Activity.RESULT_OK,
                            intent.putExtra(
                                "docCameraResult",
                                DocCameraResultData(true, fragmentResult.data!!)
                            )
                        )

                        finish()
                    }
                }
            }
        }

       navigate(
            DocCameraDialogFragmentDirections.actionDocCameraDialogFragmentToPreviewDocDialogFragment(
                scriptFunName = docCameraViewModel.scriptFunName!!,
                previewDocType = PreviewDocType.TAKE_DOC,
                docInfoData = docCameraViewModel.docInfoData
            )
        )
    }

    override fun showInfoToast() {
        Toast.makeText(requireContext(), R.string.doc_camera_toast, Toast.LENGTH_SHORT).show()
    }

    override fun cancel() {
        docCameraViewModel.clearAllImage()
        requireActivity().finish()
    }

    override fun getBaseViewModel(): BaseDialogFragmentViewModel? {
        return docCameraViewModel
    }

    private fun getGalleryImage() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = MediaStore.Images.Media.CONTENT_TYPE
        galleryActivity.launch(intent)
    }
}