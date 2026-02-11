package com.pyera.app.ui.debt

import app.cash.turbine.test
import com.pyera.app.data.local.entity.DebtEntity
import com.pyera.app.domain.repository.DebtRepository
import com.pyera.app.test.createDebt
import com.pyera.app.test.createReceivableDebt
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DebtViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    
    private lateinit var debtRepository: DebtRepository
    private lateinit var viewModel: DebtViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        debtRepository = mockk(relaxed = true)
        
        // Default flow
        every { debtRepository.getAllDebts() } returns flowOf(emptyList())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ==================== Initial State Tests ====================

    @Test
    fun `initial debts is empty`() = runTest(testDispatcher) {
        // When
        viewModel = createViewModel()
        
        // Then
        viewModel.debts.test {
            assertTrue(awaitItem().isEmpty())
        }
    }

    @Test
    fun `initial isRefreshing is false`() = runTest(testDispatcher) {
        // When
        viewModel = createViewModel()
        
        // Then
        viewModel.isRefreshing.test {
            assertFalse(awaitItem())
        }
    }

    // ==================== Debts Flow Tests ====================

    @Test
    fun `debts flow updates with repository data`() = runTest(testDispatcher) {
        // Given
        val debts = listOf(
            createDebt(id = 1, name = "John", amount = 500.0, type = "PAYABLE"),
            createReceivableDebt(id = 2, name = "Jane", amount = 1000.0)
        )
        every { debtRepository.getAllDebts() } returns flowOf(debts)
        
        // When
        viewModel = createViewModel()
        advanceUntilIdle()
        
        // Then
        viewModel.debts.test {
            val result = awaitItem()
            assertEquals(2, result.size)
            assertEquals("John", result[0].name)
            assertEquals(500.0, result[0].amount, 0.01)
            assertEquals("PAYABLE", result[0].type)
            assertEquals("Jane", result[1].name)
            assertEquals("RECEIVABLE", result[1].type)
        }
    }

    @Test
    fun `debts flow emits updates when data changes`() = runTest(testDispatcher) {
        // Given
        val debtsFlow = kotlinx.coroutines.flow.MutableStateFlow<List<DebtEntity>>(emptyList())
        every { debtRepository.getAllDebts() } returns debtsFlow
        
        viewModel = createViewModel()
        
        // When - initial empty
        viewModel.debts.test {
            assertTrue(awaitItem().isEmpty())
            
            // Update with data
            debtsFlow.value = listOf(createDebt(id = 1, name = "New Debt"))
            
            val updated = awaitItem()
            assertEquals(1, updated.size)
            assertEquals("New Debt", updated[0].name)
        }
    }

    // ==================== Add Debt Tests ====================

    @Test
    fun `addDebt calls repository with correct data`() = runTest(testDispatcher) {
        // Given
        viewModel = createViewModel()
        coEvery { debtRepository.addDebt(any()) } returns Unit
        
        val dueDate = System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000
        
        // When
        viewModel.addDebt(
            name = "Test Person",
            amount = 500.0,
            dueDate = dueDate,
            type = "PAYABLE"
        )
        advanceUntilIdle()
        
        // Then
        coVerify {
            debtRepository.addDebt(
                match { debt ->
                    debt.name == "Test Person" &&
                    debt.amount == 500.0 &&
                    debt.dueDate == dueDate &&
                    debt.type == "PAYABLE" &&
                    !debt.isPaid
                }
            )
        }
    }

    @Test
    fun `addDebt creates PAYABLE debt`() = runTest(testDispatcher) {
        // Given
        viewModel = createViewModel()
        coEvery { debtRepository.addDebt(any()) } returns Unit
        
        // When
        viewModel.addDebt(
            name = "Borrowed Money",
            amount = 1000.0,
            dueDate = System.currentTimeMillis(),
            type = "PAYABLE"
        )
        advanceUntilIdle()
        
        // Then
        coVerify {
            debtRepository.addDebt(match { it.type == "PAYABLE" })
        }
    }

    @Test
    fun `addDebt creates RECEIVABLE debt`() = runTest(testDispatcher) {
        // Given
        viewModel = createViewModel()
        coEvery { debtRepository.addDebt(any()) } returns Unit
        
        // When
        viewModel.addDebt(
            name = "Loan to Friend",
            amount = 500.0,
            dueDate = System.currentTimeMillis(),
            type = "RECEIVABLE"
        )
        advanceUntilIdle()
        
        // Then
        coVerify {
            debtRepository.addDebt(match { it.type == "RECEIVABLE" })
        }
    }

    // ==================== Mark As Paid Tests ====================

    @Test
    fun `markAsPaid updates debt with isPaid true`() = runTest(testDispatcher) {
        // Given
        viewModel = createViewModel()
        val unpaidDebt = createDebt(id = 1, name = "Unpaid", isPaid = false)
        coEvery { debtRepository.updateDebt(any()) } returns Unit
        
        // When
        viewModel.markAsPaid(unpaidDebt)
        advanceUntilIdle()
        
        // Then
        coVerify {
            debtRepository.updateDebt(
                match { debt ->
                    debt.id == 1 &&
                    debt.isPaid &&
                    debt.name == "Unpaid"
                }
            )
        }
    }

    @Test
    fun `markAsPaid preserves all other debt properties`() = runTest(testDispatcher) {
        // Given
        viewModel = createViewModel()
        val debt = createDebt(
            id = 5,
            name = "Test",
            amount = 1000.0,
            type = "PAYABLE",
            isPaid = false
        )
        coEvery { debtRepository.updateDebt(any()) } returns Unit
        
        // When
        viewModel.markAsPaid(debt)
        advanceUntilIdle()
        
        // Then
        coVerify {
            debtRepository.updateDebt(
                match { updated ->
                    updated.id == 5 &&
                    updated.name == "Test" &&
                    updated.amount == 1000.0 &&
                    updated.type == "PAYABLE" &&
                    updated.isPaid
                }
            )
        }
    }

    // ==================== Delete Debt Tests ====================

    @Test
    fun `deleteDebt calls repository`() = runTest(testDispatcher) {
        // Given
        viewModel = createViewModel()
        val debt = createDebt(id = 1, name = "To Delete")
        coEvery { debtRepository.deleteDebt(any()) } returns Unit
        
        // When
        viewModel.deleteDebt(debt)
        advanceUntilIdle()
        
        // Then
        coVerify { debtRepository.deleteDebt(debt) }
    }

    @Test
    fun `deleteDebt removes debt from repository`() = runTest(testDispatcher) {
        // Given
        viewModel = createViewModel()
        val debt = createDebt(id = 10, name = "Delete Me")
        coEvery { debtRepository.deleteDebt(any()) } returns Unit
        
        // When
        viewModel.deleteDebt(debt)
        advanceUntilIdle()
        
        // Then
        coVerify(exactly = 1) { debtRepository.deleteDebt(match { it.id == 10 }) }
    }

    // ==================== Update Debt Tests ====================

    @Test
    fun `updateDebt calls repository`() = runTest(testDispatcher) {
        // Given
        viewModel = createViewModel()
        val updatedDebt = createDebt(
            id = 1,
            name = "Updated Name",
            amount = 750.0
        )
        coEvery { debtRepository.updateDebt(any()) } returns Unit
        
        // When
        viewModel.updateDebt(updatedDebt)
        advanceUntilIdle()
        
        // Then
        coVerify { debtRepository.updateDebt(updatedDebt) }
    }

    @Test
    fun `updateDebt saves modified debt`() = runTest(testDispatcher) {
        // Given
        viewModel = createViewModel()
        val originalDebt = createDebt(id = 1, name = "Original", amount = 500.0)
        val modifiedDebt = originalDebt.copy(name = "Modified", amount = 600.0)
        coEvery { debtRepository.updateDebt(any()) } returns Unit
        
        // When
        viewModel.updateDebt(modifiedDebt)
        advanceUntilIdle()
        
        // Then
        coVerify {
            debtRepository.updateDebt(
                match { debt ->
                    debt.id == 1 &&
                    debt.name == "Modified" &&
                    debt.amount == 600.0
                }
            )
        }
    }

    // ==================== Refresh Tests ====================

    @Test
    fun `refresh sets isRefreshing to true then false`() = runTest(testDispatcher) {
        // Given
        viewModel = createViewModel()
        
        // When
        viewModel.refresh()
        
        // Then - should immediately be true
        viewModel.isRefreshing.test {
            val refreshing = awaitItem()
            assertTrue(refreshing)
        }
        
        // After delay, should be false
        advanceUntilIdle()
        
        viewModel.isRefreshing.test {
            val notRefreshing = awaitItem()
            assertFalse(notRefreshing)
        }
    }

    @Test
    fun `refresh simulates refresh delay`() = runTest(testDispatcher) {
        // Given
        viewModel = createViewModel()
        
        // When
        viewModel.refresh()
        
        // Then - immediately after calling, should be refreshing
        viewModel.isRefreshing.test {
            assertTrue(awaitItem())
        }
        
        // After advancing time, should complete
        advanceUntilIdle()
        
        viewModel.isRefreshing.test {
            assertFalse(awaitItem())
        }
    }

    // ==================== Debt Types Tests ====================

    @Test
    fun `debts can contain both PAYABLE and RECEIVABLE`() = runTest(testDispatcher) {
        // Given
        val mixedDebts = listOf(
            createDebt(id = 1, name = "I Owe Bank", amount = 5000.0, type = "PAYABLE"),
            createReceivableDebt(id = 2, name = "Friend Owes Me", amount = 200.0),
            createDebt(id = 3, name = "Credit Card", amount = 1500.0, type = "PAYABLE")
        )
        every { debtRepository.getAllDebts() } returns flowOf(mixedDebts)
        
        // When
        viewModel = createViewModel()
        advanceUntilIdle()
        
        // Then
        viewModel.debts.test {
            val result = awaitItem()
            assertEquals(3, result.size)
            
            val payables = result.filter { it.type == "PAYABLE" }
            val receivables = result.filter { it.type == "RECEIVABLE" }
            
            assertEquals(2, payables.size)
            assertEquals(1, receivables.size)
        }
    }

    // ==================== Debt Totals Calculation ====================

    @Test
    fun `can calculate total payable amount`() = runTest(testDispatcher) {
        // Given
        val debts = listOf(
            createDebt(id = 1, name = "Debt 1", amount = 500.0, type = "PAYABLE"),
            createDebt(id = 2, name = "Debt 2", amount = 300.0, type = "PAYABLE"),
            createReceivableDebt(id = 3, name = "Receivable", amount = 1000.0)
        )
        every { debtRepository.getAllDebts() } returns flowOf(debts)
        
        // When
        viewModel = createViewModel()
        advanceUntilIdle()
        
        // Then
        viewModel.debts.test {
            val result = awaitItem()
            val totalPayable = result
                .filter { it.type == "PAYABLE" }
                .sumOf { it.amount }
            assertEquals(800.0, totalPayable, 0.01)
        }
    }

    @Test
    fun `can calculate total receivable amount`() = runTest(testDispatcher) {
        // Given
        val debts = listOf(
            createDebt(id = 1, name = "Debt", amount = 500.0, type = "PAYABLE"),
            createReceivableDebt(id = 2, name = "Receivable 1", amount = 1000.0),
            createReceivableDebt(id = 3, name = "Receivable 2", amount = 250.0)
        )
        every { debtRepository.getAllDebts() } returns flowOf(debts)
        
        // When
        viewModel = createViewModel()
        advanceUntilIdle()
        
        // Then
        viewModel.debts.test {
            val result = awaitItem()
            val totalReceivable = result
                .filter { it.type == "RECEIVABLE" }
                .sumOf { it.amount }
            assertEquals(1250.0, totalReceivable, 0.01)
        }
    }

    // ==================== Paid vs Unpaid Tests ====================

    @Test
    fun `debts can be filtered by paid status`() = runTest(testDispatcher) {
        // Given
        val debts = listOf(
            createDebt(id = 1, name = "Paid", amount = 100.0, isPaid = true),
            createDebt(id = 2, name = "Unpaid 1", amount = 200.0, isPaid = false),
            createDebt(id = 3, name = "Unpaid 2", amount = 300.0, isPaid = false)
        )
        every { debtRepository.getAllDebts() } returns flowOf(debts)
        
        // When
        viewModel = createViewModel()
        advanceUntilIdle()
        
        // Then
        viewModel.debts.test {
            val result = awaitItem()
            val paid = result.filter { it.isPaid }
            val unpaid = result.filter { !it.isPaid }
            
            assertEquals(1, paid.size)
            assertEquals(2, unpaid.size)
            assertEquals(100.0, paid.sumOf { it.amount }, 0.01)
            assertEquals(500.0, unpaid.sumOf { it.amount }, 0.01)
        }
    }

    // ==================== Due Date Tests ====================

    @Test
    fun `debts have correct due dates`() = runTest(testDispatcher) {
        // Given
        val now = System.currentTimeMillis()
        val dueIn7Days = now + 7 * 24 * 60 * 60 * 1000
        val dueIn30Days = now + 30 * 24 * 60 * 60 * 1000
        
        val debts = listOf(
            createDebt(id = 1, name = "Soon", dueDate = dueIn7Days),
            createDebt(id = 2, name = "Later", dueDate = dueIn30Days)
        )
        every { debtRepository.getAllDebts() } returns flowOf(debts)
        
        // When
        viewModel = createViewModel()
        advanceUntilIdle()
        
        // Then
        viewModel.debts.test {
            val result = awaitItem()
            assertEquals(dueIn7Days, result[0].dueDate)
            assertEquals(dueIn30Days, result[1].dueDate)
        }
    }

    private fun createViewModel(): DebtViewModel {
        return DebtViewModel(
            debtRepository = debtRepository
        )
    }
}
