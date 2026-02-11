package com.pyera.app.ui.welcome

import com.pyera.app.ui.theme.tokens.ColorTokens
import com.pyera.app.ui.theme.tokens.SpacingTokens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pyera.app.ui.theme.CardBackground
import com.pyera.app.ui.theme.CardBorder
import com.pyera.app.ui.theme.DeepBackground
import com.pyera.app.ui.theme.PrimaryAccent
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import com.pyera.app.R
import com.pyera.app.ui.util.pyeraBackground
import com.pyera.app.ui.theme.TextSecondary
import com.pyera.app.ui.theme.TextTertiary
import com.pyera.app.ui.util.CurrencyFormatter

@Suppress("DEPRECATION")
@Composable
fun WelcomeScreen(
    onGetStarted: () -> Unit,
    onHaveAccount: () -> Unit,
    onOpenTerms: () -> Unit = {},
    onOpenPrivacy: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .pyeraBackground(forceDark = true)
    ) {

        // Background Image with scrim
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(450.dp)
        ) {
            androidx.compose.foundation.Image(
                painter = painterResource(id = R.drawable.bg_abstract_green_waves),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { alpha = 0.6f } // Subtle opacity
            )
            // Gradient Scrim for text readability
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                PrimaryAccent.copy(alpha = 0.06f),
                                DeepBackground
                            ),
                            startY = 0f,
                            endY = Float.POSITIVE_INFINITY
                        )
                    )
            )
        }


        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = SpacingTokens.Large),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(60.dp))

            // Logo Section
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(ColorTokens.Primary500),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = CurrencyFormatter.SYMBOL,
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold,
                    color = DeepBackground
                )
            }

            Spacer(modifier = Modifier.height(SpacingTokens.Medium))

            Text(
                text = "Pyera",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.weight(0.2f))

            // Feature Cards
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FeatureCard(
                    emoji = "📊",
                    title = "Track Everything",
                    description = "Monitor income, expenses, and budgets"
                )
                FeatureCard(
                    emoji = "🎯",
                    title = "Set Goals",
                    description = "Savings targets with progress tracking"
                )
                FeatureCard(
                    emoji = "📱",
                    title = "Scan Receipts",
                    description = "Auto-capture transactions with AI"
                )
            }

            Spacer(modifier = Modifier.weight(0.3f))

            // Headline
            Text(
                text = "Take control of",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            Text(
                text = "your finances",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = ColorTokens.Primary500,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Simple, smart, and secure money management",
                fontSize = 14.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.weight(0.2f))

            // Primary Button
            Button(
                onClick = onGetStarted,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(SpacingTokens.Medium),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ColorTokens.Primary500,
                    contentColor = DeepBackground
                )
            ) {
                Text(
                    text = "Get Started",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Secondary Button
            OutlinedButton(
                onClick = onHaveAccount,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(SpacingTokens.Medium),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color.White
                ),
                border = BorderStroke(1.dp, CardBorder)
            ) {
                Text(
                    text = "I already have an account",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(SpacingTokens.Large))

            // Terms text
            Text(
                text = "By continuing you agree to our",
                fontSize = 12.sp,
                color = TextTertiary,
                textAlign = TextAlign.Center
            )

            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onOpenTerms,
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    Text(
                        text = "Terms of Service",
                        fontSize = 12.sp,
                        color = ColorTokens.Primary500
                    )
                }
                Text(
                    text = "and",
                    fontSize = 12.sp,
                    color = TextTertiary
                )
                TextButton(
                    onClick = onOpenPrivacy,
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    Text(
                        text = "Privacy Policy",
                        fontSize = 12.sp,
                        color = ColorTokens.Primary500
                    )
                }
            }

            Spacer(modifier = Modifier.height(SpacingTokens.ExtraLarge))
        }
    }
}

@Composable
private fun FeatureCard(
    emoji: String,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(SpacingTokens.Medium))
            .background(CardBackground)
            .border(1.dp, CardBorder, RoundedCornerShape(SpacingTokens.Medium))
            .padding(SpacingTokens.Medium),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(SpacingTokens.Medium)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(ColorTokens.Primary500.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = emoji,
                fontSize = 20.sp
            )
        }

        Column {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
            Text(
                text = description,
                fontSize = 13.sp,
                color = TextSecondary
            )
        }
    }
}



