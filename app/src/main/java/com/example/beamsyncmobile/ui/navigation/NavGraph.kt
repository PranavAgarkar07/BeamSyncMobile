package com.example.beamsyncmobile.ui.navigation

import android.content.Context
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.beamsyncmobile.network.CurrentConnection
import com.example.beamsyncmobile.network.ServerConnection
import com.example.beamsyncmobile.ui.screens.connection.ConnectionScreen
import com.example.beamsyncmobile.ui.screens.downloads.DownloadsScreen
import com.example.beamsyncmobile.ui.screens.home.HomeScreen
import com.example.beamsyncmobile.ui.screens.onboarding.OnboardingScreen
import com.example.beamsyncmobile.ui.screens.settings.SettingsScreen
import com.example.beamsyncmobile.ui.screens.uploads.UploadsScreen

private const val PREFS_NAME = "beamsync_prefs"
private const val KEY_ONBOARDING_DONE = "onboarding_complete"

@Composable
fun BeamsyncNavGraph() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) }
    var onboardingDone by remember { mutableStateOf(prefs.getBoolean(KEY_ONBOARDING_DONE, false)) }

    if (!onboardingDone) {
        OnboardingScreen(
            onComplete = {
                prefs.edit().putBoolean(KEY_ONBOARDING_DONE, true).apply()
                onboardingDone = true
            },
        )
        return
    }

    val navController = rememberNavController()
    var selectedScreen by remember { mutableStateOf<Screen>(Screen.Scan) }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
            ) {
                Screens.items.forEach { item ->
                    val isSelected = item.screen == selectedScreen
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            selectedScreen = item.screen
                            navController.navigate(item.screen.route) {
                                popUpTo(Screen.Scan.route) { inclusive = true }
                                launchSingleTop = true
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = item.screen.icon,
                                contentDescription = item.label,
                            )
                        },
                        label = {
                            Text(
                                text = item.label,
                                style = MaterialTheme.typography.labelSmall,
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                        ),
                    )
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Scan.route,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            composable(Screen.Scan.route) {
                selectedScreen = Screen.Scan
                HomeScreen(
                    onConnected = { connection ->
                        CurrentConnection.set(connection)
                        navController.navigate(
                            "connection/${connection.scheme}/${connection.host}/${connection.port}/${connection.token}"
                        ) {
                            popUpTo(Screen.Scan.route) { inclusive = false }
                        }
                    },
                )
            }

            composable(
                route = Screen.Connection.route,
                arguments = listOf(
                    navArgument("scheme") { type = NavType.StringType },
                    navArgument("host") { type = NavType.StringType },
                    navArgument("port") { type = NavType.IntType },
                    navArgument("token") { type = NavType.StringType },
                ),
            ) { backStackEntry ->
                selectedScreen = Screen.Connection
                val connection = ServerConnection(
                    scheme = backStackEntry.arguments?.getString("scheme") ?: "http",
                    host = backStackEntry.arguments?.getString("host") ?: "",
                    port = backStackEntry.arguments?.getInt("port") ?: 3000,
                    token = backStackEntry.arguments?.getString("token") ?: "",
                )
                ConnectionScreen(
                    navController = navController,
                    connection = connection,
                )
            }

            composable(Screen.Downloads.route) {
                selectedScreen = Screen.Downloads
                DownloadsScreen()
            }

            composable(Screen.Uploads.route) {
                selectedScreen = Screen.Uploads
                UploadsScreen()
            }

            composable(Screen.Settings.route) {
                selectedScreen = Screen.Settings
                SettingsScreen()
            }
        }
    }
}
