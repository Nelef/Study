package com.inzisoft.ibks.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.inzisoft.ibks.PathManager
import com.inzisoft.ibks.R
import com.inzisoft.ibks.base.BaseDialogFragmentViewModel
import com.inzisoft.ibks.base.UiState
import com.inzisoft.ibks.data.internal.AlertData
import com.inzisoft.ibks.data.internal.InstructionData
import com.inzisoft.ibks.data.remote.UpdateFormDataSource
import com.inzisoft.ibks.data.web.InstructionInfo
import com.inzisoft.ibks.util.PdfRenderer
import com.inzisoft.ibks.util.log.QLog
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InstructionViewModel @Inject constructor(
    private val pathManager: PathManager,
    private val pdfRenderer: PdfRenderer?,
    private val updateFormDataSource: UpdateFormDataSource,
) : BaseDialogFragmentViewModel() {

    private val _state = mutableStateOf<UiState<InstructionData>>(UiState.None)
    val state: State<UiState<InstructionData>> = _state

    private lateinit var instructionInfo: InstructionInfo
    private var instructions: List<String> = listOf()

    private val _popupState = mutableStateOf<UiState<AlertData>>(UiState.None)
    val popupState: State<UiState<AlertData>> = _popupState

    var confirmBtnString: Int? by mutableStateOf(null)

    fun alert(data: AlertData) {
        _popupState.value = UiState.Success(data)
    }

    fun dismissAlert() {
        _popupState.value = UiState.None
    }

    fun load(json: String) = viewModelScope.launch(Dispatchers.IO) {
        _state.value = UiState.Loading()

        runCatching {
            val data = Gson().fromJson(json, InstructionInfo::class.java).apply {
                if (productCode.isEmpty()) throw IllegalArgumentException("product code is empty.")
            }

            data
        }.onSuccess {
            instructionInfo = it

            // 펀드일때,
            if(it.formCode.isNotEmpty()) {
                confirmBtnString = R.string.product_information_check
                instructions = it.formCode
                loadInstruction(instructions[0])
            } else {
                confirmBtnString = R.string.disclosure_information_check
                updateInstructionForm(it.productCode)
            }

        }.onFailure {
            _state.value = UiState.Error(it.message ?: "unknown")
        }
    }

    private fun updateInstructionForm(productCode: String) = viewModelScope.launch(Dispatchers.IO) {
        updateFormDataSource.updateProductCode(productCode).collect { state ->
            when (state) {

                is UpdateFormDataSource.UpdateFormState.Instruction -> {
                    instructions = state.list
                    QLog.d("instructions : $instructions")
                }

                UpdateFormDataSource.UpdateFormState.Loading -> {
                    _state.value = UiState.Loading(strResId = R.string.update_core_instruction)
                }

                is UpdateFormDataSource.UpdateFormState.OnError -> {
                    val message = state.message
                    QLog.e(message)
                    _state.value = UiState.Error(message)
                }

                UpdateFormDataSource.UpdateFormState.OnUpdateComplete -> {
                    if (instructions.isEmpty()) {
                        val message = "not found instruction (code : $productCode)"
                        _state.value = UiState.Error(message)
                    } else {
                        loadInstruction(instructions[0])
                    }
                }

                else -> {}
            }
        }
    }

    private fun loadInstruction(formCode: String) = viewModelScope.launch(Dispatchers.Default) {
        pdfRenderer?.let {
            if (!pdfRenderer.isRendered(formCode)) {
                pdfRenderer.render(formCode)
            }
        }

        _state.value = UiState.Success(
            InstructionData(
                title = instructionInfo.title,
                imagePaths = pathManager.getRenderImages(formCode)
            )
        )
    }
}