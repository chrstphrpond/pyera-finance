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
        object Insights : Main("main/insights") // Smart spending insights
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
    
    // Account screens
    object Accounts : Screen("accounts/list")
    object AddAccount : Screen("accounts/add")
    object EditAccount : Screen("accounts/edit/{accountId}") {
        fun createRoute(accountId: Long) = "accounts/edit/$accountId"
    }
    object AccountDetail : Screen("accounts/detail/{accountId}") {
        fun createRoute(accountId: Long) = "accounts/detail/$accountId"
    }
    object Transfer : Screen("accounts/transfer") {
        fun createRoute(fromAccountId: Long? = null) = 
            if (fromAccountId != null) "accounts/transfer?from=$fromAccountId" 
            else "accounts/transfer"
    }
    
    // Other screens
    object Bills : Screen("bills")
    object Investments : Screen("investments")
    object Chat : Screen("chat")
    object Settings : Screen("settings")
    
    // Security screens
    object SecuritySettings : Screen("security/settings")
    object SetPin : Screen("security/setup")
    object ChangePin : Screen("security/change-pin")
    object AppLock : Screen("security/lock")
    
    // Recurring Transaction screens
    object Recurring {
        object List : Screen("recurring/list")
        object Add : Screen("recurring/add")
        object Edit : Screen("recurring/edit/{id}") {
            fun createRoute(id: Long) = "recurring/edit/$id"
        }
    }
    
    // Transaction Rules screens
    object TransactionRules : Screen("rules/list")
    object AddTransactionRule : Screen("rules/add")
    object EditTransactionRule : Screen("rules/edit/{ruleId}") {
        fun createRoute(ruleId: Long) = "rules/edit/$ruleId"
    }
    
    // Transaction Templates screens
    object Templates {
        object List : Screen("templates/list")
        object Add : Screen("templates/add")
        object Edit : Screen("templates/edit/{templateId}") {
            fun createRoute(templateId: Long) = "templates/edit/$templateId"
        }
    }
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
