package com.inzisoft.ibks.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.*
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.inzisoft.ibks.R
import com.inzisoft.ibks.base.MainActivity
import com.inzisoft.ibks.util.log.QLog
import java.io.File

class RecordService : Service() {

    companion object {
        // Notification
        private const val NOTI_ID = 1

        const val INTENT_PATH = "path"

        private const val SAMPLE_RATE = 44100
        private const val BIT_RATE = 128000
        private const val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO
        private const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
    }

    // 현재상태 (녹취ON or 녹취OFF)
    private var isRecording = false
    private var isPaused = false

//    // 현재시간
//    val utilDate = Date()
//    val formatType = SimpleDateFormat("yyyyMMdd_HHmmss")
//    val time = formatType.format(utilDate)

    // bindService 설정
    private val binder = RecordServiceBinder()

    private lateinit var filePath: String

    // 테스트............
    private val bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT)
    private lateinit var audioRecord: AudioRecord
    private var recordingThread: Thread? = null

    // MediaCodec 및 MediaMuxer 객체 선언
    private lateinit var codec: MediaCodec
    private lateinit var mediaMuxer: MediaMuxer
    private var audioTrackIndex: Int = -1
    private var outputFile: File? = null
    private var bufferInfo = MediaCodec.BufferInfo()

    override fun onBind(intent: Intent): IBinder {
        filePath = intent.getStringExtra(INTENT_PATH) ?: ""

        // 테스트............
        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            SAMPLE_RATE,
            CHANNEL_CONFIG,
            AUDIO_FORMAT,
            bufferSize
        )

        // MediaCodec 초기화
        val format = MediaFormat.createAudioFormat("audio/mp4a-latm", SAMPLE_RATE, 1)
        format.setInteger(MediaFormat.KEY_BIT_RATE, BIT_RATE)
        format.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, bufferSize)
        codec = MediaCodec.createEncoderByType("audio/mp4a-latm")
        codec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        codec.start()

        // MediaMuxer 초기화
        outputFile = File(filePath)
        mediaMuxer =
            MediaMuxer(outputFile!!.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
        audioTrackIndex = mediaMuxer.addTrack(codec.outputFormat)
        mediaMuxer.start()

        return binder
    }

    override fun onRebind(intent: Intent?) {
        filePath = intent?.getStringExtra(INTENT_PATH) ?: ""
        super.onRebind(intent)
    }

    inner class RecordServiceBinder : Binder() {
        fun getService(): RecordService {
            return this@RecordService
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotification()
        QLog.i("RecordService onCreate")
    }

    override fun onUnbind(intent: Intent?): Boolean {
        stopRecording()
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        stopRecording()
        QLog.i("RecordService onDestroy")
    }

    private fun createNotification() {
        val builder = NotificationCompat.Builder(this, "default")
        builder.setSmallIcon(R.mipmap.ic_launcher)
        builder.setContentTitle("IBKS 녹취 중")
        builder.setContentText("IBKS 녹취 중입니다.")
        builder.color = Color.RED
        val notificationIntent = Intent(this, MainActivity::class.java)
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)

        // API 31 부터 pendingIntent에 FLAG 설정 필수
        val pendingIntent: PendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.getActivity(
                this,
                0,
                notificationIntent,
                PendingIntent.FLAG_IMMUTABLE
            )
        } else {
            PendingIntent.getActivity(
                this,
                0,
                notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
        }

        builder.setContentIntent(pendingIntent) // 알림 클릭 시 이동

        // 알림 표시
        // API 26 부터 NotificationChannel 객체 사용
        // 고유한 채널 ID, 사용자가 볼 수 있는 이름, 중요도 수준을 사용
        val notificationManager = this.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.createNotificationChannel(
                NotificationChannel(
                    "default",
                    "기본 채널",
                    NotificationManager.IMPORTANCE_DEFAULT
                )
            )
        }
        notificationManager.notify(NOTI_ID, builder.build()) // id : 정의해야하는 각 알림의 고유한 int값
        val notification = builder.build()
        startForeground(NOTI_ID, notification)
    }

    // 녹취 시작
    fun startRecording(context: Context) {
        if (isRecording) {
            stopRecording()
        }
        isPaused = false
        isRecording = true

        audioRecord.startRecording()
        recordingThread = Thread(Runnable {
            writeAudioDataToFile()
        })
        recordingThread?.start()

        Toast.makeText(this, R.string.start_record_message, Toast.LENGTH_SHORT).show()
    }

    // 녹취 종료
    private fun stopRecording() {
        if (isRecording) {
            filePath = ""
            isRecording = false
            isPaused = false

            audioRecord.stop()
            recordingThread?.join()

            // MediaCodec 및 MediaMuxer 해제
            codec.stop()
            codec.release()
            mediaMuxer.stop()
            mediaMuxer.release()
        }
    }

    private fun writeAudioDataToFile() {
        val audioData = ByteArray(bufferSize)
        while (isRecording) {
            val numberOfBytes = audioRecord.read(audioData, 0, bufferSize)
            if (numberOfBytes != AudioRecord.ERROR_INVALID_OPERATION) {
                val inputBufferId = codec.dequeueInputBuffer(-1)
                if (inputBufferId >= 0) {
                    val inputBuffer = codec.getInputBuffer(inputBufferId)
                    inputBuffer?.clear()
                    inputBuffer?.put(audioData)
                    codec.queueInputBuffer(inputBufferId, 0, numberOfBytes, 0, 0)
                }

                var outputBufferId = codec.dequeueOutputBuffer(bufferInfo, 0)
                while (outputBufferId >= 0) {
                    val outputBuffer = codec.getOutputBuffer(outputBufferId)
                    val encodedData = ByteArray(bufferInfo.size)
                    outputBuffer?.get(encodedData)
                    outputBuffer?.clear()
                    mediaMuxer.writeSampleData(audioTrackIndex, outputBuffer!!, bufferInfo)
                    codec.releaseOutputBuffer(outputBufferId, false)
                    outputBufferId = codec.dequeueOutputBuffer(bufferInfo, 0)
                }
            }
        }
    }

    // 녹취 일시정지
    fun pauseRecording(): Boolean {
        if (isRecording && !isPaused) {
            Toast.makeText(this, R.string.pause_record_message, Toast.LENGTH_SHORT).show()
            isPaused = true
            audioRecord.stop()
        }
        return isPaused
    }

    // 녹취 재개
    fun resumeRecording() {
        if (isRecording && isPaused) {
            Toast.makeText(this, R.string.resume_record_message, Toast.LENGTH_SHORT).show()
            isRecording = true
            isPaused = false
            audioRecord.startRecording()
        }
    }
}