package com.inzisoft.ibks

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.google.gson.Gson
import com.inzisoft.ibks.Constants.KEY_SCRIPT_FUN_NAME
import com.inzisoft.ibks.base.BaseDialogFragment

inline fun <reified T : Any> Fragment.setFragmentResultListener(
    request: FragmentRequest<T>,
    crossinline listener: ((scriptFunName: String, result: FragmentResult<T>) -> Unit)
) {
    parentFragmentManager.setFragmentResultListener(
        request.key, this
    ) fragmentResultListener@{ requestKey, bundle ->

        if (request.key != requestKey) return@fragmentResultListener

        listener(bundle.getString(KEY_SCRIPT_FUN_NAME, ""), bundleToResult(bundle, T::class.java))
    }
}

fun <T> bundleToResult(bundle: Bundle, classOfT: Class<T>): FragmentResult<T> {
    return if (bundle.containsKey(FragmentResult.CANCEL)) {
        FragmentResult.Cancel()
    } else if (bundle.containsKey(FragmentResult.ERROR)) {
        FragmentResult.Error(bundle.getString(FragmentResult.ERROR, "unknown"))
    } else {
        val data = bundle.getValue(classOfT)
        FragmentResult.OK(data)
    }
}

fun <T> Bundle.getValue(classOfT: Class<T>): T? {
    val data = getString(FragmentResult.OK)
    return if (classOfT.isAssignableFrom(String::class.java)) classOfT.cast(data) else Gson().fromJson(
        data,
        classOfT
    )
}

fun Bundle.putValue(key: String, data: Any?) {
    if (data is String) {
        putString(key, data)
    } else {
        putString(key, Gson().toJson(data))
    }
}


fun <T : Any> Fragment.setFragmentResult(request: FragmentRequest<T>, result: FragmentResult<T>) {
    val bundle = Bundle()

    if (this is BaseDialogFragment) {
        getBaseViewModel()?.scriptFunName?.let {
            bundle.putString(KEY_SCRIPT_FUN_NAME, it)
        }
    }

    when (result) {
        is FragmentResult.Cancel -> bundle.putByte(FragmentResult.CANCEL, 1)
        is FragmentResult.Error -> bundle.putString(FragmentResult.ERROR, result.message)
        is FragmentResult.OK<*> -> {
            bundle.putValue(FragmentResult.OK, result.data)
        }
    }

    parentFragmentManager.setFragmentResult(request.key, bundle)
}

fun Context.findActivity(): Activity {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }

    throw IllegalArgumentException("no activity")
}