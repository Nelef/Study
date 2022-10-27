package com.example.composevoicerecorder

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Space
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.content.ContextCompat
import com.example.composevoicerecorder.ui.theme.ComposeVoiceRecorderTheme
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.timer

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

@Preview(showBackground = true)
@Composable
fun Buttons() {
    val context = LocalContext.current

    // timer
    var time = remember { mutableStateOf(0) }
    var timerTask = remember { mutableStateOf(Timer()) }
    var timeText = remember { mutableStateOf("0:0") }

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
        verticalArrangement = Arrangement.Center
    ) {
        var audioSamplingRateValue =
            remember { mutableStateOf(SharedPreference.prefs.setAudioSamplingRateValue) }
        var audioEncodingBitRateValue =
            remember { mutableStateOf(SharedPreference.prefs.setAudioEncodingBitRateValue) }
        Text(
            text = "고음질 - 48kHz, 256kbps (48000Hz, 256000bps)\n" +
                    "중간 - 44.1kHz, 128kbps (44100Hz, 128000bps)\n" +
                    "저음질 - 44.1kHz, 64kbps (44100Hz, 64000bps)", fontSize = 12.sp
        )
        Spacer(modifier = Modifier.height(20.dp))
        Text(text = "샘플링 값은 44100Hz 미만으로 설정 시 오류가 날 수 있음.", color = Color.Red)
        Spacer(modifier = Modifier.height(20.dp))
        Text(text = "샘플링 값 : ${audioSamplingRateValue.value} Hz")
        TextField(
            value = audioSamplingRateValue.value.toString(),
            onValueChange = { audioSamplingRateValue.value = it }
        )
        Spacer(modifier = Modifier.height(20.dp))
        Text(text = "비트레이트 값 : ${audioEncodingBitRateValue.value} bps")
        TextField(
            value = audioEncodingBitRateValue.value.toString(),
            onValueChange = { audioEncodingBitRateValue.value = it }
        )
        Spacer(modifier = Modifier.height(20.dp))
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
                    // 값 저장.
                    SharedPreference.prefs.setAudioSamplingRateValue = audioSamplingRateValue.value
                    SharedPreference.prefs.setAudioEncodingBitRateValue =
                        audioEncodingBitRateValue.value

                    // 타이머 실행
                    timerTask.value = timer(period = 10) {
                        time.value++

                        val sec = time.value / 100
                        val milli = time.value % 100

                        timeText.value = "$sec : $milli"
                    }

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
            Text(text = "녹음 서비스 시작")
        }
        Button(onClick = {
            val serviceIntent = Intent(context, RecordService::class.java)
            context.stopService(serviceIntent)

            // 타이머 정지
            timerTask.value.cancel()
            time.value = 0
            timeText.value = "0:0"

            Toast.makeText(context, "Service stop", Toast.LENGTH_SHORT).show()
        }, modifier = Modifier.wrapContentSize()) {
            Text(text = "녹음 서비스 종료")
        }
        Spacer(modifier = Modifier.height(20.dp))
        Text(text = "녹음 시간 : ${timeText.value}")
        Spacer(modifier = Modifier.height(20.dp))
        Text(text = "저장 위치 : download 폴더")
    }
}