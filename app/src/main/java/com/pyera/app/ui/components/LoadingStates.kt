package com.pyera.app.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.pyera.app.ui.theme.tokens.ColorTokens
import com.pyera.app.ui.theme.tokens.SpacingTokens
import com.pyera.app.ui.util.pyeraBackground

/**
 * A centered circular progress indicator with the brand's ColorTokens.Primary500 color.
 * Used for full-screen loading states.
 */
@Composable
fun LoadingIndicator(
    modifier: Modifier = Modifier,
    message: String? = null
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            color = ColorTokens.Primary500,
            strokeWidth = 4.dp,
            modifier = Modifier.size(48.dp)
        )

        message?.let {
            Spacer(modifier = Modifier.height(SpacingTokens.Medium))
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium,
                color = ColorTokens.Primary500.copy(alpha = 0.8f)
            )
        }
    }
}

/**
 * A small loading indicator for inline loading states.
 */
@Composable
fun SmallLoadingIndicator(
    modifier: Modifier = Modifier
) {
    CircularProgressIndicator(
        color = ColorTokens.Primary500,
        strokeWidth = 2.dp,
        modifier = modifier.size(SpacingTokens.Large)
    )
}

/**
 * A shimmer loading placeholder for cards.
 * Creates an animated shimmer effect to indicate content is loading.
 */
@Composable
fun ShimmerCard(
    modifier: Modifier = Modifier
) {
    val shimmerColors = listOf(
        ColorTokens.SurfaceLevel2.copy(alpha = 0.6f),
        ColorTokens.SurfaceLevel2.copy(alpha = 0.9f),
        ColorTokens.SurfaceLevel2.copy(alpha = 0.6f)
    )

    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1200,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_translate"
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset.Zero,
        end = Offset(x = translateAnim.value, y = translateAnim.value)
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp)
            .clip(RoundedCornerShape(SpacingTokens.Medium))
            .background(brush)
    )
}

/**
 * A shimmer placeholder for list items with text content.
 */
@Composable
fun ShimmerListItem(
    modifier: Modifier = Modifier
) {
    val shimmerColors = listOf(
        ColorTokens.SurfaceLevel2.copy(alpha = 0.6f),
        ColorTokens.SurfaceLevel2.copy(alpha = 0.9f),
        ColorTokens.SurfaceLevel2.copy(alpha = 0.6f)
    )

    val transition = rememberInfiniteTransition(label = "shimmer_list")
    val translateAnim = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1200,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_list_translate"
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset.Zero,
        end = Offset(x = translateAnim.value, y = translateAnim.value)
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(SpacingTokens.Medium),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar placeholder
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(brush)
        )

        Spacer(modifier = Modifier.width(SpacingTokens.Medium))

        // Text placeholders
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(SpacingTokens.Medium)
                    .clip(RoundedCornerShape(4.dp))
                    .background(brush)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth(0.4f)
                    .height(12.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(brush)
            )
        }

        Spacer(modifier = Modifier.width(SpacingTokens.Medium))

        // Amount placeholder
        Box(
            modifier = Modifier
                .width(60.dp)
                .height(SpacingTokens.Medium)
                .clip(RoundedCornerShape(4.dp))
                .background(brush)
        )
    }
}

/**
 * A full-screen shimmer loading state with multiple placeholder items.
 */
@Composable
fun ShimmerLoadingList(
    itemCount: Int = 5,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .pyeraBackground()
    ) {
        repeat(itemCount) {
            PyeraCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = SpacingTokens.Medium, vertical = SpacingTokens.Small),
                containerColor = ColorTokens.SurfaceLevel1
            ) {
                ShimmerListItem()
            }
        }
    }
}

/**
 * A reusable loading state component with a message.
 *
 * @param message The loading message to display
 * @param modifier Modifier for styling
 */
@Composable
fun PyeraLoadingState(
    message: String = "Loading...",
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(SpacingTokens.Large),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            color = ColorTokens.Primary500,
            strokeWidth = 4.dp,
            modifier = Modifier.size(48.dp)
        )
        
        Spacer(modifier = Modifier.height(SpacingTokens.Medium))
        
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

/**
 * A shimmer effect for the balance card in the dashboard.
 */
@Composable
fun ShimmerBalanceCard(
    modifier: Modifier = Modifier
) {
    val shimmerColors = listOf(
        ColorTokens.SurfaceLevel2.copy(alpha = 0.6f),
        ColorTokens.SurfaceLevel2.copy(alpha = 0.9f),
        ColorTokens.SurfaceLevel2.copy(alpha = 0.6f)
    )

    val transition = rememberInfiniteTransition(label = "shimmer_balance")
    val translateAnim = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1200,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_balance_translate"
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset.Zero,
        end = Offset(x = translateAnim.value, y = translateAnim.value)
    )

    PyeraCard(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp),
        containerColor = ColorTokens.SurfaceLevel1
    ) {
        Column(
            modifier = Modifier.padding(SpacingTokens.MediumLarge)
        ) {
            // Label placeholder
            Box(
                modifier = Modifier
                    .width(100.dp)
                    .height(14.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(brush)
            )

            Spacer(modifier = Modifier.height(SpacingTokens.Medium))

            // Balance placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(36.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(brush)
            )

            Spacer(modifier = Modifier.height(SpacingTokens.Large))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Income placeholder
                Box(
                    modifier = Modifier
                        .width(80.dp)
                        .height(40.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(brush)
                )

                // Expense placeholder
                Box(
                    modifier = Modifier
                        .width(80.dp)
                        .height(40.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(brush)
                )
            }
        }
    }
}



