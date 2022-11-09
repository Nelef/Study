package com.inzisoft.ibks.data.internal

import android.content.Context
import android.graphics.Bitmap
import android.text.TextUtils
import com.inzisoft.ibks.PathManager
import com.inzisoft.ibks.util.FileUtils
import java.io.Serializable

data class DocInfoData(
    val docCode: String,
    val docName: String,
    val maskingYn: Boolean,
    val docImageDataList: MutableList<DocImageData>,
    val docCtgCd: String
) : Serializable

data class DocImageData(
    var cacheOriginImagePath: String,
    val realImagePath: String
) {
    private var cacheMaskedImagePath: String = ""

    /**
     * 캐쉬 마스킹 이미지 패스를 만든다.
     */
    fun setMaskedImagePath(entryId: String, docCode: String, index: Int, pathManager: PathManager) {
        cacheMaskedImagePath = pathManager.getEvidenceDocCacheMaskJpgImage(entryId = entryId, docCode = docCode, index = index)
    }

    /**
     * 캐쉬의 마스킹 이미지가 있으면 마스킹 이미지 패스를 리턴한다.
     * 없으면 캐쉬 원본 이미지 패스를 리턴한다.
     */
    fun getCacheAvailableImagePath(): String {
        return if(hasMaskedImage()) {
            cacheMaskedImagePath
        } else {
            cacheOriginImagePath
        }
    }

    /**
     * 마스킹 이미지가 있으면 원본 이미지를 삭제한다.
     * 원본 이미지만 존재하면 삭제하지 않는다.
     * 그렇게 해서 이미지 하나만 남도록 한다.
     */
    fun deleteUnavailableImage() {
        if(hasMaskedImage()) {
            FileUtils.delete(cacheOriginImagePath)
            cacheOriginImagePath = ""
        }
    }

    /**
     * 마스크 이미지를 원본 이미지로 이름을 변경한다.
     */
    fun renameToRealImage() {
        FileUtils.rename(getCacheAvailableImagePath(), realImagePath)
        clearImage()
    }

    /**
     * 마스킹 이미지 삭제한다.
     */
    fun deleteMaskedImage() {
        if(cacheMaskedImagePath.isNotEmpty()) {
            FileUtils.delete(cacheMaskedImagePath)
        }
        cacheMaskedImagePath = ""
    }

    /**
     * 캐쉬에 있는 마스킹 이미지, 원본 이미지 모두 삭제한다.
     */
    fun clearImage() {
        if(hasOriginImage()) {
            FileUtils.delete(cacheOriginImagePath)
        }

        if(hasMaskedImage()) {
            FileUtils.delete(cacheMaskedImagePath)
        }
        cacheOriginImagePath = ""
        cacheMaskedImagePath = ""
    }

    private fun hasMaskedImage(): Boolean {
        return !TextUtils.isEmpty(cacheMaskedImagePath)
    }

    private fun hasOriginImage(): Boolean {
        return !TextUtils.isEmpty(cacheOriginImagePath)
    }

    fun saveMaskedImageFile(context: Context, maskedBitmap: Bitmap) {
        FileUtils.saveToJpgImageFile(context = context, bitmap = maskedBitmap, imagePath = cacheMaskedImagePath)
    }

    fun renameTo(entryId: String, docCode: String, index: Int, pathManager: PathManager) {
        if(hasMaskedImage()) {
            val dstMaskedImagePath = pathManager.getEvidenceDocCacheMaskJpgImage(
                entryId = entryId,
                docCode = docCode,
                index = index
            )
            FileUtils.rename(src = cacheMaskedImagePath, dst = dstMaskedImagePath)
            cacheMaskedImagePath = dstMaskedImagePath
        }

        val dstOriginImagePath = pathManager.getEvidenceDocCacheNoneMaskJpgImage(
            entryId = entryId,
            docCode = docCode,
            index = index
        )
        FileUtils.rename(src = cacheOriginImagePath, dst = dstOriginImagePath)
        cacheOriginImagePath = dstOriginImagePath
    }
}


