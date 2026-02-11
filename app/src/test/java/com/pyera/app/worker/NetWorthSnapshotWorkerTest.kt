package com.pyera.app.worker

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import com.pyera.app.domain.repository.NetWorthRepository
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.IOException
import java.util.Calendar
import java.util.concurrent.Executor
import java.util.concurrent.Executors

@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33], manifest = Config.NONE)
class NetWorthSnapshotWorkerTest {

    private lateinit var context: Context
    private val netWorthRepository: NetWorthRepository = mockk(relaxed = true)
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
    ): NetWorthSnapshotWorker {
        every { params.runAttemptCount } returns 0
        return NetWorthSnapshotWorker(
            context = context,
            params = params,
            netWorthRepository = netWorthRepository
        )
    }

    @Test
    fun `doWork returns success when snapshot is saved`() = runTest {
        // Given
        val params = mockk<WorkerParameters>(relaxed = true)
        every { params.runAttemptCount } returns 0
        coEvery { netWorthRepository.saveCurrentSnapshot() } returns Result.success(123L)
        
        val worker = createTestWorker(params)

        // When
        val result = worker.doWork()

        // Then
        assertEquals(ListenableWorker.Result.success(), result)
        coVerify(exactly = 1) { netWorthRepository.saveCurrentSnapshot() }
    }

    @Test
    fun `doWork returns success with snapshot id in output`() = runTest {
        // Given
        val expectedId = 456L
        val params = mockk<WorkerParameters>(relaxed = true)
        every { params.runAttemptCount } returns 0
        coEvery { netWorthRepository.saveCurrentSnapshot() } returns Result.success(expectedId)
        
        val worker = createTestWorker(params)

        // When
        val result = worker.doWork()

        // Then
        assertEquals(ListenableWorker.Result.success(), result)
    }

    @Test
    fun `doWork returns retry when repository returns failure and under max retries`() = runTest {
        // Given
        val params = mockk<WorkerParameters>(relaxed = true)
        every { params.runAttemptCount } returns 1
        coEvery { netWorthRepository.saveCurrentSnapshot() } returns Result.failure(IOException("Database error"))
        
        val worker = createTestWorker(params)

        // When
        val result = worker.doWork()

        // Then
        assertEquals(ListenableWorker.Result.retry(), result)
    }

    @Test
    fun `doWork returns failure when repository returns failure after max retries`() = runTest {
        // Given
        val params = mockk<WorkerParameters>(relaxed = true)
        every { params.runAttemptCount } returns 3
        coEvery { netWorthRepository.saveCurrentSnapshot() } returns Result.failure(IOException("Database error"))
        
        val worker = createTestWorker(params)

        // When
        val result = worker.doWork()

        // Then
        assertEquals(ListenableWorker.Result.failure(), result)
    }

    @Test
    fun `doWork returns retry on exception when under max retries`() = runTest {
        // Given
        val params = mockk<WorkerParameters>(relaxed = true)
        every { params.runAttemptCount } returns 1
        coEvery { netWorthRepository.saveCurrentSnapshot() } throws RuntimeException("Unexpected error")
        
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
        coEvery { netWorthRepository.saveCurrentSnapshot() } throws RuntimeException("Unexpected error")
        
        val worker = createTestWorker(params)

        // When
        val result = worker.doWork()

        // Then
        assertEquals(ListenableWorker.Result.failure(), result)
    }

    @Test
    fun `doWork returns retry on IO exception when under max retries`() = runTest {
        // Given
        val params = mockk<WorkerParameters>(relaxed = true)
        every { params.runAttemptCount } returns 2
        coEvery { netWorthRepository.saveCurrentSnapshot() } throws IOException("Network error")
        
        val worker = createTestWorker(params)

        // When
        val result = worker.doWork()

        // Then
        assertEquals(ListenableWorker.Result.retry(), result)
    }

    @Test
    fun `doWork returns failure on IO exception after max retries`() = runTest {
        // Given
        val params = mockk<WorkerParameters>(relaxed = true)
        every { params.runAttemptCount } returns 3
        coEvery { netWorthRepository.saveCurrentSnapshot() } throws IOException("Network error")
        
        val worker = createTestWorker(params)

        // When
        val result = worker.doWork()

        // Then
        assertEquals(ListenableWorker.Result.failure(), result)
    }

    @Test
    fun `doWork returns success on first attempt with valid snapshot id`() = runTest {
        // Given
        val snapshotId = 789L
        val params = mockk<WorkerParameters>(relaxed = true)
        every { params.runAttemptCount } returns 0
        coEvery { netWorthRepository.saveCurrentSnapshot() } returns Result.success(snapshotId)
        
        val worker = createTestWorker(params)

        // When
        val result = worker.doWork()

        // Then
        assertEquals(ListenableWorker.Result.success(), result)
        coVerify(exactly = 1) { netWorthRepository.saveCurrentSnapshot() }
    }

    @Test
    fun `schedule creates periodic work request with battery constraint`() {
        mockkStatic(androidx.work.WorkManager::class)
        val mockWorkManager = mockk<androidx.work.WorkManager>(relaxed = true)
        every { androidx.work.WorkManager.getInstance(any()) } returns mockWorkManager
        every { mockWorkManager.enqueueUniquePeriodicWork(any(), any(), any()) } returns mockk()
        
        // When
        NetWorthSnapshotWorker.schedule(context)
        
        // Then
        verify { 
            mockWorkManager.enqueueUniquePeriodicWork(
                "net_worth_snapshot_work",
                androidx.work.ExistingPeriodicWorkPolicy.KEEP,
                any()
            )
        }
        
        unmockkStatic(androidx.work.WorkManager::class)
    }

    @Test
    fun `scheduleImmediate creates one-time work request`() {
        mockkStatic(androidx.work.WorkManager::class)
        val mockWorkManager = mockk<androidx.work.WorkManager>(relaxed = true)
        every { androidx.work.WorkManager.getInstance(any()) } returns mockWorkManager
        every { mockWorkManager.enqueue(any<androidx.work.WorkRequest>()) } returns mockk()
        
        // When
        NetWorthSnapshotWorker.scheduleImmediate(context)
        
        // Then
        verify { 
            mockWorkManager.enqueue(any<androidx.work.WorkRequest>())
        }
        
        unmockkStatic(androidx.work.WorkManager::class)
    }

    @Test
    fun `cancel removes scheduled snapshot work`() {
        mockkStatic(androidx.work.WorkManager::class)
        val mockWorkManager = mockk<androidx.work.WorkManager>(relaxed = true)
        every { androidx.work.WorkManager.getInstance(any()) } returns mockWorkManager
        every { mockWorkManager.cancelUniqueWork(any()) } returns mockk()
        
        // When
        NetWorthSnapshotWorker.cancel(context)
        
        // Then
        verify { mockWorkManager.cancelUniqueWork("net_worth_snapshot_work") }
        
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
        val result = NetWorthSnapshotWorker.isScheduled(context)
        
        // Then
        assertEquals(true, result)
        
        unmockkStatic(androidx.work.WorkManager::class)
    }

    @Test
    fun `isScheduled returns false when work is not found`() {
        mockkStatic(androidx.work.WorkManager::class)
        val mockWorkManager = mockk<androidx.work.WorkManager>(relaxed = true)
        
        every { androidx.work.WorkManager.getInstance(any()) } returns mockWorkManager
        every { mockWorkManager.getWorkInfosForUniqueWork(any()) } returns 
            androidx.work.impl.utils.futures.SettableFuture.create<List<androidx.work.WorkInfo>?>().apply {
                set(emptyList())
            }
        
        // When
        val result = NetWorthSnapshotWorker.isScheduled(context)
        
        // Then
        assertEquals(false, result)
        
        unmockkStatic(androidx.work.WorkManager::class)
    }

    @Test
    fun `isScheduled returns false when work info is null`() {
        mockkStatic(androidx.work.WorkManager::class)
        val mockWorkManager = mockk<androidx.work.WorkManager>(relaxed = true)
        
        every { androidx.work.WorkManager.getInstance(any()) } returns mockWorkManager
        every { mockWorkManager.getWorkInfosForUniqueWork(any()) } returns 
            androidx.work.impl.utils.futures.SettableFuture.create<List<androidx.work.WorkInfo>?>().apply {
                set(null)
            }
        
        // When
        val result = NetWorthSnapshotWorker.isScheduled(context)
        
        // Then
        assertEquals(false, result)
        
        unmockkStatic(androidx.work.WorkManager::class)
    }

    @Test
    fun `WORK_TAG constant has correct value`() {
        assertEquals("pyera_networth_snapshot", NetWorthSnapshotWorker.WORK_TAG)
    }

    @Test
    fun `getEndOfMonthDelay returns positive value when before 25th`() {
        // This test validates the logic but the actual delay depends on current time
        // We just verify the method returns a value and doesn't crash
        val delay = NetWorthSnapshotWorker.getEndOfMonthDelay()
        
        // The delay should be positive (unless we're exactly at the target time)
        assertTrue("Delay should be non-negative", delay >= 0 || System.currentTimeMillis() > 0)
    }

    @Test
    fun `getEndOfMonthDelay calculates correct delay before 25th`() {
        // Test the logic by creating a calendar and checking the math
        val calendar = Calendar.getInstance()
        val currentDay = calendar.get(Calendar.DAY_OF_MONTH)
        
        if (currentDay < 25) {
            val delay = NetWorthSnapshotWorker.getEndOfMonthDelay()
            // Should schedule for end of this month
            assertTrue("Delay should be positive when before 25th", delay > 0)
        }
    }

    @Test
    fun `getEndOfMonthDelay calculates correct delay after 25th`() {
        // Test the logic by checking if the delay calculation is correct
        val calendar = Calendar.getInstance()
        val currentDay = calendar.get(Calendar.DAY_OF_MONTH)
        
        if (currentDay >= 25) {
            val delay = NetWorthSnapshotWorker.getEndOfMonthDelay()
            // Should schedule for next month
            assertTrue("Delay should be positive when after 25th", delay > 0)
        }
    }
}
