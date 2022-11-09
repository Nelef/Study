package com.inzisoft.ibks.util

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.os.storage.StorageManager
import androidx.annotation.RequiresApi
import com.inzisoft.mobile.data.ImageConverter
import com.inzisoft.ibks.PathManager.Companion.EXT_JPG
import com.inzisoft.ibks.PathManager.Companion.EXT_TIF
import com.inzisoft.ibks.util.log.QLog
import java.io.*
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream
import kotlin.io.path.Path
import kotlin.io.path.deleteIfExists
import kotlin.io.path.exists

object FileUtils {

    /**
     * 외장 메모리 사용 가능 공간 가져오기(단위 : bytes)
     */
    @SuppressLint("NewApi")
    @Throws(IOException::class)
    private fun getRemainStorageSize(context: Context): Long {
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED, ignoreCase = true)) {
            return 0L
        }
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val storageManager = context.getSystemService(
                StorageManager::class.java
            )
            val appSpecificInternalDirUuid =
                storageManager.getUuidForPath(context.filesDir)
            storageManager.getAllocatableBytes(appSpecificInternalDirUuid)
        } else {
            val path = Environment.getExternalStorageDirectory()
            val stat = StatFs(path.path)
            val blockSize = stat.blockSizeLong
            val availableBlocks = stat.availableBlocksLong
            availableBlocks * blockSize
        }
    }

    fun availableStorage(context: Context, downloadFileSize: Long): Boolean {
        return try {
            val remainStorageSize = getRemainStorageSize(context)
            val bufferSize = (50 * 1024 * 1024).toLong() // 버퍼 50MB
            val availableSize = remainStorageSize - downloadFileSize - bufferSize
            (remainStorageSize > 0 && availableSize > 0)
        } catch (e: IOException) {
            QLog.e(e)
            false
        }
    }

    fun saveBitmap(
        path: String,
        image: Bitmap,
        format: CompressFormat = CompressFormat.JPEG,
        quality: Int = 100
    ): Boolean {
        var ret: Boolean
        File(path).parentFile?.apply {
            mkdirs()
        }
        FileOutputStream(path).use { stream ->
            ret = image.compress(format, quality, stream)
        }

        return ret
    }

    fun createTifImage(context: Context, jpgImageList: List<String>, tifImagePath: String) {
        for (i: Int in jpgImageList.indices) {
            val bitmap = BitmapFactory.decodeFile(jpgImageList[i])
            when (i) {
                0 -> saveToTiffImageFile(
                    context = context,
                    bitmap = bitmap,
                    dstFilePath = tifImagePath
                )
                else -> mergeTiffImage(
                    context = context,
                    tiffImagePath = tifImagePath,
                    bitmap = bitmap
                )
            }
        }
    }

    /**
     * Bitmap을 ByteArray 로 변환
     */
    private fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(CompressFormat.JPEG, 100, stream)
        return stream.toByteArray()
    }

    /**
     * Bitmap을 tif 이미지 파일로 변환하여 저장함.
     */
    private fun saveToTiffImageFile(
        context: Context,
        bitmap: Bitmap,
        dstFilePath: String
    ) {
        val byteImage = bitmapToByteArray(bitmap)
        if (byteImage != null) {
            val tiffImage = saveToTifImage(context = context, image = byteImage)
            val tiffImageFile = File(dstFilePath)

            tiffImageFile.parentFile?.apply {
                mkdirs()
            }

            tiffImageFile.writeBytes(tiffImage)
        }
    }

    /**
     * 일반 이미지를 tif 이미지로 변환하여 리턴
     */
    private fun saveToTifImage(context: Context, image: ByteArray): ByteArray {
        return convertImage(
            context,
            image,
            ImageConverter.IMAGE_FILE_TYPE_TIFF,
            ImageConverter.IMAGE_COMP_TYPE_JPEG2000_IN_TIFF,
            100
        )
    }

    fun saveToJpgImageFile(context: Context, bitmap: Bitmap, imagePath: String) {
        val bitmapByteArray = bitmapToByteArray(bitmap)
        val jpgImage = convertImage(
            context,
            bitmapByteArray,
            ImageConverter.IMAGE_FILE_TYPE_JPEG,
            ImageConverter.IMAGE_FILE_TYPE_JPEG,
            30
        )

        File(imagePath).apply {
            parentFile?.apply {
                mkdirs()
            }
            writeBytes(jpgImage)
        }

        Arrays.fill(jpgImage, 0)
    }

    private fun convertImage(
        context: Context,
        image: ByteArray,
        fileType: Int,
        comptype: Int,
        comprate: Int
    ): ByteArray {
        val imageConvert = ImageConverter(context)
        imageConvert.initImageIOAdapter()
        return imageConvert.saveImageMem(
            image,
            fileType,
            comptype,
            comprate.toDouble()
        )
    }

    /**
     * tif 파일에 일반 이미지를 머지하여 변환
     */
    fun mergeTiffImage(
        context: Context,
        tiffImagePath: String,
        bitmap: Bitmap
    ) {
        val byteImage = bitmapToByteArray(bitmap)
        if (byteImage != null) {
            val tiffImageFile = File(tiffImagePath)
            tiffImageFile.writeBytes(
                mergeTiffImage(
                    context,
                    tiffImageFile.readBytes(),
                    byteImage
                )
            )
        } else {
        }
    }

    /**
     * tif 이미지에 일반이미지를 머지하여 리턴
     */
    fun mergeTiffImage(context: Context, tiffImage: ByteArray, image: ByteArray): ByteArray {
        val imageConvert = ImageConverter(context)
        imageConvert.initImageIOAdapter()
        return imageConvert.mergeTiffMem(
            tiffImage,
            imageConvert.saveImageMem(
                image,
                ImageConverter.IMAGE_FILE_TYPE_TIFF,
                ImageConverter.IMAGE_COMP_TYPE_JPEG2000_IN_TIFF,
                100.0
            )
        )
    }

    /**
     * tif 이미지 파일을 jpg 이미지 파일로 변환하여 변환된 jpg 이미지 path를 리턴
     */
    fun convertTiffToJpg(context: Context, tiffImagePath: String): List<String> {
        val imageConvert = ImageConverter(context)
        imageConvert.initImageIOAdapter()
        val imagePaths = mutableListOf<String>()
        // tiff 파일의 페이지 개수를 반환
        val tiffCnt: Int = imageConvert.getTiffTotalPageFile(tiffImagePath)
        for (i in 0 until tiffCnt) {
            // multi-tiff 파일에서 한 장씩 추출
            imageConvert.extractTiffFile(
                tiffImagePath, i + 1,
                tiffImagePath.replace(EXT_TIF, "_${i + 1}$EXT_TIF")
            )

            // TIFF이미지를 JPG로 변환
            imageConvert.saveImageFile(
                tiffImagePath.replace(EXT_TIF, "_${i + 1}$EXT_TIF"),
                ImageConverter.IMAGE_FILE_TYPE_JPEG,
                ImageConverter.IMAGE_COMP_TYPE_NONE,
                75.0,
                tiffImagePath.replace(EXT_TIF, "_${i + 1}$EXT_JPG")
            )
            imagePaths.add(tiffImagePath.replace(EXT_TIF, "_${i + 1}$EXT_JPG"))
        }

        return imagePaths
    }

    @Throws(IOException::class)
    fun readFile(path: String): ByteArray {
        return readFile(File(path))
    }

    @Throws(IOException::class)
    fun readFile(file: File): ByteArray {
        val size = file.length().toInt()
        val bytes = ByteArray(size)

        BufferedInputStream(FileInputStream(file)).use {
            it.read(bytes, 0, bytes.size)
        }

        return bytes
    }

    @Throws(IOException::class)
    fun copy(src: String, dst: String) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            copy(File(src), File(dst))
        } else {
            copy(Path(src), Path(dst))
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @Throws(IOException::class)
    fun copy(src: Path, dst: Path) {
        delete(dst)

        dst.toFile().mkdirs()

        Files.walk(src).forEach {
            Files.copy(it, dst.resolve(src.relativize(it)), StandardCopyOption.REPLACE_EXISTING)
        }
    }

    fun copy(src: File, dst: File) {
        dst.mkdirs()

        src.copyRecursively(dst, true)
    }

    fun delete(path: String) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            delete(File(path))
        } else {
            delete(Path(path))
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun delete(path: Path) {
        if (path.exists()) {
            Files.walk(path).sorted(Comparator.reverseOrder()).forEach { it.deleteIfExists() }
        }
    }

    fun delete(file: File) {
        if (file.isDirectory) {
            file.listFiles()?.forEach { childFile ->
                delete(childFile)
            }

        }

        file.delete()
    }

    fun rename(src: String, dst: String) {
        File(dst).apply {
            if(exists()) {
                delete()
            }
        }
        File(src).renameTo(File(dst))
    }

    private const val DEFAULT_BUFFER_SIZE = 1024 * 4

    @Throws(IOException::class)
    fun zipFile(fileToZip: String, zipFile: String?) {

        ZipOutputStream(FileOutputStream(zipFile)).use { outputStream ->
            val srcFile = File(fileToZip)
            if (srcFile.isDirectory) {
                srcFile.list { dir, fileName ->
                    addToZip("", "$fileToZip/$fileName", outputStream)
                    true
                }
            } else {
                addToZip("", fileToZip, outputStream)
            }

            true
        }
    }

    @Throws(IOException::class)
    private fun addToZip(path: String, srcFile: String, zipOutputStream: ZipOutputStream) {
        val file = File(srcFile)
        val filePath = if ("" == path) file.name else path + "/" + file.name
        if (file.isDirectory) {
            val childList = file.list()

            childList?.let {
                val folderPath = "$filePath/"
                val entry = ZipEntry(folderPath)
                zipOutputStream.putNextEntry(entry)


                for (fileName in childList) {
                    addToZip(filePath, "$srcFile/$fileName", zipOutputStream)
                }
            }
        } else {
            val entry = ZipEntry(filePath)
            zipOutputStream.putNextEntry(entry)
            val `in` = FileInputStream(srcFile)
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            var len: Int
            while (`in`.read(buffer).also { len = it } != -1) {
                zipOutputStream.write(buffer, 0, len)
            }
        }
    }

    @Throws(IOException::class)
    fun unZip(zipFilePath: String, targetPath: String) {
        File(targetPath).mkdirs()

        ZipFile(zipFilePath).use { zip ->
            zip.entries().asSequence().forEach { entry ->
                when (entry.isDirectory) {
                    true -> File(targetPath, entry.name).mkdirs()
                    false -> zip.getInputStream(entry).use { input ->
                        File(targetPath, entry.name).outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                }
            }
        }
    }
}