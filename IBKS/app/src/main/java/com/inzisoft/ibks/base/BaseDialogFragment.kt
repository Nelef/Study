package com.inzisoft.ibks.base

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.os.bundleOf
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.clearFragmentResultListener
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import com.inzisoft.ibks.*
import com.inzisoft.ibks.Constants.KEY_SCRIPT_FUN_NAME
import com.inzisoft.ibks.util.log.QLog
import com.inzisoft.ibks.view.compose.AlertDialog
import com.inzisoft.ibks.view.compose.BasicDialog
import com.inzisoft.ibks.view.compose.theme.IBKSTheme
import com.inzisoft.ibks.view.dialog.DatePickerDialogFragmentDirections
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
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                if (baseCompose.topBar == null) {
                    IBKSTheme {
                        Box(
                            Modifier
                                .fillMaxSize()
                                .background(Color.Transparent)
                        ) {
                            baseCompose.content(PaddingValues(0.dp))
                            baseCompose.surface?.invoke()

                            if (stringResource(id = R.string.env_name).isNotEmpty()) {
                                Text(
                                    text = stringResource(id = R.string.env_name) + " / v" + BuildConfig.VERSION_NAME,
                                    modifier = Modifier
                                        .align(Alignment.BottomStart)
                                        .padding(10.dp),
                                    color = colorResource(id = R.color.env_name),
                                    fontWeight = FontWeight.Bold
                                )
                            }
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

    protected fun navigate(destination: NavDirections) = with(findNavController()) {
        currentDestination?.getAction(destination.actionId)?.let { navigate(destination) }
    }

    protected fun hideKeyboard() {
        val mainActivity = activity as? MainActivity ?: return
        mainActivity.hideKeyboard()
    }

    @Composable
    protected fun ShowBasicDialog(
        @StringRes titleText: Int,
        @StringRes contentText: Int,
        @StringRes leftBtnText: Int? = null,
        @StringRes rightBtnText: Int,
        onDismissRequest: (state: PopupState) -> Unit
    ) {
        ShowBasicDialog(
            getString(titleText),
            getString(contentText),
            leftBtnText?.let { getString(it) },
            getString(rightBtnText),
            onDismissRequest
        )
    }

    @Composable
    protected fun ShowBasicDialog(
        titleText: String,
        contentText: String,
        leftBtnText: String? = null,
        rightBtnText: String,
        onDismissRequest: (state: PopupState) -> Unit
    ) {
        ShowBasicDialog(
            titleText,
            buildAnnotatedString {
                append(contentText)
            },
            leftBtnText,
            rightBtnText,
            onDismissRequest
        )
    }

    @Composable
    protected fun ShowBasicDialog(
        titleText: String,
        contentText: AnnotatedString,
        leftBtnText: String? = null,
        rightBtnText: String,
        onDismissRequest: (state: PopupState) -> Unit
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0x33000000))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { },
            contentAlignment = Alignment.Center
        ) {
            BasicDialog(
                titleText = titleText,
                contentText = contentText,
                leftBtnText = leftBtnText,
                rightBtnText = rightBtnText,
                onClosed = { onDismissRequest(Cancel) },
                onLeftBtnClick = { onDismissRequest(Left) },
                onRightBtnClick = { onDismissRequest(Right) }
            )
        }
    }

    @Composable
    protected fun ShowAlertDialog(
        @StringRes contentText: Int,
        @StringRes leftBtnText: Int? = null,
        @StringRes rightBtnText: Int,
        onDismissRequest: (state: PopupState) -> Unit
    ) {
        ShowAlertDialog(
            getString(contentText),
            leftBtnText?.let { getString(it) },
            getString(rightBtnText),
            onDismissRequest
        )
    }

    @Composable
    protected fun ShowAlertDialog(
        contentText: String,
        leftBtnText: String? = null,
        rightBtnText: String = getString(R.string.confirm),
        onDismissRequest: (state: PopupState) -> Unit
    ) {
        ShowAlertDialog(
            buildAnnotatedString {
                append(contentText)
            },
            leftBtnText,
            rightBtnText,
            onDismissRequest
        )
    }

    @Composable
    protected fun ShowAlertDialog(
        contentText: AnnotatedString,
        leftBtnText: String? = null,
        rightBtnText: String = getString(R.string.confirm),
        onDismissRequest: (state: PopupState) -> Unit
    ) {

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0x33000000))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { },
            contentAlignment = Alignment.Center
        ) {
            AlertDialog(
                contentText = contentText,
                leftBtnText = leftBtnText,
                rightBtnText = rightBtnText,
                onLeftBtnClick = { onDismissRequest(Left) },
                onRightBtnClick = { onDismissRequest(Right) }
            )
        }
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
        findNavController().navigate(
            DatePickerDialogFragmentDirections.actionGlobalDatePickerDialogFragment(
                maxDate = maxDate
            )
        )
    }

}