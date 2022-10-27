package com.example.composevoicerecorder

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.media.MediaRecorder
import android.os.Build
import android.os.Environment
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import java.text.SimpleDateFormat
import java.util.*

class RecordService: Service() {
    // mediaRecorder 변수 생성
    private var mediaRecorder: MediaRecorder? = null

    // 현재상태 (녹음ON or 녹음OFF)
    var state = false

    // 현재시간
    val utilDate = Date()
    val formatType = SimpleDateFormat("yyyy-MM-dd HH-mm-ss")
    val time = formatType.format(utilDate)

    val setAudioSamplingRateValue = SharedPreference.prefs.setAudioSamplingRateValue.toString()
    val setAudioEncodingBitRateValue = SharedPreference.prefs.setAudioEncodingBitRateValue.toString()

    // output 위치 설정 - download 폴더에 test.mp3로 저장.
    val output =
        "${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)}" + "/$time ${setAudioSamplingRateValue}Hz ${setAudioEncodingBitRateValue}bps.m4a"

    override fun onBind(intent: Intent): IBinder {
        TODO("Return the communication channel to the service.")
    }

    override fun onCreate() {
        super.onCreate()
        createNotification()
        startRecording()
        Log.d(TAG, "onCreate")
    }

    override fun onDestroy() {
        super.onDestroy()
        stopRecording()
        Log.d(TAG, "onDestroy")
    }

    private fun createNotification() {
        val builder = NotificationCompat.Builder(this, "default")
        builder.setSmallIcon(R.mipmap.ic_launcher)
        builder.setContentTitle("Foreground Service")
        builder.setContentText("포그라운드 서비스")
        builder.color = Color.RED
        val notificationIntent = Intent(this, MainActivity::class.java)
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)

        val pendingIntent: PendingIntent
        // API 31 부터 pendingIntent에 FLAG 설정 필수
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            pendingIntent = PendingIntent.getActivity(
                this,
                0,
                notificationIntent,
                PendingIntent.FLAG_IMMUTABLE
            )
        } else {
            pendingIntent = PendingIntent.getActivity(
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

    // 녹음 시작
    private fun startRecording() {
        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setAudioSamplingRate(setAudioSamplingRateValue.toInt())
            setAudioEncodingBitRate(setAudioEncodingBitRateValue.toInt())
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(output) // 저장위치
            prepare()
        }

        mediaRecorder?.start()

        state = true

        Toast.makeText(this, "녹음 시작", Toast.LENGTH_SHORT).show()
    }

    // 녹음 종료
    private fun stopRecording() {
        if (state) {
            mediaRecorder?.stop()
            mediaRecorder?.release()

            state = false

            Toast.makeText(
                this,
                "다운로드 폴더 - /$time ${setAudioSamplingRateValue}Hz ${setAudioEncodingBitRateValue}bps.m4a \n 저장완료",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    companion object {
        private const val TAG = "MyServiceTag"

        // Notification
        private const val NOTI_ID = 1
    }
}