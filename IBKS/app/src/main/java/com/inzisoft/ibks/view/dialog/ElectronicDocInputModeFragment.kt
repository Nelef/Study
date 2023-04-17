package com.inzisoft.ibks.view.dialog

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.inzisoft.ibks.FragmentRequest
import com.inzisoft.ibks.FragmentResult
import com.inzisoft.ibks.data.internal.LoadForm
import com.inzisoft.ibks.data.internal.LoadOptions
import com.inzisoft.ibks.data.internal.PaperlessData
import com.inzisoft.ibks.setFragmentResult
import com.inzisoft.ibks.viewmodel.BaseElectronicDocViewModel
import com.inzisoft.ibks.viewmodel.ElectronicDocInputModeViewModel
import com.inzisoft.ibks.viewmodel.ElectronicTabData
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ElectronicDocInputModeFragment : BaseElectronicDocFragment() {
    private val electronicDocInputModeViewModel: ElectronicDocInputModeViewModel by viewModels()
    override fun getChildViewModel(): BaseElectronicDocViewModel {
        return electronicDocInputModeViewModel
    }

    override fun makePaperlessData(electronicTabData: ElectronicTabData): PaperlessData {
        val electronicDocInfo =
            electronicDocInputModeViewModel.getElectronicDocInfo(electronicTabData.title)
        return PaperlessData(
            businessLogic = electronicDocInfo.businessLogic,
            loadOptions = LoadOptions.Write(
                formList = electronicDocInfo.appendForm?.map { LoadForm(it) },
                data = electronicDocInfo.data,
                image = electronicDocInfo.penSealImg
            )
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.run {
            val args = ElectronicDocInputModeFragmentArgs.fromBundle(this)
            electronicDocInputModeViewModel.init(args.json)
            electronicDocInputModeViewModel.isFinalMode = args.isSend
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

