package com.pyera.app.data.repository

import app.cash.turbine.test
import com.pyera.app.data.local.dao.RecurringTransactionDao
import com.pyera.app.data.local.dao.TransactionDao
import com.pyera.app.data.local.entity.RecurringFrequency
import com.pyera.app.data.local.entity.RecurringTransactionEntity
import com.pyera.app.data.local.entity.TransactionEntity
import com.pyera.app.data.local.entity.TransactionType
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import com.pyera.app.worker.calculateNextDueDate

@ExperimentalCoroutinesApi
class RecurringTransactionRepositoryImplTest {

    @MockK
    private lateinit var recurringDao: RecurringTransactionDao

    @MockK
    private lateinit var transactionDao: TransactionDao

    private lateinit var repository: RecurringTransactionRepositoryImpl

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        repository = RecurringTransactionRepositoryImpl(recurringDao, transactionDao)
    }

    // ==================== getAllRecurring Tests ====================

    @Test
    fun `getAllRecurring returns flow from dao`() = runTest {
        // Arrange
        val recurring = listOf(
            RecurringTransactionEntity(
                id = 1L,
                amount = 1000.0,
                type = TransactionType.EXPENSE,
                categoryId = 1L,
                accountId = 1L,
                description = "Monthly Rent",
                frequency = RecurringFrequency.MONTHLY,
                startDate = System.currentTimeMillis(),
                nextDueDate = System.currentTimeMillis() + 30 * 24 * 60 * 60 * 1000L,
                isActive = true
            )
        )
        coEvery { recurringDao.getAllRecurring() } returns flowOf(recurring)

        // Act & Assert
        repository.getAllRecurring().test {
            val result = awaitItem()
            assertEquals(1, result.size)
            assertEquals("Monthly Rent", result[0].description)
            assertEquals(RecurringFrequency.MONTHLY, result[0].frequency)
            awaitComplete()
        }
    }

    @Test
    fun `getAllRecurring emits empty list when no recurring transactions`() = runTest {
        // Arrange
        coEvery { recurringDao.getAllRecurring() } returns flowOf(emptyList())

        // Act & Assert
        repository.getAllRecurring().test {
            val result = awaitItem()
            assertTrue(result.isEmpty())
            awaitComplete()
        }
    }

    // ==================== getAllRecurringOnce Tests ====================

    @Test
    fun `getAllRecurringOnce returns list from dao`() = runTest {
        // Arrange
        val recurring = listOf(
            RecurringTransactionEntity(
                id = 1L,
                amount = 500.0,
                type = TransactionType.EXPENSE,
                description = "Weekly Groceries",
                frequency = RecurringFrequency.WEEKLY,
                startDate = System.currentTimeMillis(),
                nextDueDate = System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000L,
                isActive = true
            )
        )
        coEvery { recurringDao.getAllRecurringOnce() } returns recurring

        // Act
        val result = repository.getAllRecurringOnce()

        // Assert
        assertEquals(1, result.size)
        assertEquals("Weekly Groceries", result[0].description)
    }

    @Test
    fun `getAllRecurringOnce returns empty list when none exist`() = runTest {
        // Arrange
        coEvery { recurringDao.getAllRecurringOnce() } returns emptyList()

        // Act
        val result = repository.getAllRecurringOnce()

        // Assert
        assertTrue(result.isEmpty())
    }

    // ==================== getActiveRecurring Tests ====================

    @Test
    fun `getActiveRecurring returns only active transactions`() = runTest {
        // Arrange
        val activeRecurring = listOf(
            RecurringTransactionEntity(
                id = 1L,
                amount = 1000.0,
                type = TransactionType.EXPENSE,
                description = "Active Expense",
                frequency = RecurringFrequency.MONTHLY,
                startDate = System.currentTimeMillis(),
                nextDueDate = System.currentTimeMillis(),
                isActive = true
            )
        )
        coEvery { recurringDao.getActiveRecurring() } returns flowOf(activeRecurring)

        // Act & Assert
        repository.getActiveRecurring().test {
            val result = awaitItem()
            assertEquals(1, result.size)
            assertTrue(result[0].isActive)
            awaitComplete()
        }
    }

    @Test
    fun `getActiveRecurring emits empty when no active transactions`() = runTest {
        // Arrange
        coEvery { recurringDao.getActiveRecurring() } returns flowOf(emptyList())

        // Act & Assert
        repository.getActiveRecurring().test {
            val result = awaitItem()
            assertTrue(result.isEmpty())
            awaitComplete()
        }
    }

    // ==================== getDueRecurring Tests ====================

    @Test
    fun `getDueRecurring returns transactions due by current date`() = runTest {
        // Arrange
        val currentDate = System.currentTimeMillis()
        val dueTransactions = listOf(
            RecurringTransactionEntity(
                id = 1L,
                amount = 1000.0,
                type = TransactionType.EXPENSE,
                description = "Due Transaction",
                frequency = RecurringFrequency.MONTHLY,
                startDate = currentDate - 30 * 24 * 60 * 60 * 1000L,
                nextDueDate = currentDate - 1000, // Due (1 second ago)
                isActive = true
            )
        )
        coEvery { recurringDao.getDueRecurring(currentDate) } returns dueTransactions

        // Act
        val result = repository.getDueRecurring(currentDate)

        // Assert
        assertEquals(1, result.size)
        assertEquals("Due Transaction", result[0].description)
    }

    @Test
    fun `getDueRecurring returns empty when no due transactions`() = runTest {
        // Arrange
        val currentDate = System.currentTimeMillis()
        coEvery { recurringDao.getDueRecurring(currentDate) } returns emptyList()

        // Act
        val result = repository.getDueRecurring(currentDate)

        // Assert
        assertTrue(result.isEmpty())
    }

    // ==================== getRecurringById Tests ====================

    @Test
    fun `getRecurringById returns transaction when found`() = runTest {
        // Arrange
        val recurring = RecurringTransactionEntity(
            id = 1L,
            amount = 1000.0,
            type = TransactionType.EXPENSE,
            description = "Found Transaction",
            frequency = RecurringFrequency.MONTHLY,
            startDate = System.currentTimeMillis(),
            nextDueDate = System.currentTimeMillis(),
            isActive = true
        )
        coEvery { recurringDao.getRecurringById(1L) } returns recurring

        // Act
        val result = repository.getRecurringById(1L)

        // Assert
        assertNotNull(result)
        assertEquals(1L, result?.id)
        assertEquals("Found Transaction", result?.description)
    }

    @Test
    fun `getRecurringById returns null when not found`() = runTest {
        // Arrange
        coEvery { recurringDao.getRecurringById(999L) } returns null

        // Act
        val result = repository.getRecurringById(999L)

        // Assert
        assertNull(result)
    }

    // ==================== getRecurringByIdFlow Tests ====================

    @Test
    fun `getRecurringByIdFlow returns flow with transaction`() = runTest {
        // Arrange
        val recurring = RecurringTransactionEntity(
            id = 1L,
            amount = 1000.0,
            type = TransactionType.EXPENSE,
            description = "Flow Transaction",
            frequency = RecurringFrequency.MONTHLY,
            startDate = System.currentTimeMillis(),
            nextDueDate = System.currentTimeMillis(),
            isActive = true
        )
        coEvery { recurringDao.getRecurringByIdFlow(1L) } returns flowOf(recurring)

        // Act & Assert
        repository.getRecurringByIdFlow(1L).test {
            val result = awaitItem()
            assertNotNull(result)
            assertEquals("Flow Transaction", result?.description)
            awaitComplete()
        }
    }

    @Test
    fun `getRecurringByIdFlow returns null when not found`() = runTest {
        // Arrange
        coEvery { recurringDao.getRecurringByIdFlow(999L) } returns flowOf(null)

        // Act & Assert
        repository.getRecurringByIdFlow(999L).test {
            val result = awaitItem()
            assertNull(result)
            awaitComplete()
        }
    }

    // ==================== addRecurring Tests ====================

    @Test
    fun `addRecurring calls dao insert and returns id`() = runTest {
        // Arrange
        val recurring = RecurringTransactionEntity(
            amount = 1000.0,
            type = TransactionType.EXPENSE,
            description = "New Recurring",
            frequency = RecurringFrequency.MONTHLY,
            startDate = System.currentTimeMillis(),
            nextDueDate = System.currentTimeMillis() + 30 * 24 * 60 * 60 * 1000L,
            isActive = true
        )
        coEvery { recurringDao.insertRecurring(recurring) } returns 1L

        // Act
        val result = repository.addRecurring(recurring)

        // Assert
        assertEquals(1L, result)
        coVerify { recurringDao.insertRecurring(recurring) }
    }

    @Test
    fun `addRecurring with different frequencies`() = runTest {
        // Test DAILY
        val daily = RecurringTransactionEntity(
            amount = 100.0,
            type = TransactionType.EXPENSE,
            description = "Daily",
            frequency = RecurringFrequency.DAILY,
            startDate = System.currentTimeMillis(),
            nextDueDate = System.currentTimeMillis(),
            isActive = true
        )
        coEvery { recurringDao.insertRecurring(any()) } returns 1L

        repository.addRecurring(daily)
        coVerify { recurringDao.insertRecurring(match { it.frequency == RecurringFrequency.DAILY }) }

        // Test WEEKLY
        val weekly = daily.copy(description = "Weekly", frequency = RecurringFrequency.WEEKLY)
        repository.addRecurring(weekly)
        coVerify { recurringDao.insertRecurring(match { it.frequency == RecurringFrequency.WEEKLY }) }

        // Test YEARLY
        val yearly = daily.copy(description = "Yearly", frequency = RecurringFrequency.YEARLY)
        repository.addRecurring(yearly)
        coVerify { recurringDao.insertRecurring(match { it.frequency == RecurringFrequency.YEARLY }) }
    }

    // ==================== updateRecurring Tests ====================

    @Test
    fun `updateRecurring calls dao update`() = runTest {
        // Arrange
        val recurring = RecurringTransactionEntity(
            id = 1L,
            amount = 1500.0,
            type = TransactionType.EXPENSE,
            description = "Updated Recurring",
            frequency = RecurringFrequency.MONTHLY,
            startDate = System.currentTimeMillis(),
            nextDueDate = System.currentTimeMillis(),
            isActive = true
        )
        coEvery { recurringDao.updateRecurring(recurring) } just Runs

        // Act
        repository.updateRecurring(recurring)

        // Assert
        coVerify { recurringDao.updateRecurring(recurring) }
    }

    // ==================== deleteRecurring Tests ====================

    @Test
    fun `deleteRecurring calls dao delete`() = runTest {
        // Arrange
        val recurring = RecurringTransactionEntity(
            id = 1L,
            amount = 1000.0,
            type = TransactionType.EXPENSE,
            description = "To Delete",
            frequency = RecurringFrequency.MONTHLY,
            startDate = System.currentTimeMillis(),
            nextDueDate = System.currentTimeMillis(),
            isActive = true
        )
        coEvery { recurringDao.deleteRecurring(recurring) } just Runs

        // Act
        repository.deleteRecurring(recurring)

        // Assert
        coVerify { recurringDao.deleteRecurring(recurring) }
    }

    @Test
    fun `deleteRecurringById calls dao deleteById`() = runTest {
        // Arrange
        coEvery { recurringDao.deleteRecurringById(1L) } just Runs

        // Act
        repository.deleteRecurringById(1L)

        // Assert
        coVerify { recurringDao.deleteRecurringById(1L) }
    }

    // ==================== toggleActiveStatus Tests ====================

    @Test
    fun `toggleActiveStatus activates recurring`() = runTest {
        // Arrange
        coEvery { recurringDao.updateActiveStatus(1L, true) } just Runs

        // Act
        repository.toggleActiveStatus(1L, true)

        // Assert
        coVerify { recurringDao.updateActiveStatus(1L, true) }
    }

    @Test
    fun `toggleActiveStatus deactivates recurring`() = runTest {
        // Arrange
        coEvery { recurringDao.updateActiveStatus(1L, false) } just Runs

        // Act
        repository.toggleActiveStatus(1L, false)

        // Assert
        coVerify { recurringDao.updateActiveStatus(1L, false) }
    }

    // ==================== processDueRecurring Tests ====================

    @Test
    fun `processDueRecurring creates transaction and updates next due date`() = runTest {
        // Arrange
        val currentTime = System.currentTimeMillis()
        val recurring = RecurringTransactionEntity(
            id = 1L,
            amount = 1000.0,
            type = TransactionType.EXPENSE,
            categoryId = 1L,
            accountId = 1L,
            description = "Monthly Bill",
            frequency = RecurringFrequency.MONTHLY,
            startDate = currentTime,
            nextDueDate = currentTime,
            isActive = true
        )
        val transaction = TransactionEntity(
            amount = 1000.0,
            note = "Monthly Bill",
            date = currentTime,
            type = "EXPENSE",
            categoryId = 1,
            accountId = 1L,
            userId = "test_user"
        )

        coEvery { transactionDao.insertTransaction(transaction) } returns 1L
        coEvery { recurringDao.updateNextDueDate(any(), any()) } just Runs

        // Act
        repository.processDueRecurring(recurring, transaction)

        // Assert
        coVerify { transactionDao.insertTransaction(transaction) }
        coVerify { recurringDao.updateNextDueDate(1L, any()) }
    }

    @Test
    fun `processDueRecurring deactivates when end date reached`() = runTest {
        // Arrange
        val currentTime = System.currentTimeMillis()
        val nextMonth = currentTime + 30L * 24 * 60 * 60 * 1000
        val recurring = RecurringTransactionEntity(
            id = 1L,
            amount = 1000.0,
            type = TransactionType.EXPENSE,
            description = "Limited Recurring",
            frequency = RecurringFrequency.MONTHLY,
            startDate = currentTime,
            endDate = currentTime, // End date is now
            nextDueDate = currentTime,
            isActive = true
        )
        val transaction = TransactionEntity(
            amount = 1000.0,
            note = "Limited Recurring",
            date = currentTime,
            type = "EXPENSE",
            accountId = 1L,
            userId = "test_user"
        )

        coEvery { transactionDao.insertTransaction(any()) } returns 1L
        coEvery { recurringDao.updateActiveStatus(any(), any()) } just Runs

        // Act
        repository.processDueRecurring(recurring, transaction)

        // Assert
        coVerify { transactionDao.insertTransaction(any()) }
        coVerify { recurringDao.updateActiveStatus(1L, false) }
        coVerify(exactly = 0) { recurringDao.updateNextDueDate(any(), any()) }
    }

    @Test
    fun `processDueRecurring continues when end date not reached`() = runTest {
        // Arrange
        val currentTime = System.currentTimeMillis()
        val futureEndDate = currentTime + 90L * 24 * 60 * 60 * 1000
        val recurring = RecurringTransactionEntity(
            id = 1L,
            amount = 1000.0,
            type = TransactionType.EXPENSE,
            description = "Ongoing Recurring",
            frequency = RecurringFrequency.MONTHLY,
            startDate = currentTime,
            endDate = futureEndDate, // End date is in future
            nextDueDate = currentTime,
            isActive = true
        )
        val transaction = TransactionEntity(
            amount = 1000.0,
            note = "Ongoing Recurring",
            date = currentTime,
            type = "EXPENSE",
            accountId = 1L,
            userId = "test_user"
        )

        coEvery { transactionDao.insertTransaction(any()) } returns 1L
        coEvery { recurringDao.updateNextDueDate(any(), any()) } just Runs

        // Act
        repository.processDueRecurring(recurring, transaction)

        // Assert
        coVerify { transactionDao.insertTransaction(any()) }
        coVerify { recurringDao.updateNextDueDate(1L, any()) }
        coVerify(exactly = 0) { recurringDao.updateActiveStatus(any(), any()) }
    }

    @Test
    fun `processDueRecurring with null end date continues indefinitely`() = runTest {
        // Arrange
        val currentTime = System.currentTimeMillis()
        val recurring = RecurringTransactionEntity(
            id = 1L,
            amount = 500.0,
            type = TransactionType.EXPENSE,
            description = "Never-ending Recurring",
            frequency = RecurringFrequency.WEEKLY,
            startDate = currentTime,
            endDate = null, // No end date
            nextDueDate = currentTime,
            isActive = true
        )
        val transaction = TransactionEntity(
            amount = 500.0,
            note = "Never-ending Recurring",
            date = currentTime,
            type = "EXPENSE",
            accountId = 1L,
            userId = "test_user"
        )

        coEvery { transactionDao.insertTransaction(any()) } returns 1L
        coEvery { recurringDao.updateNextDueDate(any(), any()) } just Runs

        // Act
        repository.processDueRecurring(recurring, transaction)

        // Assert
        coVerify { recurringDao.updateNextDueDate(1L, any()) }
        coVerify(exactly = 0) { recurringDao.updateActiveStatus(any(), any()) }
    }

    @Test
    fun `processDueRecurring calculates correct next due date for different frequencies`() = runTest {
        // Test DAILY
        val currentTime = System.currentTimeMillis()
        val dailyRecurring = RecurringTransactionEntity(
            id = 1L,
            amount = 100.0,
            type = TransactionType.EXPENSE,
            description = "Daily",
            frequency = RecurringFrequency.DAILY,
            startDate = currentTime,
            nextDueDate = currentTime,
            isActive = true
        )
        val dailyTransaction = TransactionEntity(
            amount = 100.0,
            note = "Daily",
            date = currentTime,
            type = "EXPENSE",
            accountId = 1L,
            userId = "test_user"
        )

        coEvery { transactionDao.insertTransaction(any()) } returns 1L
        coEvery { recurringDao.updateNextDueDate(any(), any()) } just Runs

        repository.processDueRecurring(dailyRecurring, dailyTransaction)

        // Verify next due date is approximately 1 day later
        coVerify { recurringDao.updateNextDueDate(1L, match { it > currentTime + 23 * 60 * 60 * 1000L }) }
    }

    // ==================== getRecurringCount Tests ====================

    @Test
    fun `getRecurringCount returns count from dao`() = runTest {
        // Arrange
        coEvery { recurringDao.getRecurringCount() } returns 5

        // Act
        val result = repository.getRecurringCount()

        // Assert
        assertEquals(5, result)
    }

    @Test
    fun `getRecurringCount returns zero when none`() = runTest {
        // Arrange
        coEvery { recurringDao.getRecurringCount() } returns 0

        // Act
        val result = repository.getRecurringCount()

        // Assert
        assertEquals(0, result)
    }

    // ==================== getActiveRecurringCount Tests ====================

    @Test
    fun `getActiveRecurringCount returns count from dao`() = runTest {
        // Arrange
        coEvery { recurringDao.getActiveRecurringCount() } returns 3

        // Act
        val result = repository.getActiveRecurringCount()

        // Assert
        assertEquals(3, result)
    }

    @Test
    fun `getActiveRecurringCount returns zero when none active`() = runTest {
        // Arrange
        coEvery { recurringDao.getActiveRecurringCount() } returns 0

        // Act
        val result = repository.getActiveRecurringCount()

        // Assert
        assertEquals(0, result)
    }

    // ==================== Edge Cases ====================

    @Test
    fun `addRecurring with INCOME type`() = runTest {
        // Arrange
        val recurring = RecurringTransactionEntity(
            amount = 50000.0,
            type = TransactionType.INCOME,
            description = "Monthly Salary",
            frequency = RecurringFrequency.MONTHLY,
            startDate = System.currentTimeMillis(),
            nextDueDate = System.currentTimeMillis(),
            isActive = true
        )
        coEvery { recurringDao.insertRecurring(any()) } returns 1L

        // Act
        repository.addRecurring(recurring)

        // Assert
        coVerify { recurringDao.insertRecurring(match { it.type == TransactionType.INCOME }) }
    }

    @Test
    fun `addRecurring with very large amount`() = runTest {
        // Arrange
        val largeAmount = 10_000_000.0
        val recurring = RecurringTransactionEntity(
            amount = largeAmount,
            type = TransactionType.INCOME,
            description = "Big Payment",
            frequency = RecurringFrequency.YEARLY,
            startDate = System.currentTimeMillis(),
            nextDueDate = System.currentTimeMillis(),
            isActive = true
        )
        coEvery { recurringDao.insertRecurring(any()) } returns 1L

        // Act
        repository.addRecurring(recurring)

        // Assert
        coVerify { recurringDao.insertRecurring(match { it.amount == largeAmount }) }
    }

    @Test
    fun `processDueRecurring with BIWEEKLY frequency`() = runTest {
        // Arrange
        val currentTime = System.currentTimeMillis()
        val biweeklyRecurring = RecurringTransactionEntity(
            id = 1L,
            amount = 500.0,
            type = TransactionType.EXPENSE,
            description = "Biweekly Payment",
            frequency = RecurringFrequency.BIWEEKLY,
            startDate = currentTime,
            nextDueDate = currentTime,
            isActive = true
        )
        val transaction = TransactionEntity(
            amount = 500.0,
            note = "Biweekly Payment",
            date = currentTime,
            type = "EXPENSE",
            accountId = 1L,
            userId = "test_user"
        )

        coEvery { transactionDao.insertTransaction(any()) } returns 1L
        coEvery { recurringDao.updateNextDueDate(any(), any()) } just Runs

        // Act
        repository.processDueRecurring(biweeklyRecurring, transaction)

        // Assert - Verify next due date is approximately 14 days later
        coVerify { recurringDao.updateNextDueDate(1L, match { it > currentTime + 13 * 24 * 60 * 60 * 1000L }) }
    }

    @Test
    fun `processDueRecurring with QUARTERLY frequency`() = runTest {
        // Arrange
        val currentTime = System.currentTimeMillis()
        val quarterlyRecurring = RecurringTransactionEntity(
            id = 1L,
            amount = 1500.0,
            type = TransactionType.EXPENSE,
            description = "Quarterly Bill",
            frequency = RecurringFrequency.QUARTERLY,
            startDate = currentTime,
            nextDueDate = currentTime,
            isActive = true
        )
        val transaction = TransactionEntity(
            amount = 1500.0,
            note = "Quarterly Bill",
            date = currentTime,
            type = "EXPENSE",
            accountId = 1L,
            userId = "test_user"
        )

        coEvery { transactionDao.insertTransaction(any()) } returns 1L
        coEvery { recurringDao.updateNextDueDate(any(), any()) } just Runs

        // Act
        repository.processDueRecurring(quarterlyRecurring, transaction)

        // Assert - Verify next due date is approximately 90 days later
        coVerify { recurringDao.updateNextDueDate(1L, match { it > currentTime + 85 * 24 * 60 * 60 * 1000L }) }
    }

    @Test
    fun `multiple operations sequence`() = runTest {
        // Arrange
        val recurring1 = RecurringTransactionEntity(
            id = 1L,
            amount = 1000.0,
            type = TransactionType.EXPENSE,
            description = "Recurring 1",
            frequency = RecurringFrequency.MONTHLY,
            startDate = System.currentTimeMillis(),
            nextDueDate = System.currentTimeMillis(),
            isActive = true
        )
        val recurring2 = RecurringTransactionEntity(
            id = 2L,
            amount = 500.0,
            type = TransactionType.EXPENSE,
            description = "Recurring 2",
            frequency = RecurringFrequency.WEEKLY,
            startDate = System.currentTimeMillis(),
            nextDueDate = System.currentTimeMillis(),
            isActive = true
        )

        coEvery { recurringDao.insertRecurring(any()) } returnsMany listOf(1L, 2L)
        coEvery { recurringDao.updateActiveStatus(any(), any()) } just Runs
        coEvery { recurringDao.deleteRecurringById(any()) } just Runs

        // Act
        val id1 = repository.addRecurring(recurring1)
        val id2 = repository.addRecurring(recurring2)
        repository.toggleActiveStatus(id1, false)
        repository.deleteRecurringById(id2)

        // Assert
        assertEquals(1L, id1)
        assertEquals(2L, id2)
        coVerify { recurringDao.updateActiveStatus(1L, false) }
        coVerify { recurringDao.deleteRecurringById(2L) }
    }
}
