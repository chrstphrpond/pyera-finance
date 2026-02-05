package com.pyera.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector

// Main Screens
sealed class Screen(val route: String) {
    object Welcome : Screen("welcome")
    object Onboarding : Screen("onboarding")
    object Login : Screen("login")
    object Register : Screen("register")
    object ForgotPassword : Screen("forgot_password")
    
    sealed class Main(route: String) : Screen(route) {
        object Dashboard : Main("main/dashboard")
        object Transactions : Main("main/transactions")
        object Budget : Main("main/budget")
        object Savings : Main("main/savings")
        object Profile : Main("main/profile")
        object Analysis : Main("main/analysis") // Keep for navigation but not in bottom bar
        object Debt : Main("main/debt") // Keep for navigation but move to tab
    }
    
    object AddTransaction : Screen("transaction/add")
    object EditTransaction : Screen("transaction/edit/{transactionId}") {
        fun createRoute(transactionId: String) = "transaction/edit/$transactionId"
    }
    
    // Budget screens
    object BudgetDetail : Screen("budget/detail/{budgetId}") {
        fun createRoute(budgetId: Int) = "budget/detail/$budgetId"
    }
    object CreateBudget : Screen("budget/create")
    object EditBudget : Screen("budget/edit/{budgetId}") {
        fun createRoute(budgetId: Int) = "budget/edit/$budgetId"
    }
    
    // Other screens
    object Bills : Screen("bills")
    object Investments : Screen("investments")
    object Chat : Screen("chat")
    object Settings : Screen("settings")
}

// Bottom Navigation - 5 Items Maximum
sealed class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector
) {
    object Dashboard : BottomNavItem(
        route = Screen.Main.Dashboard.route,
        title = "Home",
        icon = Icons.Outlined.Home,
        selectedIcon = Icons.Filled.Home
    )
    
    object Transactions : BottomNavItem(
        route = Screen.Main.Transactions.route,
        title = "Activity",
        icon = Icons.AutoMirrored.Outlined.ReceiptLong,
        selectedIcon = Icons.AutoMirrored.Filled.ReceiptLong
    )
    
    object Budget : BottomNavItem(
        route = Screen.Main.Budget.route,
        title = "Budget",
        icon = Icons.Outlined.AccountBalanceWallet,
        selectedIcon = Icons.Filled.AccountBalanceWallet
    )
    
    object Savings : BottomNavItem(
        route = Screen.Main.Savings.route,
        title = "Savings",
        icon = Icons.Outlined.Savings,
        selectedIcon = Icons.Filled.Savings
    )
    
    object Profile : BottomNavItem(
        route = Screen.Main.Profile.route,
        title = "Profile",
        icon = Icons.Outlined.Person,
        selectedIcon = Icons.Filled.Person
    )
}

// Items array for BottomNavigation
val bottomNavItems = listOf(
    BottomNavItem.Dashboard,
    BottomNavItem.Transactions,
    BottomNavItem.Budget,
    BottomNavItem.Savings,
    BottomNavItem.Profile
)

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
