package com.inzisoft.ibks.util.log

import android.os.*
import com.orhanobut.logger.DiskLogStrategy
import com.orhanobut.logger.FormatStrategy
import com.orhanobut.logger.LogStrategy
import com.orhanobut.logger.Logger
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

class LogFileStrategy(
    builder: Builder
) : FormatStrategy {

    private val dirPath: String
    private val date: Date
    private val dateFormat: DateFormat
    private val tag: String
    private val logStrategy: LogStrategy

    init {
        dirPath = builder.dirPath
        date = builder.date
        dateFormat = builder.dateFormat
        tag = builder.tag
        logStrategy = builder.logStrategy
    }

    companion object {
        fun newBuilder(dirPath: String): Builder {
            return Builder(dirPath)
        }
    }

    override fun log(priority: Int, onceOnlyTag: String?, message: String) {
        if (message.isEmpty()) return
        date.time = System.currentTimeMillis()
        val builder = StringBuilder()
            .append(dateFormat.format(date))
            .append(" ")
            .append(logLevel(priority))
            .append("/")
            .append(tag)
            .append(": ")
            .append(message)
            .appendLine()

        logStrategy.log(priority, tag, builder.toString())
    }

    private fun logLevel(value: Int): String? {
        return when (value) {
            Logger.VERBOSE -> "V"
            Logger.DEBUG -> "D"
            Logger.INFO -> "I"
            Logger.WARN -> "W"
            Logger.ERROR -> "E"
            Logger.ASSERT -> "A"
            else -> "UNKNOWN"
        }
    }

    internal class WriteHandler(
        looper: Looper,
        private val folder: String,
        private val maxFileSize: Int
    ) : Handler(looper) {

        companion object {
            private const val FILE_PREFIX = "QLog-"
            private const val FILE_DATE_FORMAT = "yyyy-MM-dd-HH"
        }

        init {
            deleteExpiredFile()
        }

        override fun handleMessage(msg: Message) {
            val content = msg.obj as String
            val logFile = getLogFile(folder)

            FileWriter(logFile, true).use {
                writeLog(it, content)
            }
        }

        private fun deleteExpiredFile() {
            val expiredFiles = File(folder).listFiles { _, name ->
                val dateString = name.substringAfter(FILE_PREFIX).substringBefore("_")

                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                    val dateOfFile = SimpleDateFormat(FILE_DATE_FORMAT).parse(dateString)
                    Calendar.getInstance().time = Date()
                    Calendar.getInstance().add(Calendar.DATE, -5)
                    val validDay = Calendar.getInstance().time

                    dateOfFile?.before(validDay) ?: true
                } else {
                    val dateOfFile =
                        LocalDate.parse(dateString, DateTimeFormatter.ofPattern(FILE_DATE_FORMAT))
                    val today = LocalDate.now()

                    dateOfFile.isBefore(today.minusDays(5))
                }
            }

            expiredFiles?.forEach { file ->
                file.delete()
            }
        }

        /**
         * This is always called on a single background thread.
         * Implementing classes must ONLY write to the fileWriter and nothing more.
         * The abstract class takes care of everything else including close the stream and catching IOException
         *
         * @param fileWriter an instance of FileWriter already initialised to the correct file
         */
        @Throws(IOException::class)
        private fun writeLog(fileWriter: FileWriter, content: String) {
            fileWriter.append(content)
        }

        private fun getLogFile(folderName: String): File {
            val fileName = "$FILE_PREFIX${SimpleDateFormat(FILE_DATE_FORMAT).format(Date())}"

            val folder = File(folderName)
            if (!folder.exists()) {
                folder.mkdirs()
            }

            var newFileCount = 0
            var newFile: File
            var existingFile: File? = null

            newFile = File(folder, String.format("%s_%s.txt", fileName, newFileCount))
            while (newFile.exists()) {
                existingFile = newFile
                newFileCount++
                newFile = File(folder, String.format("%s_%s.txt", fileName, newFileCount))
            }
            return if (existingFile != null) {
                if (existingFile.length() >= maxFileSize)
                    newFile
                else
                    existingFile
            } else
                newFile
        }
    }

    class Builder(val dirPath: String) {
        var date: Date = Date()
        var dateFormat: DateFormat = SimpleDateFormat("yyyy.MM.dd HH:mm:ss.SSS", Locale.KOREA)
        var tag: String = ""
        lateinit var logStrategy: LogStrategy

        fun date(date: Date): Builder {
            this.date = date
            return this
        }

        fun dateFormat(dateFormat: DateFormat): Builder {
            this.dateFormat = dateFormat
            return this
        }

        fun logStrategy(logStrategy: LogStrategy): Builder {
            this.logStrategy = logStrategy
            return this
        }

        fun tag(tag: String): Builder {
            this.tag = tag
            return this
        }

        fun build(): LogFileStrategy {
            val ht = HandlerThread("AndroidFileLogger.$dirPath")
            ht.start()
            val handler: Handler = WriteHandler(ht.looper, dirPath, MAX_BYTES)
            logStrategy = DiskLogStrategy(handler)
            return LogFileStrategy(this)
        }

        companion object {
            private const val MAX_BYTES = 500 * 1024 // 500K averages to a 4000 lines per file
        }
    }
}