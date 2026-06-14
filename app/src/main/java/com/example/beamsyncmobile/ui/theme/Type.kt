package com.example.beamsyncmobile.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

object BeamsyncFonts {
    // Using system monospace as JetBrains Mono fallback during development
    // When fonts are bundled, switch to FontFamily(Font(R.font.jetbrains_mono_*))
    val monospace = FontFamily.Monospace
    val sansSerif = FontFamily.SansSerif
}

object BeamsyncTextStyles {
    // Data / Monospace styles
    val dataXs = TextStyle(
        fontFamily = BeamsyncFonts.monospace,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp
    )
    val dataSm = TextStyle(
        fontFamily = BeamsyncFonts.monospace,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    )
    val dataBase = TextStyle(
        fontFamily = BeamsyncFonts.monospace,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp
    )
    val dataLg = TextStyle(
        fontFamily = BeamsyncFonts.monospace,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 28.sp
    )
    val dataXl = TextStyle(
        fontFamily = BeamsyncFonts.monospace,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 36.sp
    )
    val data2xl = TextStyle(
        fontFamily = BeamsyncFonts.monospace,
        fontWeight = FontWeight.Bold,
        fontSize = 36.sp,
        lineHeight = 44.sp
    )

    // Display / Hero styles
    val displayBase = TextStyle(
        fontFamily = BeamsyncFonts.monospace,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 36.sp,
        lineHeight = 44.sp
    )
    val displayLg = TextStyle(
        fontFamily = BeamsyncFonts.monospace,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 48.sp,
        lineHeight = 56.sp
    )
    val hero = TextStyle(
        fontFamily = BeamsyncFonts.monospace,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 64.sp,
        lineHeight = 72.sp
    )
}

val Typography = Typography(
    displayLarge = BeamsyncTextStyles.hero,
    displayMedium = BeamsyncTextStyles.displayLg,
    displaySmall = BeamsyncTextStyles.displayBase,
)
