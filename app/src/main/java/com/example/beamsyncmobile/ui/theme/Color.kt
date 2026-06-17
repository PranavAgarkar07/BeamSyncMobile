package com.example.beamsyncmobile.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.ColorScheme

// ── Legacy raw palette — components reference these directly ─────────────
object BeamsyncColors {
    // Light surfaces
    val surfaceBase    = Color(0xFFF8FBF8)
    val surfaceRaised  = Color(0xFFFFFFFF)
    val surfaceHigher  = Color(0xFFFFFFFF)
    val surfaceOverlay = Color(0xFFFFFFFF)
    val overlayDim     = Color(0x33000000)

    // Green accent palette
    val accentPrimary   = Color(0xFF2E7D32)
    val accentSecondary = Color(0xFF7C5E00)
    val accentTertiary  = Color(0xFFA35100)

    // Green gradient
    val accentGradientStart = Color(0xFF2E7D32)
    val accentGradientMid   = Color(0xFF4CAF50)
    val accentGradientEnd   = Color(0xFF66BB6A)

    // Semantic
    val surfacePositive  = Color(0xFF2E7D32)
    val surfaceCritical  = Color(0xFFD32F2F)
    val surfaceWarning   = Color(0xFFD4A017)

    // Text — light mode
    val textPrimary   = Color(0xFF1C1B1F)
    val textSecondary = Color(0xFF49454F)
    val textDisabled  = Color(0xFFCAC4D0)
    val textAccent    = Color(0xFF2E7D32)
    val textGreen     = Color(0xFF2E7D32)
    val textError     = Color(0xFFD32F2F)

    // Strokes
    val strokeDefault = Color(0xFFCAC4D0)
    val strokeActive  = Color(0xFF2E7D32)
    val strokeGlow    = Color(0x332E7D32)

    // Glows
    val glowPrimary   = Color(0x332E7D32)
    val glowGold      = Color(0x267C5E00)
    val glowPositive  = Color(0x262E7D32)
}

// ── M3 Light Color Scheme — Green Tonal Palette ──────────────────────────
// Source colors:
//   Primary   = #2E7D32 (Forest Green)
//   Secondary = #7C5E00 (Warm Gold)
//   Tertiary  = #A35100 (Warm Amber)
//
// Light scheme tone mapping (M3 standard):
//   *Accent       T40  — medium vibrant accent
//   On*Accent     T100 — white text on accent
//   *Container    T90  — light tinted container background
//   On*Container  T10  — very dark readable text on container
//   Surface       T98  — near-white
//   OnSurface     T10  — near-black text
//   Outline       T50  — visible borders
//
// Research basis:
//   Green reduces visual fatigue and improves reading performance
//   compared to white backgrounds (Frontiers in Psychology, 2025).
//   Green also signals calm, safety, and growth — ideal for a
//   file-transfer app where users monitor progress for extended periods.

private val primary         = Color(0xFF2E7D32)
private val onPrimary       = Color(0xFFFFFFFF)
private val primaryContainer = Color(0xFFC8E6C9)
private val onPrimaryContainer = Color(0xFF002106)

private val secondary         = Color(0xFF7C5E00)
private val onSecondary       = Color(0xFFFFFFFF)
private val secondaryContainer = Color(0xFFFFEAA7)
private val onSecondaryContainer = Color(0xFF271900)

private val tertiary         = Color(0xFFA35100)
private val onTertiary       = Color(0xFFFFFFFF)
private val tertiaryContainer = Color(0xFFFFDCC2)
private val onTertiaryContainer = Color(0xFF361400)

private val error         = Color(0xFFBA1A1A)
private val onError       = Color(0xFFFFFFFF)
private val errorContainer = Color(0xFFFFDAD6)
private val onErrorContainer = Color(0xFF410002)

private val background   = Color(0xFFFEF9F0)
private val onBackground = Color(0xFF1C1B1F)

private val surface          = Color(0xFFFFFFFF)
private val onSurface        = Color(0xFF1C1B1F)
private val surfaceVariant   = Color(0xFFDEE4D8)
private val onSurfaceVariant = Color(0xFF424940)

private val outline        = Color(0xFF72796C)
private val outlineVariant = Color(0xFFC2C9BC)

private val scrim          = Color(0xFF000000)
private val inverseSurface   = Color(0xFF313033)
private val inverseOnSurface = Color(0xFFF4EFF4)
private val inversePrimary   = Color(0xFF81C784)
private val surfaceTint      = primary

val BeamsyncColorScheme: ColorScheme = lightColorScheme(
    primary              = primary,
    onPrimary            = onPrimary,
    primaryContainer     = primaryContainer,
    onPrimaryContainer   = onPrimaryContainer,
    secondary            = secondary,
    onSecondary          = onSecondary,
    secondaryContainer   = secondaryContainer,
    onSecondaryContainer = onSecondaryContainer,
    tertiary             = tertiary,
    onTertiary           = onTertiary,
    tertiaryContainer    = tertiaryContainer,
    onTertiaryContainer  = onTertiaryContainer,
    error                = error,
    onError              = onError,
    errorContainer       = errorContainer,
    onErrorContainer     = onErrorContainer,
    background           = background,
    onBackground         = onBackground,
    surface              = surface,
    onSurface            = onSurface,
    surfaceVariant       = surfaceVariant,
    onSurfaceVariant     = onSurfaceVariant,
    outline              = outline,
    outlineVariant       = outlineVariant,
    scrim                = scrim,
    inverseSurface       = inverseSurface,
    inverseOnSurface     = inverseOnSurface,
    inversePrimary       = inversePrimary,
    surfaceTint          = surfaceTint,
    surfaceDim           = Color(0xFFDFDAD5),
    surfaceBright        = Color(0xFFFEF9F0),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow    = Color(0xFFF6F2EC),
    surfaceContainer       = Color(0xFFF0ECE6),
    surfaceContainerHigh   = Color(0xFFEAE6E0),
    surfaceContainerHighest = Color(0xFFDFDBD5),
)
