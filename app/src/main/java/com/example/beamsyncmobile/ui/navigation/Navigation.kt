package com.example.beamsyncmobile.ui.navigation

sealed class Screen(val route: String, val label: String) {
    data object Scan : Screen("scan", "SCAN")
    data object Connection : Screen("connection/{scheme}/{host}/{port}/{token}", "CONNECTED")
    data object Downloads : Screen("downloads", "RECEIVE")
    data object Uploads : Screen("uploads", "SEND")
    data object Settings : Screen("settings", "SETTINGS")
}

data class BottomNavItem(val screen: Screen, val label: String)

object Screens {
    val items = listOf(
        BottomNavItem(Screen.Scan, "SCAN"),
        BottomNavItem(Screen.Downloads, "RECEIVE"),
        BottomNavItem(Screen.Uploads, "SEND"),
        BottomNavItem(Screen.Settings, "SETTINGS"),
    )
}
