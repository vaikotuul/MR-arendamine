package com.example.mobiilr

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import java.io.File
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink


@Composable
fun AppNav(appViewModel: AppViewModel = viewModel()) {
    // Üks ViewModel kogu rakenduse jaoks (Activity skoobis) – kõik ekraanid näevad sama olekut.
    val uiState by appViewModel.uiState.collectAsStateWithLifecycle()
    val navController = rememberNavController()
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = currentRoute != "settings",
                    onClick = { navController.goTab("home") },
                    icon = {},
                    label = { Text("Home") }
                )
                NavigationBarItem(
                    selected = currentRoute == "settings",
                    onClick = { navController.goTab("settings") },
                    icon = {},
                    label = { Text("Settings") }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("home") {
                // Süsteemi kaamerarakendus kirjutab täismõõdus pildi meie cache'i (üks fail, iga kord üle kirjutatud).
                // Android võib cache'i ise tühjendada, kui ruumi napib.
                val context = LocalContext.current
                val photoFile = remember { File(context.cacheDir, "photo.jpg") }
                val photoUri = remember { FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", photoFile) }
                val takePhoto = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { saved ->
                    val bitmap = if (saved) loadPhoto(photoFile) else null
                    if (bitmap != null) {
                        appViewModel.onPhotoTaken(bitmap)
                        navController.navigate("photo")
                    }
                }
                HomeScreen(
                    uiState = uiState,
                    onGreetClick = appViewModel::onGreetClicked,
                    onOpenDetail = { navController.navigate("detail/42") },
                    onTakePhoto = { takePhoto.launch(photoUri) }
                )
            }
            composable("photo") {
                PhotoScreen(photo = uiState.photo, onBack = { navController.popBackStack() })
            }
            // Sügavlink myapp://detail/{id} viib otse sellele ekraanile
            composable(
                route = "detail/{id}",
                arguments = listOf(navArgument("id") { type = NavType.StringType }),
                deepLinks = listOf(navDeepLink { uriPattern = "myapp://detail/{id}" })
            ) { entry ->
                DetailScreen(
                    id = entry.arguments?.getString("id") ?: "",
                    onBack = { navController.popBackStack() }
                )
            }
            composable("settings") {
                SettingsScreen(uiState = uiState)
            }
        }
    }
}

private fun NavController.goTab(r: String) = navigate(r) {
    popUpTo(graph.findStartDestination().id) { saveState = true }
    launchSingleTop = true
    restoreState = true
}