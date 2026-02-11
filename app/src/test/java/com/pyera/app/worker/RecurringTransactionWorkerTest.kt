package com.pyera.app.worker

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import com.google.firebase.auth.FirebaseUser
import com.pyera.app.data.local.entity.RecurringFrequency
import com.pyera.app.data.local.entity.RecurringTransactionEntity
import com.pyera.app.data.local.entity.TransactionEntity
import com.pyera.app.data.local.entity.TransactionType
import com.pyera.app.domain.repository.AuthRepository
import com.pyera.app.domain.repository.RecurringTransactionRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.unmockkStatic
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.IOException

@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33], manifest = Config.NONE)
class RecurringTransactionWorkerTest {

    private lateinit var context: Context
    private lateinit var worker: RecurringTransactionWorker
    private val recurringRepository: RecurringTransactionRepository = mockk(relaxed = true)
    private val authRepository: AuthRepository = mockk(relaxed = true)
    private val mockUser: FirebaseUser = mockk(relaxed = true)
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        context = ApplicationProvider.getApplicationContext()
        WorkerTestFixtures.resetCounter()
        
        // Setup default mock behavior
        every { authRepository.currentUser } returns mockUser
        every { mockUser.uid } returns "test_user_123"
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    private fun createTestWorker(
        params: WorkerParameters = mockk(relaxed = true)
    ): RecurringTransactionWorker {
        every { params.runAttemptCount } returns 0
        return RecurringTransactionWorker(
            context = context,
            params = params,
            recurringRepository = recurringRepository,
            authRepository = authRepository
        )
    }

    @Test
    fun `doWork processes due transactions and returns success when all succeed`() = runTest {
        // Given
        val dueTransactions = listOf(
            WorkerTestFixtures.recurringTransaction(
                id = 1L,
                amount = 100.0,
                accountId = 1L,
                description = "Monthly Rent"
            )
        )
        
        val params = mockk<WorkerParameters>(relaxed = true)
        every { params.runAttemptCount } returns 0
        coEvery { recurringRepository.getDueRecurring(any()) } returns dueTransactions
        coEvery { recurringRepository.processDueRecurring(any(), any()) } returns Unit
        
        val worker = createTestWorker(params)

        // When
        val result = worker.doWork()

        // Then
        assertEquals(ListenableWorker.Result.success(), result)
        coVerify(exactly = 1) { recurringRepository.getDueRecurring(any()) }
        coVerify(exactly = 1) { recurringRepository.processDueRecurring(any(), any()) }
    }

    @Test
    fun `doWork processes multiple due transactions`() = runTest {
        // Given
        val dueTransactions = WorkerTestFixtures.dueRecurringTransactions(3)
        
        val params = mockk<WorkerParameters>(relaxed = true)
        every { params.runAttemptCount } returns 0
        coEvery { recurringRepository.getDueRecurring(any()) } returns dueTransactions
        coEvery { recurringRepository.processDueRecurring(any(), any()) } returns Unit
        
        val worker = createTestWorker(params)

        // When
        val result = worker.doWork()

        // Then
        assertEquals(ListenableWorker.Result.success(), result)
        coVerify(exactly = 1) { recurringRepository.getDueRecurring(any()) }
        coVerify(exactly = 3) { recurringRepository.processDueRecurring(any(), any()) }
    }

    @Test
    fun `doWork returns success when no due transactions`() = runTest {
        // Given
        val params = mockk<WorkerParameters>(relaxed = true)
        every { params.runAttemptCount } returns 0
        coEvery { recurringRepository.getDueRecurring(any()) } returns emptyList()
        
        val worker = createTestWorker(params)

        // When
        val result = worker.doWork()

        // Then
        assertEquals(ListenableWorker.Result.success(), result)
        coVerify(exactly = 1) { recurringRepository.getDueRecurring(any()) }
        coVerify(exactly = 0) { recurringRepository.processDueRecurring(any(), any()) }
    }

    @Test
    fun `doWork returns retry on IO exception when under max retries`() = runTest {
        // Given
        val params = mockk<WorkerParameters>(relaxed = true)
        every { params.runAttemptCount } returns 1
        coEvery { recurringRepository.getDueRecurring(any()) } throws IOException("Network error")
        
        val worker = createTestWorker(params)

        // When
        val result = worker.doWork()

        // Then
        assertEquals(ListenableWorker.Result.retry(), result)
    }

    @Test
    fun `doWork returns failure on exception after max retries`() = runTest {
        // Given
        val params = mockk<WorkerParameters>(relaxed = true)
        every { params.runAttemptCount } returns 3
        coEvery { recurringRepository.getDueRecurring(any()) } throws IOException("Network error")
        
        val worker = createTestWorker(params)

        // When
        val result = worker.doWork()

        // Then
        assertEquals(ListenableWorker.Result.failure(), result)
    }

    @Test
    fun `doWork continues processing when individual transaction fails`() = runTest {
        // Given
        val dueTransactions = WorkerTestFixtures.dueRecurringTransactions(3)
        
        val params = mockk<WorkerParameters>(relaxed = true)
        every { params.runAttemptCount } returns 0
        coEvery { recurringRepository.getDueRecurring(any()) } returns dueTransactions
        
        // First transaction fails, others succeed
        coEvery { recurringRepository.processDueRecurring(dueTransactions[0], any()) } throws RuntimeException("Error")
        coEvery { recurringRepository.processDueRecurring(dueTransactions[1], any()) } returns Unit
        coEvery { recurringRepository.processDueRecurring(dueTransactions[2], any()) } returns Unit
        
        val worker = createTestWorker(params)

        // When
        val result = worker.doWork()

        // Then - should return success because not all failed (partial success is still success)
        assertEquals(ListenableWorker.Result.success(), result)
        coVerify(exactly = 3) { recurringRepository.processDueRecurring(any(), any()) }
    }

    @Test
    fun `doWork returns retry when all transactions fail and under max retries`() = runTest {
        // Given
        val dueTransactions = WorkerTestFixtures.dueRecurringTransactions(2)
        
        val params = mockk<WorkerParameters>(relaxed = true)
        every { params.runAttemptCount } returns 1
        coEvery { recurringRepository.getDueRecurring(any()) } returns dueTransactions
        coEvery { recurringRepository.processDueRecurring(any(), any()) } throws RuntimeException("Error")
        
        val worker = createTestWorker(params)

        // When
        val result = worker.doWork()

        // Then - should return retry because errorCount > 0 and runAttemptCount < MAX_RETRY_COUNT
        assertEquals(ListenableWorker.Result.retry(), result)
    }

    @Test
    fun `doWork skips transaction when user is not authenticated`() = runTest {
        // Given
        val dueTransactions = listOf(
            WorkerTestFixtures.recurringTransaction(id = 1L, accountId = 1L)
        )
        
        val params = mockk<WorkerParameters>(relaxed = true)
        every { params.runAttemptCount } returns 0
        every { authRepository.currentUser } returns null
        coEvery { recurringRepository.getDueRecurring(any()) } returns dueTransactions
        
        val worker = createTestWorker(params)

        // When
        val result = worker.doWork()

        // Then - should still succeed, just skip processing
        assertEquals(ListenableWorker.Result.success(), result)
        // processDueRecurring should not be called when user is null
        coVerify(exactly = 0) { recurringRepository.processDueRecurring(any(), any()) }
    }

    @Test
    fun `doWork skips transaction when accountId is null`() = runTest {
        // Given
        val dueTransactions = listOf(
            WorkerTestFixtures.recurringTransaction(id = 1L, accountId = null)
        )
        
        val params = mockk<WorkerParameters>(relaxed = true)
        every { params.runAttemptCount } returns 0
        coEvery { recurringRepository.getDueRecurring(any()) } returns dueTransactions
        
        val worker = createTestWorker(params)

        // When
        val result = worker.doWork()

        // Then - should still succeed, just skip processing
        assertEquals(ListenableWorker.Result.success(), result)
        coVerify(exactly = 0) { recurringRepository.processDueRecurring(any(), any()) }
    }

    @Test
    fun `schedule creates periodic work request with correct constraints`() {
        // This test verifies the static schedule method configuration
        // We can't easily test the actual WorkManager enqueue without Robolectric shadows,
        // but we can verify the method doesn't throw and uses correct constants
        
        mockkStatic(androidx.work.WorkManager::class)
        val mockWorkManager = mockk<androidx.work.WorkManager>(relaxed = true)
        every { androidx.work.WorkManager.getInstance(any()) } returns mockWorkManager
        every { mockWorkManager.enqueueUniquePeriodicWork(any(), any(), any()) } returns mockk()
        
        // When
        RecurringTransactionWorker.schedule(context)
        
        // Then
        verify { 
            mockWorkManager.enqueueUniquePeriodicWork(
                "recurring_transaction_work",
                androidx.work.ExistingPeriodicWorkPolicy.KEEP,
                any()
            )
        }
        
        unmockkStatic(androidx.work.WorkManager::class)
    }

    @Test
    fun `cancel removes scheduled work`() {
        mockkStatic(androidx.work.WorkManager::class)
        val mockWorkManager = mockk<androidx.work.WorkManager>(relaxed = true)
        every { androidx.work.WorkManager.getInstance(any()) } returns mockWorkManager
        every { mockWorkManager.cancelUniqueWork(any()) } returns mockk()
        
        // When
        RecurringTransactionWorker.cancel(context)
        
        // Then
        verify { mockWorkManager.cancelUniqueWork("recurring_transaction_work") }
        
        unmockkStatic(androidx.work.WorkManager::class)
    }

    @Test
    fun `isScheduled returns true when work is enqueued`() {
        mockkStatic(androidx.work.WorkManager::class)
        val mockWorkManager = mockk<androidx.work.WorkManager>(relaxed = true)
        val mockWorkInfo = mockk<androidx.work.WorkInfo>(relaxed = true)
        
        every { androidx.work.WorkManager.getInstance(any()) } returns mockWorkManager
        every { mockWorkManager.getWorkInfosForUniqueWork(any()) } returns 
            androidx.work.impl.utils.futures.SettableFuture.create<List<androidx.work.WorkInfo>?>().apply {
                set(listOf(mockWorkInfo))
            }
        every { mockWorkInfo.state } returns androidx.work.WorkInfo.State.ENQUEUED
        
        // When
        val result = RecurringTransactionWorker.isScheduled(context)
        
        // Then
        assertEquals(true, result)
        
        unmockkStatic(androidx.work.WorkManager::class)
    }

    @Test
    fun `WORK_TAG constant has correct value`() {
        assertEquals("pyera_recurring", RecurringTransactionWorker.WORK_TAG)
    }
}
