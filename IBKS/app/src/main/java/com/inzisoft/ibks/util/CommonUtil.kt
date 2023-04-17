package com.inzisoft.ibks.util

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import androidx.annotation.RequiresPermission
import androidx.core.content.getSystemService
import com.inzisoft.ibks.util.log.QLog
import java.util.*

object CommonUtil {

    @SuppressLint("HardwareIds")
    @RequiresPermission(anyOf = [Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_PHONE_NUMBERS])
    fun getDeviceNumber(context: Context) = with(context) {
        try {
            val number = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                getSystemService<TelephonyManager>()?.line1Number ?: throw NullPointerException()
            } else {
                val subscriptionManager =
                    getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as? SubscriptionManager
                        ?: throw NullPointerException("Not found subscriptManager")

                subscriptionManager.getPhoneNumber(SubscriptionManager.DEFAULT_SUBSCRIPTION_ID)
            }
            number.replaceRange(0..2, "0").replace("(\\d{3})(\\d{3,4})(\\d{4})".toRegex(), "$1-$2-$3")
        } catch (e: Exception) {
            QLog.e(e)
            "010-0000-0000"
        }
    }
}