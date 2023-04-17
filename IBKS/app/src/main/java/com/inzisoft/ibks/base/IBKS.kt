package com.inzisoft.ibks.base

import android.app.Application
import com.inzisoft.ibks.BuildConfig
import com.inzisoft.mobile.camera.module.CameraAPILevelHelper
import com.inzisoft.mobile.data.MIDReaderProfile
import com.nprotect.IxSecureManager
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class IBKS : Application() {

    override fun onCreate() {
        super.onCreate()
        IxSecureManager.initLicense(applicationContext, "F27C546A17E2", "KVAN_01")
        setCameraConfiguration()
    }

    /**
     * 카메라 환경 설정
     */
    private fun setCameraConfiguration() {
        // 인식모듈 기능별 설정값 정의 예시
        val profile = MIDReaderProfile.getInstance()
        // 아래는 디폴트 세팅이다. 필요한 항목에 따라 값을 다르게 세팅하여 사용해야 한다.
        profile.USE_DEEP_LEARNING_AUTO_CROP = false
        profile.DEBUGABLE = BuildConfig.DEBUG
        profile.SAVE_IMAGE = false
        profile.NEED_ENC_IMAGE_DATA = false
        profile.NEED_ENC_TEXT_DATA = BuildConfig.ENCRYPT_KEYPAD
        profile.SET_USE_MANUAL_CROP = true
        profile.NO_RRN = false
        profile.SAVE_IMAGE_MODE = false
        profile.CHECK_VALIDATION = false
        // preview 인식시 frame 갯수에 제한을 걸어 휴대단말의 부하를 줄임(발열개선)
        profile.PASS_FRAMES_PER_USE = 5
        // preview 인식 시 신분증 영역에 신분증이 위치여부를 알려주고, 빛반사 유무를 체크하게 해줌
        profile.FIND_EDGE_ON_PREVIEW = true
        // 빛반사 유무 민감도 설정 0-100 (100에 가까울 수록 민감함)
        profile.OCR_SUITABLE_SENSITIVITY = 0
    }
}