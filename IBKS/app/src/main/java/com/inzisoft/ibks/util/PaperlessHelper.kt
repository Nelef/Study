package com.inzisoft.ibks.util

import com.ml.Constants
import com.ml.callback.PaperlessInterface
import com.ml.controller.PaperlessView
import com.ml.data.PdfSettingInfoData

class PaperlessHelper(
    private val paperlessView: PaperlessView,
    private val pdfLoadListener: PaperlessInterface.ILoadPDFCallback
) :
    PaperlessInterface.ILoadPDFCallback {

    private val openPdfQueue = ArrayDeque<String>()

    fun loadPdf(pdfPaths: List<String>) {
        openPdfQueue.addAll(pdfPaths)
        loadNextPdf()
    }

    private fun loadNextPdf() {
        val path = openPdfQueue.removeFirst()
        paperlessView.loadPDFView(PdfSettingInfoData(path), this)
    }

    override fun beforeToLoadPDF(pdfLoadMode: Constants.PDFLoadMode?, pdfFilePath: String?) {
    }

    override fun completeToLoadPDF(
        pdfLoadMode: Constants.PDFLoadMode?,
        pdfFilePath: String?,
        errorCode: Constants.PaperlessErrorCode?
    ) {
        when (errorCode) {
            Constants.PaperlessErrorCode.ERR_NONE -> {
                if (openPdfQueue.isEmpty()) {
                    pdfLoadListener.completeToLoadPDF(pdfLoadMode, "", errorCode)
                } else {
                    loadNextPdf()
                }
            }
            else -> {
                pdfLoadListener.completeToLoadPDF(pdfLoadMode, pdfFilePath, errorCode)
            }
        }
    }

    override fun beforeToChangeViewMode(viewMode: Constants.ViewMode?) {
        pdfLoadListener.beforeToChangeViewMode(viewMode)
    }

    override fun completeToChangeViewMode(viewMode: Constants.ViewMode?) {
        pdfLoadListener.completeToChangeViewMode(viewMode)
    }

}