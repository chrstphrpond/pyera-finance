package com.pyera.app.data.repository

import app.cash.turbine.test
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.WriteBatch
import com.pyera.app.data.local.dao.BudgetDao
import com.pyera.app.data.local.dao.TransactionDao
import com.pyera.app.data.local.entity.BudgetEntity
import com.pyera.app.data.local.entity.BudgetPeriod
import com.pyera.app.data.local.entity.BudgetStatus
import com.pyera.app.data.local.entity.BudgetSummary
import com.pyera.app.data.local.entity.BudgetWithSpending
import com.pyera.app.data.local.entity.TransactionEntity
import com.pyera.app.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseUser
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.Calendar

@ExperimentalCoroutinesApi
class BudgetRepositoryImplTest {

    @MockK
    private lateinit var budgetDao: BudgetDao

    @MockK
    private lateinit var transactionDao: TransactionDao

    @MockK
    private lateinit var authRepository: AuthRepository

    @MockK
    private lateinit var firestore: FirebaseFirestore

    @MockK
    private lateinit var firebaseUser: FirebaseUser

    @MockK
    private lateinit var batch: WriteBatch

    @MockK
    private lateinit var collectionReference: CollectionReference

    @MockK
    private lateinit var documentReference: DocumentReference

    private lateinit var repository: BudgetRepositoryImpl

    private val testUserId = "test_user_123"

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        
        every { authRepository.currentUser } returns firebaseUser
        every { firebaseUser.uid } returns testUserId
        
        repository = BudgetRepositoryImpl(budgetDao, transactionDao, authRepository, firestore)
    }

    // ==================== createBudget Tests ====================

    @Test
    fun `createBudget inserts budget with updated timestamp`() = runTest {
        // Arrange
        val budget = BudgetEntity(
            id = 0,
            userId = testUserId,
            categoryId = 1,
            amount = 1000.0,
            period = BudgetPeriod.MONTHLY,
            startDate = System.currentTimeMillis(),
            endDate = System.currentTimeMillis() + 30 * 24 * 60 * 60 * 1000L,
            isActive = true
        )
        coEvery { budgetDao.insertBudget(any()) } returns 1L

        // Act
        val result = repository.createBudget(budget)

        // Assert
        assertEquals(1L, result)
        coVerify { budgetDao.insertBudget(match { it.updatedAt > 0 }) }
    }

    @Test
    fun `createBudget propagates exception`() = runTest {
        // Arrange
        val budget = BudgetEntity(
            userId = testUserId,
            categoryId = 1,
            amount = 1000.0,
            period = BudgetPeriod.MONTHLY
        )
        coEvery { budgetDao.insertBudget(any()) } throws RuntimeException("Insert failed")

        // Act & Assert
        try {
            repository.createBudget(budget)
            fail("Expected exception")
        } catch (e: RuntimeException) {
            assertEquals("Insert failed", e.message)
        }
    }

    // ==================== updateBudget Tests ====================

    @Test
    fun `updateBudget updates with new timestamp`() = runTest {
        // Arrange
        val budget = BudgetEntity(
            id = 1,
            userId = testUserId,
            categoryId = 1,
            amount = 1500.0,
            period = BudgetPeriod.MONTHLY,
            updatedAt = 0L
        )
        coEvery { budgetDao.updateBudget(any()) } just Runs

        // Act
        repository.updateBudget(budget)

        // Assert
        coVerify { budgetDao.updateBudget(match { it.updatedAt > 0 }) }
    }

    // ==================== deleteBudget Tests ====================

    @Test
    fun `deleteBudget calls dao delete`() = runTest {
        // Arrange
        val budget = BudgetEntity(
            id = 1,
            userId = testUserId,
            categoryId = 1,
            amount = 1000.0,
            period = BudgetPeriod.MONTHLY
        )
        coEvery { budgetDao.deleteBudget(budget) } just Runs

        // Act
        repository.deleteBudget(budget)

        // Assert
        coVerify { budgetDao.deleteBudget(budget) }
    }

    @Test
    fun `deleteBudgetById calls dao deleteById`() = runTest {
        // Arrange
        coEvery { budgetDao.deleteBudgetById(1) } just Runs

        // Act
        repository.deleteBudgetById(1)

        // Assert
        coVerify { budgetDao.deleteBudgetById(1) }
    }

    // ==================== Query Operations Tests ====================

    @Test
    fun `getBudgetById returns flow from dao`() = runTest {
        // Arrange
        val budget = BudgetEntity(
            id = 1,
            userId = testUserId,
            categoryId = 1,
            amount = 1000.0,
            period = BudgetPeriod.MONTHLY
        )
        coEvery { budgetDao.getBudgetById(1) } returns flowOf(budget)

        // Act & Assert
        repository.getBudgetById(1).test {
            val result = awaitItem()
            assertEquals(1, result?.id)
            assertEquals(1000.0, result?.amount)
            awaitComplete()
        }
    }

    @Test
    fun `getBudgetById returns null when not found`() = runTest {
        // Arrange
        coEvery { budgetDao.getBudgetById(999) } returns flowOf(null)

        // Act & Assert
        repository.getBudgetById(999).test {
            val result = awaitItem()
            assertNull(result)
            awaitComplete()
        }
    }

    @Test
    fun `getAllBudgetsForUser returns flow from dao`() = runTest {
        // Arrange
        val budgets = listOf(
            BudgetEntity(
                id = 1,
                userId = testUserId,
                categoryId = 1,
                amount = 1000.0,
                period = BudgetPeriod.MONTHLY
            ),
            BudgetEntity(
                id = 2,
                userId = testUserId,
                categoryId = 2,
                amount = 500.0,
                period = BudgetPeriod.WEEKLY
            )
        )
        coEvery { budgetDao.getAllBudgetsForUser(testUserId) } returns flowOf(budgets)

        // Act
        val result = repository.getAllBudgetsForUser(testUserId).first()

        // Assert
        assertEquals(2, result.size)
        assertEquals(1000.0, result[0].amount, 0.01)
        assertEquals(500.0, result[1].amount, 0.01)
    }

    @Test
    fun `getActiveBudgetsForUser returns only active budgets`() = runTest {
        // Arrange
        val budgets = listOf(
            BudgetEntity(
                id = 1,
                userId = testUserId,
                categoryId = 1,
                amount = 1000.0,
                period = BudgetPeriod.MONTHLY,
                isActive = true
            )
        )
        coEvery { budgetDao.getActiveBudgetsForUser(testUserId) } returns flowOf(budgets)

        // Act
        val result = repository.getActiveBudgetsForUser(testUserId).first()

        // Assert
        assertEquals(1, result.size)
        assertTrue(result[0].isActive)
    }

    @Test
    fun `getBudgetsByPeriod filters by period`() = runTest {
        // Arrange
        val budgets = listOf(
            BudgetEntity(
                id = 1,
                userId = testUserId,
                categoryId = 1,
                amount = 1000.0,
                period = BudgetPeriod.MONTHLY
            )
        )
        coEvery { budgetDao.getBudgetsByPeriod(BudgetPeriod.MONTHLY, testUserId) } returns flowOf(budgets)

        // Act
        val result = repository.getBudgetsByPeriod(BudgetPeriod.MONTHLY, testUserId).first()

        // Assert
        assertEquals(1, result.size)
        assertEquals(BudgetPeriod.MONTHLY, result[0].period)
    }

    // ==================== Budget with Spending Tests ====================

    @Test
    fun `getBudgetsWithSpending returns budgets with calculated days remaining`() = runTest {
        // Arrange
        val endDate = System.currentTimeMillis() + 5 * 24 * 60 * 60 * 1000L // 5 days from now
        val budgetWithSpending = BudgetWithSpending(
            id = 1,
            userId = testUserId,
            categoryId = 1,
            categoryName = "Food",
            categoryColor = 0xFF0000,
            categoryIcon = "🍔",
            amount = 1000.0,
            period = BudgetPeriod.MONTHLY,
            startDate = System.currentTimeMillis(),
            isActive = true,
            alertThreshold = 0.8f,
            spentAmount = 500.0,
            remainingAmount = 500.0,
            progressPercentage = 50f,
            isOverBudget = false,
            daysRemaining = 0
        )
        coEvery {
            budgetDao.getBudgetsWithSpending(
                testUserId,
                any(),
                endDate,
            )
        } returns flowOf(listOf(budgetWithSpending))

        // Act
        val result = repository.getBudgetsWithSpending(
            testUserId,
            System.currentTimeMillis(),
            endDate,
        ).first()

        // Assert
        assertEquals(1, result.size)
        assertTrue(result[0].daysRemaining >= 4) // Approximately 5 days
    }

    @Test
    fun `getBudgetWithSpendingById returns single budget`() = runTest {
        // Arrange
        val endDate = System.currentTimeMillis() + 10 * 24 * 60 * 60 * 1000L
        val budgetWithSpending = BudgetWithSpending(
            id = 1,
            userId = testUserId,
            categoryId = 1,
            categoryName = "Food",
            categoryColor = 0xFF0000,
            categoryIcon = "🍔",
            amount = 1000.0,
            period = BudgetPeriod.MONTHLY,
            startDate = System.currentTimeMillis(),
            isActive = true,
            alertThreshold = 0.8f,
            spentAmount = 800.0,
            remainingAmount = 200.0,
            progressPercentage = 80f,
            isOverBudget = false,
            daysRemaining = 0
        )
        coEvery {
            budgetDao.getBudgetWithSpendingById(
                1,
                any(),
                endDate,
            )
        } returns flowOf(budgetWithSpending)

        // Act
        val result = repository.getBudgetWithSpendingById(
            1,
            System.currentTimeMillis(),
            endDate,
        ).first()

        // Assert
        assertNotNull(result)
        assertEquals(1, result?.id)
        assertEquals(80f, result?.progressPercentage)
    }

    @Test
    fun `getBudgetsByStatus filters by status`() = runTest {
        // Arrange
        val startDate = System.currentTimeMillis()
        val endDate = startDate + 30 * 24 * 60 * 60 * 1000L
        val budgets = listOf(
            BudgetWithSpending(
                id = 1,
                userId = testUserId,
                categoryId = 1,
                categoryName = "Food",
                categoryColor = 0xFF0000,
                categoryIcon = "🍔",
                amount = 1000.0,
                period = BudgetPeriod.MONTHLY,
                startDate = startDate,
                isActive = true,
                alertThreshold = 0.8f,
                spentAmount = 900.0,
                remainingAmount = 100.0,
                progressPercentage = 90f,
                isOverBudget = false,
                daysRemaining = 10
            )
        )
        coEvery { budgetDao.getBudgetsWithSpending(testUserId, startDate, endDate) } returns flowOf(budgets)

        // Act
        val result = repository.getBudgetsByStatus(
            testUserId,
            BudgetStatus.WARNING,
            startDate,
            endDate
        ).first()

        // Assert
        assertEquals(1, result.size)
        assertEquals(BudgetStatus.WARNING, result[0].status)
    }

    // ==================== Summary Tests ====================

    @Test
    fun `getBudgetSummary returns summary from dao`() = runTest {
        // Arrange
        val startDate = System.currentTimeMillis()
        val endDate = startDate + 30 * 24 * 60 * 60 * 1000L
        val summary = BudgetSummary(
            totalBudgets = 5,
            totalBudgetAmount = 5000.0,
            totalSpent = 3000.0,
            totalRemaining = 2000.0,
            overallProgress = 60f,
            overBudgetCount = 1,
            warningCount = 2,
            healthyCount = 2
        )
        coEvery { budgetDao.getBudgetSummary(testUserId, startDate, endDate) } returns flowOf(summary)

        // Act
        val result = repository.getBudgetSummary(testUserId, startDate, endDate).first()

        // Assert
        assertEquals(5, result.totalBudgets)
        assertEquals(5000.0, result.totalBudgetAmount, 0.01)
        assertEquals(3000.0, result.totalSpent, 0.01)
        assertEquals(60f, result.overallProgress, 0.01f)
    }

    @Test
    fun `getActiveBudgetCount returns count from dao`() = runTest {
        // Arrange
        coEvery { budgetDao.getActiveBudgetCount(testUserId) } returns flowOf(5)

        // Act
        val result = repository.getActiveBudgetCount(testUserId).first()

        // Assert
        assertEquals(5, result)
    }

    @Test
    fun `getOverBudgetCount returns count from dao`() = runTest {
        // Arrange
        val startDate = System.currentTimeMillis()
        val endDate = startDate + 30 * 24 * 60 * 60 * 1000L
        coEvery { budgetDao.getOverBudgetCount(testUserId, startDate, endDate) } returns flowOf(2)

        // Act
        val result = repository.getOverBudgetCount(testUserId, startDate, endDate).first()

        // Assert
        assertEquals(2, result)
    }

    // ==================== calculatePeriodDates Tests ====================

    @Test
    fun `calculatePeriodDates returns correct daily period`() {
        // Arrange
        val calendar = Calendar.getInstance()
        calendar.set(2024, Calendar.JANUARY, 15, 0, 0, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startDate = calendar.timeInMillis

        // Act
        val (start, end) = repository.calculatePeriodDates(BudgetPeriod.DAILY, startDate)

        // Assert
        val expectedEnd = start + 24 * 60 * 60 * 1000L - 1
        assertEquals(startDate, start)
        assertTrue(kotlin.math.abs(end - expectedEnd) <= 1000L) // Allow 1 second tolerance
    }

    @Test
    fun `calculatePeriodDates returns correct weekly period`() {
        // Arrange
        val calendar = Calendar.getInstance()
        calendar.set(2024, Calendar.JANUARY, 15, 0, 0, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startDate = calendar.timeInMillis

        // Act
        val (start, end) = repository.calculatePeriodDates(BudgetPeriod.WEEKLY, startDate)

        // Assert
        val expectedEnd = start + 7 * 24 * 60 * 60 * 1000L - 1
        assertEquals(startDate, start)
        assertTrue(end > start)
    }

    @Test
    fun `calculatePeriodDates returns correct monthly period`() {
        // Arrange
        val calendar = Calendar.getInstance()
        calendar.set(2024, Calendar.JANUARY, 15, 0, 0, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startDate = calendar.timeInMillis

        // Act
        val (start, end) = repository.calculatePeriodDates(BudgetPeriod.MONTHLY, startDate)

        // Assert
        assertEquals(startDate, start)
        assertTrue(end > start)
        
        // Verify it's approximately 30 days
        val diffDays = (end - start) / (24 * 60 * 60 * 1000L)
        assertTrue(diffDays in 27..31)
    }

    @Test
    fun `calculatePeriodDates returns correct yearly period`() {
        // Arrange
        val calendar = Calendar.getInstance()
        calendar.set(2024, Calendar.JANUARY, 15, 0, 0, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startDate = calendar.timeInMillis

        // Act
        val (start, end) = repository.calculatePeriodDates(BudgetPeriod.YEARLY, startDate)

        // Assert
        assertEquals(startDate, start)
        assertTrue(end > start)
        
        // Verify it's approximately 365 days
        val diffDays = (end - start) / (24 * 60 * 60 * 1000L)
        assertTrue(diffDays in 364..366)
    }

    // ==================== Budget Management Tests ====================

    @Test
    fun `deactivateBudget calls dao deactivate`() = runTest {
        // Arrange
        coEvery { budgetDao.deactivateBudget(1) } just Runs

        // Act
        repository.deactivateBudget(1)

        // Assert
        coVerify { budgetDao.deactivateBudget(1) }
    }

    @Test
    fun `activateBudget calls dao activate`() = runTest {
        // Arrange
        coEvery { budgetDao.activateBudget(1) } just Runs

        // Act
        repository.activateBudget(1)

        // Assert
        coVerify { budgetDao.activateBudget(1) }
    }

    @Test
    fun `setBudgetForCategory updates existing budget`() = runTest {
        // Arrange
        val existingBudget = BudgetEntity(
            id = 1,
            userId = testUserId,
            categoryId = 1,
            amount = 500.0,
            period = BudgetPeriod.WEEKLY,
            isActive = true
        )
        coEvery { budgetDao.getActiveBudgetForCategory(1) } returns flowOf(existingBudget)
        coEvery { budgetDao.updateBudget(any()) } just Runs

        // Act
        repository.setBudgetForCategory(1, 1000.0, BudgetPeriod.MONTHLY, testUserId)

        // Assert
        coVerify { budgetDao.updateBudget(match { it.amount == 1000.0 && it.period == BudgetPeriod.MONTHLY }) }
    }

    @Test
    fun `setBudgetForCategory creates new budget when none exists`() = runTest {
        // Arrange
        coEvery { budgetDao.getActiveBudgetForCategory(1) } returns flowOf(null)
        coEvery { budgetDao.insertBudget(any()) } returns 1L

        // Act
        repository.setBudgetForCategory(1, 1000.0, BudgetPeriod.MONTHLY, testUserId)

        // Assert
        coVerify { budgetDao.insertBudget(match { 
            it.categoryId == 1 && 
            it.amount == 1000.0 && 
            it.period == BudgetPeriod.MONTHLY &&
            it.userId == testUserId
        }) }
    }

    @Test
    fun `hasActiveBudget returns true when budget exists`() = runTest {
        // Arrange
        val budget = BudgetEntity(
            id = 1,
            userId = testUserId,
            categoryId = 1,
            amount = 1000.0,
            period = BudgetPeriod.MONTHLY
        )
        coEvery { budgetDao.getActiveBudgetForCategory(1) } returns flowOf(budget)

        // Act
        val result = repository.hasActiveBudget(1)

        // Assert
        assertTrue(result)
    }

    @Test
    fun `hasActiveBudget returns false when no budget exists`() = runTest {
        // Arrange
        coEvery { budgetDao.getActiveBudgetForCategory(1) } returns flowOf(null)

        // Act
        val result = repository.hasActiveBudget(1)

        // Assert
        assertFalse(result)
    }

    @Test
    fun `getCategorySpendingProgress calculates correct progress`() = runTest {
        // Arrange
        val budget = BudgetEntity(
            id = 1,
            userId = testUserId,
            categoryId = 1,
            amount = 1000.0,
            period = BudgetPeriod.MONTHLY
        )
        val transactions = listOf(
            TransactionEntity(
                id = 1L,
                amount = 400.0,
                note = "Expense 1",
                date = System.currentTimeMillis(),
                type = "EXPENSE",
                categoryId = 1,
                accountId = 1L,
                userId = testUserId
            )
        )
        coEvery { budgetDao.getActiveBudgetForCategory(1) } returns flowOf(budget)
        coEvery { 
            transactionDao.getTransactionsByCategoryAndTypeBetweenDates(
                1, "EXPENSE", any(), any()
            ) 
        } returns flowOf(transactions)

        // Act
        val result = repository.getCategorySpendingProgress(
            1, 
            System.currentTimeMillis(), 
            System.currentTimeMillis() + 30 * 24 * 60 * 60 * 1000L
        )

        // Assert
        assertEquals(0.4f, result, 0.01f)
    }

    @Test
    fun `getCategorySpendingProgress returns zero when no budget`() = runTest {
        // Arrange
        coEvery { budgetDao.getActiveBudgetForCategory(1) } returns flowOf(null)

        // Act
        val result = repository.getCategorySpendingProgress(1, 0L, 1000L)

        // Assert
        assertEquals(0f, result, 0.01f)
    }

    @Test
    fun `getCategorySpendingProgress returns zero when budget amount is zero`() = runTest {
        // Arrange
        val budget = BudgetEntity(
            id = 1,
            userId = testUserId,
            categoryId = 1,
            amount = 0.0,
            period = BudgetPeriod.MONTHLY
        )
        coEvery { budgetDao.getActiveBudgetForCategory(1) } returns flowOf(budget)
        coEvery { 
            transactionDao.getTransactionsByCategoryAndTypeBetweenDates(any(), any(), any(), any()) 
        } returns flowOf(emptyList())

        // Act
        val result = repository.getCategorySpendingProgress(1, 0L, 1000L)

        // Assert
        assertEquals(0f, result, 0.01f)
    }

    @Test
    fun `getCategorySpendingProgress coerces to max of 1f`() = runTest {
        // Arrange
        val budget = BudgetEntity(
            id = 1,
            userId = testUserId,
            categoryId = 1,
            amount = 500.0,
            period = BudgetPeriod.MONTHLY
        )
        val transactions = listOf(
            TransactionEntity(
                id = 1L,
                amount = 1000.0, // More than budget
                note = "Big expense",
                date = System.currentTimeMillis(),
                type = "EXPENSE",
                categoryId = 1,
                accountId = 1L,
                userId = testUserId
            )
        )
        coEvery { budgetDao.getActiveBudgetForCategory(1) } returns flowOf(budget)
        coEvery { 
            transactionDao.getTransactionsByCategoryAndTypeBetweenDates(any(), any(), any(), any()) 
        } returns flowOf(transactions)

        // Act
        val result = repository.getCategorySpendingProgress(1, 0L, 1000L)

        // Assert
        assertEquals(1f, result, 0.01f) // Should be capped at 1.0
    }

    // ==================== syncBudgets Tests ====================

    @Test
    fun `syncBudgets returns success when user authenticated`() = runTest {
        // Arrange
        val budgets = listOf(
            BudgetEntity(
                id = 1,
                userId = testUserId,
                categoryId = 1,
                amount = 1000.0,
                period = BudgetPeriod.MONTHLY
            )
        )
        coEvery { budgetDao.getAllBudgetsForUserOnce(testUserId) } returns budgets
        every { firestore.batch() } returns batch
        every { firestore.collection("users") } returns collectionReference
        every { collectionReference.document(testUserId) } returns documentReference
        every { documentReference.collection("budgets") } returns collectionReference
        every { collectionReference.document("1") } returns documentReference
        every { batch.set(any<DocumentReference>(), any<Map<String, Any?>>(), any()) } returns batch
        coEvery { batch.commit().await() } returns mockk()

        // Act
        val result = repository.syncBudgets()

        // Assert
        assertTrue(result.isSuccess)
        coVerify { batch.commit().await() }
    }

    @Test
    fun `syncBudgets returns success when no user`() = runTest {
        // Arrange
        every { authRepository.currentUser } returns null

        // Act
        val result = repository.syncBudgets()

        // Assert
        assertTrue(result.isSuccess)
        coVerify(exactly = 0) { budgetDao.getAllBudgetsForUserOnce(any()) }
    }

    @Test
    fun `syncBudgets returns success when no budgets`() = runTest {
        // Arrange
        coEvery { budgetDao.getAllBudgetsForUserOnce(testUserId) } returns emptyList()

        // Act
        val result = repository.syncBudgets()

        // Assert
        assertTrue(result.isSuccess)
        verify(exactly = 0) { firestore.batch() }
    }

    @Test
    fun `syncBudgets returns failure on firestore exception`() = runTest {
        // Arrange
        val budgets = listOf(
            BudgetEntity(
                id = 1,
                userId = testUserId,
                categoryId = 1,
                amount = 1000.0,
                period = BudgetPeriod.MONTHLY
            )
        )
        coEvery { budgetDao.getAllBudgetsForUserOnce(testUserId) } returns budgets
        every { firestore.batch() } returns batch
        every { firestore.collection("users") } returns collectionReference
        every { collectionReference.document(testUserId) } returns documentReference
        every { documentReference.collection("budgets") } returns collectionReference
        every { collectionReference.document(any()) } returns documentReference
        every { batch.set(any<DocumentReference>(), any<Map<String, Any?>>(), any()) } returns batch
        coEvery { batch.commit().await() } throws RuntimeException("Firestore error")

        // Act
        val result = repository.syncBudgets()

        // Assert
        assertTrue(result.isFailure)
        assertEquals("Firestore error", result.exceptionOrNull()?.message)
    }

    @Test
    fun `syncBudgets maps all fields correctly`() = runTest {
        // Arrange
        val budget = BudgetEntity(
            id = 1,
            userId = testUserId,
            categoryId = 5,
            amount = 1000.0,
            period = BudgetPeriod.MONTHLY,
            startDate = 1234567000L,
            endDate = 1234653400L,
            isActive = true,
            createdAt = 1234566000L,
            updatedAt = 1234568000L,
            alertThreshold = 0.8f
        )
        coEvery { budgetDao.getAllBudgetsForUserOnce(testUserId) } returns listOf(budget)
        
        val capturedData = slot<Map<String, Any?>>()
        every { firestore.batch() } returns batch
        every { firestore.collection("users") } returns collectionReference
        every { collectionReference.document(testUserId) } returns documentReference
        every { documentReference.collection("budgets") } returns collectionReference
        every { collectionReference.document("1") } returns documentReference
        every { batch.set(any<DocumentReference>(), capture(capturedData), any()) } returns batch
        coEvery { batch.commit().await() } returns mockk()

        // Act
        repository.syncBudgets()

        // Assert
        val data = capturedData.captured
        assertEquals(1, data["id"])
        assertEquals(testUserId, data["userId"])
        assertEquals(5, data["categoryId"])
        assertEquals(1000.0, data["amount"])
        assertEquals("MONTHLY", data["period"])
        assertEquals(1234567000L, data["startDate"])
        assertEquals(1234653400L, data["endDate"])
        assertEquals(true, data["isActive"])
        assertEquals(1234566000L, data["createdAt"])
        assertEquals(1234568000L, data["updatedAt"])
        assertEquals(0.8f, data["alertThreshold"])
    }
}
