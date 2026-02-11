package com.pyera.app.data.repository

import app.cash.turbine.test
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.WriteBatch
import com.pyera.app.data.local.dao.TransactionDao
import com.pyera.app.data.local.entity.TransactionEntity
import com.pyera.app.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseUser
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class TransactionRepositoryImplTest {

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

    private lateinit var repository: TransactionRepositoryImpl

    private val testUserId = "test_user_123"

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        
        every { authRepository.currentUser } returns firebaseUser
        every { firebaseUser.uid } returns testUserId
        
        repository = TransactionRepositoryImpl(transactionDao, authRepository, firestore)
    }

    // ==================== insertTransaction Tests ====================

    @Test
    fun `insertTransaction calls dao insert`() = runTest {
        // Arrange
        val transaction = TransactionEntity(
            id = 1L,
            amount = 100.0,
            note = "Test transaction",
            date = System.currentTimeMillis(),
            type = "EXPENSE",
            categoryId = 1,
            accountId = 1L,
            userId = testUserId
        )
        coEvery { transactionDao.insertTransaction(transaction) } returns 1L

        // Act
        repository.insertTransaction(transaction)

        // Assert
        coVerify { transactionDao.insertTransaction(transaction) }
    }

    @Test
    fun `insertTransaction propagates exception`() = runTest {
        // Arrange
        val transaction = TransactionEntity(
            id = 1L,
            amount = 100.0,
            note = "Test transaction",
            date = System.currentTimeMillis(),
            type = "EXPENSE",
            categoryId = 1,
            accountId = 1L,
            userId = testUserId
        )
        coEvery { transactionDao.insertTransaction(transaction) } throws RuntimeException("Insert failed")

        // Act & Assert
        try {
            repository.insertTransaction(transaction)
            fail("Expected exception")
        } catch (e: RuntimeException) {
            assertEquals("Insert failed", e.message)
        }
    }

    // ==================== updateTransaction Tests ====================

    @Test
    fun `updateTransaction calls dao update`() = runTest {
        // Arrange
        val transaction = TransactionEntity(
            id = 1L,
            amount = 150.0,
            note = "Updated transaction",
            date = System.currentTimeMillis(),
            type = "EXPENSE",
            categoryId = 1,
            accountId = 1L,
            userId = testUserId
        )
        coEvery { transactionDao.updateTransaction(transaction) } just Runs

        // Act
        repository.updateTransaction(transaction)

        // Assert
        coVerify { transactionDao.updateTransaction(transaction) }
    }

    @Test
    fun `updateTransaction propagates exception`() = runTest {
        // Arrange
        val transaction = TransactionEntity(
            id = 1L,
            amount = 150.0,
            note = "Updated transaction",
            date = System.currentTimeMillis(),
            type = "EXPENSE",
            categoryId = 1,
            accountId = 1L,
            userId = testUserId
        )
        coEvery { transactionDao.updateTransaction(transaction) } throws RuntimeException("Update failed")

        // Act & Assert
        try {
            repository.updateTransaction(transaction)
            fail("Expected exception")
        } catch (e: RuntimeException) {
            assertEquals("Update failed", e.message)
        }
    }

    // ==================== deleteTransaction Tests ====================

    @Test
    fun `deleteTransaction calls dao delete`() = runTest {
        // Arrange
        val transaction = TransactionEntity(
            id = 1L,
            amount = 100.0,
            note = "Test transaction",
            date = System.currentTimeMillis(),
            type = "EXPENSE",
            categoryId = 1,
            accountId = 1L,
            userId = testUserId
        )
        coEvery { transactionDao.deleteTransaction(transaction) } just Runs

        // Act
        repository.deleteTransaction(transaction)

        // Assert
        coVerify { transactionDao.deleteTransaction(transaction) }
    }

    @Test
    fun `deleteTransaction propagates exception`() = runTest {
        // Arrange
        val transaction = TransactionEntity(
            id = 1L,
            amount = 100.0,
            note = "Test transaction",
            date = System.currentTimeMillis(),
            type = "EXPENSE",
            categoryId = 1,
            accountId = 1L,
            userId = testUserId
        )
        coEvery { transactionDao.deleteTransaction(transaction) } throws RuntimeException("Delete failed")

        // Act & Assert
        try {
            repository.deleteTransaction(transaction)
            fail("Expected exception")
        } catch (e: RuntimeException) {
            assertEquals("Delete failed", e.message)
        }
    }

    // ==================== getAllTransactions Tests ====================

    @Test
    fun `getAllTransactions returns flow from dao`() = runTest {
        // Arrange
        val transactions = listOf(
            TransactionEntity(
                id = 1L,
                amount = 100.0,
                note = "Transaction 1",
                date = System.currentTimeMillis(),
                type = "EXPENSE",
                categoryId = 1,
                accountId = 1L,
                userId = testUserId
            ),
            TransactionEntity(
                id = 2L,
                amount = 200.0,
                note = "Transaction 2",
                date = System.currentTimeMillis(),
                type = "INCOME",
                categoryId = 2,
                accountId = 1L,
                userId = testUserId
            )
        )
        coEvery { transactionDao.getAllTransactions() } returns flowOf(transactions)

        // Act & Assert
        repository.getAllTransactions().test {
            val result = awaitItem()
            assertEquals(2, result.size)
            assertEquals("Transaction 1", result[0].note)
            assertEquals("Transaction 2", result[1].note)
            awaitComplete()
        }
    }

    @Test
    fun `getAllTransactions emits empty list when no transactions`() = runTest {
        // Arrange
        coEvery { transactionDao.getAllTransactions() } returns flowOf(emptyList())

        // Act & Assert
        repository.getAllTransactions().test {
            val result = awaitItem()
            assertTrue(result.isEmpty())
            awaitComplete()
        }
    }

    // ==================== getTransactionsByAccount Tests ====================

    @Test
    fun `getTransactionsByAccount returns flow from dao`() = runTest {
        // Arrange
        val accountId = 1L
        val transactions = listOf(
            TransactionEntity(
                id = 1L,
                amount = 100.0,
                note = "Account Transaction",
                date = System.currentTimeMillis(),
                type = "EXPENSE",
                categoryId = 1,
                accountId = accountId,
                userId = testUserId
            )
        )
        coEvery { transactionDao.getTransactionsByAccount(accountId) } returns flowOf(transactions)

        // Act & Assert
        repository.getTransactionsByAccount(accountId).test {
            val result = awaitItem()
            assertEquals(1, result.size)
            assertEquals(accountId, result[0].accountId)
            awaitComplete()
        }
    }

    @Test
    fun `getTransactionsByAccount filters by correct account id`() = runTest {
        // Arrange
        val accountId = 2L
        val transactions = listOf(
            TransactionEntity(
                id = 1L,
                amount = 100.0,
                note = "Transaction for account 2",
                date = System.currentTimeMillis(),
                type = "EXPENSE",
                categoryId = 1,
                accountId = accountId,
                userId = testUserId
            )
        )
        coEvery { transactionDao.getTransactionsByAccount(accountId) } returns flowOf(transactions)

        // Act
        val result = repository.getTransactionsByAccount(accountId)

        // Assert
        result.test {
            val items = awaitItem()
            assertEquals(1, items.size)
            assertEquals(accountId, items[0].accountId)
            awaitComplete()
        }
        coVerify { transactionDao.getTransactionsByAccount(accountId) }
    }

    // ==================== getTransactionsForExport Tests ====================

    @Test
    fun `getTransactionsForExport returns list from dao`() = runTest {
        // Arrange
        val transactions = listOf(
            TransactionEntity(
                id = 1L,
                amount = 100.0,
                note = "Export transaction 1",
                date = System.currentTimeMillis(),
                type = "EXPENSE",
                categoryId = 1,
                accountId = 1L,
                userId = testUserId
            ),
            TransactionEntity(
                id = 2L,
                amount = 200.0,
                note = "Export transaction 2",
                date = System.currentTimeMillis(),
                type = "INCOME",
                categoryId = 2,
                accountId = 1L,
                userId = testUserId
            )
        )
        coEvery { transactionDao.getAllTransactionsOnce() } returns transactions

        // Act
        val result = repository.getTransactionsForExport()

        // Assert
        assertEquals(2, result.size)
        assertEquals("Export transaction 1", result[0].note)
        assertEquals("Export transaction 2", result[1].note)
    }

    @Test
    fun `getTransactionsForExport returns empty list when no transactions`() = runTest {
        // Arrange
        coEvery { transactionDao.getAllTransactionsOnce() } returns emptyList()

        // Act
        val result = repository.getTransactionsForExport()

        // Assert
        assertTrue(result.isEmpty())
    }

    // ==================== syncPendingTransactions Tests ====================

    @Test
    fun `syncPendingTransactions returns success when user authenticated`() = runTest {
        // Arrange
        val transactions = listOf(
            TransactionEntity(
                id = 1L,
                amount = 100.0,
                note = "Sync transaction",
                date = System.currentTimeMillis(),
                type = "EXPENSE",
                categoryId = 1,
                accountId = 1L,
                userId = testUserId
            )
        )
        coEvery { transactionDao.getAllTransactionsOnce() } returns transactions
        every { firestore.batch() } returns batch
        every { firestore.collection("users") } returns collectionReference
        every { collectionReference.document(testUserId) } returns documentReference
        every { documentReference.collection("transactions") } returns collectionReference
        every { collectionReference.document("1") } returns documentReference
        every { batch.set(any<DocumentReference>(), any<Map<String, Any?>>(), any()) } returns batch
        coEvery { batch.commit().await() } returns mockk()

        // Act
        val result = repository.syncPendingTransactions()

        // Assert
        assertTrue(result.isSuccess)
        coVerify { batch.commit().await() }
    }

    @Test
    fun `syncPendingTransactions returns success when no user`() = runTest {
        // Arrange
        every { authRepository.currentUser } returns null

        // Act
        val result = repository.syncPendingTransactions()

        // Assert
        assertTrue(result.isSuccess)
        coVerify(exactly = 0) { transactionDao.getAllTransactionsOnce() }
    }

    @Test
    fun `syncPendingTransactions returns success when no transactions`() = runTest {
        // Arrange
        coEvery { transactionDao.getAllTransactionsOnce() } returns emptyList()

        // Act
        val result = repository.syncPendingTransactions()

        // Assert
        assertTrue(result.isSuccess)
        verify(exactly = 0) { firestore.batch() }
    }

    @Test
    fun `syncPendingTransactions returns failure on firestore exception`() = runTest {
        // Arrange
        val transactions = listOf(
            TransactionEntity(
                id = 1L,
                amount = 100.0,
                note = "Sync transaction",
                date = System.currentTimeMillis(),
                type = "EXPENSE",
                categoryId = 1,
                accountId = 1L,
                userId = testUserId
            )
        )
        coEvery { transactionDao.getAllTransactionsOnce() } returns transactions
        every { firestore.batch() } returns batch
        every { firestore.collection("users") } returns collectionReference
        every { collectionReference.document(testUserId) } returns documentReference
        every { documentReference.collection("transactions") } returns collectionReference
        every { collectionReference.document("1") } returns documentReference
        every { batch.set(any<DocumentReference>(), any<Map<String, Any?>>(), any()) } returns batch
        coEvery { batch.commit().await() } throws RuntimeException("Firestore error")

        // Act
        val result = repository.syncPendingTransactions()

        // Assert
        assertTrue(result.isFailure)
        assertEquals("Firestore error", result.exceptionOrNull()?.message)
    }

    @Test
    fun `syncPendingTransactions maps all fields correctly`() = runTest {
        // Arrange
        val transaction = TransactionEntity(
            id = 1L,
            amount = 100.0,
            note = "Test",
            date = 1234567890L,
            type = "EXPENSE",
            categoryId = 5,
            accountId = 2L,
            userId = testUserId,
            isTransfer = true,
            transferAccountId = 3L,
            createdAt = 1234567000L,
            updatedAt = 1234568000L,
            receiptImagePath = "/path/to/image.jpg",
            receiptCloudUrl = "https://storage.url/image.jpg",
            hasReceipt = true
        )
        coEvery { transactionDao.getAllTransactionsOnce() } returns listOf(transaction)
        
        val capturedData = slot<Map<String, Any?>>()
        every { firestore.batch() } returns batch
        every { firestore.collection("users") } returns collectionReference
        every { collectionReference.document(testUserId) } returns documentReference
        every { documentReference.collection("transactions") } returns collectionReference
        every { collectionReference.document("1") } returns documentReference
        every { batch.set(any<DocumentReference>(), capture(capturedData), any()) } returns batch
        coEvery { batch.commit().await() } returns mockk()

        // Act
        repository.syncPendingTransactions()

        // Assert
        val data = capturedData.captured
        assertEquals(1L, data["id"])
        assertEquals(testUserId, data["userId"])
        assertEquals(100.0, data["amount"])
        assertEquals("Test", data["note"])
        assertEquals(1234567890L, data["date"])
        assertEquals("EXPENSE", data["type"])
        assertEquals(5, data["categoryId"])
        assertEquals(2L, data["accountId"])
        assertEquals(true, data["isTransfer"])
        assertEquals(3L, data["transferAccountId"])
        assertEquals(1234567000L, data["createdAt"])
        assertEquals(1234568000L, data["updatedAt"])
        assertEquals("/path/to/image.jpg", data["receiptImagePath"])
        assertEquals("https://storage.url/image.jpg", data["receiptCloudUrl"])
        assertEquals(true, data["hasReceipt"])
    }

    @Test
    fun `syncPendingTransactions handles null categoryId`() = runTest {
        // Arrange
        val transaction = TransactionEntity(
            id = 1L,
            amount = 100.0,
            note = "Test",
            date = System.currentTimeMillis(),
            type = "EXPENSE",
            categoryId = null,
            accountId = 1L,
            userId = testUserId
        )
        coEvery { transactionDao.getAllTransactionsOnce() } returns listOf(transaction)
        
        val capturedData = slot<Map<String, Any?>>()
        every { firestore.batch() } returns batch
        every { firestore.collection("users") } returns collectionReference
        every { collectionReference.document(testUserId) } returns documentReference
        every { documentReference.collection("transactions") } returns collectionReference
        every { collectionReference.document("1") } returns documentReference
        every { batch.set(any<DocumentReference>(), capture(capturedData), any()) } returns batch
        coEvery { batch.commit().await() } returns mockk()

        // Act
        repository.syncPendingTransactions()

        // Assert
        val data = capturedData.captured
        assertNull(data["categoryId"])
    }

    @Test
    fun `syncPendingTransactions batches multiple transactions`() = runTest {
        // Arrange
        val transactions = (1..5).map { id ->
            TransactionEntity(
                id = id.toLong(),
                amount = 100.0 * id,
                note = "Transaction $id",
                date = System.currentTimeMillis(),
                type = if (id % 2 == 0) "INCOME" else "EXPENSE",
                categoryId = id,
                accountId = 1L,
                userId = testUserId
            )
        }
        coEvery { transactionDao.getAllTransactionsOnce() } returns transactions
        every { firestore.batch() } returns batch
        every { firestore.collection("users") } returns collectionReference
        every { collectionReference.document(testUserId) } returns documentReference
        every { documentReference.collection("transactions") } returns collectionReference
        every { collectionReference.document(any()) } returns documentReference
        every { batch.set(any<DocumentReference>(), any<Map<String, Any?>>(), any()) } returns batch
        coEvery { batch.commit().await() } returns mockk()

        // Act
        val result = repository.syncPendingTransactions()

        // Assert
        assertTrue(result.isSuccess)
        verify(exactly = 5) { batch.set(any<DocumentReference>(), any<Map<String, Any?>>(), any()) }
    }
}
