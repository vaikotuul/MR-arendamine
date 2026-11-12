package com.example.mobiilr

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class AppUiState(
    val message: String = "Hello Android!",
    val clickCount: Int = 0,
    val darkTheme: Boolean = false, // salvestatud SharedPreferences'isse
    val note: String = ""           // salvestatud faili note.txt
)

//Jagatud ViewModel. Olekut saab muuta ainult siin (MutableStateFlow on privaatne)
// ekraanid näevad seda ainult lugemiseks (StateFlow).
// Kettale kirjutamine on Storage.kt failis – ViewModel hoiab ainult ekraanil näidatavat olekut.

class AppViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(AppUiState())
    val uiState: StateFlow<AppUiState> = _uiState.asStateFlow()
    private var loaded = false

    //Syndmus: kasutaja vajutas avaekraanil nuppu "Click me"
    fun onGreetClicked() {
        _uiState.update { state ->
            state.copy(
                message = "Hello from our app!",
                clickCount = state.clickCount + 1
            )
        }
    }

    //Syndmus: rakenduse käivitamisel loeti salvestatud andmed. Ekraani pööramisel ei loeta uuesti.
    fun onStorageLoaded(darkTheme: Boolean, note: String) {
        if (loaded) return
        loaded = true
        _uiState.update { it.copy(darkTheme = darkTheme, note = note) }
    }

    //Syndmus: kasutaja muutis teemat
    fun onDarkThemeChanged(dark: Boolean) {
        _uiState.update { it.copy(darkTheme = dark) }
    }

    //Syndmus: märge salvestati faili
    fun onNoteSaved(note: String) {
        _uiState.update { it.copy(note = note) }
    }
}