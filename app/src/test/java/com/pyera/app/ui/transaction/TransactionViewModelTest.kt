package com.pyera.app.ui.transaction

import android.database.sqlite.SQLiteException
import app.cash.turbine.test
import com.pyera.app.data.local.entity.AccountEntity
import com.pyera.app.data.local.entity.CategoryEntity
import com.pyera.app.data.local.entity.TransactionEntity
import com.pyera.app.domain.repository.AccountRepository
import com.pyera.app.domain.repository.AuthRepository
import com.pyera.app.domain.repository.CategoryRepository
import com.pyera.app.domain.repository.OcrRepository
import com.pyera.app.domain.repository.TransactionRepository
import com.pyera.app.domain.repository.TransactionRuleRepository
import com.pyera.app.domain.smart.SmartCategorizer
import com.pyera.app.test.createAccount
import com.pyera.app.test.createCategory
import com.pyera.app.test.createExpenseTransaction
import com.pyera.app.test.createIncomeTransaction
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class TransactionViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    
    private lateinit var transactionRepository: TransactionRepository
    private lateinit var categoryRepository: CategoryRepository
    private lateinit var accountRepository: AccountRepository
    private lateinit var authRepository: AuthRepository
    private lateinit var ocrRepository: OcrRepository
    private lateinit var smartCategorizer: SmartCategorizer
    private lateinit var transactionRuleRepository: TransactionRuleRepository
    
    private lateinit var viewModel: TransactionViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        transactionRepository = mockk(relaxed = true)
        categoryRepository = mockk(relaxed = true)
        accountRepository = mockk(relaxed = true)
        authRepository = mockk(relaxed = true)
        ocrRepository = mockk(relaxed = true)
        smartCategorizer = mockk(relaxed = true)
        transactionRuleRepository = mockk(relaxed = true)
        
        // Default flows
        every { transactionRepository.getAllTransactions() } returns flowOf(emptyList())
        every { categoryRepository.getAllCategories() } returns flowOf(emptyList())
        every { accountRepository.getActiveAccounts() } returns flowOf(emptyList())
        every { authRepository.currentUser } returns mockk { every { uid } returns "test_user" }
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
            assertTrue(state.transactions.isEmpty())
            assertTrue(state.filteredTransactions.isEmpty())
            assertTrue(state.categories.isEmpty())
            assertTrue(state.accounts.isEmpty())
            assertNull(state.defaultAccount)
            assertFalse(state.isLoading)
            assertFalse(state.isRefreshing)
            assertNull(state.error)
            assertEquals("", state.searchQuery)
            assertEquals(TransactionFilter.ALL, state.selectedFilter)
            assertEquals(TransactionSort.DATE_DESC, state.selectedSort)
            assertEquals(DateRangeFilter.ALL, state.dateRangeFilter)
            assertNull(state.selectedCategoryId)
        }
    }

    @Test
    fun `state updates with transactions data`() = runTest(testDispatcher) {
        // Given
        val transactions = listOf(
            createExpenseTransaction(id = 1, amount = 100.0),
            createIncomeTransaction(id = 2, amount = 1000.0)
        )
        every { transactionRepository.getAllTransactions() } returns flowOf(transactions)
        
        // When
        viewModel = createViewModel()
        advanceUntilIdle()
        
        // Then
        viewModel.state.test {
            val state = awaitItem()
            assertEquals(2, state.transactions.size)
            assertEquals(2, state.filteredTransactions.size)
            assertFalse(state.isLoading)
        }
    }

    // ==================== Search Tests ====================

    @Test
    fun `searchTransactions filters by query`() = runTest(testDispatcher) {
        // Given
        val transactions = listOf(
            createExpenseTransaction(id = 1, note = "Grocery Store", amount = 100.0),
            createExpenseTransaction(id = 2, note = "Gas Station", amount = 50.0),
            createIncomeTransaction(id = 3, note = "Salary", amount = 1000.0)
        )
        val categories = listOf(
            createCategory(id = 1, name = "Food"),
            createCategory(id = 2, name = "Transport")
        )
        
        every { transactionRepository.getAllTransactions() } returns flowOf(transactions)
        every { categoryRepository.getAllCategories() } returns flowOf(categories)
        
        viewModel = createViewModel()
        advanceUntilIdle()
        
        // When
        viewModel.searchTransactions("grocery")
        
        // Then
        viewModel.state.test {
            val state = awaitItem()
            assertEquals("grocery", state.searchQuery)
            assertEquals(1, state.filteredTransactions.size)
            assertEquals("Grocery Store", state.filteredTransactions.first().note)
        }
    }

    @Test
    fun `search is case insensitive`() = runTest(testDispatcher) {
        // Given
        val transactions = listOf(
            createExpenseTransaction(id = 1, note = "GROCERY Store")
        )
        every { transactionRepository.getAllTransactions() } returns flowOf(transactions)
        
        viewModel = createViewModel()
        advanceUntilIdle()
        
        // When
        viewModel.searchTransactions("grocery")
        
        // Then
        viewModel.state.test {
            val state = awaitItem()
            assertEquals(1, state.filteredTransactions.size)
        }
    }

    // ==================== Filter Tests ====================

    @Test
    fun `filterTransactions filters by INCOME type`() = runTest(testDispatcher) {
        // Given
        val transactions = listOf(
            createExpenseTransaction(id = 1, amount = 100.0),
            createIncomeTransaction(id = 2, amount = 1000.0),
            createIncomeTransaction(id = 3, amount = 500.0)
        )
        every { transactionRepository.getAllTransactions() } returns flowOf(transactions)
        
        viewModel = createViewModel()
        advanceUntilIdle()
        
        // When
        viewModel.filterTransactions(TransactionFilter.INCOME)
        
        // Then
        viewModel.state.test {
            val state = awaitItem()
            assertEquals(TransactionFilter.INCOME, state.selectedFilter)
            assertEquals(2, state.filteredTransactions.size)
            assertTrue(state.filteredTransactions.all { it.type == "INCOME" })
        }
    }

    @Test
    fun `filterTransactions filters by EXPENSE type`() = runTest(testDispatcher) {
        // Given
        val transactions = listOf(
            createExpenseTransaction(id = 1, amount = 100.0),
            createExpenseTransaction(id = 2, amount = 50.0),
            createIncomeTransaction(id = 3, amount = 1000.0)
        )
        every { transactionRepository.getAllTransactions() } returns flowOf(transactions)
        
        viewModel = createViewModel()
        advanceUntilIdle()
        
        // When
        viewModel.filterTransactions(TransactionFilter.EXPENSE)
        
        // Then
        viewModel.state.test {
            val state = awaitItem()
            assertEquals(TransactionFilter.EXPENSE, state.selectedFilter)
            assertEquals(2, state.filteredTransactions.size)
            assertTrue(state.filteredTransactions.all { it.type == "EXPENSE" })
        }
    }

    @Test
    fun `filterByCategory filters by categoryId`() = runTest(testDispatcher) {
        // Given
        val transactions = listOf(
            createExpenseTransaction(id = 1, categoryId = 1),
            createExpenseTransaction(id = 2, categoryId = 2),
            createExpenseTransaction(id = 3, categoryId = 1)
        )
        every { transactionRepository.getAllTransactions() } returns flowOf(transactions)
        
        viewModel = createViewModel()
        advanceUntilIdle()
        
        // When
        viewModel.filterByCategory(1)
        
        // Then
        viewModel.state.test {
            val state = awaitItem()
            assertEquals(1, state.selectedCategoryId)
            assertEquals(2, state.filteredTransactions.size)
            assertTrue(state.filteredTransactions.all { it.categoryId == 1 })
        }
    }

    @Test
    fun `clearFilters resets all filters`() = runTest(testDispatcher) {
        // Given
        val transactions = listOf(
            createExpenseTransaction(id = 1, note = "Test"),
            createIncomeTransaction(id = 2, note = "Salary")
        )
        every { transactionRepository.getAllTransactions() } returns flowOf(transactions)
        
        viewModel = createViewModel()
        advanceUntilIdle()
        
        // Apply some filters
        viewModel.searchTransactions("test")
        viewModel.filterTransactions(TransactionFilter.EXPENSE)
        viewModel.sortTransactions(TransactionSort.AMOUNT_ASC)
        
        // When
        viewModel.clearFilters()
        
        // Then
        viewModel.state.test {
            val state = awaitItem()
            assertEquals("", state.searchQuery)
            assertEquals(TransactionFilter.ALL, state.selectedFilter)
            assertEquals(TransactionSort.DATE_DESC, state.selectedSort)
            assertNull(state.selectedCategoryId)
            assertEquals(2, state.filteredTransactions.size)
        }
    }

    // ==================== Sort Tests ====================

    @Test
    fun `sortTransactions sorts by amount descending`() = runTest(testDispatcher) {
        // Given
        val transactions = listOf(
            createExpenseTransaction(id = 1, amount = 100.0),
            createExpenseTransaction(id = 2, amount = 500.0),
            createExpenseTransaction(id = 3, amount = 50.0)
        )
        every { transactionRepository.getAllTransactions() } returns flowOf(transactions)
        
        viewModel = createViewModel()
        advanceUntilIdle()
        
        // When
        viewModel.sortTransactions(TransactionSort.AMOUNT_DESC)
        
        // Then
        viewModel.state.test {
            val state = awaitItem()
            assertEquals(TransactionSort.AMOUNT_DESC, state.selectedSort)
            assertEquals(500.0, state.filteredTransactions[0].amount, 0.01)
            assertEquals(100.0, state.filteredTransactions[1].amount, 0.01)
            assertEquals(50.0, state.filteredTransactions[2].amount, 0.01)
        }
    }

    @Test
    fun `sortTransactions sorts by amount ascending`() = runTest(testDispatcher) {
        // Given
        val transactions = listOf(
            createExpenseTransaction(id = 1, amount = 100.0),
            createExpenseTransaction(id = 2, amount = 500.0),
            createExpenseTransaction(id = 3, amount = 50.0)
        )
        every { transactionRepository.getAllTransactions() } returns flowOf(transactions)
        
        viewModel = createViewModel()
        advanceUntilIdle()
        
        // When
        viewModel.sortTransactions(TransactionSort.AMOUNT_ASC)
        
        // Then
        viewModel.state.test {
            val state = awaitItem()
            assertEquals(50.0, state.filteredTransactions[0].amount, 0.01)
            assertEquals(100.0, state.filteredTransactions[1].amount, 0.01)
            assertEquals(500.0, state.filteredTransactions[2].amount, 0.01)
        }
    }

    // ==================== Delete Tests ====================

    @Test
    fun `deleteTransaction by id removes transaction`() = runTest(testDispatcher) {
        // Given
        val transaction = createExpenseTransaction(id = 1)
        every { transactionRepository.getAllTransactions() } returns flowOf(listOf(transaction))
        coEvery { transactionRepository.deleteTransaction(any()) } returns Unit
        
        viewModel = createViewModel()
        advanceUntilIdle()
        
        // When
        viewModel.deleteTransaction(1)
        advanceUntilIdle()
        
        // Then
        coVerify { transactionRepository.deleteTransaction(any()) }
    }

    @Test
    fun `deleteTransaction shows error on IOException`() = runTest(testDispatcher) {
        // Given
        val transaction = createExpenseTransaction(id = 1)
        every { transactionRepository.getAllTransactions() } returns flowOf(listOf(transaction))
        coEvery { transactionRepository.deleteTransaction(any()) } throws IOException("Network error")
        
        viewModel = createViewModel()
        advanceUntilIdle()
        
        // When
        viewModel.deleteTransaction(1)
        advanceUntilIdle()
        
        // Then
        viewModel.state.test {
            val state = awaitItem()
            assertTrue(state.error?.contains("Network error") == true)
        }
    }

    // ==================== Add Transaction Tests ====================

    @Test
    fun `addTransaction with valid categoryId skips auto-categorization`() = runTest(testDispatcher) {
        // Given
        val transaction = createExpenseTransaction(id = 0, categoryId = 5)
        coEvery { transactionRepository.insertTransaction(any()) } returns Unit
        
        viewModel = createViewModel()
        
        // When
        viewModel.addTransaction(transaction)
        advanceUntilIdle()
        
        // Then
        coVerify { transactionRepository.insertTransaction(match { it.categoryId == 5 }) }
    }

    @Test
    fun `addTransaction with invalid categoryId triggers smart categorization`() = runTest(testDispatcher) {
        // Given
        val transaction = createExpenseTransaction(id = 0, categoryId = 0, note = "Uber ride")
        val category = createCategory(id = 5, name = "Transport")
        
        coEvery { transactionRepository.insertTransaction(any()) } returns Unit
        coEvery { transactionRuleRepository.applyRulesToTransaction(any(), any()) } returns null
        every { smartCategorizer.predict("Uber ride") } returns "Transport"
        coEvery { categoryRepository.getCategoryByName("Transport") } returns category
        
        viewModel = createViewModel()
        
        // When
        viewModel.addTransaction(transaction)
        advanceUntilIdle()
        
        // Then
        coVerify { transactionRepository.insertTransaction(match { it.categoryId == 5 }) }
    }

    @Test
    fun `addTransaction validates amount`() = runTest(testDispatcher) {
        // Given
        viewModel = createViewModel()
        
        // When - negative amount
        viewModel.addTransaction(createExpenseTransaction(amount = -100.0))
        
        // Then
        viewModel.state.test {
            val state = awaitItem()
            assertTrue(state.error?.contains("Amount") == true)
        }
    }

    // ==================== Grouped Transactions Tests ====================

    @Test
    fun `getGroupedTransactions groups by date`() = runTest(testDispatcher) {
        // Given
        val now = System.currentTimeMillis()
        val yesterday = now - 24 * 60 * 60 * 1000
        val twoDaysAgo = now - 2 * 24 * 60 * 60 * 1000
        
        val transactions = listOf(
            createExpenseTransaction(id = 1, date = now, note = "Today 1"),
            createExpenseTransaction(id = 2, date = now - 1000, note = "Today 2"),
            createExpenseTransaction(id = 3, date = yesterday, note = "Yesterday 1"),
            createExpenseTransaction(id = 4, date = twoDaysAgo, note = "Older")
        )
        every { transactionRepository.getAllTransactions() } returns flowOf(transactions)
        
        viewModel = createViewModel()
        advanceUntilIdle()
        
        // When
        val grouped = viewModel.getGroupedTransactions()
        
        // Then
        assertTrue(grouped.containsKey("Today"))
        assertTrue(grouped.containsKey("Yesterday"))
        assertEquals(2, grouped["Today"]?.size)
        assertEquals(1, grouped["Yesterday"]?.size)
    }

    // ==================== Legacy Compatibility Tests ====================

    @Test
    fun `setSearchQuery delegates to searchTransactions`() = runTest(testDispatcher) {
        // Given
        viewModel = createViewModel()
        
        // When
        viewModel.setSearchQuery("test")
        
        // Then
        viewModel.state.test {
            val state = awaitItem()
            assertEquals("test", state.searchQuery)
        }
    }

    @Test
    fun `setTypeFilter with ALL maps to TransactionFilter ALL`() = runTest(testDispatcher) {
        // Given
        every { transactionRepository.getAllTransactions() } returns flowOf(emptyList())
        viewModel = createViewModel()
        
        // When
        viewModel.setTypeFilter(TransactionTypeFilter.ALL)
        
        // Then
        viewModel.state.test {
            val state = awaitItem()
            assertEquals(TransactionFilter.ALL, state.selectedFilter)
        }
    }

    @Test
    fun `setTypeFilter with INCOME maps to TransactionFilter INCOME`() = runTest(testDispatcher) {
        // Given
        every { transactionRepository.getAllTransactions() } returns flowOf(emptyList())
        viewModel = createViewModel()
        
        // When
        viewModel.setTypeFilter(TransactionTypeFilter.INCOME)
        
        // Then
        viewModel.state.test {
            val state = awaitItem()
            assertEquals(TransactionFilter.INCOME, state.selectedFilter)
        }
    }

    @Test
    fun `setTypeFilter with EXPENSE maps to TransactionFilter EXPENSE`() = runTest(testDispatcher) {
        // Given
        every { transactionRepository.getAllTransactions() } returns flowOf(emptyList())
        viewModel = createViewModel()
        
        // When
        viewModel.setTypeFilter(TransactionTypeFilter.EXPENSE)
        
        // Then
        viewModel.state.test {
            val state = awaitItem()
            assertEquals(TransactionFilter.EXPENSE, state.selectedFilter)
        }
    }

    // ==================== Refresh Tests ====================

    @Test
    fun `refreshTransactions sets isRefreshing to true then false`() = runTest(testDispatcher) {
        // Given
        every { transactionRepository.getAllTransactions() } returns flowOf(emptyList())
        viewModel = createViewModel()
        
        // When
        viewModel.refreshTransactions()
        
        // Then
        viewModel.state.test {
            val loadingState = awaitItem()
            assertTrue(loadingState.isRefreshing)
        }
    }

    @Test
    fun `clearError removes error from state`() = runTest(testDispatcher) {
        // Given
        viewModel = createViewModel()
        viewModel.showError("Test error")
        
        // When
        viewModel.clearError()
        
        // Then
        viewModel.state.test {
            val state = awaitItem()
            assertNull(state.error)
        }
    }

    private fun createViewModel(): TransactionViewModel {
        return TransactionViewModel(
            transactionRepository = transactionRepository,
            categoryRepository = categoryRepository,
            accountRepository = accountRepository,
            authRepository = authRepository,
            ocrRepository = ocrRepository,
            smartCategorizer = smartCategorizer,
            transactionRuleRepository = transactionRuleRepository
        )
    }
}
