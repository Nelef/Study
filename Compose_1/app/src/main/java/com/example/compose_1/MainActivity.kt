package com.example.compose_1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.compose_1.ui.theme.Compose_1Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
//            Compose_1Theme {
//                // A surface container using the 'background' color from the theme
//                Surface(
//                    modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background
//                ) {
//                    DefaultPreview()
//                }
//            }
            DefaultPreview()
        }
    }
}

@Preview(name = "Preview_title", showBackground = true)
@Composable
fun DefaultPreview() {
    Column {
        Greeting("Android")
        Card_test()
        BoxExample()
    }
}

@Composable
fun Greeting(name: String = "test") {
    Text(text = "Hello $name!")
}

@Composable
fun Card_test() {
    Card(
        elevation = 10.dp, shape = RoundedCornerShape(13.dp),
        backgroundColor = Color.LightGray, modifier = Modifier.padding(16.dp),
    ) {
        Column {
            Text(text = "Simple Card", modifier = Modifier.padding(16.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Compose", modifier = Modifier.padding(16.dp))
        }
    }
}

@Composable
fun BoxExample() {
    Box(
        modifier = Modifier
            .background(color = Color.Cyan)
            .size(150.dp, 200.dp)
    ) {
        Box(
            modifier = Modifier
                .background(color = Color.Black)
                .size(50.dp, 100.dp)
                .align(Alignment.TopEnd)
        )
        Text(
            text = "Hello",
            style = MaterialTheme.typography.h5,
            modifier = Modifier
                .background(color = Color.White)
                .padding(10.dp)
                .align(
                    Alignment.BottomCenter
                )
        )
    }
}