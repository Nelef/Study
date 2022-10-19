package com.example.composevoicerecorder

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.content.ContextCompat
import com.example.composevoicerecorder.ui.theme.ComposeVoiceRecorderTheme

private const val TAG = "MainActivity"
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
    // 권한 요청
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permission Accepted: Do something
            Log.d(TAG,"PERMISSION GRANTED")

        } else {
            // Permission Denied: Do something
            Log.d(TAG,"PERMISSION DENIED")
        }
    }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        Button(onClick = {
            // 권한 설정(마이크, 폴더접근)
            val permissions = arrayOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            for(permission in permissions) {
                Log.d(TAG, "permission: $permission")
                when (PackageManager.PERMISSION_GRANTED) {
                    // permission 체크
                    ContextCompat.checkSelfPermission(
                        context,
                        permission
                    ) -> {
                        // 권한 부여가 이미 됨.
                        Log.d("ExampleScreen","Code requires permission")
                    }
                    else -> {
                        // 권한 부여가 안되었다면 요청.
                        launcher.launch(permission)
                    }
                }
            }

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