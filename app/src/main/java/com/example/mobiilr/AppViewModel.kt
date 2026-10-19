package com.example.mobiilr

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class AppUiState(
    val message: String = "Hello Android!",
    val clickCount: Int = 0,
    val photo: Bitmap? = null
)

//Jagatud ViewModel. Olekut saab muuta ainult siin (MutableStateFlow on privaatne)
// ekraanid näevad seda ainult lugemiseks (StateFlow).

class AppViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(AppUiState())
    val uiState: StateFlow<AppUiState> = _uiState.asStateFlow()

    //Syndmus: kasutaja vajutas avaekraanil nuppu "Click me"
    fun onGreetClicked() {
        _uiState.update { state ->
            state.copy(
                message = "Hello from our app!",
                clickCount = state.clickCount + 1
            )
        }
    }

    //Syndmus: kaamerast tuli pilt
    fun onPhotoTaken(photo: Bitmap) {
        _uiState.update { it.copy(photo = photo) }
    }
}