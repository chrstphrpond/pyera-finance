package com.pyera.app.data.repository

import androidx.room.withTransaction
import com.pyera.app.data.local.PyeraDatabase
import com.pyera.app.data.local.dao.AccountDao
import com.pyera.app.data.local.dao.TransactionDao
import com.pyera.app.data.local.entity.AccountEntity
import com.pyera.app.data.local.entity.AccountType
import com.pyera.app.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseUser
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class AccountRepositoryImplTest {

    @MockK
    private lateinit var database: PyeraDatabase

    @MockK
    private lateinit var accountDao: AccountDao

    @MockK
    private lateinit var transactionDao: TransactionDao

    @MockK
    private lateinit var authRepository: AuthRepository

    @MockK
    private lateinit var firebaseUser: FirebaseUser

    private lateinit var repository: AccountRepositoryImpl

    private val testUserId = "test_user_123"

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        
        every { authRepository.currentUser } returns firebaseUser
        every { firebaseUser.uid } returns testUserId
        
        repository = AccountRepositoryImpl(database, accountDao, transactionDao, authRepository)
    }

    // ==================== createAccount Tests ====================

    @Test
    fun `createAccount with valid data returns success`() = runTest {
        // Arrange
        coEvery { accountDao.getAccountsByUser(testUserId) } returns flowOf(emptyList())
        coEvery { accountDao.insertAccount(any()) } returns 1L
        coEvery { accountDao.getAccountCount(testUserId) } returns 1
        coEvery { accountDao.setDefaultAccount(any(), any()) } just Runs
        coEvery { database.withTransaction(any<suspend () -> Result<Long>>()) } coAnswers {
            firstArg<suspend () -> Result<Long>>().invoke()
        }

        // Act
        val result = repository.createAccount(
            name = "Test Account",
            type = AccountType.BANK,
            initialBalance = 1000.0,
            currency = "PHP",
            color = 0xFF0000,
            icon = "🏦",
            isDefault = false
        )

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(1L, result.getOrNull())
        coVerify { accountDao.insertAccount(any()) }
    }

    @Test
    fun `createAccount with blank name returns failure`() = runTest {
        // Act
        val result = repository.createAccount(
            name = "",
            type = AccountType.BANK,
            initialBalance = 1000.0,
            currency = "PHP",
            color = 0xFF0000,
            icon = "🏦",
            isDefault = false
        )

        // Assert
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
        assertEquals("Account name cannot be empty", result.exceptionOrNull()?.message)
    }

    @Test
    fun `createAccount with duplicate name returns failure`() = runTest {
        // Arrange
        val existingAccount = AccountEntity(
            id = 1L,
            userId = testUserId,
            name = "Test Account",
            type = AccountType.BANK,
            balance = 500.0,
            currency = "PHP",
            color = 0xFF0000,
            icon = "🏦"
        )
        coEvery { accountDao.getAccountsByUser(testUserId) } returns flowOf(listOf(existingAccount))

        // Act
        val result = repository.createAccount(
            name = "Test Account",
            type = AccountType.BANK,
            initialBalance = 1000.0,
            currency = "PHP",
            color = 0xFF0000,
            icon = "🏦",
            isDefault = false
        )

        // Assert
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
        assertEquals("Account name already exists", result.exceptionOrNull()?.message)
    }

    @Test
    fun `createAccount sets as default when it's the first account`() = runTest {
        // Arrange
        coEvery { accountDao.getAccountsByUser(testUserId) } returns flowOf(emptyList())
        coEvery { accountDao.insertAccount(any()) } returns 1L
        coEvery { accountDao.getAccountCount(testUserId) } returns 1
        coEvery { accountDao.setDefaultAccount(any(), any()) } just Runs
        coEvery { database.withTransaction(any<suspend () -> Result<Long>>()) } coAnswers {
            firstArg<suspend () -> Result<Long>>().invoke()
        }

        // Act
        repository.createAccount(
            name = "First Account",
            type = AccountType.BANK,
            initialBalance = 1000.0,
            currency = "PHP",
            color = 0xFF0000,
            icon = "🏦",
            isDefault = false
        )

        // Assert
        coVerify { accountDao.setDefaultAccount(testUserId, 1L) }
    }

    @Test
    fun `createAccount handles database exception`() = runTest {
        // Arrange
        coEvery { accountDao.getAccountsByUser(testUserId) } returns flowOf(emptyList())
        coEvery { database.withTransaction(any<suspend () -> Result<Long>>()) } throws RuntimeException("Database error")

        // Act
        val result = repository.createAccount(
            name = "Test Account",
            type = AccountType.BANK,
            initialBalance = 1000.0,
            currency = "PHP",
            color = 0xFF0000,
            icon = "🏦",
            isDefault = false
        )

        // Assert
        assertTrue(result.isFailure)
        assertEquals("Database error", result.exceptionOrNull()?.message)
    }

    // ==================== transferBetweenAccounts Tests ====================

    @Test
    fun `transferBetweenAccounts with valid data returns success`() = runTest {
        // Arrange
        val fromAccount = AccountEntity(
            id = 1L,
            userId = testUserId,
            name = "From Account",
            type = AccountType.BANK,
            balance = 5000.0,
            currency = "PHP",
            color = 0xFF0000,
            icon = "🏦"
        )
        val toAccount = AccountEntity(
            id = 2L,
            userId = testUserId,
            name = "To Account",
            type = AccountType.EWALLET,
            balance = 1000.0,
            currency = "PHP",
            color = 0x00FF00,
            icon = "📱"
        )
        
        coEvery { accountDao.getAccountById(1L) } returns fromAccount
        coEvery { accountDao.getAccountById(2L) } returns toAccount
        coEvery { transactionDao.insertTransaction(any()) } returns 1L
        coEvery { accountDao.updateBalance(any(), any(), any()) } just Runs
        coEvery { database.withTransaction(any<suspend () -> Result<Unit>>()) } coAnswers {
            firstArg<suspend () -> Result<Unit>>().invoke()
        }

        // Act
        val result = repository.transferBetweenAccounts(
            fromAccountId = 1L,
            toAccountId = 2L,
            amount = 1000.0,
            description = "Test transfer",
            date = System.currentTimeMillis()
        )

        // Assert
        assertTrue(result.isSuccess)
        coVerify(exactly = 2) { transactionDao.insertTransaction(any()) }
        coVerify { accountDao.updateBalance(1L, 4000.0, any()) }
        coVerify { accountDao.updateBalance(2L, 2000.0, any()) }
    }

    @Test
    fun `transferBetweenAccounts with same account returns failure`() = runTest {
        // Arrange
        coEvery { database.withTransaction(any<suspend () -> Result<Unit>>()) } coAnswers {
            firstArg<suspend () -> Result<Unit>>().invoke()
        }

        // Act
        val result = repository.transferBetweenAccounts(
            fromAccountId = 1L,
            toAccountId = 1L,
            amount = 1000.0,
            description = "Test transfer",
            date = System.currentTimeMillis()
        )

        // Assert
        assertTrue(result.isFailure)
        assertEquals("Cannot transfer to the same account", result.exceptionOrNull()?.message)
    }

    @Test
    fun `transferBetweenAccounts with zero amount returns failure`() = runTest {
        // Arrange
        coEvery { database.withTransaction(any<suspend () -> Result<Unit>>()) } coAnswers {
            firstArg<suspend () -> Result<Unit>>().invoke()
        }

        // Act
        val result = repository.transferBetweenAccounts(
            fromAccountId = 1L,
            toAccountId = 2L,
            amount = 0.0,
            description = "Test transfer",
            date = System.currentTimeMillis()
        )

        // Assert
        assertTrue(result.isFailure)
        assertEquals("Transfer amount must be positive", result.exceptionOrNull()?.message)
    }

    @Test
    fun `transferBetweenAccounts with negative amount returns failure`() = runTest {
        // Arrange
        coEvery { database.withTransaction(any<suspend () -> Result<Unit>>()) } coAnswers {
            firstArg<suspend () -> Result<Unit>>().invoke()
        }

        // Act
        val result = repository.transferBetweenAccounts(
            fromAccountId = 1L,
            toAccountId = 2L,
            amount = -100.0,
            description = "Test transfer",
            date = System.currentTimeMillis()
        )

        // Assert
        assertTrue(result.isFailure)
        assertEquals("Transfer amount must be positive", result.exceptionOrNull()?.message)
    }

    @Test
    fun `transferBetweenAccounts with insufficient balance returns failure`() = runTest {
        // Arrange
        val fromAccount = AccountEntity(
            id = 1L,
            userId = testUserId,
            name = "From Account",
            type = AccountType.BANK,
            balance = 500.0,
            currency = "PHP",
            color = 0xFF0000,
            icon = "🏦"
        )
        val toAccount = AccountEntity(
            id = 2L,
            userId = testUserId,
            name = "To Account",
            type = AccountType.EWALLET,
            balance = 1000.0,
            currency = "PHP",
            color = 0x00FF00,
            icon = "📱"
        )
        
        coEvery { accountDao.getAccountById(1L) } returns fromAccount
        coEvery { accountDao.getAccountById(2L) } returns toAccount
        coEvery { database.withTransaction(any<suspend () -> Result<Unit>>()) } coAnswers {
            firstArg<suspend () -> Result<Unit>>().invoke()
        }

        // Act
        val result = repository.transferBetweenAccounts(
            fromAccountId = 1L,
            toAccountId = 2L,
            amount = 1000.0,
            description = "Test transfer",
            date = System.currentTimeMillis()
        )

        // Assert
        assertTrue(result.isFailure)
        assertEquals("Insufficient balance in source account", result.exceptionOrNull()?.message)
    }

    @Test
    fun `transferBetweenAccounts with non-existent source account returns failure`() = runTest {
        // Arrange
        coEvery { accountDao.getAccountById(1L) } returns null
        coEvery { database.withTransaction(any<suspend () -> Result<Unit>>()) } coAnswers {
            firstArg<suspend () -> Result<Unit>>().invoke()
        }

        // Act
        val result = repository.transferBetweenAccounts(
            fromAccountId = 1L,
            toAccountId = 2L,
            amount = 1000.0,
            description = "Test transfer",
            date = System.currentTimeMillis()
        )

        // Assert
        assertTrue(result.isFailure)
        assertEquals("Source account not found", result.exceptionOrNull()?.message)
    }

    @Test
    fun `transferBetweenAccounts with non-existent destination account returns failure`() = runTest {
        // Arrange
        val fromAccount = AccountEntity(
            id = 1L,
            userId = testUserId,
            name = "From Account",
            type = AccountType.BANK,
            balance = 5000.0,
            currency = "PHP",
            color = 0xFF0000,
            icon = "🏦"
        )
        
        coEvery { accountDao.getAccountById(1L) } returns fromAccount
        coEvery { accountDao.getAccountById(2L) } returns null
        coEvery { database.withTransaction(any<suspend () -> Result<Unit>>()) } coAnswers {
            firstArg<suspend () -> Result<Unit>>().invoke()
        }

        // Act
        val result = repository.transferBetweenAccounts(
            fromAccountId = 1L,
            toAccountId = 2L,
            amount = 1000.0,
            description = "Test transfer",
            date = System.currentTimeMillis()
        )

        // Assert
        assertTrue(result.isFailure)
        assertEquals("Destination account not found", result.exceptionOrNull()?.message)
    }

    @Test
    fun `transferBetweenAccounts uses default description when blank`() = runTest {
        // Arrange
        val fromAccount = AccountEntity(
            id = 1L,
            userId = testUserId,
            name = "Savings",
            type = AccountType.BANK,
            balance = 5000.0,
            currency = "PHP",
            color = 0xFF0000,
            icon = "🏦"
        )
        val toAccount = AccountEntity(
            id = 2L,
            userId = testUserId,
            name = "GCash",
            type = AccountType.EWALLET,
            balance = 1000.0,
            currency = "PHP",
            color = 0x00FF00,
            icon = "📱"
        )
        
        coEvery { accountDao.getAccountById(1L) } returns fromAccount
        coEvery { accountDao.getAccountById(2L) } returns toAccount
        coEvery { transactionDao.insertTransaction(any()) } returns 1L
        coEvery { accountDao.updateBalance(any(), any(), any()) } just Runs
        coEvery { database.withTransaction(any<suspend () -> Result<Unit>>()) } coAnswers {
            firstArg<suspend () -> Result<Unit>>().invoke()
        }

        // Act
        repository.transferBetweenAccounts(
            fromAccountId = 1L,
            toAccountId = 2L,
            amount = 1000.0,
            description = "",
            date = System.currentTimeMillis()
        )

        // Assert
        coVerify { transactionDao.insertTransaction(match { it.note == "Transfer to GCash" }) }
        coVerify { transactionDao.insertTransaction(match { it.note == "Transfer from Savings" }) }
    }

    // ==================== deleteAccount Tests ====================

    @Test
    fun `deleteAccount with no transactions returns success`() = runTest {
        // Arrange
        coEvery { transactionDao.getTransactionCountByAccount(1L) } returns 0
        coEvery { accountDao.deleteAccount(1L) } just Runs

        // Act
        val result = repository.deleteAccount(1L)

        // Assert
        assertTrue(result.isSuccess)
        coVerify { accountDao.deleteAccount(1L) }
    }

    @Test
    fun `deleteAccount with transactions returns failure`() = runTest {
        // Arrange
        coEvery { transactionDao.getTransactionCountByAccount(1L) } returns 5

        // Act
        val result = repository.deleteAccount(1L)

        // Assert
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalStateException)
        assertEquals("Cannot delete account with transactions. Archive it instead.", result.exceptionOrNull()?.message)
    }

    @Test
    fun `deleteAccount handles database exception`() = runTest {
        // Arrange
        coEvery { transactionDao.getTransactionCountByAccount(1L) } returns 0
        coEvery { accountDao.deleteAccount(1L) } throws RuntimeException("Delete failed")

        // Act
        val result = repository.deleteAccount(1L)

        // Assert
        assertTrue(result.isFailure)
        assertEquals("Delete failed", result.exceptionOrNull()?.message)
    }

    // ==================== Additional CRUD Tests ====================

    @Test
    fun `updateAccount with valid data returns success`() = runTest {
        // Arrange
        val account = AccountEntity(
            id = 1L,
            userId = testUserId,
            name = "Updated Account",
            type = AccountType.BANK,
            balance = 1000.0,
            currency = "PHP",
            color = 0xFF0000,
            icon = "🏦"
        )
        coEvery { accountDao.getAccountsByUser(testUserId) } returns flowOf(listOf(account))
        coEvery { accountDao.updateAccount(any()) } just Runs

        // Act
        val result = repository.updateAccount(account)

        // Assert
        assertTrue(result.isSuccess)
        coVerify { accountDao.updateAccount(any()) }
    }

    @Test
    fun `updateAccount with duplicate name returns failure`() = runTest {
        // Arrange
        val existingAccount = AccountEntity(
            id = 1L,
            userId = testUserId,
            name = "Account One",
            type = AccountType.BANK,
            balance = 1000.0,
            currency = "PHP",
            color = 0xFF0000,
            icon = "🏦"
        )
        val anotherAccount = AccountEntity(
            id = 2L,
            userId = testUserId,
            name = "Account Two",
            type = AccountType.BANK,
            balance = 2000.0,
            currency = "PHP",
            color = 0x00FF00,
            icon = "📱"
        )
        coEvery { accountDao.getAccountsByUser(testUserId) } returns flowOf(listOf(existingAccount, anotherAccount))

        // Act - Try to rename account 2 to "Account One"
        val result = repository.updateAccount(anotherAccount.copy(name = "Account One"))

        // Assert
        assertTrue(result.isFailure)
        assertEquals("Account name already exists", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getAllAccounts returns flow from dao`() = runTest {
        // Arrange
        val accounts = listOf(
            AccountEntity(
                id = 1L,
                userId = testUserId,
                name = "Account 1",
                type = AccountType.BANK,
                balance = 1000.0,
                currency = "PHP",
                color = 0xFF0000,
                icon = "🏦"
            )
        )
        coEvery { accountDao.getAccountsByUser(testUserId) } returns flowOf(accounts)

        // Act
        val result = repository.getAllAccounts().first()

        // Assert
        assertEquals(1, result.size)
        assertEquals("Account 1", result[0].name)
    }

    @Test
    fun `getAccountById returns account from dao`() = runTest {
        // Arrange
        val account = AccountEntity(
            id = 1L,
            userId = testUserId,
            name = "Test Account",
            type = AccountType.BANK,
            balance = 1000.0,
            currency = "PHP",
            color = 0xFF0000,
            icon = "🏦"
        )
        coEvery { accountDao.getAccountById(1L) } returns account

        // Act
        val result = repository.getAccountById(1L)

        // Assert
        assertNotNull(result)
        assertEquals("Test Account", result?.name)
    }

    @Test
    fun `getTotalBalance returns balance from dao`() = runTest {
        // Arrange
        coEvery { accountDao.getTotalBalance(testUserId) } returns 5000.0

        // Act
        val result = repository.getTotalBalance()

        // Assert
        assertEquals(5000.0, result, 0.01)
    }

    @Test
    fun `getTotalBalance returns zero when null`() = runTest {
        // Arrange
        coEvery { accountDao.getTotalBalance(testUserId) } returns null

        // Act
        val result = repository.getTotalBalance()

        // Assert
        assertEquals(0.0, result, 0.01)
    }

    @Test
    fun `recalculateBalance returns calculated balance`() = runTest {
        // Arrange
        val account = AccountEntity(
            id = 1L,
            userId = testUserId,
            name = "Test Account",
            type = AccountType.BANK,
            balance = 1000.0,
            currency = "PHP",
            color = 0xFF0000,
            icon = "🏦"
        )
        coEvery { accountDao.getAccountById(1L) } returns account
        coEvery { transactionDao.getAccountIncomeSum(1L) } returns 5000.0
        coEvery { transactionDao.getAccountExpenseSum(1L) } returns 2000.0
        coEvery { accountDao.updateBalance(1L, 3000.0) } just Runs

        // Act
        val result = repository.recalculateBalance(1L)

        // Assert
        assertTrue(result.isSuccess)
        val balance = requireNotNull(result.getOrNull())
        assertEquals(3000.0, balance, 0.01)
    }

    @Test
    fun `recalculateBalance returns failure for non-existent account`() = runTest {
        // Arrange
        coEvery { accountDao.getAccountById(1L) } returns null

        // Act
        val result = repository.recalculateBalance(1L)

        // Assert
        assertTrue(result.isFailure)
        assertEquals("Account not found", result.exceptionOrNull()?.message)
    }

    @Test
    fun `validateAccountName returns false for blank name`() = runTest {
        // Act
        val result = repository.validateAccountName("")

        // Assert
        assertFalse(result)
    }

    @Test
    fun `canDeleteAccount returns true when no transactions`() = runTest {
        // Arrange
        coEvery { transactionDao.getTransactionCountByAccount(1L) } returns 0

        // Act
        val result = repository.canDeleteAccount(1L)

        // Assert
        assertTrue(result)
    }

    @Test
    fun `canDeleteAccount returns false when has transactions`() = runTest {
        // Arrange
        coEvery { transactionDao.getTransactionCountByAccount(1L) } returns 5

        // Act
        val result = repository.canDeleteAccount(1L)

        // Assert
        assertFalse(result)
    }
}
