package com.inzisoft.ibks.view.dialog

import android.os.Bundle
import android.view.View
import androidx.compose.ui.res.stringResource
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import com.inzisoft.ibks.FragmentRequest
import com.inzisoft.ibks.FragmentResult
import com.inzisoft.ibks.R
import com.inzisoft.ibks.base.*
import com.inzisoft.ibks.data.internal.AlertData
import com.inzisoft.ibks.setFragmentResult
import com.inzisoft.ibks.util.log.QLog
import com.inzisoft.ibks.view.compose.FloatingRecordButton
import com.inzisoft.ibks.view.compose.InstructionViewer
import com.inzisoft.ibks.view.compose.Loading
import com.inzisoft.ibks.viewmodel.InstructionViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class InstructionDialogFragment : BaseDialogFragment() {

    val viewModel: InstructionViewModel by viewModels()

    init {
        baseCompose.content = {
            val data = when (val state = viewModel.state.value) {
                is UiState.Success -> state.data
                else -> null
            }

            val title =
                if (data?.title?.isNotBlank() == true)
                    data.title
                else
                    stringResource(id = R.string.title_instruction)
            val imageList = data?.imagePaths ?: listOf()

            InstructionViewer(
                title = title,
                visible = viewModel.state.value is UiState.Success,
                imagePaths = imageList,
                confirmBtnText = viewModel.confirmBtnString,
                onClose = { cancel() },
                onConfirm = { confirm() }
            )

            FloatingRecordButton(
                showFloatingRecordButton = (activity as MainActivity).viewModel.showFloatingRecordButton,
                recordState = (activity as MainActivity).viewModel.recordState.value,
                recordTime = (activity as MainActivity).viewModel.recordTime.value,
                recordFileList = (activity as MainActivity).viewModel.recordList,
                onRecord = { (activity as MainActivity).recordStart() },
                onResume = { (activity as MainActivity).recordResume() },
                onPause = { (activity as MainActivity).recordPause() },
                onStop = {
                    viewModel.alert(
                        AlertData(
                            contentText = getString(R.string.alert_finish_record),
                            leftBtnText = getString(R.string.cancel),
                            rightBtnText = getString(R.string.finish),
                            onDismissRequest = { state ->
                                if (state == Right) {
                                    (activity as MainActivity).recordStop()
                                }
                                viewModel.dismissAlert()
                            }
                        )
                    )
                },
                onRecordList = { (activity as MainActivity).viewModel.updateRecordList() }
            )
        }

        baseCompose.surface = {
            when (val state = viewModel.state.value) {
                is UiState.Loading -> Loading()
                is UiState.Error -> {
                    QLog.e(state.message)
                    Firebase.crashlytics.recordException(Exception(state.message))
                    ShowAlertDialog(
                        contentText = stringResource(R.string.error_instruction),
                        rightBtnText = stringResource(R.string.confirm),
                        onDismissRequest = {
                            cancel()
                        }
                    )
                }
                else -> {}
            }

            when (val popupState = viewModel.popupState.value) {
                is UiState.Success -> {
                    val popupData = popupState.data

                    ShowAlertDialog(
                        contentText = popupData.contentText,
                        leftBtnText = popupData.leftBtnText,
                        rightBtnText = popupData.rightBtnText,
                        onDismissRequest = popupData.onDismissRequest
                    )
                }
                else -> {}
            }
        }
    }

    override fun getBaseViewModel(): BaseDialogFragmentViewModel {
        return viewModel
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.let {
            with(InstructionDialogFragmentArgs.fromBundle(it)) {
                viewModel.load(json)
            }
        }
    }

    private fun confirm() {
        setFragmentResult(FragmentRequest.Instruction, FragmentResult.OK(Unit))
        finish()
    }

    private fun cancel() {
        setFragmentResult(FragmentRequest.Instruction, FragmentResult.Cancel())
        finish()
    }

    private fun finish() {
        findNavController().navigateUp()
    }
}