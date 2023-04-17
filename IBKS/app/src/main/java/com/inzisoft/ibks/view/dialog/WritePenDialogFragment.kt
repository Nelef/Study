package com.inzisoft.ibks.view.dialog

import android.os.Bundle
import android.view.View
import androidx.fragment.app.clearFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.inzisoft.ibks.*
import com.inzisoft.ibks.base.BaseDialogFragment
import com.inzisoft.ibks.base.BaseDialogFragmentViewModel
import com.inzisoft.ibks.base.Right
import com.inzisoft.ibks.base.UiState
import com.inzisoft.ibks.data.internal.AlertData
import com.inzisoft.ibks.data.internal.PenDialogType
import com.inzisoft.ibks.data.web.AuthCameraData
import com.inzisoft.ibks.view.compose.SealDialog
import com.inzisoft.ibks.view.compose.WritePenDialog
import com.inzisoft.ibks.view.compose.WritePenSealDialog
import com.inzisoft.ibks.view.compose.WritePenSealOnlyDialog
import com.inzisoft.ibks.viewmodel.WritePenViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WritePenDialogFragment : BaseDialogFragment() {

    val viewModel: WritePenViewModel by viewModels()

    init {
        baseCompose.content = {
            val data = when (val state = viewModel.uiState) {
                is UiState.Success -> state.data
                else -> null
            }

            when (viewModel.penDialogTypeState) {
                PenDialogType.PEN -> WritePenDialog(
                    visible = data != null,
                    title = data?.title ?: "",
                    onCancel = { cancel() },
                    onConfirm = { penImage1, pointList1, penImage2, pointList2 ->
                        data?.let {
                            viewModel.confirm(
                                data.penData,
                                penImage1,
                                pointList1,
                                data.signPenData,
                                penImage2,
                                pointList2,
                                data.sealData,
                                null,
                                ::confirm
                            )
                        }
                    },
                    subtitle1 = data?.penData?.subtitle ?: "",
                    placeholder1 = data?.penData?.placeholder ?: "",
                    imagePath1 = data?.penData?.imagePath ?: "",
                    pointList1 = data?.penData?.pointList,
                    showPen2 = data?.signPenData != null,
                    subtitle2 = data?.signPenData?.subtitle,
                    placeholder2 = data?.signPenData?.placeholder,
                )

                PenDialogType.PENSEAL -> {
                    WritePenSealDialog(
                        visible = data != null,
                        title = data?.title ?: "",
                        onCancel = { cancel() },
                        onConfirm = { penImage1, pointList1, penImage2, pointList2, sealImage ->
                            data?.let {
                                viewModel.confirm(
                                    data.penData,
                                    penImage1,
                                    pointList1,
                                    data.signPenData,
                                    penImage2,
                                    pointList2,
                                    data.sealData,
                                    sealImage,
                                    ::confirm
                                )
                            }
                        },
                        imagePath1 = data?.penData?.imagePath ?: "",
                        pointList1 = data?.penData?.pointList,
                        imagePath2 = data?.signPenData?.imagePath,
                        pointList2 = data?.signPenData?.pointList,
                        sealBitmap = viewModel.sealImageData,
                        onRetake = { showSealCameraDialogFragment() },
                        onChoice = { isSealChoice, isEmptyOther ->
                            onChoicePenSealBtn(isSealChoice, isEmptyOther)
                        },
                        sealMode = viewModel.sealMode
                    )
                }

                PenDialogType.SEAL -> {
                    SealDialog(
                        visible = true,
                        title = data?.title ?: "",
                        subtitle = data?.sealData?.subtitle ?: "",
                        sealBitmap = viewModel.sealImageData,
                        onCancel = { cancel() },
                        onRetake = { showSealCameraDialogFragment() },
                        onConfirm = { sealImage ->
                            data?.let {
                                viewModel.confirm(
                                    null,
                                    null,
                                    null,
                                    null,
                                    null,
                                    null,
                                    data.sealData,
                                    sealImage,
                                    ::confirm
                                )
                            }
                        }
                    )
                }

                PenDialogType.PENSEALONLY -> {
                    WritePenSealOnlyDialog(
                        visible = data != null,
                        title = data?.title ?: "",
                        onCancel = { cancel() },
                        onConfirm = { penImage, pointList, sealImage ->
                            data?.let {
                                viewModel.confirm(
                                    data.penData,
                                    penImage,
                                    pointList,
                                    null,
                                    null,
                                    null,
                                    data.sealData,
                                    sealImage,
                                    ::confirm
                                )
                            }
                        },
                        imagePath = data?.penData?.imagePath ?: "",
                        pointList = data?.penData?.pointList,
                        sealBitmap = viewModel.sealImageData,
                        onRetake = { showSealCameraDialogFragment() },
                    )
                }
                else -> {}
            }
        }
        baseCompose.surface = {
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
            val arguments = WritePenDialogFragmentArgs.fromBundle(it)
            viewModel.load(arguments.data)
        }
    }

    private fun confirm(result: String) {
        setFragmentResult(FragmentRequest.WritePen, FragmentResult.OK(result))
        finish()
    }

    private fun cancel() {
        setFragmentResult(FragmentRequest.WritePen, FragmentResult.Cancel())
        finish()
    }

    private fun finish() {
        findNavController().navigateUp()
    }

    private fun showSealCameraDialogFragment() {
        setFragmentResultListener(FragmentRequest.SealCamera) { scriptFunName, result ->
            clearFragmentResultListener(FragmentRequest.SealCamera.key)
            when (result) {
                is FragmentResult.Cancel -> {}
                is FragmentResult.Error -> {}
                is FragmentResult.OK -> {

                    val sealImage = result.data
                    viewModel.sealImageData = sealImage
                    //viewModel.sendNormalAuthDataToWeb(scriptFunName, state.authCameraData)
                }
            }
        }
        val authCameraData = AuthCameraData("seal", "ocr", "N", "N")

        navigate(
            WritePenDialogFragmentDirections.actionGlobalSealCameraDialogFragment(
                authCameraData
            )
        )
    }

    private fun onChoicePenSealBtn(isSealChoice: Boolean, isEmptyOther: Boolean) {
        if (isSealChoice) {
            // 인감 선택 시
            if (isEmptyOther) {
                // 서명이 비어 있다면,
                viewModel.sealMode = true
                showSealCameraDialogFragment()
            } else {
                // 서명이 있다면.
                viewModel.alert(
                    AlertData(
                        contentText = getString(R.string.choice_popup_seal_write),
                        leftBtnText = getString(R.string.cancel),
                        rightBtnText = getString(R.string.confirm),
                        onDismissRequest = { state ->
                            if (state == Right) {
                                viewModel.sealMode = true
                                showSealCameraDialogFragment()
                                viewModel.dismissAlert()
                            } else {
                                viewModel.dismissAlert()
                            }
                        }
                    )
                )
            }
        } else {
            // 서명 선택 시
            if (isEmptyOther) {
                // 인감이 비어 있다면,
                viewModel.sealMode = false
            } else {
                // 인감이 있다면,
                viewModel.alert(
                    AlertData(
                        contentText = getString(R.string.choice_popup_pen_write),
                        leftBtnText = getString(R.string.cancel),
                        rightBtnText = getString(R.string.confirm),
                        onDismissRequest = { state ->
                            if (state == Right) {
                                viewModel.sealMode = false
                                viewModel.sealImageData = null
                                viewModel.dismissAlert()
                            } else {
                                viewModel.dismissAlert()
                            }
                        }
                    )
                )
            }
        }
    }
}