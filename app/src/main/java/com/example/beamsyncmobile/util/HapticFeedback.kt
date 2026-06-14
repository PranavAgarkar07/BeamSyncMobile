package com.example.beamsyncmobile.util

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

object BeamsyncHapticFeedback {
    private fun getVibrator(context: Context): Vibrator {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            manager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }

    fun click(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getVibrator(context).vibrate(
                VibrationEffect.createOneShot(10, VibrationEffect.DEFAULT_AMPLITUDE)
            )
        }
    }

    fun doubleClick(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getVibrator(context).vibrate(
                VibrationEffect.createWaveform(
                    longArrayOf(0, 15, 20, 15),
                    intArrayOf(0, VibrationEffect.DEFAULT_AMPLITUDE, 0, VibrationEffect.DEFAULT_AMPLITUDE),
                    -1,
                )
            )
        }
    }

    fun success(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getVibrator(context).vibrate(
                VibrationEffect.createWaveform(
                    longArrayOf(0, 30, 20, 50),
                    intArrayOf(0, VibrationEffect.DEFAULT_AMPLITUDE, 0, 128),
                    -1,
                )
            )
        }
    }

    fun error(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getVibrator(context).vibrate(
                VibrationEffect.createOneShot(400, VibrationEffect.DEFAULT_AMPLITUDE)
            )
        }
    }

    fun qrScanned(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getVibrator(context).vibrate(
                VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE)
            )
        }
    }
}

@Composable
fun rememberHaptic(): BeamsyncHapticFeedback {
    return BeamsyncHapticFeedback
}
