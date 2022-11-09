package com.inzisoft.ibks.util.skeypad

import android.app.Activity
import android.widget.EditText
import androidx.core.widget.addTextChangedListener
import com.inzisoft.ibks.util.log.QLog
import com.nprotect.keycryptm.Defines
import com.nprotect.keycryptm.IxConfigureInputItem
import com.nprotect.keycryptm.IxKeypadManageHelper

object SecureKeyPad {

    /**
     * 보안 키패드 사용 시 발견된 이슈들
     *
     * 일반적으로 사용하는데는 문제가 없었고, DialogFragment 에서 사용시에 문제가 발생함
     *
     * 1. activity 방식으로 사용시
     *      - DialogFragment 내 필드에서 보안키패드를 사용시 키패드영역 외의 터치이벤트가 FragmentDialog 아닌
     *        DialogFragment를 호출한 Fragment에게 전달되는 이슈
     *
     *        ex) MainActivity > WebFragment > DialogFragment > 보안캐패드 Activity
     *            DialogFragment 를 스크롤하거나 버튼을 누르고 싶은데 WebFragment가 스크롤되고 버튼이 눌림
     *
     * 2. view 방식으로 사용시
     *
     *      1) DialogFragment 필드에서 보안키패드를 사용시 보안키패드가 DialogFragment 뒤로 나옴
     *         > key_pad_view (RelativeLayout)이 Activity 단에 있어서 다이얼로그 아래로 나오는것..
     *
     *      2) AndroidView 생성 후 key_pad_view id를 추가해서 진행해보았으나 DialogFragment에서는 id를 찾지못하는 이슈 발생
     *
     *  2022.10.05 DialogFragment에서는 사용 불가 판단
     *  2022.10.07 activity 화면으로 구현
     *
     */

    private var keypadMngHelper: IxKeypadManageHelper? = null
    private var secureEditText: EditText? = null
    private const val REQUEST_CODE_KEYPAD = 0x2231

    private var density = 1f

    fun show(
        activity: Activity?,
        keyPadType: Int = Defines.KEYPAD_TYPE_QWERTY,
        minLength: Int = 4,
        maxLength: Int = 14,
        onKeypadChangeHeight: (height: Int) -> Unit,
        onTextChanged: (text: String) -> Unit,
        onConfirm: (ByteArray?) -> Unit = {}
    ) {
        activity ?: kotlin.run {
            QLog.e("SecureKeyPad", "not found activity.")
            return
        }

        density = activity.resources.displayMetrics.density

        if (keypadMngHelper == null) {
            keypadMngHelper = IxKeypadManageHelper(activity, REQUEST_CODE_KEYPAD).apply {
                setUiVisibility(Defines.FLAG_INHERIT_UI_VISIBILITY)
                setEnableViewMode(true)
            }
        }

        if (secureEditText == null) {
            secureEditText = EditText(activity.baseContext)
        }

        secureEditText?.addTextChangedListener { text ->
            onTextChanged(text.toString())
        }

        keypadMngHelper?.apply {
            setSecureKeypadEventListener(object : IxKeypadManageHelper.SecureKeypadEventListener {

                override fun onKeypadChangeHeight(height: Int) {
                    onKeypadChangeHeight((height / density).toInt())
                }

                override fun onInputViolationOccured(errorCode: Int) {}

                override fun onKeypadCreate() {}

                override fun onKeypadFinish() {
                    onKeypadChangeHeight(0)
                    onConfirm(getRealTextS(secureEditText))
                }

                override fun onKeypadFinish(resultCode: Int) {}

                override fun onChangeEditText(editText: EditText) {}

                override fun onInputChanged(editText: EditText, count: Int) {}

                override fun onInitializing(onloading: Boolean) {}

            })
        }?.also {
            val inputConfig =
                IxConfigureInputItem(secureEditText, keyPadType, Defines.SHUFFLE_TYPE_GAPKEY)

            inputConfig.minLength = minLength
            inputConfig.maxLength = maxLength
            it.configureInputBoxAndStart(secureEditText, inputConfig)
        }
    }

    fun onConfigurationChanged() {
        keypadMngHelper?.onConfigurationChanged()
    }

    fun hide() {
        keypadMngHelper?.hideSecureKeypad()
    }

    fun finish() {
        keypadMngHelper?.finish()
        keypadMngHelper = null
        secureEditText = null
    }
}