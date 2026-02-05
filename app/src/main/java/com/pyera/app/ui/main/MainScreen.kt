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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.pyera.app.ui.dashboard.DashboardScreen
import com.pyera.app.ui.transaction.AddTransactionScreen
import com.pyera.app.ui.transaction.TransactionListScreen
import com.pyera.app.ui.debt.DebtScreen
import com.pyera.app.ui.savings.SavingsScreen
import com.pyera.app.ui.bills.BillsScreen
import com.pyera.app.ui.investments.InvestmentsScreen
import com.pyera.app.ui.chat.ChatScreen
import com.pyera.app.ui.profile.ProfileScreen
import com.pyera.app.ui.analysis.AnalysisScreen
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

                // Add Transaction
                composable(
                    route = Screen.AddTransaction.route,
                    enterTransition = { slideInHorizontally(initialOffsetX = { 1000 }) + fadeIn() },
                    exitTransition = { slideOutHorizontally(targetOffsetX = { -1000 }) + fadeOut() }
                ) {
                    AddTransactionScreen(navController = bottomNavController)
                }
            }
        }
    }
}
