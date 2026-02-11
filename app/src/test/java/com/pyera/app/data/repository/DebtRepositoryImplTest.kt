package com.pyera.app.data.repository

import app.cash.turbine.test
import com.pyera.app.data.local.dao.DebtDao
import com.pyera.app.data.local.entity.DebtEntity
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class DebtRepositoryImplTest {

    @MockK
    private lateinit var debtDao: DebtDao

    private lateinit var repository: DebtRepositoryImpl

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        repository = DebtRepositoryImpl(debtDao)
    }

    // ==================== getAllDebts Tests ====================

    @Test
    fun `getAllDebts returns flow from dao`() = runTest {
        // Arrange
        val debts = listOf(
            DebtEntity(
                id = 1,
                name = "John Doe",
                amount = 5000.0,
                dueDate = System.currentTimeMillis() + 30 * 24 * 60 * 60 * 1000L,
                type = "RECEIVABLE",
                isPaid = false
            ),
            DebtEntity(
                id = 2,
                name = "ABC Store",
                amount = 2000.0,
                dueDate = System.currentTimeMillis() + 15 * 24 * 60 * 60 * 1000L,
                type = "PAYABLE",
                isPaid = false
            )
        )
        coEvery { debtDao.getAllDebts() } returns flowOf(debts)

        // Act & Assert
        repository.getAllDebts().test {
            val result = awaitItem()
            assertEquals(2, result.size)
            assertEquals("John Doe", result[0].name)
            assertEquals("RECEIVABLE", result[0].type)
            assertEquals("ABC Store", result[1].name)
            assertEquals("PAYABLE", result[1].type)
            awaitComplete()
        }
    }

    @Test
    fun `getAllDebts emits empty list when no debts`() = runTest {
        // Arrange
        coEvery { debtDao.getAllDebts() } returns flowOf(emptyList())

        // Act & Assert
        repository.getAllDebts().test {
            val result = awaitItem()
            assertTrue(result.isEmpty())
            awaitComplete()
        }
    }

    @Test
    fun `getAllDebts emits updates when data changes`() = runTest {
        // Arrange
        val initialDebts = listOf(
            DebtEntity(
                id = 1,
                name = "Debt 1",
                amount = 1000.0,
                dueDate = System.currentTimeMillis(),
                type = "PAYABLE",
                isPaid = false
            )
        )
        val updatedDebts = listOf(
            DebtEntity(
                id = 1,
                name = "Debt 1",
                amount = 1000.0,
                dueDate = System.currentTimeMillis(),
                type = "PAYABLE",
                isPaid = false
            ),
            DebtEntity(
                id = 2,
                name = "Debt 2",
                amount = 2000.0,
                dueDate = System.currentTimeMillis(),
                type = "RECEIVABLE",
                isPaid = false
            )
        )
        coEvery { debtDao.getAllDebts() } returns flowOf(initialDebts, updatedDebts)

        // Act & Assert
        repository.getAllDebts().test {
            val first = awaitItem()
            assertEquals(1, first.size)

            val second = awaitItem()
            assertEquals(2, second.size)

            awaitComplete()
        }
    }

    // ==================== addDebt Tests ====================

    @Test
    fun `addDebt calls dao insert`() = runTest {
        // Arrange
        val debt = DebtEntity(
            name = "New Debt",
            amount = 3000.0,
            dueDate = System.currentTimeMillis() + 60 * 24 * 60 * 60 * 1000L,
            type = "PAYABLE",
            isPaid = false
        )
        coEvery { debtDao.insertDebt(debt) } just Runs

        // Act
        repository.addDebt(debt)

        // Assert
        coVerify { debtDao.insertDebt(debt) }
    }

    @Test
    fun `addDebt with PAYABLE type`() = runTest {
        // Arrange
        val debt = DebtEntity(
            name = "Credit Card",
            amount = 5000.0,
            dueDate = System.currentTimeMillis() + 30 * 24 * 60 * 60 * 1000L,
            type = "PAYABLE",
            isPaid = false
        )
        coEvery { debtDao.insertDebt(any()) } just Runs

        // Act
        repository.addDebt(debt)

        // Assert
        coVerify { debtDao.insertDebt(match { it.type == "PAYABLE" }) }
    }

    @Test
    fun `addDebt with RECEIVABLE type`() = runTest {
        // Arrange
        val debt = DebtEntity(
            name = "Loan to Friend",
            amount = 10000.0,
            dueDate = System.currentTimeMillis() + 90 * 24 * 60 * 60 * 1000L,
            type = "RECEIVABLE",
            isPaid = false
        )
        coEvery { debtDao.insertDebt(any()) } just Runs

        // Act
        repository.addDebt(debt)

        // Assert
        coVerify { debtDao.insertDebt(match { it.type == "RECEIVABLE" }) }
    }

    @Test
    fun `addDebt propagates exception`() = runTest {
        // Arrange
        val debt = DebtEntity(
            name = "Test Debt",
            amount = 1000.0,
            dueDate = System.currentTimeMillis(),
            type = "PAYABLE",
            isPaid = false
        )
        coEvery { debtDao.insertDebt(any()) } throws RuntimeException("Insert failed")

        // Act & Assert
        try {
            repository.addDebt(debt)
            fail("Expected exception")
        } catch (e: RuntimeException) {
            assertEquals("Insert failed", e.message)
        }
    }

    @Test
    fun `addDebt with zero amount`() = runTest {
        // Arrange
        val debt = DebtEntity(
            name = "Zero Debt",
            amount = 0.0,
            dueDate = System.currentTimeMillis(),
            type = "PAYABLE",
            isPaid = false
        )
        coEvery { debtDao.insertDebt(any()) } just Runs

        // Act
        repository.addDebt(debt)

        // Assert
        coVerify { debtDao.insertDebt(match { it.amount == 0.0 }) }
    }

    @Test
    fun `addDebt with past due date`() = runTest {
        // Arrange
        val pastDate = System.currentTimeMillis() - 30 * 24 * 60 * 60 * 1000L
        val debt = DebtEntity(
            name = "Overdue Debt",
            amount = 2000.0,
            dueDate = pastDate,
            type = "PAYABLE",
            isPaid = false
        )
        coEvery { debtDao.insertDebt(any()) } just Runs

        // Act
        repository.addDebt(debt)

        // Assert
        coVerify { debtDao.insertDebt(match { it.dueDate == pastDate }) }
    }

    // ==================== updateDebt Tests ====================

    @Test
    fun `updateDebt calls dao update`() = runTest {
        // Arrange
        val debt = DebtEntity(
            id = 1,
            name = "Updated Debt",
            amount = 4000.0,
            dueDate = System.currentTimeMillis() + 45 * 24 * 60 * 60 * 1000L,
            type = "PAYABLE",
            isPaid = false
        )
        coEvery { debtDao.updateDebt(debt) } just Runs

        // Act
        repository.updateDebt(debt)

        // Assert
        coVerify { debtDao.updateDebt(debt) }
    }

    @Test
    fun `updateDebt with isPaid status change`() = runTest {
        // Arrange
        val debt = DebtEntity(
            id = 1,
            name = "Paid Debt",
            amount = 3000.0,
            dueDate = System.currentTimeMillis(),
            type = "PAYABLE",
            isPaid = true
        )
        coEvery { debtDao.updateDebt(any()) } just Runs

        // Act
        repository.updateDebt(debt)

        // Assert
        coVerify { debtDao.updateDebt(match { it.isPaid }) }
    }

    @Test
    fun `updateDebt propagates exception`() = runTest {
        // Arrange
        val debt = DebtEntity(
            id = 1,
            name = "Test",
            amount = 1000.0,
            dueDate = System.currentTimeMillis(),
            type = "PAYABLE",
            isPaid = false
        )
        coEvery { debtDao.updateDebt(any()) } throws RuntimeException("Update failed")

        // Act & Assert
        try {
            repository.updateDebt(debt)
            fail("Expected exception")
        } catch (e: RuntimeException) {
            assertEquals("Update failed", e.message)
        }
    }

    @Test
    fun `updateDebt changes amount`() = runTest {
        // Arrange
        val originalDebt = DebtEntity(
            id = 1,
            name = "Original",
            amount = 1000.0,
            dueDate = System.currentTimeMillis(),
            type = "PAYABLE",
            isPaid = false
        )
        val updatedDebt = originalDebt.copy(amount = 1500.0)
        coEvery { debtDao.updateDebt(any()) } just Runs

        // Act
        repository.updateDebt(updatedDebt)

        // Assert
        coVerify { debtDao.updateDebt(match { it.amount == 1500.0 }) }
    }

    // ==================== deleteDebt Tests ====================

    @Test
    fun `deleteDebt calls dao delete`() = runTest {
        // Arrange
        val debt = DebtEntity(
            id = 1,
            name = "To Delete",
            amount = 1000.0,
            dueDate = System.currentTimeMillis(),
            type = "PAYABLE",
            isPaid = false
        )
        coEvery { debtDao.deleteDebt(debt) } just Runs

        // Act
        repository.deleteDebt(debt)

        // Assert
        coVerify { debtDao.deleteDebt(debt) }
    }

    @Test
    fun `deleteDebt propagates exception`() = runTest {
        // Arrange
        val debt = DebtEntity(
            id = 1,
            name = "Test",
            amount = 1000.0,
            dueDate = System.currentTimeMillis(),
            type = "PAYABLE",
            isPaid = false
        )
        coEvery { debtDao.deleteDebt(any()) } throws RuntimeException("Delete failed")

        // Act & Assert
        try {
            repository.deleteDebt(debt)
            fail("Expected exception")
        } catch (e: RuntimeException) {
            assertEquals("Delete failed", e.message)
        }
    }

    @Test
    fun `deleteDebt by specific id`() = runTest {
        // Arrange
        val debt = DebtEntity(
            id = 999,
            name = "Specific Debt",
            amount = 5000.0,
            dueDate = System.currentTimeMillis(),
            type = "RECEIVABLE",
            isPaid = false
        )
        coEvery { debtDao.deleteDebt(any()) } just Runs

        // Act
        repository.deleteDebt(debt)

        // Assert
        coVerify { debtDao.deleteDebt(match { it.id == 999 }) }
    }

    // ==================== Debt Tracking Tests ====================

    @Test
    fun `debt tracking - add and retrieve`() = runTest {
        // Arrange
        val newDebt = DebtEntity(
            name = "Bank Loan",
            amount = 50000.0,
            dueDate = System.currentTimeMillis() + 365 * 24 * 60 * 60 * 1000L,
            type = "PAYABLE",
            isPaid = false
        )
        val allDebts = listOf(newDebt)

        coEvery { debtDao.insertDebt(any()) } just Runs
        coEvery { debtDao.getAllDebts() } returns flowOf(allDebts)

        // Act
        repository.addDebt(newDebt)

        // Assert
        repository.getAllDebts().test {
            val result = awaitItem()
            assertEquals(1, result.size)
            assertEquals("Bank Loan", result[0].name)
            assertEquals(50000.0, result[0].amount, 0.01)
            awaitComplete()
        }
    }

    @Test
    fun `debt tracking - mark as paid`() = runTest {
        // Arrange
        val unpaidDebt = DebtEntity(
            id = 1,
            name = "Utility Bill",
            amount = 2500.0,
            dueDate = System.currentTimeMillis(),
            type = "PAYABLE",
            isPaid = false
        )
        val paidDebt = unpaidDebt.copy(isPaid = true)

        coEvery { debtDao.updateDebt(any()) } just Runs

        // Act
        repository.updateDebt(paidDebt)

        // Assert
        coVerify { debtDao.updateDebt(match { it.isPaid }) }
    }

    @Test
    fun `debt tracking - PAYABLE vs RECEIVABLE calculation`() = runTest {
        // Arrange
        val debts = listOf(
            DebtEntity(
                id = 1,
                name = "I owe Bank",
                amount = 10000.0,
                dueDate = System.currentTimeMillis(),
                type = "PAYABLE",
                isPaid = false
            ),
            DebtEntity(
                id = 2,
                name = "Friend owes me",
                amount = 5000.0,
                dueDate = System.currentTimeMillis(),
                type = "RECEIVABLE",
                isPaid = false
            )
        )
        coEvery { debtDao.getAllDebts() } returns flowOf(debts)

        // Act
        val result = repository.getAllDebts()

        // Assert
        result.test {
            val items = awaitItem()
            val totalPayable = items.filter { it.type == "PAYABLE" }.sumOf { it.amount }
            val totalReceivable = items.filter { it.type == "RECEIVABLE" }.sumOf { it.amount }

            assertEquals(10000.0, totalPayable, 0.01)
            assertEquals(5000.0, totalReceivable, 0.01)
            awaitComplete()
        }
    }

    // ==================== Edge Cases ====================

    @Test
    fun `addDebt with very large amount`() = runTest {
        // Arrange
        val largeAmount = 1_000_000_000.0
        val debt = DebtEntity(
            name = "Big Loan",
            amount = largeAmount,
            dueDate = System.currentTimeMillis(),
            type = "PAYABLE",
            isPaid = false
        )
        coEvery { debtDao.insertDebt(any()) } just Runs

        // Act
        repository.addDebt(debt)

        // Assert
        coVerify { debtDao.insertDebt(match { it.amount == largeAmount }) }
    }

    @Test
    fun `addDebt with very long name`() = runTest {
        // Arrange
        val longName = "A".repeat(500)
        val debt = DebtEntity(
            name = longName,
            amount = 1000.0,
            dueDate = System.currentTimeMillis(),
            type = "PAYABLE",
            isPaid = false
        )
        coEvery { debtDao.insertDebt(any()) } just Runs

        // Act
        repository.addDebt(debt)

        // Assert
        coVerify { debtDao.insertDebt(match { it.name == longName }) }
    }

    @Test
    fun `updateDebt with negative amount`() = runTest {
        // Arrange
        val debt = DebtEntity(
            id = 1,
            name = "Refund",
            amount = -500.0,
            dueDate = System.currentTimeMillis(),
            type = "PAYABLE",
            isPaid = false
        )
        coEvery { debtDao.updateDebt(any()) } just Runs

        // Act
        repository.updateDebt(debt)

        // Assert
        coVerify { debtDao.updateDebt(match { it.amount == -500.0 }) }
    }

    @Test
    fun `debt with zero id is treated as new`() = runTest {
        // Arrange
        val newDebt = DebtEntity(
            id = 0,
            name = "New Debt",
            amount = 1000.0,
            dueDate = System.currentTimeMillis(),
            type = "PAYABLE",
            isPaid = false
        )
        coEvery { debtDao.insertDebt(any()) } just Runs

        // Act
        repository.addDebt(newDebt)

        // Assert
        coVerify { debtDao.insertDebt(match { it.id == 0 }) }
    }

    @Test
    fun `multiple debt operations sequence`() = runTest {
        // Arrange
        val debt1 = DebtEntity(id = 1, name = "Debt 1", amount = 1000.0, dueDate = System.currentTimeMillis(), type = "PAYABLE", isPaid = false)
        val debt2 = DebtEntity(id = 2, name = "Debt 2", amount = 2000.0, dueDate = System.currentTimeMillis(), type = "RECEIVABLE", isPaid = false)

        coEvery { debtDao.insertDebt(any()) } just Runs
        coEvery { debtDao.updateDebt(any()) } just Runs
        coEvery { debtDao.deleteDebt(any()) } just Runs

        // Act - Create
        repository.addDebt(debt1)
        repository.addDebt(debt2)

        // Act - Update
        val updatedDebt = debt1.copy(isPaid = true)
        repository.updateDebt(updatedDebt)

        // Act - Delete
        repository.deleteDebt(debt2)

        // Assert
        coVerify(exactly = 2) { debtDao.insertDebt(any()) }
        coVerify { debtDao.updateDebt(match { it.isPaid }) }
        coVerify { debtDao.deleteDebt(match { it.id == 2L }) }
    }
}
