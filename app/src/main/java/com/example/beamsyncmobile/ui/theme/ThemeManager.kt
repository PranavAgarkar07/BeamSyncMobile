package com.example.beamsyncmobile.ui.theme

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

enum class ThemeMode { SYSTEM, LIGHT, DARK }

object ThemeManager {
    private const val PREFS_NAME = "beam_sync_theme"
    private const val KEY_MODE = "theme_mode"

    private var prefs: android.content.SharedPreferences? = null

    var mode: ThemeMode by mutableStateOf(ThemeMode.SYSTEM)
        private set

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val ordinal = prefs?.getInt(KEY_MODE, ThemeMode.SYSTEM.ordinal) ?: ThemeMode.SYSTEM.ordinal
        mode = ThemeMode.entries[ordinal]
    }

    fun changeMode(newMode: ThemeMode) {
        mode = newMode
        prefs?.edit()?.putInt(KEY_MODE, newMode.ordinal)?.apply()
    }
}
