package com.pyera.app.ui.util

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import com.pyera.app.ui.theme.AccentGreen
import kotlinx.coroutines.delay

/**
 * Animates a number counting up from 0 to the target value.
 * Perfect for balance displays and statistics.
 *
 * @param targetValue The final value to animate to
 * @param durationMs Animation duration in milliseconds
 * @param delayMs Initial delay before animation starts
 */
@Composable
fun animateCountUp(
    targetValue: Double,
    durationMs: Int = 1200,
    delayMs: Int = 300
): Double {
    var animatedValue by remember { mutableStateOf(0.0) }
    
    LaunchedEffect(targetValue) {
        delay(delayMs.toLong())
        val startTime = System.currentTimeMillis()
        val endTime = startTime + durationMs
        
        while (System.currentTimeMillis() < endTime) {
            val elapsed = System.currentTimeMillis() - startTime
            val progress = (elapsed.toFloat() / durationMs).coerceIn(0f, 1f)
            // Ease out cubic for smooth deceleration
            val easedProgress = 1 - (1 - progress) * (1 - progress) * (1 - progress)
            animatedValue = targetValue * easedProgress
            delay(16) // ~60fps
        }
        animatedValue = targetValue
    }
    
    return animatedValue
}

/**
 * Animates an integer counting up from 0 to the target value.
 */
@Composable
fun animateCountUpInt(
    targetValue: Int,
    durationMs: Int = 1000,
    delayMs: Int = 200
): Int {
    return animateCountUp(targetValue.toDouble(), durationMs, delayMs).toInt()
}

/**
 * Creates a staggered animation delay for list items.
 * Apply to each item in a list with its index.
 *
 * @param index The index of the item in the list
 * @param baseDelayMs Base delay before any animation
 * @param staggerMs Delay between each item
 */
@Composable
fun Modifier.staggeredSlideIn(
    index: Int,
    baseDelayMs: Int = 100,
    staggerMs: Int = 50,
    slideDistance: Float = 30f
): Modifier = composed {
    var visible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        delay((baseDelayMs + (index * staggerMs)).toLong())
        visible = true
    }
    
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "Stagger Alpha"
    )
    
    val offsetY by animateFloatAsState(
        targetValue = if (visible) 0f else slideDistance,
        animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
        label = "Stagger Offset"
    )
    
    this.graphicsLayer {
        this.alpha = alpha
        this.translationY = offsetY
    }
}

/**
 * Adds a subtle pulsing glow effect behind the element.
 * Great for primary action buttons to draw attention.
 *
 * @param glowColor The color of the glow
 * @param enabled Whether the glow animation is active
 */
@Composable
fun Modifier.pulseGlow(
    glowColor: Color = AccentGreen,
    enabled: Boolean = true
): Modifier = composed {
    if (!enabled) return@composed this
    
    val infiniteTransition = rememberInfiniteTransition(label = "Pulse Glow")
    
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Glow Alpha"
    )
    
    val glowScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Glow Scale"
    )
    
    this.drawBehind {
        drawCircle(
            color = glowColor.copy(alpha = glowAlpha),
            radius = size.minDimension / 2 * glowScale
        )
    }
}

/**
 * Fade-in animation on first composition.
 *
 * @param durationMs Duration of the fade animation
 * @param delayMs Delay before animation starts
 */
@Composable
fun Modifier.fadeInOnMount(
    durationMs: Int = 400,
    delayMs: Int = 0
): Modifier = composed {
    var visible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        delay(delayMs.toLong())
        visible = true
    }
    
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = durationMs, easing = FastOutSlowInEasing),
        label = "Fade In Alpha"
    )
    
    this.graphicsLayer { this.alpha = alpha }
}
