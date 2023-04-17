package com.inzisoft.ibks.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.clearFragmentResultListener
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import com.inzisoft.ibks.FragmentRequest
import com.inzisoft.ibks.FragmentResult
import com.inzisoft.ibks.R
import com.inzisoft.ibks.data.internal.DialogData
import com.inzisoft.ibks.setFragmentResultListener
import com.inzisoft.ibks.util.log.QLog
import com.inzisoft.ibks.view.dialog.DatePickerDialogFragmentDirections
import java.util.*

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

    override fun onResume() {
        super.onResume()
        Firebase.analytics.logEvent(
            FirebaseAnalytics.Event.SCREEN_VIEW,
            bundleOf(
                FirebaseAnalytics.Param.SCREEN_NAME to this.javaClass.simpleName,
                FirebaseAnalytics.Param.SCREEN_CLASS to activity?.javaClass?.simpleName
            )
        )
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
        @StringRes rightBtnText: Int = R.string.confirm,
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
        rightBtnText: String = getString(R.string.confirm),
        onDismissRequest: (state: PopupState) -> Unit
    ) {
        showBasicDialog(
            titleText,
            buildAnnotatedString {
                append(contentText)
            },
            leftBtnText,
            rightBtnText,
            onDismissRequest
        )
    }

    protected fun showBasicDialog(
        titleText: String,
        contentText: AnnotatedString,
        leftBtnText: String? = null,
        rightBtnText: String = getString(R.string.confirm),
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
        @StringRes rightBtnText: Int = R.string.confirm,
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
        rightBtnText: String = getString(R.string.confirm),
        onDismissRequest: (state: PopupState) -> Unit
    ) {
        showAlertDialog(
            buildAnnotatedString {
                append(contentText)
            },
            leftBtnText,
            rightBtnText,
            onDismissRequest
        )
    }

    protected fun showAlertDialog(
        contentText: AnnotatedString,
        leftBtnText: String? = null,
        rightBtnText: String = getString(R.string.confirm),
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

    protected fun navigate(destination: NavDirections) = with(findNavController()) {
        currentDestination?.getAction(destination.actionId)?.let { navigate(destination) }
    }

    protected fun terminateApplication() {
        (activity as MainActivity).quitApplication()
    }


    protected fun showDatePicker(maxDate: Date? = null, onDateChangeListener: (Date) -> Unit) {
        setFragmentResultListener(FragmentRequest.DatePicker) { _, result ->
            clearFragmentResultListener(FragmentRequest.DatePicker.key)

            when (result) {
                is FragmentResult.OK -> {
                    result.data?.run(onDateChangeListener)
                }
                else -> {}
            }
        }
        navigate(
            DatePickerDialogFragmentDirections.actionGlobalDatePickerDialogFragment(
                maxDate = maxDate
            )
        )
    }

}