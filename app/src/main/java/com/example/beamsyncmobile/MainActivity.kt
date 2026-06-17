package com.example.beamsyncmobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.beamsyncmobile.ui.navigation.BeamsyncNavGraph
import com.example.beamsyncmobile.ui.theme.BeamSyncMobileTheme
import com.example.beamsyncmobile.ui.theme.ThemeManager

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        ThemeManager.init(this)
        enableEdgeToEdge()
        setContent {
            BeamSyncMobileTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    BeamsyncNavGraph()
                }
            }
        }
    }
}
