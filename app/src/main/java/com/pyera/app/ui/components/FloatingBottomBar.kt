package com.pyera.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.automirrored.outlined.ShowChart
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.pyera.app.ui.navigation.Screen
import com.pyera.app.ui.theme.*
import com.pyera.app.ui.theme.tokens.ColorTokens
import com.pyera.app.ui.theme.tokens.SpacingTokens
import androidx.compose.material3.MaterialTheme

/**
 * Floating pill-shaped bottom navigation bar
 */
@Composable
fun FloatingBottomBar(
    navController: NavController,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = SpacingTokens.Medium, vertical = SpacingTokens.MediumSmall)
            .navigationBarsPadding()
    ) {
        // Main floating bar container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .shadow(
                    elevation = 18.dp,
                    shape = RoundedCornerShape(Radius.BottomBar),
                    spotColor = Color.Black.copy(alpha = 0.5f)
                )
                .clip(RoundedCornerShape(Radius.BottomBar))
                .border(1.dp, ColorBorderSubtle, RoundedCornerShape(Radius.BottomBar))
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            ColorTokens.SurfaceLevel2,
                            SurfaceSecondary
                        )
                    )
                )
                .padding(horizontal = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Home
                NavItem(
                    icon = Icons.Outlined.Home,
                    selectedIcon = Icons.Filled.Home,
                    label = "Home",
                    isSelected = currentRoute == Screen.Main.Dashboard.route,
                    onClick = {
                        navController.navigate(Screen.Main.Dashboard.route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )

                // Transactions
                NavItem(
                    icon = Icons.AutoMirrored.Outlined.ReceiptLong,
                    selectedIcon = Icons.AutoMirrored.Filled.ReceiptLong,
                    label = "Transactions",
                    isSelected = currentRoute == Screen.Main.Transactions.route,
                    onClick = {
                        navController.navigate(Screen.Main.Transactions.route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )

                // Central Add Button
                CentralAddButton(onClick = onAddClick)

                // Analysis
                NavItem(
                    icon = Icons.AutoMirrored.Outlined.ShowChart,
                    selectedIcon = Icons.AutoMirrored.Filled.ShowChart,
                    label = "Analysis",
                    isSelected = currentRoute == Screen.Main.Analysis.route,
                    onClick = {
                        navController.navigate(Screen.Main.Analysis.route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )

                // Profile
                NavItem(
                    icon = Icons.Outlined.Person,
                    selectedIcon = Icons.Filled.Person,
                    label = "Profile",
                    isSelected = currentRoute == Screen.Main.Profile.route,
                    onClick = {
                        navController.navigate(Screen.Main.Profile.route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun NavItem(
    icon: ImageVector,
    selectedIcon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f,
        label = "nav_item_scale"
    )

    val iconColor by animateColorAsState(
        targetValue = if (isSelected) PrimaryAccent else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
        animationSpec = tween(200),
        label = "nav_item_color"
    )

    val labelColor by animateColorAsState(
        targetValue = if (isSelected) PrimaryAccent else TextMuted,
        animationSpec = tween(200),
        label = "nav_label_color"
    )

    Column(
        modifier = Modifier
            .scale(scale)
            .clickable(
                onClick = onClick,
                interactionSource = interactionSource,
                indication = null
            )
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = if (isSelected) selectedIcon else icon,
            contentDescription = label,
            tint = iconColor,
            modifier = Modifier.size(SpacingTokens.Large)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            color = labelColor,
            fontSize = 10.sp
        )
    }
}

@Composable
private fun CentralAddButton(
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else 1f,
        animationSpec = tween(100),
        label = "add_button_scale"
    )

    Box(
        modifier = Modifier
            .scale(scale)
            .size(56.dp)
            .shadow(
                elevation = 12.dp,
                shape = CircleShape,
                spotColor = PrimaryAccent.copy(alpha = 0.5f)
            )
            .clip(CircleShape)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        PrimaryAccentLight,
                        PrimaryAccentDark
                    )
                )
            )
            .clickable(
                onClick = onClick,
                interactionSource = interactionSource,
                indication = null
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Add Transaction",
            tint = BackgroundPrimary,
            modifier = Modifier.size(28.dp)
        )
    }
}


