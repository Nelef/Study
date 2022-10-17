package com.example.voicerecorder

import android.Manifest
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

        binding.buttonStartRecording.setOnClickListener {
            mediaRecorder = MediaRecorder()

            mediaRecorder?.setAudioSource(MediaRecorder.AudioSource.MIC)
            mediaRecorder?.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            mediaRecorder?.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            mediaRecorder?.setOutputFile(output)

            mediaRecorder?.prepare()
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
                    Toast.makeText(this, "녹음 정지", Toast.LENGTH_SHORT).show()
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