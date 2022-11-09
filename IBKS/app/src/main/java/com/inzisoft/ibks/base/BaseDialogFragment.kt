package com.inzisoft.ibks.base

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.inzisoft.ibks.Constants.KEY_SCRIPT_FUN_NAME
import com.inzisoft.ibks.R
import com.inzisoft.ibks.data.internal.DialogData
import com.inzisoft.ibks.util.log.QLog
import com.inzisoft.ibks.view.compose.theme.IBKSTheme
import java.util.*

open class BaseDialogFragment : AppCompatDialogFragment() {

    protected val baseCompose = BaseCompose()

    open fun getBaseViewModel(): BaseDialogFragmentViewModel? {
        return null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, R.style.FullscreenDialogTheme)
        isCancelable = false
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        QLog.i("[${this.javaClass.simpleName}] 시작")
        return ComposeView(requireContext()).apply {
            setContent {
                if (baseCompose.topBar == null) {
                    IBKSTheme {
                        Box(
                            Modifier
                                .fillMaxSize()
                                .background(androidx.compose.ui.graphics.Color.Transparent)
                        ) {
                            baseCompose.content(PaddingValues(0.dp))
                            baseCompose.surface?.invoke()
                        }
                    }
                } else {
                    baseCompose.baseScreen.invoke()
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.let {
            getBaseViewModel()?.apply {
                scriptFunName = it.getString(KEY_SCRIPT_FUN_NAME, "")
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.apply {
            with(WindowCompat.getInsetsController(this, decorView)) {
                systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                hide(WindowInsetsCompat.Type.systemBars())
            }
        }
        return dialog
    }

    override fun onDestroyView() {
        QLog.i("[${this.javaClass.simpleName}] 종료")
        super.onDestroyView()
    }

    protected fun hideKeyboard() {
        val mainActivity = activity as? MainActivity ?: return
        mainActivity.hideKeyboard()
    }

    protected fun showBasicDialog(
        @StringRes titleText: Int,
        @StringRes contentText: Int,
        @StringRes leftBtnText: Int? = null,
        @StringRes rightBtnText: Int,
        onDismissRequest: (state: PopupState) -> Unit
    ) {
        showBasicDialog(
            getString(titleText),
            getString(contentText),
            leftBtnText?.let { getString(it) },
            getString(rightBtnText),
            onDismissRequest
        )
    }

    protected fun showBasicDialog(
        titleText: String,
        contentText: String,
        leftBtnText: String? = null,
        rightBtnText: String,
        onDismissRequest: (state: PopupState) -> Unit
    ) {
        val mainActivity = activity as? MainActivity ?: return
        mainActivity.showBasicDialog(
            DialogData(
                titleText,
                contentText,
                leftBtnText,
                rightBtnText,
                onDismissRequest
            )
        )
    }

    protected fun dismissBasicDialog() {
        val mainActivity = activity as? MainActivity ?: return
        mainActivity.dismissBasicDialog()
    }

    protected fun showAlertDialog(
        @StringRes contentText: Int,
        @StringRes leftBtnText: Int? = null,
        @StringRes rightBtnText: Int,
        onDismissRequest: (state: PopupState) -> Unit
    ) {
        showAlertDialog(
            getString(contentText),
            leftBtnText?.let { getString(it) },
            getString(rightBtnText),
            onDismissRequest
        )
    }

    protected fun showAlertDialog(
        contentText: String,
        leftBtnText: String? = null,
        rightBtnText: String,
        onDismissRequest: (state: PopupState) -> Unit
    ) {
        val mainActivity = activity as? MainActivity ?: return
        mainActivity.showAlertDialog(
            DialogData(
                "",
                contentText,
                leftBtnText,
                rightBtnText,
                onDismissRequest
            )
        )
    }

    protected fun dismissAlertDialog() {
        val mainActivity = activity as? MainActivity ?: return
        mainActivity.dismissAlertDialog()
    }

    protected fun showDatePicker(onDateChangeListener: (Date) -> Unit) {
        val cal = Calendar.getInstance()
        val datePicker = DatePickerDialog(
            requireContext(),
            { view, year, month, dayOfMonth ->
                onDateChangeListener(GregorianCalendar(year, month, dayOfMonth).time)
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DATE)
        )
        datePicker.setCancelable(false)
        datePicker.window?.apply {
            with(WindowCompat.getInsetsController(this, decorView)) {
                systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                hide(WindowInsetsCompat.Type.systemBars())
            }
        }
        datePicker.show()
    }
}