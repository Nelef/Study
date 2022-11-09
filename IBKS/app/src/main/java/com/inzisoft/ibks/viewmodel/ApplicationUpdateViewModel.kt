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
class ApplicationUpdateViewModel @Inject constructor() :
    BaseViewModel() {

    var uiState: UiState<Boolean> by mutableStateOf(UiState.None)
    var updateComplete: Boolean by mutableStateOf(false)
    var current: Int by mutableStateOf(0)
    var total: Int by mutableStateOf(0)
    var progress: Float by mutableStateOf(0.0f)

    fun startUpdate() = viewModelScope.launch(Dispatchers.IO) {
        updateComplete = true // 임시

        // TODO 앱 업데이트 api 생성 후 작업
//        applicationUpdateRepository.update().collect { state ->
//            when (state) {
//                is ApplicationUpdateRepository.UpdateAppState.OnError -> uiState =
//                    UiState.Error(state.message)
//                is ApplicationUpdateRepository.UpdateAppState.OnProgress -> {
//                    progress = state.progress
//                }
//                is ApplicationUpdateRepository.UpdateAppState.OnStart -> {
//                    current = state.currentCount
//                    total = state.totalCount
//                }
//                ApplicationUpdateRepository.UpdateAppState.OnUpdateComplete -> updateComplete = true
//                else -> {}
//            }
//        }
    }

}