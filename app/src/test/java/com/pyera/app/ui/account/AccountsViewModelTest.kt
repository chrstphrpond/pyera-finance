package com.pyera.app.ui.account

import app.cash.turbine.test
import com.pyera.app.data.local.entity.AccountEntity
import com.pyera.app.data.local.entity.AccountType
import com.pyera.app.domain.repository.AccountRepository
import com.pyera.app.domain.repository.TransactionRepository
import com.pyera.app.test.createAccount
import com.pyera.app.test.createCashAccount
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
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AccountsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    
    private lateinit var accountRepository: AccountRepository
    private lateinit var transactionRepository: TransactionRepository
    private lateinit var viewModel: AccountsViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        accountRepository = mockk(relaxed = true)
        transactionRepository = mockk(relaxed = true)
        
        // Default flows
        every { accountRepository.getAllAccounts() } returns flowOf(emptyList())
        every { accountRepository.getActiveAccounts() } returns flowOf(emptyList())
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
        viewModel.isLoading.test {
            assertFalse(awaitItem())
        }
        viewModel.error.test {
            assertNull(awaitItem())
        }
        viewModel.selectedAccount.test {
            assertNull(awaitItem())
        }
    }

    @Test
    fun `initial accounts is empty`() = runTest(testDispatcher) {
        // When
        viewModel = createViewModel()
        
        // Then
        viewModel.accounts.test {
            assertTrue(awaitItem().isEmpty())
        }
    }

    @Test
    fun `initial activeAccounts is empty`() = runTest(testDispatcher) {
        // When
        viewModel = createViewModel()
        
        // Then
        viewModel.activeAccounts.test {
            assertTrue(awaitItem().isEmpty())
        }
    }

    @Test
    fun `initial totalBalance is zero`() = runTest(testDispatcher) {
        // When
        viewModel = createViewModel()
        
        // Then
        viewModel.totalBalance.test {
            assertEquals(0.0, awaitItem(), 0.01)
        }
    }

    // ==================== Accounts Flow Tests ====================

    @Test
    fun `accounts flow updates with repository data`() = runTest(testDispatcher) {
        // Given
        val accounts = listOf(
            createAccount(id = 1, name = "Bank", balance = 1000.0),
            createCashAccount(id = 2, balance = 500.0)
        )
        every { accountRepository.getAllAccounts() } returns flowOf(accounts)
        
        // When
        viewModel = createViewModel()
        advanceUntilIdle()
        
        // Then
        viewModel.accounts.test {
            val result = awaitItem()
            assertEquals(2, result.size)
            assertEquals("Bank", result[0].name)
            assertEquals(1000.0, result[0].balance, 0.01)
        }
    }

    @Test
    fun `activeAccounts flow updates with repository data`() = runTest(testDispatcher) {
        // Given
        val accounts = listOf(
            createAccount(id = 1, name = "Active", isArchived = false),
            createAccount(id = 2, name = "Archived", isArchived = true)
        )
        every { accountRepository.getActiveAccounts() } returns flowOf(
            accounts.filter { !it.isArchived }
        )
        
        // When
        viewModel = createViewModel()
        advanceUntilIdle()
        
        // Then
        viewModel.activeAccounts.test {
            val result = awaitItem()
            assertEquals(1, result.size)
            assertEquals("Active", result[0].name)
        }
    }

    @Test
    fun `totalBalance calculates from non-archived accounts`() = runTest(testDispatcher) {
        // Given
        val accounts = listOf(
            createAccount(id = 1, balance = 1000.0, isArchived = false),
            createAccount(id = 2, balance = 500.0, isArchived = false),
            createAccount(id = 3, balance = 200.0, isArchived = true) // Should be excluded
        )
        every { accountRepository.getAllAccounts() } returns flowOf(accounts)
        
        // When
        viewModel = createViewModel()
        advanceUntilIdle()
        
        // Then
        viewModel.totalBalance.test {
            // Only counts non-archived: 1000 + 500 = 1500
            assertEquals(1500.0, awaitItem(), 0.01)
        }
    }

    // ==================== Account Selection Tests ====================

    @Test
    fun `selectAccount updates selectedAccount`() = runTest(testDispatcher) {
        // Given
        viewModel = createViewModel()
        val account = createAccount(id = 1, name = "Test Account")
        
        // When
        viewModel.selectAccount(account)
        
        // Then
        viewModel.selectedAccount.test {
            val result = awaitItem()
            assertNotNull(result)
            assertEquals(1L, result?.id)
            assertEquals("Test Account", result?.name)
        }
    }

    @Test
    fun `selectAccount with null clears selection`() = runTest(testDispatcher) {
        // Given
        viewModel = createViewModel()
        viewModel.selectAccount(createAccount())
        
        // When
        viewModel.selectAccount(null)
        
        // Then
        viewModel.selectedAccount.test {
            assertNull(awaitItem())
        }
    }

    // ==================== Load Account Detail Tests ====================

    @Test
    fun `loadAccountDetail fetches and sets account`() = runTest(testDispatcher) {
        // Given
        viewModel = createViewModel()
        val account = createAccount(id = 1, name = "Test")
        coEvery { accountRepository.getAccountById(1) } returns account
        
        // When
        viewModel.loadAccountDetail(1)
        advanceUntilIdle()
        
        // Then
        viewModel.selectedAccount.test {
            val result = awaitItem()
            assertNotNull(result)
            assertEquals("Test", result?.name)
        }
        viewModel.isLoading.test {
            assertFalse(awaitItem())
        }
    }

    @Test
    fun `loadAccountDetail sets error on failure`() = runTest(testDispatcher) {
        // Given
        viewModel = createViewModel()
        coEvery { accountRepository.getAccountById(1) } throws RuntimeException("DB error")
        
        // When
        viewModel.loadAccountDetail(1)
        advanceUntilIdle()
        
        // Then
        viewModel.error.test {
            val error = awaitItem()
            assertNotNull(error)
            assertTrue(error?.contains("DB error") == true)
        }
        viewModel.isLoading.test {
            assertFalse(awaitItem())
        }
    }

    // ==================== Form State Tests ====================

    @Test
    fun `updateFormState updates individual fields`() = runTest(testDispatcher) {
        // Given
        viewModel = createViewModel()
        
        // When
        viewModel.updateFormState(name = "New Account")
        viewModel.updateFormState(type = AccountType.CASH)
        viewModel.updateFormState(initialBalance = "500.00")
        viewModel.updateFormState(currency = "USD")
        viewModel.updateFormState(color = 0xFF0000)
        viewModel.updateFormState(icon = "💵")
        viewModel.updateFormState(isDefault = true)
        
        // Then
        viewModel.formState.test {
            val state = awaitItem()
            assertEquals("New Account", state.name)
            assertEquals(AccountType.CASH, state.type)
            assertEquals("500.00", state.initialBalance)
            assertEquals("USD", state.currency)
            assertEquals(0xFF0000, state.color)
            assertEquals("💵", state.icon)
            assertTrue(state.isDefault)
        }
    }

    @Test
    fun `resetFormState resets to defaults`() = runTest(testDispatcher) {
        // Given
        viewModel = createViewModel()
        viewModel.updateFormState(name = "Test", initialBalance = "1000")
        
        // When
        viewModel.resetFormState()
        
        // Then
        viewModel.formState.test {
            val state = awaitItem()
            assertEquals("", state.name)
            assertEquals(AccountType.BANK, state.type)
            assertEquals("", state.initialBalance)
            assertEquals("PHP", state.currency)
            assertFalse(state.isEditing)
            assertNull(state.editingAccountId)
        }
    }

    @Test
    fun `initEditForm sets form state from account`() = runTest(testDispatcher) {
        // Given
        viewModel = createViewModel()
        val account = createAccount(
            id = 5,
            name = "Test Account",
            type = AccountType.CASH,
            balance = 1500.0,
            currency = "USD",
            color = 0xFF00FF,
            icon = "💰",
            isDefault = true
        )
        
        // When
        viewModel.initEditForm(account)
        
        // Then
        viewModel.formState.test {
            val state = awaitItem()
            assertEquals("Test Account", state.name)
            assertEquals(AccountType.CASH, state.type)
            assertEquals("1500.0", state.initialBalance)
            assertEquals("USD", state.currency)
            assertEquals(0xFF00FF, state.color)
            assertEquals("💰", state.icon)
            assertTrue(state.isDefault)
            assertTrue(state.isEditing)
            assertEquals(5L, state.editingAccountId)
        }
    }

    // ==================== Create Account Tests ====================

    @Test
    fun `createAccount with valid data calls repository`() = runTest(testDispatcher) {
        // Given
        viewModel = createViewModel()
        viewModel.updateFormState(
            name = "New Account",
            type = AccountType.BANK,
            initialBalance = "1000",
            currency = "PHP",
            color = 0xFF4CAF50.toInt(),
            icon = "🏦",
            isDefault = false
        )
        
        coEvery { 
            accountRepository.createAccount(any(), any(), any(), any(), any(), any(), any()) 
        } returns Result.success(1L)
        
        // When
        viewModel.createAccount()
        advanceUntilIdle()
        
        // Then
        coVerify {
            accountRepository.createAccount(
                name = "New Account",
                type = AccountType.BANK,
                initialBalance = 1000.0,
                currency = "PHP",
                color = 0xFF4CAF50.toInt(),
                icon = "🏦",
                isDefault = false
            )
        }
    }

    @Test
    fun `createAccount resets form on success`() = runTest(testDispatcher) {
        // Given
        viewModel = createViewModel()
        viewModel.updateFormState(name = "Test", initialBalance = "1000")
        coEvery { accountRepository.createAccount(any(), any(), any(), any(), any(), any(), any()) } returns Result.success(1L)
        
        // When
        var successCalled = false
        viewModel.createAccount { successCalled = true }
        advanceUntilIdle()
        
        // Then
        assertTrue(successCalled)
        viewModel.formState.test {
            val state = awaitItem()
            assertEquals("", state.name)
        }
    }

    @Test
    fun `createAccount with blank name sets error`() = runTest(testDispatcher) {
        // Given
        viewModel = createViewModel()
        viewModel.updateFormState(name = "")
        
        // When
        viewModel.createAccount()
        
        // Then
        viewModel.error.test {
            val error = awaitItem()
            assertNotNull(error)
            assertEquals("Account name is required", error)
        }
    }

    @Test
    fun `createAccount with too long name sets error`() = runTest(testDispatcher) {
        // Given
        viewModel = createViewModel()
        viewModel.updateFormState(name = "a".repeat(51))
        
        // When
        viewModel.createAccount()
        
        // Then
        viewModel.error.test {
            val error = awaitItem()
            assertNotNull(error)
            assertTrue(error?.contains("too long") == true)
        }
    }

    // ==================== Update Account Tests ====================

    @Test
    fun `updateAccount with valid data calls repository`() = runTest(testDispatcher) {
        // Given
        val existingAccount = createAccount(
            id = 1,
            name = "Old Name",
            balance = 1000.0
        )
        coEvery { accountRepository.getAccountById(1) } returns existingAccount
        coEvery { accountRepository.updateAccount(any()) } returns Result.success(Unit)
        
        viewModel = createViewModel()
        viewModel.initEditForm(existingAccount)
        viewModel.updateFormState(name = "Updated Name")
        
        // When
        viewModel.updateAccount()
        advanceUntilIdle()
        
        // Then
        coVerify {
            accountRepository.updateAccount(
                match { account ->
                    account.id == 1L &&
                    account.name == "Updated Name" &&
                    account.balance == 1000.0
                }
            )
        }
    }

    @Test
    fun `updateAccount resets form on success`() = runTest(testDispatcher) {
        // Given
        val existingAccount = createAccount(id = 1)
        coEvery { accountRepository.getAccountById(1) } returns existingAccount
        coEvery { accountRepository.updateAccount(any()) } returns Result.success(Unit)
        
        viewModel = createViewModel()
        viewModel.initEditForm(existingAccount)
        
        // When
        var successCalled = false
        viewModel.updateAccount { successCalled = true }
        advanceUntilIdle()
        
        // Then
        assertTrue(successCalled)
        viewModel.formState.test {
            val state = awaitItem()
            assertFalse(state.isEditing)
        }
    }

    // ==================== Delete Account Tests ====================

    @Test
    fun `deleteAccount calls repository`() = runTest(testDispatcher) {
        // Given
        viewModel = createViewModel()
        coEvery { accountRepository.deleteAccount(1) } returns Result.success(Unit)
        
        // When
        viewModel.deleteAccount(1)
        advanceUntilIdle()
        
        // Then
        coVerify { accountRepository.deleteAccount(1) }
    }

    @Test
    fun `deleteAccount calls onSuccess callback`() = runTest(testDispatcher) {
        // Given
        viewModel = createViewModel()
        coEvery { accountRepository.deleteAccount(1) } returns Result.success(Unit)
        
        // When
        var successCalled = false
        viewModel.deleteAccount(1) { successCalled = true }
        advanceUntilIdle()
        
        // Then
        assertTrue(successCalled)
    }

    // ==================== Archive Account Tests ====================

    @Test
    fun `archiveAccount calls repository`() = runTest(testDispatcher) {
        // Given
        viewModel = createViewModel()
        coEvery { accountRepository.archiveAccount(1) } returns Result.success(Unit)
        
        // When
        viewModel.archiveAccount(1)
        advanceUntilIdle()
        
        // Then
        coVerify { accountRepository.archiveAccount(1) }
    }

    // ==================== Unarchive Account Tests ====================

    @Test
    fun `unarchiveAccount calls repository`() = runTest(testDispatcher) {
        // Given
        viewModel = createViewModel()
        coEvery { accountRepository.unarchiveAccount(1) } returns Result.success(Unit)
        
        // When
        viewModel.unarchiveAccount(1)
        advanceUntilIdle()
        
        // Then
        coVerify { accountRepository.unarchiveAccount(1) }
    }

    // ==================== Set Default Account Tests ====================

    @Test
    fun `setDefaultAccount calls repository`() = runTest(testDispatcher) {
        // Given
        viewModel = createViewModel()
        coEvery { accountRepository.setDefaultAccount(1) } returns Result.success(Unit)
        
        // When
        viewModel.setDefaultAccount(1)
        advanceUntilIdle()
        
        // Then
        coVerify { accountRepository.setDefaultAccount(1) }
    }

    // ==================== Transfer Tests ====================

    @Test
    fun `transferBetweenAccounts calls repository`() = runTest(testDispatcher) {
        // Given
        viewModel = createViewModel()
        coEvery { 
            accountRepository.transferBetweenAccounts(any(), any(), any(), any(), any()) 
        } returns Result.success(Unit)
        
        // When
        viewModel.transferBetweenAccounts(
            fromAccountId = 1,
            toAccountId = 2,
            amount = 500.0,
            description = "Test transfer"
        )
        advanceUntilIdle()
        
        // Then
        coVerify {
            accountRepository.transferBetweenAccounts(
                fromAccountId = 1,
                toAccountId = 2,
                amount = 500.0,
                description = "Test transfer",
                any()
            )
        }
    }

    @Test
    fun `transferBetweenAccounts calls onSuccess callback`() = runTest(testDispatcher) {
        // Given
        viewModel = createViewModel()
        coEvery { accountRepository.transferBetweenAccounts(any(), any(), any(), any(), any()) } returns Result.success(Unit)
        
        // When
        var successCalled = false
        viewModel.transferBetweenAccounts(
            fromAccountId = 1,
            toAccountId = 2,
            amount = 100.0,
            description = "Transfer"
        ) { successCalled = true }
        advanceUntilIdle()
        
        // Then
        assertTrue(successCalled)
    }

    // ==================== Clear Error Tests ====================

    @Test
    fun `clearError removes error`() = runTest(testDispatcher) {
        // Given
        viewModel = createViewModel()
        viewModel.updateFormState(name = "") // This will cause validation error
        viewModel.createAccount()
        
        // When
        viewModel.clearError()
        
        // Then
        viewModel.error.test {
            assertNull(awaitItem())
        }
    }

    // ==================== Transactions For Account Tests ====================

    @Test
    fun `transactionsForAccount returns flow from repository`() = runTest(testDispatcher) {
        // Given
        viewModel = createViewModel()
        val transactions = emptyList<com.pyera.app.data.local.entity.TransactionEntity>()
        every { transactionRepository.getTransactionsByAccount(1) } returns flowOf(transactions)
        
        // When
        val result = viewModel.transactionsForAccount(1)
        
        // Then
        result.test {
            assertEquals(transactions, awaitItem())
        }
    }

    private fun createViewModel(): AccountsViewModel {
        return AccountsViewModel(
            accountRepository = accountRepository,
            transactionRepository = transactionRepository
        )
    }
}
