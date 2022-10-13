package com.example.movieapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.movieapp.ui.theme.MovieAppTheme
import kotlinx.coroutines.launch

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
        val scope = rememberCoroutineScope()//??
        val scaffoldState = rememberScaffoldState()//??
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(text = "Title text") },
                    navigationIcon = {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    },
                    actions = {
                        Icon(imageVector = Icons.Default.Favorite, contentDescription = "Favorite")
                        Icon(imageVector = Icons.Default.Search, contentDescription = "Search")
                    }
                )
            }, bottomBar = {
                BottomAppBar(
                    Modifier.height(100.dp),
                    content = {
                        Box(
                            modifier = Modifier
                                .background(Color.Black)
                                .height(50.dp),
                            content = {
                                Text(modifier = Modifier.align(Alignment.Center), text = "아무거나 다 넣어버려~", color = Color.White)
                            }
                        )
                    }
                )
            },
            scaffoldState = scaffoldState,
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {
                        scope.launch {
                            scaffoldState.snackbarHostState.showSnackbar(message = "Hello! Snackbar")
                        }
                    },
                    Modifier.size(200.dp, 100.dp),
                    shape = RoundedCornerShape(20.dp),
                    content = {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Favorite"
                        )
                    }
                )
            }, floatingActionButtonPosition = FabPosition.Center
        ) {
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