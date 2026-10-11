package com.example.mobiilr

import android.content.Intent
import android.net.Uri
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

// Navigatsiooni UI testid – käivitavad päris rakenduse seadmes (emulaator või telefon).
@RunWith(AndroidJUnit4::class)
class NavigationTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun rakendusAvanebAvaekraanil() {
        composeRule.onNodeWithText("Hello Android!").assertIsDisplayed()
        composeRule.onNodeWithText("Open detail").assertIsDisplayed()
    }

    @Test
    fun openDetail_viibDetailvaatesse() {
        composeRule.onNodeWithText("Open detail").performClick()

        composeRule.onNodeWithText("Detail 42").assertIsDisplayed()
    }

    @Test
    fun backNupp_viibTagasiAvaekraanile() {
        composeRule.onNodeWithText("Open detail").performClick()
        composeRule.onNodeWithText("Back").performClick()

        composeRule.onNodeWithText("Open detail").assertIsDisplayed()
    }

    @Test
    fun settingsVaheleht_naitabJagatudOlekut() {
        // Avaekraanil vajutus muudab ViewModel'i olekut, Settings peab sama olekut nägema
        composeRule.onNodeWithText("Click me").performClick()
        composeRule.onNodeWithText("Settings").performClick()

        composeRule.onNodeWithText("Home button clicked 1 times").assertIsDisplayed()
    }

    @Test
    fun homeVaheleht_viibSettingsistTagasi() {
        composeRule.onNodeWithText("Settings").performClick()
        composeRule.onNodeWithText("Home").performClick()

        composeRule.onNodeWithText("Open detail").assertIsDisplayed()
    }
}

// Sügavlingi test – rakendus avatakse URL-iga myapp://detail/{id}, mitte nupuga.
@RunWith(AndroidJUnit4::class)
class DeepLinkTest {

    @get:Rule
    val composeRule = createEmptyComposeRule()

    @Test
    fun sugavlink_avabDetailvaateOigeId_ga() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("myapp://detail/5"), context, MainActivity::class.java)

        ActivityScenario.launch<MainActivity>(intent).use {
            composeRule.onNodeWithText("Detail 5").assertIsDisplayed()
        }
    }
}
