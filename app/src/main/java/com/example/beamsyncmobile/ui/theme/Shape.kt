package com.example.beamsyncmobile.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// ──────────────────────────────────────────────
// BeamSync Shape System
// All corners are sharp (0dp rounded) to signal
// precision and industrial reliability
// ──────────────────────────────────────────────

object BeamsyncShapes {
    val sharp = RoundedCornerShape(0.dp)
    val card = RoundedCornerShape(0.dp)
    val button = RoundedCornerShape(0.dp)
    val dialog = RoundedCornerShape(0.dp)
    val bottomSheet = RoundedCornerShape(0.dp)
    val input = RoundedCornerShape(0.dp)
    val chip = RoundedCornerShape(0.dp)
    val progressBar = RoundedCornerShape(0.dp)
}

val Shapes = Shapes(
    small = BeamsyncShapes.sharp,
    medium = BeamsyncShapes.sharp,
    large = BeamsyncShapes.sharp,
    extraLarge = BeamsyncShapes.sharp,
)
