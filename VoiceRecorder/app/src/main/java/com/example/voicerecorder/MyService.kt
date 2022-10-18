package com.example.voicerecorder

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

class MyService : Service() {
    private var mediaRecorder: MediaRecorder? = null
    var state = false
    var recordingStopped = false

    val output = "${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)}" + "/test.mp3"

    override fun onBind(intent: Intent): IBinder {
        throw UnsupportedOperationException("Not yet implemented")
    }

    override fun onCreate() {
        super.onCreate()
        createNotification()
        mThread!!.start()
        //test
        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(output) // 저장위치
            prepare()
        }

        mediaRecorder?.start()

        state = true

        Toast.makeText(this, "녹음 시작", Toast.LENGTH_SHORT).show()
        //test
        Log.d(TAG, "onCreate")
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand")
        return super.onStartCommand(intent, flags, startId)
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

    private var mThread: Thread? = object : Thread("My Thread") {
        override fun run() {
            super.run()
            for (i in 0..99) {
                Log.d(TAG, "count : $i")
                try {
                    sleep(1000)
                } catch (e: InterruptedException) {
                    currentThread().interrupt()
                    break
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mThread != null) {
            mThread!!.interrupt()
            mThread = null
        }
        if(state){
            mediaRecorder?.stop()
            mediaRecorder?.release()

            state = false

            Toast.makeText(this, "녹음 중지", Toast.LENGTH_SHORT).show()
        }
        Log.d(TAG, "onDestroy")
    }

    companion object {
        private const val TAG = "MyServiceTag"

        // Notification
        private const val NOTI_ID = 1
    }
}