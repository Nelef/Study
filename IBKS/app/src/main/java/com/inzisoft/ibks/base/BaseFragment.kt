package com.inzisoft.ibks.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import com.inzisoft.ibks.data.internal.DialogData
import com.inzisoft.ibks.util.log.QLog

open class BaseFragment : Fragment() {

    protected val baseCompose = BaseCompose()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        QLog.i("[${this.javaClass.simpleName}] 시작")
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                baseCompose.baseScreen.invoke()
            }
        }
    }

    override fun onDestroyView() {
        QLog.i("[${this.javaClass.simpleName}] 종료")
        super.onDestroyView()
    }

    protected fun availableNetwork(): Boolean {
        val mainActivity = activity as? MainActivity ?: return false
        return mainActivity.networkAvailable()
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

    protected fun showSurface(test: @Composable (() -> Unit)?) {
        val mainActivity = activity as? MainActivity ?: return
        mainActivity.showSurface(test)
    }

    protected fun dismissAlertDialog() {
        val mainActivity = activity as? MainActivity ?: return
        mainActivity.dismissAlertDialog()
    }


    protected fun terminateApplication() {
        (activity as MainActivity).quitApplication()
    }

}