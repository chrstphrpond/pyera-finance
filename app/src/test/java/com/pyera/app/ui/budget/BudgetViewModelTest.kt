package com.pyera.app.ui.budget

import app.cash.turbine.test
import com.google.firebase.auth.FirebaseUser
import com.pyera.app.data.local.entity.BudgetEntity
import com.pyera.app.data.local.entity.BudgetPeriod
import com.pyera.app.data.local.entity.BudgetStatus
import com.pyera.app.data.local.entity.BudgetSummary
import com.pyera.app.data.local.entity.BudgetWithSpending
import com.pyera.app.data.local.entity.CategoryEntity
import com.pyera.app.domain.repository.AuthRepository
import com.pyera.app.domain.repository.BudgetRepository
import com.pyera.app.domain.repository.CategoryRepository
import com.pyera.app.test.createBudgetSummary
import com.pyera.app.test.createBudgetWithSpending
import com.pyera.app.test.createCategory
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class BudgetViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    
    private lateinit var budgetRepository: BudgetRepository
    private lateinit var categoryRepository: CategoryRepository
    private lateinit var authRepository: AuthRepository
    private lateinit var mockFirebaseUser: FirebaseUser
    
    private lateinit var viewModel: BudgetViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        budgetRepository = mockk(relaxed = true)
        categoryRepository = mockk(relaxed = true)
        authRepository = mockk(relaxed = true)
        mockFirebaseUser = mockk(relaxed = true)
        
        // Default Firebase auth setup
        every { authRepository.currentUser } returns mockFirebaseUser
        every { mockFirebaseUser.uid } returns "test_user"
        
        // Default flows
        every { categoryRepository.getAllCategories() } returns flowOf(emptyList())
        every { budgetRepository.getBudgetsWithSpending(any(), any(), any()) } returns flowOf(emptyList())
        every { budgetRepository.getActiveBudgetCount(any()) } returns flowOf(0)
        every { budgetRepository.calculatePeriodDates(any(), any()) } returns Pair(0L, Long.MAX_VALUE)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ==================== Initial State Tests ====================

    @Test
    fun `initial state has default values`() = runTest(testDispatcher) {
        // When
        viewModel = createViewModel()
        
        // Then
        viewModel.state.test {
            val state = awaitItem()
            assertFalse(state.isLoading)
            assertTrue(state.items.isEmpty())
            assertEquals("", state.currentPeriod)
        }
    }

    @Test
    fun `initial selected period is MONTHLY`() = runTest(testDispatcher) {
        // When
        viewModel = createViewModel()
        
        // Then
        viewModel.selectedPeriod.test {
            val period = awaitItem()
            assertEquals(BudgetPeriod.MONTHLY, period)
        }
    }

    @Test
    fun `initial status filter is null`() = runTest(testDispatcher) {
        // When
        viewModel = createViewModel()
        
        // Then
        viewModel.statusFilter.test {
            val filter = awaitItem()
            assertNull(filter)
        }
    }

    // ==================== Period Selection Tests ====================

    @Test
    fun `setPeriod updates selectedPeriod`() = runTest(testDispatcher) {
        // Given
        viewModel = createViewModel()
        
        // When
        viewModel.setPeriod(BudgetPeriod.WEEKLY)
        
        // Then
        viewModel.selectedPeriod.test {
            val period = awaitItem()
            assertEquals(BudgetPeriod.WEEKLY, period)
        }
    }

    @Test
    fun `setPeriod updates state currentPeriod string`() = runTest(testDispatcher) {
        // Given
        viewModel = createViewModel()
        
        // When
        viewModel.setPeriod(BudgetPeriod.DAILY)
        
        // Then
        viewModel.state.test {
            val state = awaitItem()
            assertEquals("Daily", state.currentPeriod)
        }
    }

    @Test
    fun `setPeriod with YEARLY updates currentPeriod string`() = runTest(testDispatcher) {
        // Given
        viewModel = createViewModel()
        
        // When
        viewModel.setPeriod(BudgetPeriod.YEARLY)
        
        // Then
        viewModel.state.test {
            val state = awaitItem()
            assertEquals("Yearly", state.currentPeriod)
        }
    }

    // ==================== Status Filter Tests ====================

    @Test
    fun `setStatusFilter updates filter`() = runTest(testDispatcher) {
        // Given
        viewModel = createViewModel()
        
        // When
        viewModel.setStatusFilter(BudgetStatus.OVER_BUDGET)
        
        // Then
        viewModel.statusFilter.test {
            val filter = awaitItem()
            assertEquals(BudgetStatus.OVER_BUDGET, filter)
        }
    }

    @Test
    fun `setStatusFilter with null clears filter`() = runTest(testDispatcher) {
        // Given
        viewModel = createViewModel()
        viewModel.setStatusFilter(BudgetStatus.WARNING)
        
        // When
        viewModel.setStatusFilter(null)
        
        // Then
        viewModel.statusFilter.test {
            val filter = awaitItem()
            assertNull(filter)
        }
    }

    // ==================== Budget Data Tests ====================

    @Test
    fun `budgets flow updates with repository data`() = runTest(testDispatcher) {
        // Given
        val budgets = listOf(
            createBudgetWithSpending(id = 1, categoryName = "Food", spentAmount = 200.0),
            createBudgetWithSpending(id = 2, categoryName = "Transport", spentAmount = 100.0)
        )
        every { budgetRepository.getBudgetsWithSpending(any(), any(), any()) } returns flowOf(budgets)
        
        // When
        viewModel = createViewModel()
        advanceUntilIdle()
        
        // Then
        viewModel.budgets.test {
            val result = awaitItem()
            assertEquals(2, result.size)
            assertEquals("Food", result[0].categoryName)
            assertEquals("Transport", result[1].categoryName)
        }
    }

    @Test
    fun `state maps budgets to BudgetItems correctly`() = runTest(testDispatcher) {
        // Given
        val budgets = listOf(
            createBudgetWithSpending(
                id = 1,
                categoryName = "Food",
                categoryColor = 0xFF00FF,
                categoryIcon = "🍔",
                amount = 1000.0,
                spentAmount = 200.0
            )
        )
        every { budgetRepository.getBudgetsWithSpending(any(), any(), any()) } returns flowOf(budgets)
        
        // When
        viewModel = createViewModel()
        advanceUntilIdle()
        
        // Then
        viewModel.state.test {
            val state = awaitItem()
            assertEquals(1, state.items.size)
            val item = state.items[0]
            assertEquals("Food", item.category.name)
            assertEquals(0xFF00FF, item.category.color)
            assertEquals("🍔", item.category.icon)
            assertEquals(1000.0, item.budgetAmount, 0.01)
            assertEquals(200.0, item.spentAmount, 0.01)
        }
    }

    @Test
    fun `BudgetItem progress calculation is correct`() = runTest(testDispatcher) {
        // Given
        val budgetItem = BudgetItem(
            category = createCategory(),
            budgetAmount = 1000.0,
            spentAmount = 250.0
        )
        
        // Then
        assertEquals(0.25f, budgetItem.progress, 0.01f)
        assertEquals(750.0, budgetItem.remaining, 0.01)
    }

    @Test
    fun `BudgetItem progress is 0 when budgetAmount is 0`() = runTest(testDispatcher) {
        // Given
        val budgetItem = BudgetItem(
            category = createCategory(),
            budgetAmount = 0.0,
            spentAmount = 100.0
        )
        
        // Then
        assertEquals(0f, budgetItem.progress, 0.01f)
    }

    // ==================== Budget Summary Tests ====================

    @Test
    fun `budgetSummary updates with repository data`() = runTest(testDispatcher) {
        // Given
        val summary = createBudgetSummary(
            totalBudgets = 5,
            totalBudgetAmount = 5000.0,
            totalSpent = 2000.0
        )
        every { budgetRepository.getBudgetSummary(any(), any(), any()) } returns flowOf(summary)
        
        // When
        viewModel = createViewModel()
        advanceUntilIdle()
        
        // Then
        viewModel.budgetSummary.test {
            val result = awaitItem()
            assertNotNull(result)
            assertEquals(5, result?.totalBudgets)
            assertEquals(5000.0, result?.totalBudgetAmount, 0.01)
            assertEquals(2000.0, result?.totalSpent, 0.01)
        }
    }

    // ==================== Create Budget Tests ====================

    @Test
    fun `createBudget calls repository with correct data`() = runTest(testDispatcher) {
        // Given
        viewModel = createViewModel()
        coEvery { budgetRepository.createBudget(any()) } returns 1L
        
        // When
        viewModel.createBudget(
            categoryId = 5,
            amount = 1000.0,
            period = BudgetPeriod.MONTHLY,
            alertThreshold = 0.8f
        )
        advanceUntilIdle()
        
        // Then
        coVerify {
            budgetRepository.createBudget(
                match { budget ->
                    budget.userId == "test_user" &&
                    budget.categoryId == 5 &&
                    budget.amount == 1000.0 &&
                    budget.period == BudgetPeriod.MONTHLY &&
                    budget.alertThreshold == 0.8f &&
                    budget.isActive
                }
            )
        }
    }

    @Test
    fun `createBudget sets success state on completion`() = runTest(testDispatcher) {
        // Given
        viewModel = createViewModel()
        coEvery { budgetRepository.createBudget(any()) } returns 1L
        
        // When
        viewModel.createBudget(categoryId = 1, amount = 1000.0)
        advanceUntilIdle()
        
        // Then
        viewModel.createBudgetState.test {
            val state = awaitItem()
            assertTrue(state.success)
            assertNull(state.error)
        }
    }

    @Test
    fun `createBudget sets error state on IOException`() = runTest(testDispatcher) {
        // Given
        viewModel = createViewModel()
        coEvery { budgetRepository.createBudget(any()) } throws IOException("Network error")
        
        // When
        viewModel.createBudget(categoryId = 1, amount = 1000.0)
        advanceUntilIdle()
        
        // Then
        viewModel.createBudgetState.test {
            val state = awaitItem()
            assertFalse(state.success)
            assertNotNull(state.error)
            assertTrue(state.error?.contains("Network error") == true)
        }
    }

    // ==================== Update Budget Tests ====================

    @Test
    fun `updateBudget calls repository`() = runTest(testDispatcher) {
        // Given
        viewModel = createViewModel()
        val budget = BudgetEntity(
            id = 1,
            userId = "test_user",
            categoryId = 1,
            amount = 2000.0,
            period = BudgetPeriod.MONTHLY
        )
        coEvery { budgetRepository.updateBudget(any()) } returns Unit
        
        // When
        viewModel.updateBudget(budget)
        advanceUntilIdle()
        
        // Then
        coVerify { budgetRepository.updateBudget(budget) }
    }

    // ==================== Delete Budget Tests ====================

    @Test
    fun `deleteBudget calls repository`() = runTest(testDispatcher) {
        // Given
        viewModel = createViewModel()
        val budget = BudgetEntity(
            id = 1,
            userId = "test_user",
            categoryId = 1,
            amount = 1000.0
        )
        coEvery { budgetRepository.deleteBudget(any()) } returns Unit
        
        // When
        viewModel.deleteBudget(budget)
        advanceUntilIdle()
        
        // Then
        coVerify { budgetRepository.deleteBudget(budget) }
    }

    // ==================== Deactivate Budget Tests ====================

    @Test
    fun `deactivateBudget calls repository`() = runTest(testDispatcher) {
        // Given
        viewModel = createViewModel()
        coEvery { budgetRepository.deactivateBudget(any()) } returns Unit
        
        // When
        viewModel.deactivateBudget(1)
        advanceUntilIdle()
        
        // Then
        coVerify { budgetRepository.deactivateBudget(1) }
    }

    // ==================== Set Budget For Category Tests ====================

    @Test
    fun `setBudgetForCategory calls repository`() = runTest(testDispatcher) {
        // Given
        viewModel = createViewModel()
        coEvery { 
            budgetRepository.setBudgetForCategory(any(), any(), any(), any()) 
        } returns Unit
        
        // When
        viewModel.setBudgetForCategory(categoryId = 5, amount = 1500.0)
        advanceUntilIdle()
        
        // Then
        coVerify { 
            budgetRepository.setBudgetForCategory(
                categoryId = 5,
                amount = 1500.0,
                period = BudgetPeriod.MONTHLY,
                userId = "test_user"
            )
        }
    }

    @Test
    fun `setBudget delegates to setBudgetForCategory`() = runTest(testDispatcher) {
        // Given
        viewModel = createViewModel()
        coEvery { 
            budgetRepository.setBudgetForCategory(any(), any(), any(), any()) 
        } returns Unit
        
        // When
        viewModel.setBudget(categoryId = 3, amount = 2000.0)
        advanceUntilIdle()
        
        // Then
        coVerify { 
            budgetRepository.setBudgetForCategory(
                categoryId = 3,
                amount = 2000.0,
                any(),
                any()
            )
        }
    }

    // ==================== Validation Tests ====================

    @Test
    fun `validateBudgetAmount returns Success for valid amount`() = runTest(testDispatcher) {
        // Given
        viewModel = createViewModel()
        
        // When
        val result = viewModel.validateBudgetAmount("1000.00")
        
        // Then
        assertTrue(result is BudgetValidationResult.Success)
    }

    @Test
    fun `validateBudgetAmount returns Error for blank amount`() = runTest(testDispatcher) {
        // Given
        viewModel = createViewModel()
        
        // When
        val result = viewModel.validateBudgetAmount("")
        
        // Then
        assertTrue(result is BudgetValidationResult.Error)
        assertEquals("Amount is required", (result as BudgetValidationResult.Error).message)
    }

    @Test
    fun `validateBudgetAmount returns Error for invalid amount`() = runTest(testDispatcher) {
        // Given
        viewModel = createViewModel()
        
        // When
        val result = viewModel.validateBudgetAmount("abc")
        
        // Then
        assertTrue(result is BudgetValidationResult.Error)
        assertEquals("Invalid amount", (result as BudgetValidationResult.Error).message)
    }

    @Test
    fun `validateBudgetAmount returns Error for zero amount`() = runTest(testDispatcher) {
        // Given
        viewModel = createViewModel()
        
        // When
        val result = viewModel.validateBudgetAmount("0")
        
        // Then
        assertTrue(result is BudgetValidationResult.Error)
        assertEquals("Amount must be greater than 0", (result as BudgetValidationResult.Error).message)
    }

    @Test
    fun `validateBudgetAmount returns Error for too large amount`() = runTest(testDispatcher) {
        // Given
        viewModel = createViewModel()
        
        // When
        val result = viewModel.validateBudgetAmount("9999999999")
        
        // Then
        assertTrue(result is BudgetValidationResult.Error)
        assertEquals("Amount is too large", (result as BudgetValidationResult.Error).message)
    }

    @Test
    fun `validateBudgetCategory returns Success for valid category`() = runTest(testDispatcher) {
        // Given
        viewModel = createViewModel()
        
        // When
        val result = viewModel.validateBudgetCategory(5)
        
        // Then
        assertTrue(result is BudgetValidationResult.Success)
    }

    @Test
    fun `validateBudgetCategory returns Error for null category`() = runTest(testDispatcher) {
        // Given
        viewModel = createViewModel()
        
        // When
        val result = viewModel.validateBudgetCategory(null)
        
        // Then
        assertTrue(result is BudgetValidationResult.Error)
        assertEquals("Please select a category", (result as BudgetValidationResult.Error).message)
    }

    @Test
    fun `validateBudgetCategory returns Error for zero category`() = runTest(testDispatcher) {
        // Given
        viewModel = createViewModel()
        
        // When
        val result = viewModel.validateBudgetCategory(0)
        
        // Then
        assertTrue(result is BudgetValidationResult.Error)
    }

    // ==================== Create Budget State Tests ====================

    @Test
    fun `updateCreateState updates state correctly`() = runTest(testDispatcher) {
        // Given
        viewModel = createViewModel()
        
        // When
        viewModel.updateCreateState(
            categoryId = 5,
            amount = "1000",
            period = BudgetPeriod.WEEKLY,
            alertThreshold = 0.9f
        )
        
        // Then
        viewModel.createBudgetState.test {
            val state = awaitItem()
            assertEquals(5, state.categoryId)
            assertEquals("1000", state.amount)
            assertEquals(BudgetPeriod.WEEKLY, state.period)
            assertEquals(0.9f, state.alertThreshold, 0.01f)
        }
    }

    @Test
    fun `resetCreateState resets to defaults`() = runTest(testDispatcher) {
        // Given
        viewModel = createViewModel()
        viewModel.updateCreateState(categoryId = 5, amount = "1000")
        
        // When
        viewModel.resetCreateState()
        
        // Then
        viewModel.createBudgetState.test {
            val state = awaitItem()
            assertNull(state.categoryId)
            assertEquals("", state.amount)
            assertEquals(BudgetPeriod.MONTHLY, state.period)
            assertFalse(state.success)
            assertNull(state.error)
        }
    }

    // ==================== Error Handling Tests ====================

    @Test
    fun `clearError removes error`() = runTest(testDispatcher) {
        // Given
        viewModel = createViewModel()
        coEvery { budgetRepository.createBudget(any()) } throws IOException("Network error")
        viewModel.createBudget(categoryId = 1, amount = 1000.0)
        advanceUntilIdle()
        
        // When
        viewModel.clearError()
        
        // Then
        viewModel.error.test {
            val error = awaitItem()
            assertNull(error)
        }
    }

    // ==================== Select Budget Tests ====================

    @Test
    fun `selectBudget updates selectedBudget`() = runTest(testDispatcher) {
        // Given
        viewModel = createViewModel()
        val budget = createBudgetWithSpending(id = 1, categoryName = "Food")
        
        // When
        viewModel.selectBudget(budget)
        
        // Then
        viewModel.selectedBudget.test {
            val result = awaitItem()
            assertNotNull(result)
            assertEquals(1, result?.id)
            assertEquals("Food", result?.categoryName)
        }
    }

    @Test
    fun `selectBudget with null clears selection`() = runTest(testDispatcher) {
        // Given
        viewModel = createViewModel()
        viewModel.selectBudget(createBudgetWithSpending())
        
        // When
        viewModel.selectBudget(null)
        
        // Then
        viewModel.selectedBudget.test {
            val result = awaitItem()
            assertNull(result)
        }
    }

    // ==================== Categories Flow Tests ====================

    @Test
    fun `categories flow updates with repository data`() = runTest(testDispatcher) {
        // Given
        val categories = listOf(
            createCategory(id = 1, name = "Food"),
            createCategory(id = 2, name = "Transport")
        )
        every { categoryRepository.getAllCategories() } returns flowOf(categories)
        
        // When
        viewModel = createViewModel()
        advanceUntilIdle()
        
        // Then
        viewModel.categories.test {
            val result = awaitItem()
            assertEquals(2, result.size)
            assertEquals("Food", result[0].name)
            assertEquals("Transport", result[1].name)
        }
    }

    // ==================== Refresh Tests ====================

    @Test
    fun `refreshBudgets updates date range`() = runTest(testDispatcher) {
        // Given
        viewModel = createViewModel()
        
        // When
        viewModel.refreshBudgets()
        advanceUntilIdle()
        
        // Then - verify no error is thrown and method completes
        viewModel.isLoading.test {
            val loading = awaitItem()
            assertFalse(loading)
        }
    }

    private fun createViewModel(): BudgetViewModel {
        return BudgetViewModel(
            budgetRepository = budgetRepository,
            categoryRepository = categoryRepository,
            authRepository = authRepository
        )
    }
}
