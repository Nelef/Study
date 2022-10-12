package com.example.compose_1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.example.compose_1.ui.theme.Compose_1Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Compose_1Theme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    DefaultPreview()
                }
            }
        }
    }
}

@Preview(name = "Hello", showBackground = true)
@Composable
fun DefaultPreview() {
    Compose_1Theme {
        Greeting("Android")
        Column {
            Greeting("Android")
            Card_test()
        }
    }
}

@Preview(showBackground = true)
@Composable
fun Greeting(name: String = "test") {
    Text(text = "Hello $name!")
}

@Preview
@Composable
fun Card_test() {
    Card {
        Text(text = "Simple Card")
    }
}