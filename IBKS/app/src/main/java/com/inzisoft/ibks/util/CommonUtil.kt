package com.inzisoft.ibks.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.icu.text.SimpleDateFormat
import android.util.Base64
import java.util.Date

object CommonUtil {

    fun convertBase64StringToBitmap(imageStr: String): Bitmap {
        val imageBytes = Base64.decode(imageStr, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
    }

    fun convertDate(
        srcDate: String,
        srcFormat: String = "yyyyMMdd",
        dstFormat: String = "yyyy-MM-dd"
    ): String {
        val dateFormat = SimpleDateFormat(srcFormat)
        val birth: Date = dateFormat.parse(srcDate)
        return SimpleDateFormat(dstFormat).format(birth)
    }
}