package com.example.mobiilr
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize

@Composable
fun HomeScreen(
    uiState: AppUiState,
    onGreetClick: () -> Unit,
    onOpenDetail: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(uiState.message)
        Text("Clicked ${uiState.clickCount} times")
        Button(onClick = onGreetClick) {
            Text("Click me")
        }
        Button(onClick = onOpenDetail) {
            Text("Open detail")
        }
    }
}