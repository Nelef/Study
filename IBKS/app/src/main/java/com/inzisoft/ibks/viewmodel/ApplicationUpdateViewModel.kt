package com.inzisoft.ibks.viewmodel

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.lifecycle.viewModelScope
import com.inzisoft.ibks.BuildConfig
import com.inzisoft.ibks.base.BaseViewModel
import com.inzisoft.ibks.util.log.QLog
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.DataInputStream
import java.io.File
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class ApplicationUpdateViewModel @Inject constructor() : BaseViewModel() {

    var uiState by mutableStateOf<ApplicationState>(ApplicationState.None)
    var progress by mutableStateOf(ApplicationState.Progress(0f, 0f, 0f))

    private lateinit var version: String
    private lateinit var fileName: String
    private lateinit var fileRefNo: String
    var fromSplash = true

    fun init() {
        uiState = ApplicationState.None
        progress = ApplicationState.Progress(0f, 0f, 0f)
    }

    fun load(version: String, fileName: String, fileRefNo: String, fromSplash: Boolean) {
        this.version = version
        this.fileName = fileName
        this.fileRefNo = fileRefNo
        this.fromSplash = fromSplash
    }

    fun startUpdate(context: Context) =
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // 현재시간
                val time = SimpleDateFormat("yyMMdd_HHmmss").format(Date())

                val resultFile = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                    "${time}_${fileName}"
                )

                if (resultFile.exists()) resultFile.delete()

                val resultUri: Uri
                val outputStream: OutputStream

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val resolver = context.contentResolver
                    val mimeType = fileName.substring(fileName.indexOf(".") + 1, fileName.length)
                    resultUri = resolver.insert(MediaStore.Files.getContentUri("external"), ContentValues().apply {
                        put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                        put(MediaStore.MediaColumns.MIME_TYPE, "application/$mimeType")
                        put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + File.separator)
                    }) ?: throw NullPointerException("not found download path")
                    outputStream = resolver.openOutputStream(resultUri) ?: throw NullPointerException("not found download path")
                } else {
                    resultUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.provider",
                            resultFile
                        )
                    } else {
                        resultFile.toUri()
                    }
                    outputStream = resultFile.outputStream()
                }

                val uri = Uri.Builder()
                    .scheme(BuildConfig.SERVER_PROTOCOL)
                    .encodedAuthority("${BuildConfig.API_SERVER_URL}:${BuildConfig.API_SERVER_PORT}")
                    .appendEncodedPath(BuildConfig.MAIN_API)
                    .appendEncodedPath("apk/download")
                    .appendQueryParameter("fileRefNo", fileRefNo)
                    .build()

                val urlConnection = URL(uri.toString()).openConnection() as HttpURLConnection
                urlConnection.connect()

                if (urlConnection.responseCode == HttpURLConnection.HTTP_OK) {
                    val contentLength =
                        try {
                            urlConnection.contentLengthLong
                        } catch (e: NoSuchMethodError) {
                            urlConnection.contentLength.toLong()
                        }

                    updateDownload(0, contentLength)

                    val bufferSize = 8 * 1024

                    DataInputStream(urlConnection.inputStream).use { input ->
                        outputStream.use { output ->
                            var bytesCopied: Long = 0
                            val buffer = ByteArray(bufferSize)
                            var bytes = input.read(buffer)

                            while (bytes >= 0) {
                                output.write(buffer, 0, bytes)
                                bytesCopied += bytes
                                bytes = input.read(buffer)

                                updateDownload(bytesCopied, contentLength)
                            }

                            uiState = ApplicationState.Complete(resultUri)
                        }
                    }

                } else {
                    uiState = ApplicationState.Error(urlConnection.responseCode)
                    QLog.e("error : ${urlConnection.responseMessage} (CODE : ${urlConnection.responseCode})")
                }
            } catch (e: Exception) {
//                Firebase.crashlytics.recordException(e)
                QLog.e("[APK Download] Fail to start. ", e)
                uiState = ApplicationState.Error(ApplicationState.Error.ERROR_UNKNOWN)
            }
        }

    private fun updateDownload(current: Long, total: Long) {
        val downloadedMb = current / 1000.0f / 1000.0f
        val totalMb = total / 1000.0f / 1000.0f
        val progress = current.toFloat() / total.toFloat()

        if ((this.progress.progress * 100).toInt() != (progress * 100).toInt()) {
            QLog.i("[APK Download] progress : $downloadedMb MB / $totalMb MB (${progress * 100}%)")
        }

        this.progress = ApplicationState.Progress(downloadedMb, totalMb, progress)
    }

}

sealed class ApplicationState {
    object None : ApplicationState()
    data class Error(val code: Int) : ApplicationState() {
        companion object {
            const val ERROR_UNKNOWN = -999
        }
    }

    data class Progress(val downloaded: Float, val total: Float, val progress: Float) :
        ApplicationState()

    data class Complete(val uri: Uri) : ApplicationState()
}