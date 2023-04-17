package com.inzisoft.ibks

import com.inzisoft.paperless.ods.PaperlessConstants
import java.io.File
import java.io.FilenameFilter

/**
 * Path manager
 *
 * @property filesPath
 * @property cachePath
 * @property externalFilesPath
 * @property externalDownLoadsPath
 * @property externalDocumentsPath
 * @property externalCachePath
 * @constructor Create empty Path manager
 */
class PathManager(
    private val filesPath: String,
    private val cachePath: String,
    private val externalFilesPath: String?,
    private val externalDownLoadsPath: String?,
    private val externalDocumentsPath: String?,
    private val externalCachePath: String?
) {
    companion object {
        const val EXT_PDF = ".pdf"
        const val EXT_XML = ".xml"
        const val EXT_JPG = ".jpg"
        const val EXT_TIF = ".tif"
        const val EXT_PNG = ".png"
        const val EXT_MP3 = ".mp3"
        const val EXT_CFG = ".cfg"
        const val EXT_ZIP = ".zip"
    }

    object Recruiter {
        const val DIRS = "recruiter"
        const val NAME_IMAGE = "P_RECR_NM"
        const val SIGN_IMAGE = "P_RECR_SIGN"
    }

    private object Log {
        const val DIRS = "log"
    }

    private object Form {
        const val DIRS = PaperlessConstants.FORM_DIR
    }

    private object Biz {
        const val DIRS = PaperlessConstants.BUSINESS_LOGIC_DIR
    }

    private object Temp {
        const val DIRS = "temp"
    }

    private object TempIdCard {
        const val DIRS = "temp_idcard"
    }

    private object Pen {
        const val DIRS = "pen"
    }

    private object Seal {
        const val DIRS = "seal"
    }

    private object Docs {
        const val DIRS = "docs"
    }

    private object Result {
        const val DIRS = "result"
    }

    private object Etc {
        const val DIRS = "etc"
    }

    private object Record {
        const val DIRS = "ROV"
    }

    fun getCacheDir() = "$externalCachePath"
    fun getCacheDir(entryId: String) = "$externalCachePath/$entryId"

    /**
     * 로그
     */
    fun getLogDir() = "${externalFilesPath}/${Log.DIRS}"


    /**
     * 모집인
     */
    fun getUserDir(provId: String, userId: String) = "$externalDocumentsPath/$provId/$userId"
    fun getUserCacheDir(provId: String, userId: String) = "$externalCachePath/$provId/$userId"

    fun getRecruiterDir(provId: String, userId: String) =
        "${getUserDir(provId, userId)}/${Recruiter.DIRS}"

    fun getRecruiterNameImage(provId: String, userId: String) =
        "${getRecruiterDir(provId, userId)}/${Recruiter.NAME_IMAGE}$EXT_PNG"

    fun getRecruiterSignImage(provId: String, userId: String) =
        "${getRecruiterDir(provId, userId)}/${Recruiter.SIGN_IMAGE}$EXT_PNG"

    fun getRecruiterNameImage(entryId: String) =
        "${getPenImageDir(entryId)}/${Recruiter.NAME_IMAGE}$EXT_PNG"

    fun getRecruiterSignImage(entryId: String) =
        "${getPenImageDir(entryId)}/${Recruiter.SIGN_IMAGE}$EXT_PNG"

    /**
     * 펜
     */
    fun getPenCacheDir() = "$externalCachePath/${Pen.DIRS}"
    fun getPenCacheImagePath(id: String) = "${getPenCacheDir()}/${id}$EXT_PNG"
    fun getPenImageDir(entryId: String) = "$externalCachePath/$entryId/${Pen.DIRS}"
    fun getPenImagePath(entryId: String?, id: String) = entryId?.run {
        "${getPenImageDir(entryId)}/$id$EXT_PNG"
    } ?: getPenCacheImagePath(id)


    /**
     * 비즈로직
     */
    fun getBizRootDir() = "$externalDownLoadsPath"
    fun getBizDir() = "${getBizRootDir()}/${Biz.DIRS}"

    /**
     * 서식
     */
    fun getFormRootDir() = "$externalDownLoadsPath"
    fun getFormDir() = "${getFormRootDir()}/${Form.DIRS}"
    fun getFormDir(code: String) = "${getFormRootDir()}/${Form.DIRS}/$code"
    fun getPdfPath(code: String) = "${getFormDir(code)}/$code$EXT_PDF"
    fun getXmlPath(code: String) = "${getFormDir(code)}/$code$EXT_XML"
    fun getRenderImage(code: String, index: Int) =
        "${getFormDir(code)}/${code}_$index$EXT_JPG"

    fun getRenderImages(code: String): List<String> {
        val formDir = File(getFormDir(code))

        val images = formDir.listFiles(object : FilenameFilter {
            override fun accept(dir: File?, name: String?): Boolean {
                name?.apply {
                    if (endsWith(EXT_JPG) && startsWith("${code}_"))
                        return true
                }
                return false
            }
        })

        val imagePaths = mutableListOf<String>()
        images?.forEach { image ->
            imagePaths.add(image.absolutePath)
        }

        return imagePaths
    }

    fun getTempDir() = "$externalCachePath/${Temp.DIRS}"

    fun getTempDir(entryId: String) = "${getTempDir()}/$entryId"

    fun getTempWebDataPath(entryId: String) =
        "${getTempDir(entryId)}/${Constants.TEMP_DATA_WEB_JSON_FILE_NAME}"

    fun getTempPenDir(entryId: String) = "${getTempDir(entryId)}/${Pen.DIRS}"

    fun getTempDocsDir(entryId: String) = "${getTempDir(entryId)}/attach"

    fun getTempZipPath(entryId: String) = "$externalCachePath/${Temp.DIRS}/$entryId$EXT_ZIP"

    fun getIdCardTempDir() = "${externalCachePath}/${TempIdCard.DIRS}"

    fun getIdCardTempDir(docCode: String) = "${getIdCardTempDir()}/$docCode"

    fun getIdCardJpgImage(docCode: String, index: Int) =
        "${getIdCardTempDir(docCode)}/${docCode}_${
            String.format(
                "%02d",
                index + 1
            )
        }$EXT_JPG"

    /**
     * Get evidence doc image dir path
     *
     */
    fun getEvidenceDocDir(entryId: String) = "${externalCachePath}/$entryId/${Docs.DIRS}"

    fun getEvidenceDocDir(entryId: String, docCode: String) =
        "${getEvidenceDocDir(entryId)}/$docCode"

    fun getWebTempDataFilePath(entryId: String, fileName: String) =
        "${getEvidenceDocDir(entryId)}/$fileName"

    /**
     * Get evidence doc image count
     *
     * @param code
     * @return
     */
    fun getEvidenceDocImageCount(entryId: String, docCode: String): Int {
        val dir = File(getEvidenceDocDir(entryId, docCode))

        return dir.list { _, name ->
            name.startsWith(docCode) && name.endsWith(EXT_JPG)
        }?.size ?: 0
    }

    /**
     * 증빙서류 None 마스킹 Cache
     */
    fun getEvidenceDocCacheNoneMaskJpgImage(entryId: String, docCode: String, index: Int) =
        "${getEvidenceDocDir(entryId, docCode)}/Cache/${docCode}_${
            String.format(
                "%02d",
                index + 1
            )
        }$EXT_JPG"

    /**
     * 증빙서류 마스킹 Cache
     */
    fun getEvidenceDocCacheMaskJpgImage(entryId: String, docCode: String, index: Int) =
        "${getEvidenceDocDir(entryId, docCode)}/Cache/${docCode}_${
            String.format(
                "%02d_Masked",
                index + 1
            )
        }$EXT_JPG"

    /**
     * 전송된 이미지를 쌓아놓음.
     */
    fun getEvidenceJpgImage(entryId: String, docCode: String, index: Int) =
        "${getEvidenceDocDir(entryId, docCode)}/${docCode}_${
            String.format(
                "%02d",
                index + 1
            )
        }$EXT_JPG"

    /**
     * 녹취
     */
    fun getRecordDir() = "$externalCachePath/${Record.DIRS}"
    fun getRecordDir(entryId: String) = "${getRecordDir()}/$entryId"
    fun getRecordDir(entryId: String, code: String) = "${getRecordDir(entryId)}/$code"
    fun getRecordPath(entryId: String, code: String, index: Int) =
        "${getRecordDir(entryId, code)}/${code}_${String.format("%02d", index + 1)}$EXT_MP3"
    fun getRecordFiles(entryId: String, code: String): Array<out File> {
        return File(getRecordDir(entryId, code)).listFiles { _, name ->
            name.startsWith(code) && name.endsWith(EXT_MP3)
        } ?: emptyArray()
    }
    fun getRecordCount(entryId: String, code: String): Int {
        val dir = File(getRecordDir(entryId, code))

        return dir.list { _, name ->
            name.startsWith(code) && name.endsWith(EXT_MP3)
        }?.size ?: 0
    }


    /**
     * 결과
     */
    fun getTempRoot(entryId: String) = "$externalCachePath/$entryId/${Temp.DIRS}"

    fun getResultRoot(entryId: String) = "$externalCachePath/$entryId/${Result.DIRS}"

    fun getResultXmlDir(entryId: String, order: Int) = "${getResultRoot(entryId)}/$order"

    fun getResultImageDir(entryId: String) = "${getResultRoot(entryId)}/IMG"

    fun getResultImageDir(entryId: String, code: String) = "${getResultImageDir(entryId)}/$code"

    fun getResultDummyImageDir(entryId: String, type: String, code: String, order: Int) = "${getResultImageDir(entryId)}/${type}_${code}_$order"

    fun getResultConfig(entryId: String) =
        "${getResultRoot(entryId)}/Data$EXT_CFG"

    fun getIBKSConfig(entryId: String) =
        "${getResultRoot(entryId)}/Ibks$EXT_CFG"

    fun getExDataPath(entryId: String, name: String) = "${getResultRoot(entryId)}/${name}$EXT_CFG"

    fun getResultZip(entryId: String) =
        "$externalCachePath/$entryId/$entryId$EXT_ZIP"

    fun getEtcCacheDir() = "${externalCachePath}/${Etc.DIRS}"

    fun getSendImageFilePath(entryId: String, code: String, index: String) = "${getResultImageDir(entryId, code)}/${code}_${index}$EXT_PNG"
}