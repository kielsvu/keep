package com.keepr.ui.components

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer

private val MotionEasing = FastOutSlowInEasing

const val MotionPress = 90
const val MotionFast = 140
const val MotionState = 180
const val MotionEnter = 220
const val MotionExit = 150

fun pushEnter(): EnterTransition =
    fadeIn(tween(MotionEnter, easing = MotionEasing)) +
        slideInHorizontally(tween(MotionEnter, easing = MotionEasing)) { it / 18 } +
        scaleIn(tween(MotionEnter, easing = MotionEasing), initialScale = 0.985f)

fun pushExit(): ExitTransition =
    fadeOut(tween(MotionExit, easing = MotionEasing)) +
        slideOutHorizontally(tween(MotionExit, easing = MotionEasing)) { -it / 24 } +
        scaleOut(tween(MotionExit, easing = MotionEasing), targetScale = 0.995f)

fun popEnter(): EnterTransition =
    fadeIn(tween(MotionEnter, easing = MotionEasing)) +
        slideInHorizontally(tween(MotionEnter, easing = MotionEasing)) { -it / 22 } +
        scaleIn(tween(MotionEnter, easing = MotionEasing), initialScale = 0.99f)

fun popExit(): ExitTransition =
    fadeOut(tween(MotionExit, easing = MotionEasing)) +
        slideOutHorizontally(tween(MotionExit, easing = MotionEasing)) { it / 20 } +
        scaleOut(tween(MotionExit, easing = MotionEasing), targetScale = 0.995f)

fun enterFromBottom(delay: Int = 0): EnterTransition =
    fadeIn(tween(MotionEnter, delay, MotionEasing)) +
        slideInVertically(tween(MotionEnter, delay, MotionEasing)) { it / 12 } +
        scaleIn(tween(MotionEnter, delay, MotionEasing), initialScale = 0.988f)

fun exitToBottom(): ExitTransition =
    fadeOut(tween(MotionExit, easing = MotionEasing)) +
        slideOutVertically(tween(MotionExit, easing = MotionEasing)) { it / 14 } +
        scaleOut(tween(MotionExit, easing = MotionEasing), targetScale = 0.995f)

@Composable
fun Modifier.pressScale(
    pressedScale: Float = 0.985f,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
): Modifier {
    val pressed by interactionSource.collectIsPressedAsState()
    val scale = androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (pressed) pressedScale else 1f,
        animationSpec = spring(
            dampingRatio = 0.82f,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "press_scale"
    )
    return this.graphicsLayer {
        scaleX = scale.value
        scaleY = scale.value
    }
}
