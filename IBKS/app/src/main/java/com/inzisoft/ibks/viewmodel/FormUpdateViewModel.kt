package com.inzisoft.ibks.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.inzisoft.ibks.base.BaseViewModel
import com.inzisoft.ibks.base.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FormUpdateViewModel @Inject constructor() :
    BaseViewModel() {

    var uiState: UiState<Boolean> by mutableStateOf(UiState.None)
    var updateComplete: Boolean by mutableStateOf(false)
    var current: Int by mutableStateOf(0)
    var total: Int by mutableStateOf(0)
    var progress: Float by mutableStateOf(0.0f)

    fun startUpdate() = viewModelScope.launch(Dispatchers.IO) {
        // TODO 폼 업데이트 api 생성 후 작업
        updateComplete = true

//        formUpdateRepository.update().collect { state ->
//            when (state) {
//                is FormUpdateRepository.UpdateFormState.OnError -> uiState =
//                    UiState.Error(state.message)
//                is FormUpdateRepository.UpdateFormState.OnProgress -> {
//                    progress = state.progress
//                }
//                is FormUpdateRepository.UpdateFormState.OnStart -> {
//                    current = state.currentCount
//                    total = state.totalCount
//                }
//                FormUpdateRepository.UpdateFormState.OnUpdateComplete -> updateComplete = true
//                else -> {}
//            }
//        }
    }

}