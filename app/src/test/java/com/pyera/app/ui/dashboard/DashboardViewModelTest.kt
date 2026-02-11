package com.pyera.app.ui.dashboard

import app.cash.turbine.test
import com.google.firebase.auth.FirebaseUser
import com.pyera.app.data.local.entity.CategoryEntity
import com.pyera.app.data.local.entity.SavingsGoalEntity
import com.pyera.app.data.local.entity.TransactionEntity
import com.pyera.app.domain.repository.AuthRepository
import com.pyera.app.domain.repository.BudgetRepository
import com.pyera.app.domain.repository.CategoryRepository
import com.pyera.app.domain.repository.SavingsRepository
import com.pyera.app.domain.repository.TransactionRepository
import com.pyera.app.test.createCategory
import com.pyera.app.test.createExpenseTransaction
import com.pyera.app.test.createIncomeTransaction
import com.pyera.app.test.createSavingsGoal
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    
    private lateinit var transactionRepository: TransactionRepository
    private lateinit var budgetRepository: BudgetRepository
    private lateinit var savingsRepository: SavingsRepository
    private lateinit var categoryRepository: CategoryRepository
    private lateinit var authRepository: AuthRepository
    private lateinit var mockFirebaseUser: FirebaseUser
    
    private lateinit var viewModel: DashboardViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        // Initialize mocks
        transactionRepository = mockk(relaxed = true)
        budgetRepository = mockk(relaxed = true)
        savingsRepository = mockk(relaxed = true)
        categoryRepository = mockk(relaxed = true)
        authRepository = mockk(relaxed = true)
        mockFirebaseUser = mockk(relaxed = true)
        
        // Default Firebase auth setup
        every { authRepository.currentUser } returns mockFirebaseUser
        every { mockFirebaseUser.uid } returns "test_user"
        every { mockFirebaseUser.displayName } returns "Test User"
        every { mockFirebaseUser.email } returns "test@example.com"
        
        // Default flows
        every { transactionRepository.getAllTransactions() } returns flowOf(emptyList())
        every { categoryRepository.getAllCategories() } returns flowOf(emptyList())
        every { budgetRepository.getActiveBudgetCount(any()) } returns flowOf(0)
        every { savingsRepository.getAllSavingsGoals() } returns flowOf(emptyList())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state has default values`() = runTest(testDispatcher) {
        // When
        viewModel = createViewModel()
        
        // Then
        viewModel.state.test {
            val initialState = awaitItem()
            assertEquals("User", initialState.userName)
            assertEquals(0.0, initialState.totalBalance, 0.01)
            assertEquals(0.0, initialState.totalIncome, 0.01)
            assertEquals(0.0, initialState.totalExpense, 0.01)
            assertTrue(initialState.recentTransactions.isEmpty())
            assertEquals(0, initialState.transactionCount)
            assertEquals(0, initialState.activeBudgetsCount)
            assertEquals(0, initialState.savingsGoalsCount)
            assertFalse(initialState.isLoading)
            assertNull(initialState.error)
        }
    }

    @Test
    fun `state updates with transactions data`() = runTest(testDispatcher) {
        // Given
        val transactions = listOf(
            createIncomeTransaction(id = 1, amount = 5000.0),
            createExpenseTransaction(id = 2, amount = 1000.0),
            createExpenseTransaction(id = 3, amount = 500.0)
        )
        val categories = listOf(
            createCategory(id = 1, name = "Food"),
            createCategory(id = 10, name = "Salary", type = "INCOME")
        )
        
        every { transactionRepository.getAllTransactions() } returns flowOf(transactions)
        every { categoryRepository.getAllCategories() } returns flowOf(categories)
        every { budgetRepository.getActiveBudgetCount("test_user") } returns flowOf(2)
        every { savingsRepository.getAllSavingsGoals() } returns flowOf(listOf(createSavingsGoal()))
        
        // When
        viewModel = createViewModel()
        advanceUntilIdle()
        
        // Then
        viewModel.state.test {
            val state = awaitItem()
            assertEquals(5000.0, state.totalIncome, 0.01)
            assertEquals(1500.0, state.totalExpense, 0.01)
            assertEquals(3500.0, state.totalBalance, 0.01)
            assertEquals(3, state.transactionCount)
            assertEquals(2, state.activeBudgetsCount)
            assertEquals(1, state.savingsGoalsCount)
            assertEquals("Test User", state.userName)
        }
    }

    @Test
    fun `state shows email when displayName is null`() = runTest(testDispatcher) {
        // Given
        every { mockFirebaseUser.displayName } returns null
        
        // When
        viewModel = createViewModel()
        advanceUntilIdle()
        
        // Then
        viewModel.state.test {
            val state = awaitItem()
            assertEquals("test@example.com", state.userName)
        }
    }

    @Test
    fun `state shows email when displayName is empty`() = runTest(testDispatcher) {
        // Given
        every { mockFirebaseUser.displayName } returns ""
        
        // When
        viewModel = createViewModel()
        advanceUntilIdle()
        
        // Then
        viewModel.state.test {
            val state = awaitItem()
            assertEquals("test@example.com", state.userName)
        }
    }

    @Test
    fun `state shows User when no Firebase user`() = runTest(testDispatcher) {
        // Given
        every { authRepository.currentUser } returns null
        
        // When
        viewModel = createViewModel()
        advanceUntilIdle()
        
        // Then
        viewModel.state.test {
            val state = awaitItem()
            assertEquals("User", state.userName)
        }
    }

    @Test
    fun `recent transactions limited to 5 most recent`() = runTest(testDispatcher) {
        // Given
        val now = System.currentTimeMillis()
        val transactions = (1..10).map { index ->
            createExpenseTransaction(
                id = index.toLong(),
                amount = 100.0 * index,
                date = now - (index * 1000) // Decreasing timestamps
            )
        }
        val categories = listOf(createCategory())
        
        every { transactionRepository.getAllTransactions() } returns flowOf(transactions)
        every { categoryRepository.getAllCategories() } returns flowOf(categories)
        
        // When
        viewModel = createViewModel()
        advanceUntilIdle()
        
        // Then
        viewModel.state.test {
            val state = awaitItem()
            assertEquals(5, state.recentTransactions.size)
            // Most recent should be id=1 (newest date)
            assertEquals(1L, state.recentTransactions.first().id)
        }
    }

    @Test
    fun `transaction UI model has correct format`() = runTest(testDispatcher) {
        // Given
        val transactions = listOf(
            createExpenseTransaction(id = 1, amount = 1500.50, note = "Grocery Shopping")
        )
        val categories = listOf(createCategory(id = 1, name = "Food"))
        
        every { transactionRepository.getAllTransactions() } returns flowOf(transactions)
        every { categoryRepository.getAllCategories() } returns flowOf(categories)
        
        // When
        viewModel = createViewModel()
        advanceUntilIdle()
        
        // Then
        viewModel.state.test {
            val state = awaitItem()
            val transaction = state.recentTransactions.first()
            assertEquals(1L, transaction.id)
            assertEquals("Grocery Shopping", transaction.title)
            assertEquals("Food", transaction.category)
            assertEquals("1,500.50", transaction.amount)
            assertFalse(transaction.isIncome)
        }
    }

    @Test
    fun `transaction shows Uncategorized when no category`() = runTest(testDispatcher) {
        // Given
        val transactions = listOf(
            createExpenseTransaction(id = 1, categoryId = null)
        )
        val categories = emptyList<CategoryEntity>()
        
        every { transactionRepository.getAllTransactions() } returns flowOf(transactions)
        every { categoryRepository.getAllCategories() } returns flowOf(categories)
        
        // When
        viewModel = createViewModel()
        advanceUntilIdle()
        
        // Then
        viewModel.state.test {
            val state = awaitItem()
            assertEquals("Uncategorized", state.recentTransactions.first().category)
        }
    }

    @Test
    fun `transaction shows default title when note is blank`() = runTest(testDispatcher) {
        // Given
        val transactions = listOf(
            createExpenseTransaction(id = 1, note = "")
        )
        val categories = listOf(createCategory())
        
        every { transactionRepository.getAllTransactions() } returns flowOf(transactions)
        every { categoryRepository.getAllCategories() } returns flowOf(categories)
        
        // When
        viewModel = createViewModel()
        advanceUntilIdle()
        
        // Then
        viewModel.state.test {
            val state = awaitItem()
            assertEquals("Transaction", state.recentTransactions.first().title)
        }
    }

    @Test
    fun `refresh triggers data reload`() = runTest(testDispatcher) {
        // Given
        val transactions = listOf(createIncomeTransaction())
        val categories = listOf(createCategory())
        
        every { transactionRepository.getAllTransactions() } returns flowOf(transactions)
        every { categoryRepository.getAllCategories() } returns flowOf(categories)
        
        viewModel = createViewModel()
        advanceUntilIdle()
        
        // When
        viewModel.refresh()
        
        // Then - verify state is still correct after refresh
        viewModel.state.test {
            val state = awaitItem()
            assertEquals(1, state.transactionCount)
        }
    }

    @Test
    fun `zero budget count when userId is blank`() = runTest(testDispatcher) {
        // Given
        every { mockFirebaseUser.uid } returns ""
        
        // When
        viewModel = createViewModel()
        advanceUntilIdle()
        
        // Then
        viewModel.state.test {
            val state = awaitItem()
            assertEquals(0, state.activeBudgetsCount)
        }
    }

    @Test
    fun `correctly calculates zero balance with no transactions`() = runTest(testDispatcher) {
        // Given
        every { transactionRepository.getAllTransactions() } returns flowOf(emptyList())
        every { categoryRepository.getAllCategories() } returns flowOf(emptyList())
        
        // When
        viewModel = createViewModel()
        advanceUntilIdle()
        
        // Then
        viewModel.state.test {
            val state = awaitItem()
            assertEquals(0.0, state.totalBalance, 0.01)
            assertEquals(0.0, state.totalIncome, 0.01)
            assertEquals(0.0, state.totalExpense, 0.01)
        }
    }

    private fun createViewModel(): DashboardViewModel {
        return DashboardViewModel(
            transactionRepository = transactionRepository,
            budgetRepository = budgetRepository,
            savingsRepository = savingsRepository,
            categoryRepository = categoryRepository,
            authRepository = authRepository
        )
    }
}
