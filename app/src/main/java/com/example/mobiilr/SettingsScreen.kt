package com.example.mobiilr

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen(
    uiState: AppUiState,
    onDarkThemeChange: (Boolean) -> Unit,
    onSaveNote: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // Tekstikasti sisu; algväärtus on failist loetud märge
    var text by remember { mutableStateOf(uiState.note) }

    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically)
    ) {
        Text("Settings")
        Text("Home button clicked ${uiState.clickCount} times")

        // 1) Seade, mis salvestub SharedPreferences'isse
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Dark theme", modifier = Modifier.padding(end = 8.dp))
            Switch(checked = uiState.darkTheme, onCheckedChange = onDarkThemeChange)
        }

        // 2) Tekst, mis salvestub faili note.txt
        OutlinedTextField(value = text, onValueChange = { text = it }, label = { Text("Note") })
        Button(onClick = { onSaveNote(text) }) { Text("Save note") }
        Text("Saved note: ${uiState.note}")
    }
}