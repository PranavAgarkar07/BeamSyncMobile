package com.example.beamsyncmobile.ui.navigation

sealed class Screen(val route: String) {
    data object Permissions : Screen("permissions")
    data object Home : Screen("home")
    data object History : Screen("history")
    data object Settings : Screen("settings")
    data object About : Screen("about")
    data object QrScanner : Screen("qrScanner/{mode}") {
        fun createRoute(mode: String) = "qrScanner/$mode"
    }
    data object Downloads : Screen("downloads")
    data object Uploads : Screen("uploads")
}
