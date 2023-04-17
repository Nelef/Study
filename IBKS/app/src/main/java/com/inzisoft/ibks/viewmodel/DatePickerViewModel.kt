package com.inzisoft.ibks.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inzisoft.ibks.base.UiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

class DatePickerViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = mutableStateOf<UiState<DatePickerItem>>(UiState.None)
    val uiState: State<UiState<DatePickerItem>> = _uiState
    var date = Date()
    val maxDate = savedStateHandle.get<Date?>("maxDate")

    fun init(title: String, year: Int, monthOfYear: Int, dayOfMonth: Int) =
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = UiState.Loading()

            delay(300)

            _uiState.value = UiState.Success(
                if (year == 0 || monthOfYear == 0 || dayOfMonth == 0) {
                    val (y, m, d) = today()
                    DatePickerItem(title, y, m, d)
                } else {
                    updateDate(year, monthOfYear, dayOfMonth)
                    DatePickerItem(title, year, monthOfYear, dayOfMonth)
                }
            )
        }

    private fun today(): Triple<Int, Int, Int> {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR, 1)
        }
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH)
        val day = cal.get(Calendar.DAY_OF_MONTH)

        updateDate(year, month, day)

        return Triple(year, month, day)
    }

    fun updateDate(year: Int, monthOfYear: Int, dayOfMonth: Int) {
        date = Calendar.getInstance().apply { set(year, monthOfYear, dayOfMonth) }.time
    }
}

data class DatePickerItem(
    val title: String,
    val year: Int,
    val monthOfYear: Int,
    val dayOfMonth: Int
)