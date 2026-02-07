package com.pyera.app.ui.main

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import com.pyera.app.ui.theme.ColorSuccess
import com.pyera.app.ui.theme.ColorError
import com.pyera.app.ui.theme.NeonYellow
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.pyera.app.ui.dashboard.DashboardScreen
import com.pyera.app.ui.recurring.AddRecurringTransactionScreen
import com.pyera.app.ui.recurring.EditRecurringTransactionScreen
import com.pyera.app.ui.recurring.RecurringTransactionsScreen
import com.pyera.app.ui.rules.AddTransactionRuleScreen
import com.pyera.app.ui.rules.TransactionRulesScreen
import com.pyera.app.ui.templates.AddTemplateScreen
import com.pyera.app.ui.templates.TemplatesScreen
import com.pyera.app.ui.templates.TemplatesViewModel
import com.pyera.app.ui.transaction.AddTransactionScreen
import com.pyera.app.ui.transaction.TransactionListScreen
import com.pyera.app.ui.debt.DebtScreen
import com.pyera.app.ui.savings.SavingsScreen
import com.pyera.app.ui.bills.BillsScreen
import com.pyera.app.ui.investments.InvestmentsScreen
import com.pyera.app.ui.chat.ChatScreen
import com.pyera.app.ui.profile.ProfileScreen
import com.pyera.app.ui.analysis.AnalysisScreen
import com.pyera.app.ui.insights.InsightsScreen
import com.pyera.app.ui.security.AppLockScreen
import com.pyera.app.ui.security.ChangePinScreen
import com.pyera.app.ui.security.SecuritySettingsScreen
import com.pyera.app.ui.security.SetPinScreen
import com.pyera.app.ui.account.AccountsScreen
import com.pyera.app.ui.account.AddAccountScreen
import com.pyera.app.ui.account.AccountDetailScreen
import com.pyera.app.ui.account.TransferScreen
import com.pyera.app.ui.budget.BudgetDetailScreen
import com.pyera.app.ui.budget.BudgetListScreen
import com.pyera.app.ui.budget.BudgetViewModel
import com.pyera.app.ui.budget.CreateBudgetScreen
import com.pyera.app.ui.components.PyeraBottomBar
import com.pyera.app.ui.navigation.Screen

/**
 * CompositionLocal to provide SnackbarHostState to child screens.
 * This allows any composable within the MainScreen to show snackbars.
 */
val LocalSnackbarHostState = staticCompositionLocalOf<SnackbarHostState> {
    error("No SnackbarHostState provided")
}

@Composable
fun MainScreen() {
    val bottomNavController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    
    val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    CompositionLocalProvider(LocalSnackbarHostState provides snackbarHostState) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            bottomBar = { PyeraBottomBar(navController = bottomNavController, currentRoute = currentRoute) }
        ) { innerPadding ->
            NavHost(
                navController = bottomNavController,
                startDestination = Screen.Main.Dashboard.route,
                modifier = Modifier.padding(innerPadding)
            ) {
                // Dashboard
                composable(
                    route = Screen.Main.Dashboard.route,
                    enterTransition = { fadeIn(animationSpec = tween(300)) },
                    exitTransition = { fadeOut(animationSpec = tween(300)) }
                ) {
                    DashboardScreen(
                        onAddTransactionClick = {
                            bottomNavController.navigate(Screen.AddTransaction.route)
                        },
                        onBillsClick = {
                            bottomNavController.navigate(Screen.Bills.route)
                        },
                        onInvestmentsClick = {
                            bottomNavController.navigate(Screen.Investments.route)
                        },
                        onInsightsClick = {
                            bottomNavController.navigate(Screen.Main.Insights.route)
                        },
                        onTemplatesClick = {
                            bottomNavController.navigate(Screen.Templates.List.route)
                        }
                    )
                }

                // Transactions
                composable(
                    route = Screen.Main.Transactions.route,
                    enterTransition = { slideInHorizontally(initialOffsetX = { 1000 }) + fadeIn() },
                    exitTransition = { slideOutHorizontally(targetOffsetX = { -1000 }) + fadeOut() },
                    popEnterTransition = { slideInHorizontally(initialOffsetX = { -1000 }) + fadeIn() },
                    popExitTransition = { slideOutHorizontally(targetOffsetX = { 1000 }) + fadeOut() }
                ) {
                    TransactionListScreen()
                }

                // Analysis
                composable(
                    route = Screen.Main.Analysis.route,
                    enterTransition = { scaleIn() + fadeIn() },
                    exitTransition = { scaleOut() + fadeOut() }
                ) {
                    AnalysisScreen()
                }

                // Insights (Smart Spending Insights)
                composable(
                    route = Screen.Main.Insights.route,
                    enterTransition = { scaleIn() + fadeIn() },
                    exitTransition = { scaleOut() + fadeOut() }
                ) {
                    InsightsScreen(
                        onNavigateToBudget = {
                            bottomNavController.navigate(Screen.Main.Budget.route)
                        },
                        onNavigateToTransactions = {
                            bottomNavController.navigate(Screen.Main.Transactions.route)
                        },
                        onNavigateToSavings = {
                            bottomNavController.navigate(Screen.Main.Savings.route)
                        }
                    )
                }

                // Budget List Screen
                composable(
                    route = Screen.Main.Budget.route,
                    enterTransition = { slideInHorizontally(initialOffsetX = { 1000 }) + fadeIn() },
                    exitTransition = { slideOutHorizontally(targetOffsetX = { -1000 }) + fadeOut() }
                ) {
                    val budgetViewModel: BudgetViewModel = hiltViewModel()
                    BudgetListScreen(
                        onNavigateToCreate = {
                            bottomNavController.navigate(Screen.CreateBudget.route)
                        },
                        onNavigateToDetail = { budgetId ->
                            bottomNavController.navigate(Screen.BudgetDetail.createRoute(budgetId))
                        },
                        viewModel = budgetViewModel
                    )
                }

                // Budget Detail Screen
                composable(
                    route = Screen.BudgetDetail.route,
                    arguments = listOf(
                        navArgument("budgetId") { type = NavType.IntType }
                    ),
                    enterTransition = { slideInHorizontally(initialOffsetX = { 1000 }) + fadeIn() },
                    exitTransition = { slideOutHorizontally(targetOffsetX = { -1000 }) + fadeOut() },
                    popEnterTransition = { slideInHorizontally(initialOffsetX = { -1000 }) + fadeIn() },
                    popExitTransition = { slideOutHorizontally(targetOffsetX = { 1000 }) + fadeOut() }
                ) { backStackEntry ->
                    val budgetId = backStackEntry.arguments?.getInt("budgetId") ?: 0
                    val budgetViewModel: BudgetViewModel = hiltViewModel()
                    BudgetDetailScreen(
                        budgetId = budgetId,
                        onNavigateBack = {
                            bottomNavController.popBackStack()
                        },
                        onNavigateToEdit = { id ->
                            bottomNavController.navigate(Screen.EditBudget.createRoute(id))
                        },
                        viewModel = budgetViewModel
                    )
                }

                // Create Budget Screen
                composable(
                    route = Screen.CreateBudget.route,
                    enterTransition = { slideInHorizontally(initialOffsetX = { 1000 }) + fadeIn() },
                    exitTransition = { slideOutHorizontally(targetOffsetX = { -1000 }) + fadeOut() },
                    popEnterTransition = { slideInHorizontally(initialOffsetX = { -1000 }) + fadeIn() },
                    popExitTransition = { slideOutHorizontally(targetOffsetX = { 1000 }) + fadeOut() }
                ) {
                    val budgetViewModel: BudgetViewModel = hiltViewModel()
                    CreateBudgetScreen(
                        onNavigateBack = {
                            bottomNavController.popBackStack()
                        },
                        onBudgetCreated = {
                            bottomNavController.popBackStack()
                        },
                        viewModel = budgetViewModel
                    )
                }

                // Edit Budget Screen (reuse CreateBudgetScreen with budget ID)
                composable(
                    route = Screen.EditBudget.route,
                    arguments = listOf(
                        navArgument("budgetId") { type = NavType.IntType }
                    ),
                    enterTransition = { slideInHorizontally(initialOffsetX = { 1000 }) + fadeIn() },
                    exitTransition = { slideOutHorizontally(targetOffsetX = { -1000 }) + fadeOut() },
                    popEnterTransition = { slideInHorizontally(initialOffsetX = { -1000 }) + fadeIn() },
                    popExitTransition = { slideOutHorizontally(targetOffsetX = { 1000 }) + fadeOut() }
                ) { backStackEntry ->
                    val budgetId = backStackEntry.arguments?.getInt("budgetId") ?: 0
                    // For now, reuse the create screen
                    // In a full implementation, you'd create an EditBudgetScreen
                    val budgetViewModel: BudgetViewModel = hiltViewModel()
                    CreateBudgetScreen(
                        onNavigateBack = {
                            bottomNavController.popBackStack()
                        },
                        onBudgetCreated = {
                            bottomNavController.popBackStack()
                        },
                        viewModel = budgetViewModel
                    )
                }

                // Debt
                composable(
                    route = Screen.Main.Debt.route,
                    enterTransition = { slideInHorizontally(initialOffsetX = { 1000 }) + fadeIn() },
                    exitTransition = { slideOutHorizontally(targetOffsetX = { -1000 }) + fadeOut() }
                ) {
                    DebtScreen()
                }

                // Savings
                composable(
                    route = Screen.Main.Savings.route,
                    enterTransition = { slideInHorizontally(initialOffsetX = { 1000 }) + fadeIn() },
                    exitTransition = { slideOutHorizontally(targetOffsetX = { -1000 }) + fadeOut() }
                ) {
                    SavingsScreen()
                }

                // Bills
                composable(
                    route = Screen.Bills.route,
                    enterTransition = { slideInHorizontally(initialOffsetX = { 1000 }) + fadeIn() },
                    exitTransition = { slideOutHorizontally(targetOffsetX = { -1000 }) + fadeOut() }
                ) {
                    BillsScreen()
                }

                // Investments
                composable(
                    route = Screen.Investments.route,
                    enterTransition = { slideInHorizontally(initialOffsetX = { 1000 }) + fadeIn() },
                    exitTransition = { slideOutHorizontally(targetOffsetX = { -1000 }) + fadeOut() }
                ) {
                    InvestmentsScreen()
                }

                // Profile
                composable(
                    route = Screen.Main.Profile.route,
                    enterTransition = { scaleIn() + fadeIn() },
                    exitTransition = { scaleOut() + fadeOut() }
                ) {
                    ProfileScreen(navController = bottomNavController)
                }

                // Chat
                composable(
                    route = Screen.Chat.route,
                    enterTransition = { scaleIn(initialScale = 0.8f) + fadeIn() },
                    exitTransition = { scaleOut(targetScale = 0.8f) + fadeOut() }
                ) {
                    ChatScreen(navController = bottomNavController)
                }

                // Accounts - List
                composable(
                    route = Screen.Accounts.route,
                    enterTransition = { slideInHorizontally(initialOffsetX = { 1000 }) + fadeIn() },
                    exitTransition = { slideOutHorizontally(targetOffsetX = { -1000 }) + fadeOut() }
                ) {
                    AccountsScreen(navController = bottomNavController)
                }

                // Accounts - Add
                composable(
                    route = Screen.AddAccount.route,
                    enterTransition = { slideInHorizontally(initialOffsetX = { 1000 }) + fadeIn() },
                    exitTransition = { slideOutHorizontally(targetOffsetX = { -1000 }) + fadeOut() }
                ) {
                    val viewModel: com.pyera.app.ui.account.AccountsViewModel = hiltViewModel()
                    LaunchedEffect(Unit) {
                        viewModel.resetFormState()
                    }
                    AddAccountScreen(
                        navController = bottomNavController,
                        viewModel = viewModel
                    )
                }

                // Accounts - Edit
                composable(
                    route = Screen.EditAccount.route,
                    arguments = listOf(
                        navArgument("accountId") { type = NavType.LongType }
                    ),
                    enterTransition = { slideInHorizontally(initialOffsetX = { 1000 }) + fadeIn() },
                    exitTransition = { slideOutHorizontally(targetOffsetX = { -1000 }) + fadeOut() }
                ) { backStackEntry ->
                    val accountId = backStackEntry.arguments?.getLong("accountId") ?: 0L
                    val viewModel: com.pyera.app.ui.account.AccountsViewModel = hiltViewModel()
                    val selectedAccount by viewModel.selectedAccount.collectAsState()
                    LaunchedEffect(accountId) {
                        viewModel.loadAccountDetail(accountId)
                    }
                    LaunchedEffect(selectedAccount) {
                        selectedAccount?.let { viewModel.initEditForm(it) }
                    }
                    AddAccountScreen(
                        navController = bottomNavController,
                        viewModel = viewModel
                    )
                }

                // Accounts - Detail
                composable(
                    route = Screen.AccountDetail.route,
                    arguments = listOf(
                        navArgument("accountId") { type = NavType.LongType }
                    ),
                    enterTransition = { slideInHorizontally(initialOffsetX = { 1000 }) + fadeIn() },
                    exitTransition = { slideOutHorizontally(targetOffsetX = { -1000 }) + fadeOut() }
                ) { backStackEntry ->
                    val accountId = backStackEntry.arguments?.getLong("accountId") ?: 0L
                    AccountDetailScreen(
                        accountId = accountId,
                        navController = bottomNavController
                    )
                }

                // Accounts - Transfer
                composable(
                    route = Screen.Transfer.route,
                    arguments = listOf(
                        navArgument("from") {
                            type = NavType.LongType
                            nullable = true
                            defaultValue = null
                        }
                    ),
                    enterTransition = { slideInHorizontally(initialOffsetX = { 1000 }) + fadeIn() },
                    exitTransition = { slideOutHorizontally(targetOffsetX = { -1000 }) + fadeOut() }
                ) { backStackEntry ->
                    val fromAccountId = backStackEntry.arguments?.getLong("from")
                    TransferScreen(
                        fromAccountId = fromAccountId,
                        navController = bottomNavController
                    )
                }

                // Add Transaction
                composable(
                    route = Screen.AddTransaction.route,
                    enterTransition = { slideInHorizontally(initialOffsetX = { 1000 }) + fadeIn() },
                    exitTransition = { slideOutHorizontally(targetOffsetX = { -1000 }) + fadeOut() }
                ) {
                    AddTransactionScreen(navController = bottomNavController)
                }
                
                // Security Settings
                composable(
                    route = Screen.SecuritySettings.route,
                    enterTransition = { slideInHorizontally(initialOffsetX = { 1000 }) + fadeIn() },
                    exitTransition = { slideOutHorizontally(targetOffsetX = { -1000 }) + fadeOut() },
                    popEnterTransition = { slideInHorizontally(initialOffsetX = { -1000 }) + fadeIn() },
                    popExitTransition = { slideOutHorizontally(targetOffsetX = { 1000 }) + fadeOut() }
                ) {
                    SecuritySettingsScreen(
                        onNavigateBack = { bottomNavController.popBackStack() },
                        onNavigateToSetPin = { bottomNavController.navigate(Screen.SetPin.route) },
                        onNavigateToChangePin = { bottomNavController.navigate(Screen.ChangePin.route) }
                    )
                }
                
                // Set PIN Screen
                composable(
                    route = Screen.SetPin.route,
                    enterTransition = { slideInHorizontally(initialOffsetX = { 1000 }) + fadeIn() },
                    exitTransition = { slideOutHorizontally(targetOffsetX = { -1000 }) + fadeOut() },
                    popEnterTransition = { slideInHorizontally(initialOffsetX = { -1000 }) + fadeIn() },
                    popExitTransition = { slideOutHorizontally(targetOffsetX = { 1000 }) + fadeOut() }
                ) {
                    SetPinScreen(
                        onPinSet = { bottomNavController.popBackStack() },
                        onCancel = { bottomNavController.popBackStack() }
                    )
                }
                
                // Change PIN Screen
                composable(
                    route = Screen.ChangePin.route,
                    enterTransition = { slideInHorizontally(initialOffsetX = { 1000 }) + fadeIn() },
                    exitTransition = { slideOutHorizontally(targetOffsetX = { -1000 }) + fadeOut() },
                    popEnterTransition = { slideInHorizontally(initialOffsetX = { -1000 }) + fadeIn() },
                    popExitTransition = { slideOutHorizontally(targetOffsetX = { 1000 }) + fadeOut() }
                ) {
                    ChangePinScreen(
                        onPinChanged = { bottomNavController.popBackStack() },
                        onCancel = { bottomNavController.popBackStack() }
                    )
                }
                
                // App Lock Screen (shown when app is locked)
                composable(
                    route = Screen.AppLock.route,
                    enterTransition = { fadeIn(animationSpec = tween(300)) },
                    exitTransition = { fadeOut(animationSpec = tween(300)) }
                ) {
                    AppLockScreen(
                        onUnlockSuccess = { bottomNavController.popBackStack() }
                    )
                }

                // Recurring Transactions - List
                composable(
                    route = Screen.Recurring.List.route,
                    enterTransition = { slideInHorizontally(initialOffsetX = { 1000 }) + fadeIn() },
                    exitTransition = { slideOutHorizontally(targetOffsetX = { -1000 }) + fadeOut() }
                ) {
                    RecurringTransactionsScreen(navController = bottomNavController)
                }

                // Recurring Transactions - Add
                composable(
                    route = Screen.Recurring.Add.route,
                    enterTransition = { slideInHorizontally(initialOffsetX = { 1000 }) + fadeIn() },
                    exitTransition = { slideOutHorizontally(targetOffsetX = { -1000 }) + fadeOut() }
                ) {
                    AddRecurringTransactionScreen(navController = bottomNavController)
                }

                // Recurring Transactions - Edit
                composable(
                    route = Screen.Recurring.Edit.route,
                    arguments = listOf(
                        navArgument("id") { type = NavType.LongType }
                    ),
                    enterTransition = { slideInHorizontally(initialOffsetX = { 1000 }) + fadeIn() },
                    exitTransition = { slideOutHorizontally(targetOffsetX = { -1000 }) + fadeOut() }
                ) { backStackEntry ->
                    val recurringId = backStackEntry.arguments?.getLong("id") ?: 0L
                    EditRecurringTransactionScreen(
                        navController = bottomNavController,
                        recurringId = recurringId
                    )
                }

                // Transaction Rules - List
                composable(
                    route = Screen.TransactionRules.route,
                    enterTransition = { slideInHorizontally(initialOffsetX = { 1000 }) + fadeIn() },
                    exitTransition = { slideOutHorizontally(targetOffsetX = { -1000 }) + fadeOut() }
                ) {
                    TransactionRulesScreen(navController = bottomNavController)
                }

                // Transaction Rules - Add
                composable(
                    route = Screen.AddTransactionRule.route,
                    enterTransition = { slideInHorizontally(initialOffsetX = { 1000 }) + fadeIn() },
                    exitTransition = { slideOutHorizontally(targetOffsetX = { -1000 }) + fadeOut() }
                ) {
                    AddTransactionRuleScreen(navController = bottomNavController)
                }

                // Transaction Rules - Edit
                composable(
                    route = Screen.EditTransactionRule.route,
                    arguments = listOf(
                        navArgument("ruleId") { type = NavType.LongType }
                    ),
                    enterTransition = { slideInHorizontally(initialOffsetX = { 1000 }) + fadeIn() },
                    exitTransition = { slideOutHorizontally(targetOffsetX = { -1000 }) + fadeOut() }
                ) { backStackEntry ->
                    val ruleId = backStackEntry.arguments?.getLong("ruleId")
                    AddTransactionRuleScreen(
                        navController = bottomNavController,
                        ruleId = ruleId
                    )
                }

                // Templates - List
                composable(
                    route = Screen.Templates.List.route,
                    enterTransition = { slideInHorizontally(initialOffsetX = { 1000 }) + fadeIn() },
                    exitTransition = { slideOutHorizontally(targetOffsetX = { -1000 }) + fadeOut() }
                ) {
                    TemplatesScreen(
                        onNavigateBack = { bottomNavController.popBackStack() },
                        onAddTemplate = { bottomNavController.navigate(Screen.Templates.Add.route) },
                        onEditTemplate = { template ->
                            bottomNavController.navigate(Screen.Templates.Edit.createRoute(template.id))
                        },
                        onUseTemplate = { templateId ->
                            // Navigate to add transaction with template ID
                            bottomNavController.navigate("${Screen.AddTransaction.route}?templateId=$templateId")
                        }
                    )
                }

                // Templates - Add
                composable(
                    route = Screen.Templates.Add.route,
                    enterTransition = { slideInHorizontally(initialOffsetX = { 1000 }) + fadeIn() },
                    exitTransition = { slideOutHorizontally(targetOffsetX = { -1000 }) + fadeOut() }
                ) {
                    AddTemplateScreen(
                        onNavigateBack = { bottomNavController.popBackStack() },
                        onTemplateSaved = { bottomNavController.popBackStack() }
                    )
                }

                // Templates - Edit
                composable(
                    route = Screen.Templates.Edit.route,
                    arguments = listOf(
                        navArgument("templateId") { type = NavType.LongType }
                    ),
                    enterTransition = { slideInHorizontally(initialOffsetX = { 1000 }) + fadeIn() },
                    exitTransition = { slideOutHorizontally(targetOffsetX = { -1000 }) + fadeOut() }
                ) { backStackEntry ->
                    val templateId = backStackEntry.arguments?.getLong("templateId") ?: 0L
                    val viewModel: TemplatesViewModel = hiltViewModel()
                    
                    LaunchedEffect(templateId) {
                        val template = viewModel.getTemplateById(templateId)
                        template?.let { viewModel.initFormForEdit(it) }
                    }
                    
                    AddTemplateScreen(
                        onNavigateBack = { bottomNavController.popBackStack() },
                        onTemplateSaved = { bottomNavController.popBackStack() }
                    )
                }
            }
        }
    }
}


// ============================================================================
// Snackbar Helper Extension Functions
// ============================================================================

/**
 * Shows a success snackbar with the given message.
 * 
 * @param message The success message to display
 * @param actionLabel Optional action label (e.g., "Undo")
 * @param onAction Optional callback when action is clicked
 * @return SnackbarResult indicating user interaction
 */
suspend fun SnackbarHostState.showSuccess(
    message: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
): SnackbarResult {
    val result = showSnackbar(
        message = message,
        actionLabel = actionLabel,
        duration = SnackbarDuration.Short,
        withDismissAction = true
    )
    if (result == SnackbarResult.ActionPerformed && onAction != null) {
        onAction()
    }
    return result
}

/**
 * Shows an error snackbar with the given message.
 * 
 * @param message The error message to display
 * @param actionLabel Optional action label (e.g., "Retry")
 * @param onAction Optional callback when action is clicked
 * @return SnackbarResult indicating user interaction
 */
suspend fun SnackbarHostState.showError(
    message: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
): SnackbarResult {
    val result = showSnackbar(
        message = message,
        actionLabel = actionLabel,
        duration = SnackbarDuration.Long,
        withDismissAction = true
    )
    if (result == SnackbarResult.ActionPerformed && onAction != null) {
        onAction()
    }
    return result
}

/**
 * Shows an informational snackbar with the given message.
 * 
 * @param message The info message to display
 * @param actionLabel Optional action label
 * @param onAction Optional callback when action is clicked
 * @return SnackbarResult indicating user interaction
 */
suspend fun SnackbarHostState.showInfo(
    message: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
): SnackbarResult {
    val result = showSnackbar(
        message = message,
        actionLabel = actionLabel,
        duration = SnackbarDuration.Short,
        withDismissAction = true
    )
    if (result == SnackbarResult.ActionPerformed && onAction != null) {
        onAction()
    }
    return result
}
