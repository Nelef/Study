package com.inzisoft.ibks.util

import android.content.Context
import android.util.Pair
import bio.face.FaceDetect

class FaceDetectHelper {
    private var faDetectString: String? = null
    private var faDetectScore: String? = null

    private var faceDetect: FaceDetect? = null
    private var isInitSucceed = false

    fun processFaceDetect(context: Context, faceImage: ByteArray, step: Int, onComplete: (Boolean, String, String) -> Unit) {
        val result = when(step) {
            0 -> initFaceDetect(context)
            1 -> faceQA(faceImage)
            else -> faceDetect(faceImage)
        }

        if(step == 2 && result.first) {
            onComplete(true, faDetectString!!, faDetectScore!!)
        } else if(step < 2 && result.first) {
            processFaceDetect(context, faceImage, step+1, onComplete)
        } else {
            onComplete(false, result.second, "")
        }
    }

    private fun initFaceDetect(context: Context): Pair<Boolean, String> {
        faceDetect = FaceDetect() // create
        val rt1 = faceDetect!!.FA_Start("", context) // init
        if (rt1 == -1) {
            return Pair.create(false, "시간만료")
        }
        if (rt1 == 0) {
            return Pair.create(false, "초기화 실패")
        }
        isInitSucceed = true
        return Pair.create(true, "초기화 성공")
    }

    /**
     * Boolean : faceQA 통과여부
     * String : 에러 내용
     */
    private fun faceQA(faceImage: ByteArray): Pair<Boolean, String> {
        if (!isInitSucceed) {
            return Pair.create(false, "초기화 실패")
        }
        var result: Pair<Boolean, String>? = null
        val arr = IntArray(2)
        val QA_Result = faceDetect!!.FA_QA_BMP(faceImage, faceImage.size, arr)

        //QA 결과 출력
        result = if (QA_Result == 0) {
            Pair.create(true, "인식가능")
        } else if (QA_Result == 1) {
            Pair.create(false, "초점흐림")
        } else if (QA_Result == 2) {
            Pair.create(false, "반 사 광")
        } else if (QA_Result == 3) {
            Pair.create(false, "홀로그램")
        } else if (QA_Result == 4) {
            Pair.create(false, "흑백")
        } else {
            Pair.create(
                false,
                " 얼굴 이미지 에러($QA_Result)"
            )
        }
        return result
    }

    private fun faceDetect(faceImage: ByteArray): Pair<Boolean, String> {
        val faceDetectResult: Pair<Boolean, String>
        faDetectString = ""

        val faceDetectBuf = ByteArray(4668) // base 64
        val detectResult: Int =
            faceDetect!!.FA_Detect_bmp_base64(faceImage, faceImage.size, faceDetectBuf)

        // 얼굴사진 특징점 결과(0 이하는 에러, 1~100은 특징점 점수)
        if (detectResult > 0) {
            for (i in 0..4667) {
                faDetectString += Character.toString(faceDetectBuf[i].toChar())
            }
            faceDetectResult = Pair(true, "얼굴검출 성공")
            faDetectScore = String.format("0%d", detectResult)
        } else {
            faceDetectResult = when (detectResult) {
                -20001 -> Pair(false, "사진품질:초점흐림")
                -20002 -> Pair(false, "사진품질:반사광")
                -20003 -> Pair(false, "사진품질:홀로그램")
                -20004 -> Pair(false, "사진품질:흑백")
                -401 -> Pair(false, "얼굴검출:사진 포멧 에러")
                -201 -> Pair(false, "얼굴검출:메모리 확보 에러")
                else -> Pair(false, "얼굴검출:얼굴 검출 실패")
            }
        }
        return faceDetectResult
    }

    fun releaseFaceDetect() {
        faceDetect?.let {
            it.FA_End()
        }
    }
}