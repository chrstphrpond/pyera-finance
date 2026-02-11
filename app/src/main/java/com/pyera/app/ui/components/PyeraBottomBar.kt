package com.pyera.app.ui.components
import com.pyera.app.ui.theme.tokens.ColorTokens

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.pyera.app.ui.navigation.bottomNavItems
import com.pyera.app.ui.theme.*

@Composable
fun PyeraBottomBar(
    navController: NavController,
    currentRoute: String?
) {
    NavigationBar(
        containerColor = ColorTokens.SurfaceLevel1,
        contentColor = MaterialTheme.colorScheme.onBackground,
        tonalElevation = 0.dp
    ) {
        bottomNavItems.forEach { item ->
            val selected = currentRoute == item.route
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = if (selected) item.selectedIcon else item.icon,
                        contentDescription = item.title
                    )
                },
                label = { Text(item.title) },
                selected = selected,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = ColorTokens.Primary500,
                    selectedTextColor = ColorTokens.Primary500,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    indicatorColor = ColorTokens.Primary500.copy(alpha = 0.1f)
                )
            )
        }
    }
}


