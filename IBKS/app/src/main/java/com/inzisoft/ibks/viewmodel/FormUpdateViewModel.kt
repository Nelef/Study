package com.inzisoft.ibks.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.inzisoft.ibks.base.BaseViewModel
import com.inzisoft.ibks.base.UiState
import com.inzisoft.ibks.data.remote.UpdateFormDataSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FormUpdateViewModel @Inject constructor(private val updateFormDataSource: UpdateFormDataSource) :
    BaseViewModel() {

    var uiState: UiState<Boolean> by mutableStateOf(UiState.None)
    var updateComplete: Boolean by mutableStateOf(false)
    var current: Int by mutableStateOf(0)
    var total: Int by mutableStateOf(0)
    var progress: Float by mutableStateOf(0.0f)

    fun startUpdate() = viewModelScope.launch(Dispatchers.IO) {
        updateFormDataSource.update().collect { state ->
            when (state) {
                is UpdateFormDataSource.UpdateFormState.OnError -> uiState =
                    UiState.Error(state.message)
                is UpdateFormDataSource.UpdateFormState.OnProgress -> {
                    progress = state.progress
                }
                is UpdateFormDataSource.UpdateFormState.OnStart -> {
                    current = state.currentCount
                    total = state.totalCount
                }
                UpdateFormDataSource.UpdateFormState.OnUpdateComplete -> updateComplete = true
                else -> {}
            }
        }
    }

}