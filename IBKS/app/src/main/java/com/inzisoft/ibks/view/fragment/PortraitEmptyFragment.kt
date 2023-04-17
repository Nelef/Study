package com.inzisoft.ibks.view.fragment

import android.app.Activity.RESULT_OK
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.clearFragmentResultListener
import androidx.navigation.fragment.findNavController
import com.inzisoft.ibks.*
import com.inzisoft.ibks.Constants.KEY_DOC_CAMERA_DATA
import com.inzisoft.ibks.Constants.KEY_IMAGE_DIR_PATH
import com.inzisoft.ibks.Constants.KEY_PREVIEW_DOC_TYPE
import com.inzisoft.ibks.Constants.KEY_SCRIPT_FUN_NAME
import com.inzisoft.ibks.base.BaseFragment
import com.inzisoft.ibks.data.internal.DocImageData
import com.inzisoft.ibks.data.internal.DocInfoData
import com.inzisoft.ibks.data.web.DocCameraData
import java.io.File

class PortraitEmptyFragment: BaseFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        activity?.apply {

            val previewDocType = intent.getSerializableExtra(KEY_PREVIEW_DOC_TYPE)
            val scriptFunName = intent.getStringExtra(KEY_SCRIPT_FUN_NAME)

            if(previewDocType == PreviewDocType.PREVIEW_DOC) {
                val docCameraData = intent.getSerializableExtra(KEY_DOC_CAMERA_DATA) as DocCameraData
                val imageDirPath = intent.getStringExtra(KEY_IMAGE_DIR_PATH)
                val docImageDataList = getDocImageDataList(imageDirPath!!, docCameraData.docCode)
                val docInfoData = DocInfoData(
                    docCode = docCameraData.docCode,
                    docName = docCameraData.docName,
                    maskingYn = false,
                    docImageDataList = docImageDataList,
                    docCtgCd = docCameraData.docCtgCd?: ""
                )

                showPreviewDoc(scriptFunName!!, docInfoData)
            } else {
                val docCameraData = intent.getSerializableExtra(Constants.KEY_DOC_CAMERA_DATA) as DocCameraData
                showDocCamera(scriptFunName!!, docCameraData)
            }
        }
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    private fun getDocImageDataList(imageDirPath: String, docCode: String): MutableList<DocImageData> {
        var docImageDataList = mutableListOf<DocImageData>()
        File(imageDirPath).apply {
            val childList = list()
            childList?.apply {
                for (path: String in childList) {
                    if(path.startsWith(docCode)) {
                        docImageDataList.add(DocImageData(cacheOriginImagePath = "", realImagePath = "$imageDirPath/$path"))
                    }
                }
            }
        }

        return docImageDataList
    }

    private fun showDocCamera(scriptFunName: String, docCameraData: DocCameraData) {
        setFragmentResultListener(FragmentRequest.DocCamera) { scriptFunName, result ->
            clearFragmentResultListener(FragmentRequest.DocCamera.key)
            when (result) {
                is FragmentResult.Cancel -> {}
                is FragmentResult.Error -> {}
                is FragmentResult.OK -> {
                    // TODO: 종료 처리
                }
            }
        }

        navigate(
            PortraitEmptyFragmentDirections.actionPortraitEmptyFragmentToDocCameraDialogFragment(
                scriptFunName = scriptFunName,
                docCameraData = docCameraData
            )
        )
    }

    private fun showPreviewDoc(scriptFunName: String, docInfoData: DocInfoData) {
        setFragmentResultListener(FragmentRequest.PreviewDoc) { scriptFunName, result ->
            clearFragmentResultListener(FragmentRequest.PreviewDoc.key)
            activity?.apply {
                setResult(RESULT_OK)
                finish()
            }
        }

       navigate(
            PortraitEmptyFragmentDirections.actionPortraitEmptyFragmentToPreviewDocDialogFragment(
                scriptFunName = scriptFunName,
                previewDocType = PreviewDocType.PREVIEW_DOC,
                docInfoData = docInfoData
            )
        )
    }
}