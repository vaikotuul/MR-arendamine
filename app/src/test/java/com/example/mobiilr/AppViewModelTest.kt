package com.example.mobiilr

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

// AppViewModel'i ühiktestid – jooksevad arvutis (JVM), seadet pole vaja.
class AppViewModelTest {

    private lateinit var viewModel: AppViewModel

    @Before
    fun setUp() {
        viewModel = AppViewModel()
    }

    @Test
    fun algolek_onVaikevaartustega() {
        val state = viewModel.uiState.value
        assertEquals("Hello Android!", state.message)
        assertEquals(0, state.clickCount)
    }

    @Test
    fun onGreetClicked_muudabSonumitJaSuurendabLoendurit() {
        viewModel.onGreetClicked()

        val state = viewModel.uiState.value
        assertEquals("Hello from our app!", state.message)
        assertEquals(1, state.clickCount)
    }

    @Test
    fun onGreetClicked_mitmeKordselVajutamiselLoendurKasvab() {
        repeat(3) { viewModel.onGreetClicked() }

        val state = viewModel.uiState.value
        assertEquals("Hello from our app!", state.message)
        assertEquals(3, state.clickCount)
    }

    @Test
    fun uusViewModel_alustabPuhtaltOlekult() {
        viewModel.onGreetClicked()

        // Iga ViewModel'i eksemplar hoiab oma olekut eraldi
        val teine = AppViewModel()
        assertEquals(0, teine.uiState.value.clickCount)
        assertEquals("Hello Android!", teine.uiState.value.message)
    }
}
