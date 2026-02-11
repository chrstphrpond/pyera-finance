package com.pyera.app.ui.transaction

import app.cash.turbine.test
import com.pyera.app.data.local.entity.TransactionEntity
import com.pyera.app.domain.repository.TransactionRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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

@OptIn(ExperimentalCoroutinesApi::class)
class AddTransactionViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    
    private lateinit var transactionRepository: TransactionRepository
    private lateinit var viewModel: AddTransactionViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        transactionRepository = mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ==================== Initial State Tests ====================

    @Test
    fun `initial validation state has no errors`() = runTest(testDispatcher) {
        // When
        viewModel = AddTransactionViewModel(transactionRepository)
        
        // Then
        viewModel.validationState.test {
            val state = awaitItem()
            assertNull(state.amountError)
            assertNull(state.descriptionError)
            assertNull(state.categoryError)
        }
    }

    @Test
    fun `initial save state is Idle`() = runTest(testDispatcher) {
        // When
        viewModel = AddTransactionViewModel(transactionRepository)
        
        // Then
        viewModel.saveState.test {
            val state = awaitItem()
            assertTrue(state is SaveState.Idle)
        }
    }

    // ==================== Amount Validation Tests ====================

    @Test
    fun `validateAmount returns false for blank amount`() = runTest(testDispatcher) {
        // Given
        viewModel = AddTransactionViewModel(transactionRepository)
        
        // When
        val result = viewModel.validateAmount("")
        
        // Then
        assertFalse(result)
        viewModel.validationState.test {
            val state = awaitItem()
            assertEquals("Amount is required", state.amountError)
        }
    }

    @Test
    fun `validateAmount returns false for invalid amount`() = runTest(testDispatcher) {
        // Given
        viewModel = AddTransactionViewModel(transactionRepository)
        
        // When
        val result = viewModel.validateAmount("abc")
        
        // Then
        assertFalse(result)
        viewModel.validationState.test {
            val state = awaitItem()
            assertEquals("Invalid amount", state.amountError)
        }
    }

    @Test
    fun `validateAmount returns false for zero amount`() = runTest(testDispatcher) {
        // Given
        viewModel = AddTransactionViewModel(transactionRepository)
        
        // When
        val result = viewModel.validateAmount("0")
        
        // Then
        assertFalse(result)
        viewModel.validationState.test {
            val state = awaitItem()
            assertEquals("Amount must be greater than 0", state.amountError)
        }
    }

    @Test
    fun `validateAmount returns false for negative amount`() = runTest(testDispatcher) {
        // Given
        viewModel = AddTransactionViewModel(transactionRepository)
        
        // When
        val result = viewModel.validateAmount("-10")
        
        // Then
        assertFalse(result)
        viewModel.validationState.test {
            val state = awaitItem()
            assertEquals("Amount must be greater than 0", state.amountError)
        }
    }

    @Test
    fun `validateAmount returns false for too large amount`() = runTest(testDispatcher) {
        // Given
        viewModel = AddTransactionViewModel(transactionRepository)
        
        // When
        val result = viewModel.validateAmount("9999999999")
        
        // Then
        assertFalse(result)
        viewModel.validationState.test {
            val state = awaitItem()
            assertEquals("Amount is too large", state.amountError)
        }
    }

    @Test
    fun `validateAmount returns true for valid amount`() = runTest(testDispatcher) {
        // Given
        viewModel = AddTransactionViewModel(transactionRepository)
        
        // When
        val result = viewModel.validateAmount("100.50")
        
        // Then
        assertTrue(result)
        viewModel.validationState.test {
            val state = awaitItem()
            assertNull(state.amountError)
        }
    }

    @Test
    fun `validateAmount returns true for valid integer amount`() = runTest(testDispatcher) {
        // Given
        viewModel = AddTransactionViewModel(transactionRepository)
        
        // When
        val result = viewModel.validateAmount("100")
        
        // Then
        assertTrue(result)
        viewModel.validationState.test {
            val state = awaitItem()
            assertNull(state.amountError)
        }
    }

    // ==================== Description Validation Tests ====================

    @Test
    fun `validateDescription returns false for blank description`() = runTest(testDispatcher) {
        // Given
        viewModel = AddTransactionViewModel(transactionRepository)
        
        // When
        val result = viewModel.validateDescription("")
        
        // Then
        assertFalse(result)
        viewModel.validationState.test {
            val state = awaitItem()
            assertEquals("Description is required", state.descriptionError)
        }
    }

    @Test
    fun `validateDescription returns false for too long description`() = runTest(testDispatcher) {
        // Given
        viewModel = AddTransactionViewModel(transactionRepository)
        val longDescription = "a".repeat(201)
        
        // When
        val result = viewModel.validateDescription(longDescription)
        
        // Then
        assertFalse(result)
        viewModel.validationState.test {
            val state = awaitItem()
            assertEquals("Description is too long", state.descriptionError)
        }
    }

    @Test
    fun `validateDescription returns true for valid description`() = runTest(testDispatcher) {
        // Given
        viewModel = AddTransactionViewModel(transactionRepository)
        
        // When
        val result = viewModel.validateDescription("Grocery shopping")
        
        // Then
        assertTrue(result)
        viewModel.validationState.test {
            val state = awaitItem()
            assertNull(state.descriptionError)
        }
    }

    @Test
    fun `validateDescription returns true for max length description`() = runTest(testDispatcher) {
        // Given
        viewModel = AddTransactionViewModel(transactionRepository)
        val maxDescription = "a".repeat(200)
        
        // When
        val result = viewModel.validateDescription(maxDescription)
        
        // Then
        assertTrue(result)
        viewModel.validationState.test {
            val state = awaitItem()
            assertNull(state.descriptionError)
        }
    }

    // ==================== Category Validation Tests ====================

    @Test
    fun `validateCategory returns false for zero categoryId`() = runTest(testDispatcher) {
        // Given
        viewModel = AddTransactionViewModel(transactionRepository)
        
        // When
        val result = viewModel.validateCategory(0)
        
        // Then
        assertFalse(result)
        viewModel.validationState.test {
            val state = awaitItem()
            assertEquals("Please select a category", state.categoryError)
        }
    }

    @Test
    fun `validateCategory returns false for negative categoryId`() = runTest(testDispatcher) {
        // Given
        viewModel = AddTransactionViewModel(transactionRepository)
        
        // When
        val result = viewModel.validateCategory(-1)
        
        // Then
        assertFalse(result)
        viewModel.validationState.test {
            val state = awaitItem()
            assertEquals("Please select a category", state.categoryError)
        }
    }

    @Test
    fun `validateCategory returns true for valid categoryId`() = runTest(testDispatcher) {
        // Given
        viewModel = AddTransactionViewModel(transactionRepository)
        
        // When
        val result = viewModel.validateCategory(1)
        
        // Then
        assertTrue(result)
        viewModel.validationState.test {
            val state = awaitItem()
            assertNull(state.categoryError)
        }
    }

    // ==================== Save Transaction Tests ====================

    @Test
    fun `saveTransaction with valid data saves successfully`() = runTest(testDispatcher) {
        // Given
        viewModel = AddTransactionViewModel(transactionRepository)
        coEvery { transactionRepository.insertTransaction(any()) } returns Unit
        
        // When
        viewModel.saveTransaction(
            amount = "100.00",
            description = "Test transaction",
            categoryId = 1,
            type = "EXPENSE",
            date = System.currentTimeMillis(),
            accountId = 1,
            userId = "test_user"
        )
        advanceUntilIdle()
        
        // Then
        viewModel.saveState.test {
            val state = awaitItem()
            assertTrue(state is SaveState.Success)
        }
        coVerify { transactionRepository.insertTransaction(any()) }
    }

    @Test
    fun `saveTransaction sets Loading state during save`() = runTest(testDispatcher) {
        // Given
        viewModel = AddTransactionViewModel(transactionRepository)
        coEvery { transactionRepository.insertTransaction(any()) } returns Unit
        
        // When
        viewModel.saveTransaction(
            amount = "100.00",
            description = "Test transaction",
            categoryId = 1,
            type = "EXPENSE",
            date = System.currentTimeMillis(),
            accountId = 1,
            userId = "test_user"
        )
        
        // Then - immediate check should show loading
        viewModel.saveState.test {
            val state = awaitItem()
            assertTrue(state is SaveState.Loading)
        }
    }

    @Test
    fun `saveTransaction with invalid amount does not save`() = runTest(testDispatcher) {
        // Given
        viewModel = AddTransactionViewModel(transactionRepository)
        
        // When
        viewModel.saveTransaction(
            amount = "",
            description = "Test transaction",
            categoryId = 1,
            type = "EXPENSE",
            date = System.currentTimeMillis(),
            accountId = 1,
            userId = "test_user"
        )
        
        // Then
        coVerify(exactly = 0) { transactionRepository.insertTransaction(any()) }
        viewModel.validationState.test {
            val state = awaitItem()
            assertNotNull(state.amountError)
        }
    }

    @Test
    fun `saveTransaction with invalid description does not save`() = runTest(testDispatcher) {
        // Given
        viewModel = AddTransactionViewModel(transactionRepository)
        
        // When
        viewModel.saveTransaction(
            amount = "100.00",
            description = "",
            categoryId = 1,
            type = "EXPENSE",
            date = System.currentTimeMillis(),
            accountId = 1,
            userId = "test_user"
        )
        
        // Then
        coVerify(exactly = 0) { transactionRepository.insertTransaction(any()) }
        viewModel.validationState.test {
            val state = awaitItem()
            assertNotNull(state.descriptionError)
        }
    }

    @Test
    fun `saveTransaction with invalid category does not save`() = runTest(testDispatcher) {
        // Given
        viewModel = AddTransactionViewModel(transactionRepository)
        
        // When
        viewModel.saveTransaction(
            amount = "100.00",
            description = "Test transaction",
            categoryId = 0,
            type = "EXPENSE",
            date = System.currentTimeMillis(),
            accountId = 1,
            userId = "test_user"
        )
        
        // Then
        coVerify(exactly = 0) { transactionRepository.insertTransaction(any()) }
        viewModel.validationState.test {
            val state = awaitItem()
            assertNotNull(state.categoryError)
        }
    }

    @Test
    fun `saveTransaction with multiple validation errors shows all errors`() = runTest(testDispatcher) {
        // Given
        viewModel = AddTransactionViewModel(transactionRepository)
        
        // When
        viewModel.saveTransaction(
            amount = "",
            description = "",
            categoryId = 0,
            type = "EXPENSE",
            date = System.currentTimeMillis(),
            accountId = 1,
            userId = "test_user"
        )
        
        // Then
        viewModel.validationState.test {
            val state = awaitItem()
            assertNotNull(state.amountError)
            assertNotNull(state.descriptionError)
            assertNotNull(state.categoryError)
        }
    }

    @Test
    fun `saveTransaction with repository error sets Error state`() = runTest(testDispatcher) {
        // Given
        viewModel = AddTransactionViewModel(transactionRepository)
        coEvery { transactionRepository.insertTransaction(any()) } throws RuntimeException("Database error")
        
        // When
        viewModel.saveTransaction(
            amount = "100.00",
            description = "Test transaction",
            categoryId = 1,
            type = "EXPENSE",
            date = System.currentTimeMillis(),
            accountId = 1,
            userId = "test_user"
        )
        advanceUntilIdle()
        
        // Then
        viewModel.saveState.test {
            val state = awaitItem()
            assertTrue(state is SaveState.Error)
            assertEquals("Database error", (state as SaveState.Error).message)
        }
    }

    @Test
    fun `saveTransaction trims description whitespace`() = runTest(testDispatcher) {
        // Given
        viewModel = AddTransactionViewModel(transactionRepository)
        coEvery { transactionRepository.insertTransaction(any()) } returns Unit
        
        // When
        viewModel.saveTransaction(
            amount = "100.00",
            description = "  Test transaction  ",
            categoryId = 1,
            type = "EXPENSE",
            date = System.currentTimeMillis(),
            accountId = 1,
            userId = "test_user"
        )
        advanceUntilIdle()
        
        // Then
        coVerify { 
            transactionRepository.insertTransaction(
                match { it.note == "Test transaction" }
            ) 
        }
    }

    // ==================== Reset Tests ====================

    @Test
    fun `resetValidation clears all validation errors`() = runTest(testDispatcher) {
        // Given
        viewModel = AddTransactionViewModel(transactionRepository)
        viewModel.validateAmount("")
        viewModel.validateDescription("")
        viewModel.validateCategory(0)
        
        // When
        viewModel.resetValidation()
        
        // Then
        viewModel.validationState.test {
            val state = awaitItem()
            assertNull(state.amountError)
            assertNull(state.descriptionError)
            assertNull(state.categoryError)
        }
    }

    @Test
    fun `resetSaveState sets state to Idle`() = runTest(testDispatcher) {
        // Given
        viewModel = AddTransactionViewModel(transactionRepository)
        coEvery { transactionRepository.insertTransaction(any()) } returns Unit
        
        viewModel.saveTransaction(
            amount = "100.00",
            description = "Test",
            categoryId = 1,
            type = "EXPENSE",
            date = System.currentTimeMillis(),
            accountId = 1,
            userId = "test_user"
        )
        advanceUntilIdle()
        
        // When
        viewModel.resetSaveState()
        
        // Then
        viewModel.saveState.test {
            val state = awaitItem()
            assertTrue(state is SaveState.Idle)
        }
    }

    // ==================== Transaction Entity Creation Tests ====================

    @Test
    fun `saveTransaction creates correct TransactionEntity`() = runTest(testDispatcher) {
        // Given
        viewModel = AddTransactionViewModel(transactionRepository)
        coEvery { transactionRepository.insertTransaction(any()) } returns Unit
        
        val amount = "150.50"
        val description = "Grocery shopping"
        val categoryId = 5L
        val type = "EXPENSE"
        val date = 1234567890L
        val accountId = 2L
        val userId = "user_123"
        
        // When
        viewModel.saveTransaction(
            amount = amount,
            description = description,
            categoryId = categoryId,
            type = type,
            date = date,
            accountId = accountId,
            userId = userId
        )
        advanceUntilIdle()
        
        // Then
        coVerify {
            transactionRepository.insertTransaction(
                match { entity ->
                    entity.amount == 150.50 &&
                    entity.note == description &&
                    entity.categoryId == 5 &&
                    entity.type == type &&
                    entity.date == date &&
                    entity.accountId == accountId &&
                    entity.userId == userId
                }
            )
        }
    }
}
