package com.example.mobiilr

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text



@Composable
fun AppNav() {
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
                HomeScreen(onOpenDetail = { navController.navigate("detail/42") })
            }
            composable("detail/{id}") { entry ->
                DetailScreen(
                    id = entry.arguments?.getString("id") ?: "",
                    onBack = { navController.popBackStack() }
                )
            }
            composable("settings") {
                SettingsScreen()
            }
        }
    }
}

private fun NavController.goTab(r: String) = navigate(r) {
    popUpTo(graph.findStartDestination().id) { saveState = true }
    launchSingleTop = true
    restoreState = true
}
