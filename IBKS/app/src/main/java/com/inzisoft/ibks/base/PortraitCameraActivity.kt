package com.inzisoft.ibks.base

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.inzisoft.ibks.databinding.ActivityPortraitCameraBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PortraitCameraActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPortraitCameraBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPortraitCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}