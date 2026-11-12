package com.example.mobiilr

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mobiilr.ui.theme.MobiilrTheme

class MainActivity : ComponentActivity() {
    private val appViewModel: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Käivitamisel loeme salvestatud seade ja märke (väikesed, loetakse kohe)
        appViewModel.onStorageLoaded(darkTheme = loadDarkTheme(this), note = loadNote(this))

        setContent {
            val uiState by appViewModel.uiState.collectAsStateWithLifecycle()
            // Teema tuleb salvestatud seadest, mitte telefoni süsteemi seadest
            MobiilrTheme(darkTheme = uiState.darkTheme) {
                AppNav(appViewModel)
            }
        }
    }
}