package com.example.composevoicerecorder

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.example.composevoicerecorder.ui.theme.ComposeVoiceRecorderTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ComposeVoiceRecorderTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    Buttons()
                }
            }
        }
    }
}

@Composable
fun Buttons() {
    val context = LocalContext.current

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        Button(onClick = {
            val serviceIntent = Intent(context, RecordService::class.java)
            context.startService(serviceIntent)
            Toast.makeText(context, "Service start", Toast.LENGTH_SHORT).show()
        }, modifier = Modifier.wrapContentSize()) {
            Text(text = "서비스 시작")
        }
        Button(onClick = {
            val serviceIntent = Intent(context, RecordService::class.java)
            context.stopService(serviceIntent)
            Toast.makeText(context, "Service stop", Toast.LENGTH_SHORT).show()
        }, modifier = Modifier.wrapContentSize()) {
            Text(text = "서비스 종료")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    ComposeVoiceRecorderTheme {
        Buttons()
    }
}