package com.inzisoft.ibks.view.dialog

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.inzisoft.ibks.FragmentRequest
import com.inzisoft.ibks.FragmentResult
import com.inzisoft.ibks.data.internal.LoadOptions
import com.inzisoft.ibks.data.internal.PaperlessData
import com.inzisoft.ibks.setFragmentResult
import com.inzisoft.ibks.viewmodel.BaseElectronicDocViewModel
import com.inzisoft.ibks.viewmodel.ElectronicDocPreviewModeViewModel
import com.inzisoft.ibks.viewmodel.ElectronicTabData
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ElectronicDocPreviewModeFragment : BaseElectronicDocFragment() {
    private val electronicDocPreviewModeViewModel: ElectronicDocPreviewModeViewModel by viewModels()
    override fun getChildViewModel(): BaseElectronicDocViewModel {
        return electronicDocPreviewModeViewModel
    }

    override fun makePaperlessData(electronicTabData: ElectronicTabData): PaperlessData {
        val resultXmlPathList = mutableListOf<String>()
        electronicTabData.saveFormDataList.forEach { saveFormData ->
            resultXmlPathList.add(saveFormData.saveFileDirPath)
        }
        return PaperlessData(
            businessLogic = electronicTabData.businessLogic,
            loadOptions = LoadOptions.Restore(
                resultXmlDirPathList = resultXmlPathList
            )
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.run {
            val args = ElectronicDocPreviewModeFragmentArgs.fromBundle(this)
            electronicDocPreviewModeViewModel.init(args.json)
        }
    }

    override fun cancel(json: String) {
        setFragmentResult(
            FragmentRequest.ElectronicInputDoc, FragmentResult.OK(json)
        )
        finish()
    }

    override fun complete(json: String) {
        setFragmentResult(
            FragmentRequest.ElectronicInputDoc, FragmentResult.OK(json)
        )
        finish()
    }
}

