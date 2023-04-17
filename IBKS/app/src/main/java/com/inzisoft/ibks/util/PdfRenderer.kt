package com.inzisoft.ibks.util

import android.graphics.Bitmap
import com.inzisoft.ibks.PathManager
import com.inzisoft.paperless.pdf.PdfManager
import java.io.File
import java.io.IOException
import javax.inject.Inject

class PdfRenderer @Inject constructor(private val pathManager: PathManager) {

    fun render(formCode: String, maxPageSize: Int = 1024, pdfPath: String = pathManager.getPdfPath(formCode)) {
        val pdfHelper = PdfManager.getInstance().getPdfHelper(pdfPath) ?: throw IOException("fail to load pdf $pdfPath")

        val totalPage = pdfHelper.totalPage

        for (index in 0 until totalPage) {
            pdfHelper.goPage(index)


            val image = if (pdfHelper.currentPageWidth < pdfHelper.currentPageHeight) {
                val pageHeight = maxPageSize * pdfHelper.currentPageHeight / pdfHelper.currentPageWidth
                Bitmap.createBitmap(maxPageSize, pageHeight, Bitmap.Config.RGB_565)
            } else {
                val pageWidth = maxPageSize * pdfHelper.currentPageWidth / pdfHelper.currentPageHeight
                Bitmap.createBitmap(pageWidth, maxPageSize, Bitmap.Config.RGB_565)
            }

            pdfHelper.getPage(image)

            val imagePath = pathManager.getRenderImage(formCode, index)
            FileUtils.saveBitmap(imagePath, image)

            image.recycle()
        }

        PdfManager.getInstance().destroyPDF()
    }

    fun isRendered(formCode: String): Boolean {
        val count = PdfManager.getInstance().getPdfTotalCount(pathManager.getPdfPath(formCode))

        if (count == -1) {
            PdfManager.getInstance().destroyPDF()
            return false
        }

        for (index in 0 until count) {
            val imageFile = File(pathManager.getRenderImage(formCode, index))

            if (!imageFile.exists()) return false
        }

        PdfManager.getInstance().destroyPDF()

        return true
    }

}