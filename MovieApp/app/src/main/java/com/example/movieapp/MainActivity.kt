package com.example.movieapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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
        val scope = rememberCoroutineScope()//??
        val scaffoldState = rememberScaffoldState()//??
        Scaffold(topBar = {
            TopAppBar(backgroundColor = Color.Gray,
                elevation = 5.dp,
                title = { Text(text = "Moives") },
                navigationIcon = {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                },
                actions = {
                    Icon(imageVector = Icons.Default.Favorite, contentDescription = "Favorite")
                    Icon(imageVector = Icons.Default.Search, contentDescription = "Search")
                }
            )
        }) {
            content()
        }
    }
}

@Composable
fun MainContent(movieList: List<String> = listOf("Avatar", "300", "Harry Potter", "Life")) {
    Column(modifier = Modifier.padding(12.dp)) {
        LazyColumn {
            items(items = movieList) {
                MovieRow(movie = it)
            }
        }
    }
}

@Composable
fun MovieRow(movie: String) {
    Card(
        modifier = Modifier
            .padding(4.dp)
            .fillMaxWidth()
            .height(120.dp),
        shape = RoundedCornerShape(corner = CornerSize(14.dp)),
        elevation = 5.dp
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Surface(
                modifier = Modifier
                    .padding(12.dp)
                    .size(100.dp),
                shape = RoundedCornerShape(corner = CornerSize(14.dp)),
                elevation = 4.dp
            ) {
                Icon(imageVector = Icons.Default.AccountBox, contentDescription = "Movie Image")
            }
            Text(text = movie)
        }
    }
}
