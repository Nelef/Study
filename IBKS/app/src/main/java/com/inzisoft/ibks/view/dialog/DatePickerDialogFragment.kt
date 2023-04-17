package com.inzisoft.ibks.view.dialog

import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.DatePicker
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.inzisoft.ibks.FragmentRequest
import com.inzisoft.ibks.FragmentResult
import com.inzisoft.ibks.R
import com.inzisoft.ibks.base.BaseDialogFragment
import com.inzisoft.ibks.base.UiState
import com.inzisoft.ibks.setFragmentResult
import com.inzisoft.ibks.view.compose.BasicBottomDialog
import com.inzisoft.ibks.view.compose.ButtonStyle
import com.inzisoft.ibks.view.compose.ColorButton
import com.inzisoft.ibks.view.compose.GrayColorButton
import com.inzisoft.ibks.view.compose.theme.IBKSTheme
import com.inzisoft.ibks.viewmodel.DatePickerViewModel
import java.util.*

@Suppress("DEPRECATION")
class DatePickerDialogFragment : BaseDialogFragment(), DatePicker.OnDateChangedListener {

    private val viewModel: DatePickerViewModel by viewModels()

    init {
        baseCompose.content = {
            val state = viewModel.uiState.value

            val title = if (state is UiState.Success) {
                with(state.data.title) {
                    ifEmpty { stringResource(id = R.string.select_date) }
                }
            } else {
                stringResource(id = R.string.select_date)
            }

            val (year, month, day) = if (state is UiState.Success) {
                Triple(state.data.year, state.data.monthOfYear, state.data.dayOfMonth)
            } else {
                Triple(0, 0, 0)
            }

            DatePickerScreen(
                title = title,
                visible = state is UiState.Success,
                year = year,
                monthOfYear = month,
                dayOfMonth = day,
                onCancel = { finish() },
                onConfirm = { confirm() }
            )
        }
    }

    @Preview(widthDp = 1280, heightDp = 800)
    @Composable
    fun PreviewDatePickerScreen() {
        IBKSTheme {
            DatePickerScreen(
                title = stringResource(id = R.string.select_date),
                visible = true,
                year = 1988,
                monthOfYear = 1,
                dayOfMonth = 20,
                onCancel = {  }) {

            }
        }
    }

    @Composable
    fun DatePickerScreen(
        title: String,
        visible: Boolean,
        year: Int,
        monthOfYear: Int,
        dayOfMonth: Int,
        onCancel: () -> Unit,
        onConfirm: () -> Unit
    ) {
        BasicBottomDialog(
            title = title,
            modifier = Modifier.width(600.dp),
            onClose = onCancel,
            visible = visible
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(48.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AndroidView(
                    factory = { context ->
                        DatePicker(context).apply {
                            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                                calendarViewShown = false
                                spinnersShown = true
                            }
                            viewModel.maxDate?.let {
                                maxDate = it.time
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    it.init(
                        year,
                        monthOfYear,
                        dayOfMonth,
                        this@DatePickerDialogFragment
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) {

                    GrayColorButton(
                        onClick = onCancel,
                        modifier = Modifier.width(240.dp),
                        text = stringResource(id = R.string.cancel)
                    )

                    ColorButton(
                        onClick = onConfirm,
                        modifier = Modifier.width(240.dp),
                        text = stringResource(id = R.string.confirm),
                        buttonStyle = ButtonStyle.Big
                    )

                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.run {
            val args = DatePickerDialogFragmentArgs.fromBundle(this)
            viewModel.init(args.title, args.year, args.month, args.day)
        }
    }

    override fun onDateChanged(view: DatePicker?, year: Int, monthOfYear: Int, dayOfMonth: Int) {
        viewModel.updateDate(year, monthOfYear, dayOfMonth)
    }

    private fun confirm() {
        setFragmentResult(FragmentRequest.DatePicker, FragmentResult.OK(viewModel.date))
        finish()
    }

    private fun finish() {
        findNavController().navigateUp()
    }

}