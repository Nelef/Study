package com.example.compose_1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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
    Card(
        shape = RoundedCornerShape(13.dp),
        backgroundColor = Color.LightGray, modifier = Modifier.padding(16.dp)
    ) {
        Column {
            Text(text = "Simple Card", modifier = Modifier.padding(16.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Compose", modifier = Modifier.padding(16.dp))
        }
    }
}