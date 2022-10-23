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
import androidx.activity.result.ActivityResultLauncher
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
    lateinit var activityResultLauncher: ActivityResultLauncher<Array<String>>
    activityResultLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            if (it.all { permission -> permission.value == true }) {

            } else {
                Toast.makeText(context, "권한 거부", Toast.LENGTH_SHORT).show()
            }
        }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        Button(onClick = {
            // 권한 설정(마이크, 폴더접근)
            val permissions = ArrayList<String>()
            val permissionRequests = ArrayList<String>()
            permissions.add(Manifest.permission.RECORD_AUDIO)
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            // 권한이 부여되어 있는지 하나씩 체크
            for (permission in permissions) {
                Log.d(TAG, "$permission : 권한체크")
                // 권한이 부여되어 있지 않다면
                if (PackageManager.PERMISSION_GRANTED
                    != ContextCompat.checkSelfPermission(context, permission)
                ) {
                    // 권한요청할 array 추가
                    Log.d(TAG, "$permission : 권한없음")
                    permissionRequests.add(permission)
                } else {
                    Log.d(TAG, "$permission : 권한있음")
                }
            }
            when (permissionRequests.size) {
                // permissionRequest 체크
                0 -> {
                    // 모든 권한 부여가 이미 됨.
                    context.startService(Intent(context, RecordService::class.java))
                    Toast.makeText(context, "Service start", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    // 권한 부여가 안되었다면 요청.
                    activityResultLauncher.launch(
                        permissionRequests.toArray(arrayOfNulls(permissionRequests.size))
                    )
                }
            }

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