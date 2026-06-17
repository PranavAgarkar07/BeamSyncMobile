package com.example.beamsyncmobile.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.ColorScheme

// ── M3 Light Color Scheme ────────────────────────────────────────────────
private val lightPrimary         = Color(0xFF2E7D32)
private val lightOnPrimary       = Color(0xFFFFFFFF)
private val lightPrimaryContainer = Color(0xFFC8E6C9)
private val lightOnPrimaryContainer = Color(0xFF002106)
private val lightSecondary         = Color(0xFF7C5E00)
private val lightOnSecondary       = Color(0xFFFFFFFF)
private val lightSecondaryContainer = Color(0xFFFFEAA7)
private val lightOnSecondaryContainer = Color(0xFF271900)
private val lightTertiary         = Color(0xFFA35100)
private val lightOnTertiary       = Color(0xFFFFFFFF)
private val lightTertiaryContainer = Color(0xFFFFDCC2)
private val lightOnTertiaryContainer = Color(0xFF361400)
private val lightError         = Color(0xFFBA1A1A)
private val lightOnError       = Color(0xFFFFFFFF)
private val lightErrorContainer = Color(0xFFFFDAD6)
private val lightOnErrorContainer = Color(0xFF410002)
private val lightBackground   = Color(0xFFFEF9F0)
private val lightOnBackground = Color(0xFF1C1B1F)
private val lightSurface          = Color(0xFFFFFFFF)
private val lightOnSurface        = Color(0xFF1C1B1F)
private val lightSurfaceVariant   = Color(0xFFDEE4D8)
private val lightOnSurfaceVariant = Color(0xFF424940)
private val lightOutline        = Color(0xFF72796C)
private val lightOutlineVariant = Color(0xFFC2C9BC)
private val lightScrim          = Color(0xFF000000)
private val lightInverseSurface   = Color(0xFF313033)
private val lightInverseOnSurface = Color(0xFFF4EFF4)
private val lightInversePrimary   = Color(0xFF81C784)
private val lightSurfaceTint      = lightPrimary

val BeamsyncColorScheme: ColorScheme = lightColorScheme(
    primary              = lightPrimary,
    onPrimary            = lightOnPrimary,
    primaryContainer     = lightPrimaryContainer,
    onPrimaryContainer   = lightOnPrimaryContainer,
    secondary            = lightSecondary,
    onSecondary          = lightOnSecondary,
    secondaryContainer   = lightSecondaryContainer,
    onSecondaryContainer = lightOnSecondaryContainer,
    tertiary             = lightTertiary,
    onTertiary           = lightOnTertiary,
    tertiaryContainer    = lightTertiaryContainer,
    onTertiaryContainer  = lightOnTertiaryContainer,
    error                = lightError,
    onError              = lightOnError,
    errorContainer       = lightErrorContainer,
    onErrorContainer     = lightOnErrorContainer,
    background           = lightBackground,
    onBackground         = lightOnBackground,
    surface              = lightSurface,
    onSurface            = lightOnSurface,
    surfaceVariant       = lightSurfaceVariant,
    onSurfaceVariant     = lightOnSurfaceVariant,
    outline              = lightOutline,
    outlineVariant       = lightOutlineVariant,
    scrim                = lightScrim,
    inverseSurface       = lightInverseSurface,
    inverseOnSurface     = lightInverseOnSurface,
    inversePrimary       = lightInversePrimary,
    surfaceTint          = lightSurfaceTint,
    surfaceDim           = Color(0xFFDFDAD5),
    surfaceBright        = Color(0xFFFEF9F0),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow    = Color(0xFFF6F2EC),
    surfaceContainer       = Color(0xFFF0ECE6),
    surfaceContainerHigh   = Color(0xFFEAE6E0),
    surfaceContainerHighest = Color(0xFFDFDBD5),
)

// ── M3 Dark Color Scheme ─────────────────────────────────────────────────
private val darkPrimary         = Color(0xFF81C784)
private val darkOnPrimary       = Color(0xFF003910)
private val darkPrimaryContainer = Color(0xFF1B5E20)
private val darkOnPrimaryContainer = Color(0xFFC8E6C9)
private val darkSecondary         = Color(0xFFFFD54F)
private val darkOnSecondary       = Color(0xFF423000)
private val darkSecondaryContainer = Color(0xFF5E4600)
private val darkOnSecondaryContainer = Color(0xFFFFEAA7)
private val darkTertiary         = Color(0xFFFFB74D)
private val darkOnTertiary       = Color(0xFF502700)
private val darkTertiaryContainer = Color(0xFF753800)
private val darkOnTertiaryContainer = Color(0xFFFFDCC2)
private val darkError         = Color(0xFFFFB4AB)
private val darkOnError       = Color(0xFF690005)
private val darkErrorContainer = Color(0xFF93000A)
private val darkOnErrorContainer = Color(0xFFFFDAD6)
private val darkBackground   = Color(0xFF1B1C1E)
private val darkOnBackground = Color(0xFFE2E2E6)
private val darkSurface          = Color(0xFF1B1C1E)
private val darkOnSurface        = Color(0xFFE2E2E6)
private val darkSurfaceVariant   = Color(0xFF424940)
private val darkOnSurfaceVariant = Color(0xFFC2C9BC)
private val darkOutline        = Color(0xFF8C9386)
private val darkOutlineVariant = Color(0xFF424940)
private val darkScrim          = Color(0xFF000000)
private val darkInverseSurface   = Color(0xFFE2E2E6)
private val darkInverseOnSurface = Color(0xFF313033)
private val darkInversePrimary   = Color(0xFF2E7D32)
private val darkSurfaceTint      = darkPrimary

val BeamsyncDarkColorScheme: ColorScheme = darkColorScheme(
    primary              = darkPrimary,
    onPrimary            = darkOnPrimary,
    primaryContainer     = darkPrimaryContainer,
    onPrimaryContainer   = darkOnPrimaryContainer,
    secondary            = darkSecondary,
    onSecondary          = darkOnSecondary,
    secondaryContainer   = darkSecondaryContainer,
    onSecondaryContainer = darkOnSecondaryContainer,
    tertiary             = darkTertiary,
    onTertiary           = darkOnTertiary,
    tertiaryContainer    = darkTertiaryContainer,
    onTertiaryContainer  = darkOnTertiaryContainer,
    error                = darkError,
    onError              = darkOnError,
    errorContainer       = darkErrorContainer,
    onErrorContainer     = darkOnErrorContainer,
    background           = darkBackground,
    onBackground         = darkOnBackground,
    surface              = darkSurface,
    onSurface            = darkOnSurface,
    surfaceVariant       = darkSurfaceVariant,
    onSurfaceVariant     = darkOnSurfaceVariant,
    outline              = darkOutline,
    outlineVariant       = darkOutlineVariant,
    scrim                = darkScrim,
    inverseSurface       = darkInverseSurface,
    inverseOnSurface     = darkInverseOnSurface,
    inversePrimary       = darkInversePrimary,
    surfaceTint          = darkSurfaceTint,
    surfaceDim           = Color(0xFF121314),
    surfaceBright        = Color(0xFF414345),
    surfaceContainerLowest = Color(0xFF161719),
    surfaceContainerLow    = Color(0xFF232527),
    surfaceContainer       = Color(0xFF27292B),
    surfaceContainerHigh   = Color(0xFF323436),
    surfaceContainerHighest = Color(0xFF3D3F41),
)
