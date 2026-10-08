package com.example.mobiilr
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize

@Composable
fun HomeScreen(onOpenDetail: () -> Unit, modifier: Modifier = Modifier) {
    var message by remember { mutableStateOf("Hello Android!") }

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(message)
        Button(onClick = { message = "Hello from our app!" }) {
            Text("Click me")
        }
        Button(onClick = onOpenDetail) {
            Text("Open detail")
        }
    }
}