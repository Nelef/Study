package com.inzisoft.ibks.viewmodel.presenter

import android.content.Context
import com.inzisoft.ibks.data.internal.CameraConfig
import com.inzisoft.mobile.data.EtcIdCardRecognizeResult
import com.inzisoft.mobile.data.RecognizeResult
import com.inzisoft.mobile.recogdemolib.LibConstants
import com.inzisoft.ibks.viewmodel.AuthData

class ForeignCardPresenter(title: String): IdCardPresenter(title) {
    override fun getCameraConfig(): CameraConfig {
        return CameraConfig(LibConstants.TYPE_IDCARD_ETC, 3000000, title)
    }

    override fun getRecogData(context: Context, recogResult: RecognizeResult): AuthData {
        val result: EtcIdCardRecognizeResult = recogResult.etcIDCardRecognizeResult
        val rrn = result.rrn
        val foreignData = AuthData.ForeignData()
        foreignData.dataMap[AuthData.NAME] = result.name
        foreignData.dataMap[AuthData.FRONT_IDNUM] = rrn.substring(0, 6)
        foreignData.dataMap[AuthData.LAST_IDNUM] = rrn.substring(6)
        foreignData.dataMap[AuthData.ISSUE_DATE] = result.date
        foreignData.dataMap[AuthData.ISSUE_OFFICE] = result.issueOffice

        return foreignData
    }
}