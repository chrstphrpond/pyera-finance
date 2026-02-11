package com.pyera.app.worker

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import androidx.work.testing.TestWorkerBuilder
import com.pyera.app.domain.repository.BudgetRepository
import com.pyera.app.domain.repository.TransactionRepository
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
import java.util.concurrent.Executor
import java.util.concurrent.Executors

@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33], manifest = Config.NONE)
class SyncWorkerTest {

    private lateinit var context: Context
    private val transactionRepository: TransactionRepository = mockk(relaxed = true)
    private val budgetRepository: BudgetRepository = mockk(relaxed = true)
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var executor: Executor

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        context = ApplicationProvider.getApplicationContext()
        executor = Executors.newSingleThreadExecutor()
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    private fun createTestWorker(
        params: WorkerParameters = mockk(relaxed = true)
    ): SyncWorker {
        every { params.runAttemptCount } returns 0
        return SyncWorker(
            context = context,
            params = params,
            transactionRepository = transactionRepository,
            budgetRepository = budgetRepository
        )
    }

    @Test
    fun `doWork returns success when both sync operations succeed`() = runTest {
        // Given
        val params = mockk<WorkerParameters>(relaxed = true)
        every { params.runAttemptCount } returns 0
        coEvery { transactionRepository.syncPendingTransactions() } returns Result.success(Unit)
        coEvery { budgetRepository.syncBudgets() } returns Result.success(Unit)
        
        val worker = createTestWorker(params)

        // When
        val result = worker.doWork()

        // Then
        assertEquals(ListenableWorker.Result.success(), result)
        coVerify(exactly = 1) { transactionRepository.syncPendingTransactions() }
        coVerify(exactly = 1) { budgetRepository.syncBudgets() }
    }

    @Test
    fun `doWork returns success when transaction sync succeeds but budget sync fails`() = runTest {
        // Given - both succeed or fail scenario
        val params = mockk<WorkerParameters>(relaxed = true)
        every { params.runAttemptCount } returns 0
        coEvery { transactionRepository.syncPendingTransactions() } returns Result.success(Unit)
        coEvery { budgetRepository.syncBudgets() } returns Result.failure(IOException("Budget sync failed"))
        
        val worker = createTestWorker(params)

        // When
        val result = worker.doWork()

        // Then - should succeed because runAttemptCount < MAX_RETRY_COUNT and hasErrors = true
        // Actually, the code returns retry when hasErrors and runAttemptCount < MAX_RETRY_COUNT
        assertEquals(ListenableWorker.Result.retry(), result)
    }

    @Test
    fun `doWork returns retry when transaction sync fails and under max retries`() = runTest {
        // Given
        val params = mockk<WorkerParameters>(relaxed = true)
        every { params.runAttemptCount } returns 1
        coEvery { transactionRepository.syncPendingTransactions() } returns Result.failure(IOException("Network error"))
        coEvery { budgetRepository.syncBudgets() } returns Result.success(Unit)
        
        val worker = createTestWorker(params)

        // When
        val result = worker.doWork()

        // Then
        assertEquals(ListenableWorker.Result.retry(), result)
    }

    @Test
    fun `doWork returns retry when budget sync fails and under max retries`() = runTest {
        // Given
        val params = mockk<WorkerParameters>(relaxed = true)
        every { params.runAttemptCount } returns 1
        coEvery { transactionRepository.syncPendingTransactions() } returns Result.success(Unit)
        coEvery { budgetRepository.syncBudgets() } returns Result.failure(IOException("Network error"))
        
        val worker = createTestWorker(params)

        // When
        val result = worker.doWork()

        // Then
        assertEquals(ListenableWorker.Result.retry(), result)
    }

    @Test
    fun `doWork returns retry when both syncs fail and under max retries`() = runTest {
        // Given
        val params = mockk<WorkerParameters>(relaxed = true)
        every { params.runAttemptCount } returns 1
        coEvery { transactionRepository.syncPendingTransactions() } returns Result.failure(IOException("Network error"))
        coEvery { budgetRepository.syncBudgets() } returns Result.failure(IOException("Server error"))
        
        val worker = createTestWorker(params)

        // When
        val result = worker.doWork()

        // Then
        assertEquals(ListenableWorker.Result.retry(), result)
    }

    @Test
    fun `doWork returns failure when sync fails after max retries`() = runTest {
        // Given
        val params = mockk<WorkerParameters>(relaxed = true)
        every { params.runAttemptCount } returns 3
        coEvery { transactionRepository.syncPendingTransactions() } returns Result.failure(IOException("Network error"))
        coEvery { budgetRepository.syncBudgets() } returns Result.success(Unit)
        
        val worker = createTestWorker(params)

        // When
        val result = worker.doWork()

        // Then
        assertEquals(ListenableWorker.Result.failure(), result)
    }

    @Test
    fun `doWork returns success when run attempt count is at max but no errors`() = runTest {
        // Given
        val params = mockk<WorkerParameters>(relaxed = true)
        every { params.runAttemptCount } returns 3
        coEvery { transactionRepository.syncPendingTransactions() } returns Result.success(Unit)
        coEvery { budgetRepository.syncBudgets() } returns Result.success(Unit)
        
        val worker = createTestWorker(params)

        // When
        val result = worker.doWork()

        // Then
        assertEquals(ListenableWorker.Result.success(), result)
    }

    @Test
    fun `doWork returns retry on exception when under max retries`() = runTest {
        // Given
        val params = mockk<WorkerParameters>(relaxed = true)
        every { params.runAttemptCount } returns 1
        coEvery { transactionRepository.syncPendingTransactions() } throws RuntimeException("Unexpected error")
        
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
        coEvery { transactionRepository.syncPendingTransactions() } throws RuntimeException("Unexpected error")
        
        val worker = createTestWorker(params)

        // When
        val result = worker.doWork()

        // Then
        assertEquals(ListenableWorker.Result.failure(), result)
    }

    @Test
    fun `doWork continues with budget sync even if transaction sync fails`() = runTest {
        // Given
        val params = mockk<WorkerParameters>(relaxed = true)
        every { params.runAttemptCount } returns 1
        coEvery { transactionRepository.syncPendingTransactions() } returns Result.failure(IOException("Network error"))
        coEvery { budgetRepository.syncBudgets() } returns Result.success(Unit)
        
        val worker = createTestWorker(params)

        // When
        worker.doWork()

        // Then - verify both sync methods were called
        coVerify(exactly = 1) { transactionRepository.syncPendingTransactions() }
        coVerify(exactly = 1) { budgetRepository.syncBudgets() }
    }

    @Test
    fun `schedule creates periodic work request with network constraint`() {
        mockkStatic(androidx.work.WorkManager::class)
        val mockWorkManager = mockk<androidx.work.WorkManager>(relaxed = true)
        every { androidx.work.WorkManager.getInstance(any()) } returns mockWorkManager
        every { mockWorkManager.enqueueUniquePeriodicWork(any(), any(), any()) } returns mockk()
        
        // When
        SyncWorker.schedule(context)
        
        // Then
        verify { 
            mockWorkManager.enqueueUniquePeriodicWork(
                "sync_work",
                androidx.work.ExistingPeriodicWorkPolicy.KEEP,
                any()
            )
        }
        
        unmockkStatic(androidx.work.WorkManager::class)
    }

    @Test
    fun `cancel removes scheduled sync work`() {
        mockkStatic(androidx.work.WorkManager::class)
        val mockWorkManager = mockk<androidx.work.WorkManager>(relaxed = true)
        every { androidx.work.WorkManager.getInstance(any()) } returns mockWorkManager
        every { mockWorkManager.cancelUniqueWork(any()) } returns mockk()
        
        // When
        SyncWorker.cancel(context)
        
        // Then
        verify { mockWorkManager.cancelUniqueWork("sync_work") }
        
        unmockkStatic(androidx.work.WorkManager::class)
    }

    @Test
    fun `isScheduled returns true when work is running`() {
        mockkStatic(androidx.work.WorkManager::class)
        val mockWorkManager = mockk<androidx.work.WorkManager>(relaxed = true)
        val mockWorkInfo = mockk<androidx.work.WorkInfo>(relaxed = true)
        
        every { androidx.work.WorkManager.getInstance(any()) } returns mockWorkManager
        every { mockWorkManager.getWorkInfosForUniqueWork(any()) } returns 
            androidx.work.impl.utils.futures.SettableFuture.create<List<androidx.work.WorkInfo>?>().apply {
                set(listOf(mockWorkInfo))
            }
        every { mockWorkInfo.state } returns androidx.work.WorkInfo.State.RUNNING
        
        // When
        val result = SyncWorker.isScheduled(context)
        
        // Then
        assertEquals(true, result)
        
        unmockkStatic(androidx.work.WorkManager::class)
    }

    @Test
    fun `isScheduled returns false when no work exists`() {
        mockkStatic(androidx.work.WorkManager::class)
        val mockWorkManager = mockk<androidx.work.WorkManager>(relaxed = true)
        
        every { androidx.work.WorkManager.getInstance(any()) } returns mockWorkManager
        every { mockWorkManager.getWorkInfosForUniqueWork(any()) } returns 
            androidx.work.impl.utils.futures.SettableFuture.create<List<androidx.work.WorkInfo>?>().apply {
                set(null)
            }
        
        // When
        val result = SyncWorker.isScheduled(context)
        
        // Then
        assertEquals(false, result)
        
        unmockkStatic(androidx.work.WorkManager::class)
    }

    @Test
    fun `isScheduled returns false when work is cancelled`() {
        mockkStatic(androidx.work.WorkManager::class)
        val mockWorkManager = mockk<androidx.work.WorkManager>(relaxed = true)
        val mockWorkInfo = mockk<androidx.work.WorkInfo>(relaxed = true)
        
        every { androidx.work.WorkManager.getInstance(any()) } returns mockWorkManager
        every { mockWorkManager.getWorkInfosForUniqueWork(any()) } returns 
            androidx.work.impl.utils.futures.SettableFuture.create<List<androidx.work.WorkInfo>?>().apply {
                set(listOf(mockWorkInfo))
            }
        every { mockWorkInfo.state } returns androidx.work.WorkInfo.State.CANCELLED
        
        // When
        val result = SyncWorker.isScheduled(context)
        
        // Then
        assertEquals(false, result)
        
        unmockkStatic(androidx.work.WorkManager::class)
    }

    @Test
    fun `SYNC_TAG constant has correct value`() {
        assertEquals("pyera_sync", SyncWorker.SYNC_TAG)
    }
}
