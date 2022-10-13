package com.example.movieapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.movieapp.ui.theme.MovieAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApp() {
                MainContent()
            }
        }
    }
}

@Preview
@Composable
fun Test() {
    MyApp {
        MainContent()
    }
}

@Composable
fun MyApp(content: @Composable () -> Unit) {
    MovieAppTheme {
        Scaffold( topBar = {
            TopAppBar(
                title = { Text(text = "Title text") },
                navigationIcon = {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                },
                actions = {
                    Icon(imageVector = Icons.Default.Favorite, contentDescription = "Favorite")
                    Icon( imageVector = Icons.Default.Search, contentDescription = "Search")
                }
            )
        },) {
            content()
        }
    }
}

@Composable
fun MainContent() {
    // A surface container using the 'background' color from the theme
    Surface(color = MaterialTheme.colors.background) {
        Text(text = "Hello")
    }
}