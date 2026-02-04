package com.pyera.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Sealed class representing all navigation screens in the Pyera app.
 */
sealed class Screen(val route: String) {
    object Onboarding : Screen("onboarding")
    
    object Auth : Screen("auth_graph") {
        object Login : Screen("login")
        object Register : Screen("register")
    }

    object Main : Screen("main_graph") {
        object Dashboard : Screen("dashboard")
        object Transactions : Screen("transactions")
        object Analysis : Screen("analysis")
        
        // Budget Screens
        object Budget : Screen("budget")
        object BudgetDetail : Screen("budget_detail/{budgetId}") {
            fun createRoute(budgetId: Int) = "budget_detail/$budgetId"
        }
        object CreateBudget : Screen("create_budget")
        object EditBudget : Screen("edit_budget/{budgetId}") {
            fun createRoute(budgetId: Int) = "edit_budget/$budgetId"
        }
        
        object Debt : Screen("debt")
        object Savings : Screen("savings")
        object Bills : Screen("bills")
        object Investments : Screen("investments")
        object Chat : Screen("chat")
        object Profile : Screen("profile")
        object AddTransaction : Screen("add_transaction")
    }
}

/**
 * Bottom navigation items for the main screen
 */
sealed class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    object Dashboard : BottomNavItem(
        route = Screen.Main.Dashboard.route,
        title = "Home",
        icon = Icons.Default.Home
    )
    object Transactions : BottomNavItem(
        route = Screen.Main.Transactions.route,
        title = "Activity",
        icon = Icons.AutoMirrored.Filled.List
    )
    object Budget : BottomNavItem(
        route = Screen.Main.Budget.route,
        title = "Budget",
        icon = Icons.Default.Star
    )
    object Debt : BottomNavItem(
        route = Screen.Main.Debt.route,
        title = "Debt",
        icon = Icons.Default.Warning
    )
    object Profile : BottomNavItem(
        route = Screen.Main.Profile.route,
        title = "Profile",
        icon = Icons.Default.Person
    )
}

/**
 * Extension function to check if a screen is a budget screen
 */
fun String.isBudgetScreen(): Boolean {
    return this.startsWith("budget") || this.startsWith("create_budget") || this.startsWith("edit_budget")
}

/**
 * Extension function to get the parent screen
 */
fun String.getParentScreen(): String {
    return when {
        this.isBudgetScreen() -> Screen.Main.Budget.route
        else -> this
    }
}
