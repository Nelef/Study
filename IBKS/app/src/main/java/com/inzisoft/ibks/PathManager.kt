package com.inzisoft.ibks

import com.mobileleader.paperless.ods.PaperlessConstants
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

    private object Temp {
        const val DIRS = "temp"
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

    fun getCacheDir() = "$externalCachePath"

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
        "$externalDocumentsPath/$entryId/${Pen.DIRS}/${Recruiter.NAME_IMAGE}$EXT_PNG"

    fun getRecruiterSignImage(entryId: String) =
        "$externalDocumentsPath/$entryId/${Pen.DIRS}/${Recruiter.SIGN_IMAGE}$EXT_PNG"

    /**
     * 펜
     */
    fun getPenCacheImagePath(id: String) = "$externalCachePath/${Pen.DIRS}/${id}$EXT_PNG"
    fun getPenImagePath(entryId: String, id: String) = "$externalDocumentsPath/$entryId/${Pen.DIRS}/$id$EXT_PNG"


    /**
     * 서식
     */
    fun getFormRootDir(provId: String) = "$externalDownLoadsPath/$provId"
    fun getFormDir(provId: String, code: String) = "${getFormRootDir(provId)}/${Form.DIRS}/$code"
    fun getPdfPath(provId: String, code: String) = "${getFormDir(provId, code)}/$code$EXT_PDF"
    fun getXmlPath(provId: String, code: String) = "${getFormDir(provId, code)}/$code$EXT_XML"
    fun getRenderImage(provId: String, code: String, index: Int) =
        "${getFormDir(provId, code)}/${code}_$index$EXT_JPG"

    fun getRenderImages(provId: String, code: String): List<String> {
        val formDir = File(getFormDir(provId, code))

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

    fun getDownloadTempZipPath(entryId: String) = "$externalCachePath/${Temp.DIRS}/$entryId$EXT_ZIP"

    /**
     * Get evidence doc image dir path
     *
     * @param code
     */
    fun getEvidenceDocDir(entryId: String) = "${externalCachePath}/$entryId/${Docs.DIRS}"

    fun getWebTempDataFilePath(entryId: String, fileName: String) = "${getEvidenceDocDir(entryId)}/$fileName"
    /**
     * Get evidence doc image count
     *
     * @param code
     * @return
     */
    fun getEvidenceDocImageCount(entryId: String, docCode: String): Int {
        val dir = File(getEvidenceDocDir(entryId))

        return dir.list { _, name ->
            name.startsWith(docCode) && name.endsWith(EXT_JPG)
        }?.size ?: 0
    }

    /**
     * 증빙서류 None 마스킹 Cache
     */
    fun getEvidenceDocCacheNoneMaskJpgImage(entryId: String, docCode: String, index: Int) =
        "${getEvidenceDocDir(entryId)}/Cache/${docCode}_${String.format("%02d", index+1)}$EXT_JPG"

    /**
     * 증빙서류 마스킹 Cache
     */
    fun getEvidenceDocCacheMaskJpgImage(entryId: String, docCode: String, index: Int) =
        "${getEvidenceDocDir(entryId)}/Cache/${docCode}_${String.format("%02d_Masked", index+1)}$EXT_JPG"

    /**
     * 전송된 이미지를 쌓아놓음.
     */
    fun getEvidenceJpgImage(entryId: String, docCode: String, index: Int) =
        "${getEvidenceDocDir(entryId)}/${docCode}_${String.format("%02d", index+1)}$EXT_JPG"

    /**
     * 결과
     */
    fun getResultRoot(entryId: String) = "$externalDocumentsPath/$entryId/${Result.DIRS}"

    fun getResultXml(entryId: String) = "$externalDocumentsPath/$entryId/${Result.DIRS}/resultXml"

    fun getResultConfig(entryId: String) =
        "${getResultXml(entryId)}/Data$EXT_CFG"

    fun getResultZip(entryId: String) =
        "${getResultRoot(entryId)}/$entryId$EXT_ZIP"

    fun getEtcCacheDir() = "${externalCachePath}/${Etc.DIRS}"
}