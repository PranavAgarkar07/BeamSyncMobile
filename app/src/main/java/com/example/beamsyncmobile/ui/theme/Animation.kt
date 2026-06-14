package com.example.beamsyncmobile.ui.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween

object BeamsyncDurations {
    val press: Int = 80
    val fast: Int = 150
    val normal: Int = 200
    val slow: Int = 300
    val verySlow: Int = 400
}

object BeamsyncEasing {
    val snapDecelerate = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f)
    val linear = LinearEasing
}

object BeamsyncAnimationSpec {
    fun press(): FiniteAnimationSpec<Float> = tween(
        durationMillis = BeamsyncDurations.press,
        easing = BeamsyncEasing.linear,
    )

    fun cardEnter(): FiniteAnimationSpec<Float> = tween(
        durationMillis = BeamsyncDurations.normal,
        easing = BeamsyncEasing.snapDecelerate,
    )

    fun progressStep(): FiniteAnimationSpec<Float> = tween(
        durationMillis = BeamsyncDurations.slow,
        easing = BeamsyncEasing.linear,
    )

    fun dialogScale(): FiniteAnimationSpec<Float> = tween(
        durationMillis = BeamsyncDurations.fast,
        easing = BeamsyncEasing.snapDecelerate,
    )

    fun scrimFade(): FiniteAnimationSpec<Float> = tween(
        durationMillis = BeamsyncDurations.normal,
        easing = BeamsyncEasing.linear,
    )

    fun screenTransition(): FiniteAnimationSpec<Float> = tween(
        durationMillis = BeamsyncDurations.normal,
        easing = BeamsyncEasing.snapDecelerate,
    )
}
