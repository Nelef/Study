package com.example.voicerecorder

import android.Manifest
import android.content.Intent
import android.media.MediaRecorder
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import com.example.voicerecorder.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var mediaRecorder: MediaRecorder? = null

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        var state = false
        var recordingStopped = false

        val output = "${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)}" + "/test.mp3"

        val permissions = arrayOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        ActivityCompat.requestPermissions(this, permissions,0)

        binding.buttonStartService.setOnClickListener {
            val serviceIntent = Intent(this@MainActivity, MyService::class.java)
            startService(serviceIntent)
            Toast.makeText(this@MainActivity, "Service start", Toast.LENGTH_SHORT).show()
        }

        binding.buttonStopService.setOnClickListener {
            val serviceIntent = Intent(this@MainActivity, MyService::class.java)
            stopService(serviceIntent)
            Toast.makeText(this@MainActivity, "Service stop", Toast.LENGTH_SHORT).show()
        }

        binding.buttonStartRecording.setOnClickListener {
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
        }

        binding.buttonStopRecording.setOnClickListener{
            if(state){
                mediaRecorder?.stop()
                mediaRecorder?.release()

                state = false

                Toast.makeText(this, "녹음 중지", Toast.LENGTH_SHORT).show()
            }
        }

        binding.buttonPauseRecording.setOnClickListener {
            if (state) {
                if (!recordingStopped) {
                    Toast.makeText(this, "녹음 임시 정지", Toast.LENGTH_SHORT).show()
                    mediaRecorder?.pause()
                    recordingStopped = true
                    binding.buttonPauseRecording.text = "다시 시작"
                } else {
                    Toast.makeText(this,"다시 시작", Toast.LENGTH_SHORT).show()
                    mediaRecorder?.resume()
                    recordingStopped = false
                    binding.buttonPauseRecording.text = "정지"
                }
            }
        }
    }
}