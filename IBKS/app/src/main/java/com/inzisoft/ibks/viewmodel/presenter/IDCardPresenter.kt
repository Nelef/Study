package com.inzisoft.ibks.viewmodel.presenter

import android.content.Context
import android.util.Base64
import com.inzisoft.ibks.BuildConfig
import com.inzisoft.ibks.data.internal.CameraConfig
import com.inzisoft.ibks.util.CryptoUtil
import com.inzisoft.izmobilereader.IZMobileReaderCommon
import com.inzisoft.mobile.data.IDCardRecognizeResult
import com.inzisoft.mobile.data.RecognizeResult
import com.inzisoft.mobile.recogdemolib.LibConstants
import com.inzisoft.ibks.viewmodel.AuthData
import java.nio.charset.Charset
import java.util.*

open class IdCardPresenter(title: String): OcrCameraPresenter(title) {
    override fun getCameraConfig(): CameraConfig {
        // Type은 재외국민 신분증 인식타입으로 한다. (주민등록증/운전면허증/재외국민 인식 가능)
        return CameraConfig(LibConstants.TYPE_IDCARD_OVERSEA, 3000000, title)
    }

    override fun checkPreviewRecogResult(context: Context): Boolean {
        val idResult = RecognizeResult.getInstance().idCardRecognizeResult
        // 신분증 인식 성공 조건
        val nameLength = idResult.getNameLength(context)
        val licenseLength = idResult.getLicenseNumberLength(context)
        val dateLength = idResult.getDateLength(context)
        return nameLength > 1 && (licenseLength == 12 || dateLength == 8)
    }

    override fun getRecogData(context: Context, recogResult: RecognizeResult): AuthData {
        val result: IDCardRecognizeResult = recogResult.idCardRecognizeResult


        val rrn = if(BuildConfig.ENCRYPT_API) {
            reconstructionRrn(context, result.rrnByte)
        } else {
            reconstructionRrn(result.rrn)
        }

        return when (recogResult.recogType) {
            IZMobileReaderCommon.IZMOBILEREADER_COMMON_RESULT_TYPE_ID_DRIVING_LICENSE -> {
                // 인식결과가 운전면허증일 경우
                val licenseNum = getEncLicenseNum(context, result.licenseNumberByte)
                val driveDate = AuthData.DriveLicenseData(bitmap = recogResult.getRecogResultImage(true), faceImage = recogResult.photoFaceByte)
                driveDate.dataMap[AuthData.NAME] = result.getEncName(context).trim()
                driveDate.dataMap[AuthData.LICNUM0_1] = licenseNum[0].trim()
                driveDate.dataMap[AuthData.LICNUM2_3] = licenseNum[1].trim()
                driveDate.dataMap[AuthData.LICNUM4_9] = licenseNum[2].trim()
                driveDate.dataMap[AuthData.LICNUM10_11] = licenseNum[3].trim()
                driveDate.dataMap[AuthData.ISSUE_DATE] = result.getEncDate(context).trim()
                driveDate.dataMap[AuthData.ISSUE_OFFICE] = result.getEncIssueOffice(context).trim()
                driveDate.dataMap[AuthData.FRONT_IDNUM] = rrn.first.trim()
                driveDate.dataMap[AuthData.LAST_IDNUM] = rrn.second.trim()
                driveDate
            }
            else -> {
                val idData = if(recogResult.recogType == IZMobileReaderCommon.IZMOBILEREADER_COMMON_RESULT_TYPE_ID_RESIDENT_REGI) {
                    // 인식결과가 주민등록증일 경우
                    AuthData.IdCradData(bitmap = recogResult.getRecogResultImage(true), faceImage = recogResult.photoFaceByte)
                } else {
                    // 인식결과가 재외국민용 신분증일 경우
                    AuthData.OverSea(bitmap = recogResult.getRecogResultImage(true), faceImage = recogResult.photoFaceByte)
                }
                idData.dataMap[AuthData.NAME] = result.getEncName(context).trim()
                idData.dataMap[AuthData.ISSUE_DATE] = result.getEncDate(context).trim()
                idData.dataMap[AuthData.ISSUE_OFFICE] = result.getEncIssueOffice(context).trim()
                idData.dataMap[AuthData.FRONT_IDNUM] = rrn.first.trim()
                idData.dataMap[AuthData.LAST_IDNUM] = rrn.second.trim()
                idData
            }
        }
    }

    private fun reconstructionRrn(rrn: String): Triple<String, String, String> {
        val firstIdNum = rrn.substring(0, 6)
        val lastIdNum = rrn.substring(6)

        val firstIdNumFirstNum = when(lastIdNum[0]) {
            '1', '2' -> "19"
            else -> "20"
        }
        val birth = "${firstIdNumFirstNum}${firstIdNum.substring(0,2)}-${firstIdNum.substring(2,4)}-${firstIdNum.substring(4)}"
        return Triple(firstIdNum, lastIdNum, birth)
    }

    private fun reconstructionRrn(context: Context, encryptedRrn: ByteArray): Triple<String, String, String> {
        val resultCharSize = Integer.SIZE / java.lang.Byte.SIZE
        val lastIdNumStartIndex = resultCharSize * 6
        val firstIdNumByte = ByteArray(6)
        val lastIdNumByte = ByteArray(7)
        val decryptRrn = CryptoUtil.decrypt(context, encryptedRrn)

        var lastIdNumIndex = 0
        var firstIdNumIndex = 0
        for(i in decryptRrn.indices step(resultCharSize)) {
            if(i < lastIdNumStartIndex) {
                firstIdNumByte[firstIdNumIndex++] = decryptRrn[i]
            } else {
                lastIdNumByte[lastIdNumIndex++] = decryptRrn[i]
            }
        }

        Arrays.fill(decryptRrn, 0)
        val lastIdNumFirstNum = Character.getNumericValue(lastIdNumByte[0].toInt())
        val lastEncIdNum = CryptoUtil.encrypt(context, lastIdNumByte)
        Arrays.fill(lastIdNumByte, 0)

        val firstIdNumFirstNum = when(lastIdNumFirstNum) {
            in 1..2 -> "19"
            else -> "20"
        }

        val firstIdNum = String(firstIdNumByte)
        val birth = "${firstIdNumFirstNum}${firstIdNum.substring(0,2)}-${firstIdNum.substring(2,4)}-${firstIdNum.substring(4)}"
        return Triple(firstIdNum, Base64.encodeToString(lastEncIdNum, Base64.NO_WRAP), birth)
    }

    private fun getEncLicenseNum(context: Context, encryptedLicnese: ByteArray): MutableList<String> {
        val resultCharSize = Integer.SIZE / java.lang.Byte.SIZE

        val license0_1 = ByteArray(resultCharSize * 2)
        val license2_3 = ByteArray(2)
        val license4_9 = ByteArray(6)
        val license10_11 = ByteArray(2)
        val decryptLicense = CryptoUtil.decrypt(context, encryptedLicnese)

        for(i in 0 until 12) {
            when(i) {
                // 한글 or 숫자가 있는 첫번째 두번째자리는 4바이트씩 받아서 나중에 UTF-32LE charset으로 decode 한다.
                in 0..1 -> System.arraycopy(decryptLicense, i*resultCharSize, license0_1, i*resultCharSize, resultCharSize)
                in 2..3 -> System.arraycopy(decryptLicense, i*resultCharSize, license2_3, i-2, 1)
                in 4..9 -> System.arraycopy(decryptLicense, i*resultCharSize, license4_9, i-4, 1)
                in 10..11 -> System.arraycopy(decryptLicense, i*resultCharSize, license10_11, i-10, 1)
            }
        }

//        val encryptedLicense4_9 = CryptoUtil.encrypt(context, license4_9)
        Arrays.fill(decryptLicense, 0)
//        Arrays.fill(license4_9, 0)

        val result = mutableListOf<String>()
        result.add(0, String(license0_1, Charset.forName("UTF-32LE")))
        result.add(1, String(license2_3))
//        result.add(2, Base64.encodeToString(encryptedLicense4_9, Base64.NO_WRAP))
        result.add(2, String(license4_9))
        result.add(3, String(license10_11))

        return result
    }
}