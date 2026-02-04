package com.pyera.app.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.pyera.app.ui.navigation.BottomNavItem
import com.pyera.app.ui.theme.AccentGreen
import com.pyera.app.ui.theme.CardBorder
import com.pyera.app.ui.theme.DeepBackground
import com.pyera.app.ui.theme.TextTertiary

@Composable
fun PyeraBottomBar(navController: NavController) {
    val items = listOf(
        BottomNavItem.Dashboard,
        BottomNavItem.Transactions,
        BottomNavItem.Budget,
        BottomNavItem.Debt,
        BottomNavItem.Profile
    )

    NavigationBar(
        containerColor = DeepBackground,
        contentColor = AccentGreen,
        modifier = Modifier.drawBehind {
            drawLine(
                color = CardBorder,
                start = Offset(0f, 0f),
                end = Offset(size.width, 0f),
                strokeWidth = 1.dp.toPx()
            )
        }
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        items.forEach { item ->
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.title,
                        modifier = Modifier.size(26.dp)
                    )
                },
                label = { Text(text = item.title) },
                selected = currentRoute == item.route,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = DeepBackground,
                    selectedTextColor = AccentGreen,
                    indicatorColor = AccentGreen,
                    unselectedIconColor = TextTertiary,
                    unselectedTextColor = TextTertiary
                ),
                onClick = {
                    navController.navigate(item.route) {
                        navController.graph.startDestinationRoute?.let { route ->
                            popUpTo(route) {
                                saveState = true
                            }
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}
