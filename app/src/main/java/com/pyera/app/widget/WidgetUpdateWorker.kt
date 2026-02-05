package com.pyera.app.widget

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import java.util.concurrent.TimeUnit

/**
 * Worker that periodically updates all widgets
 */
class WidgetUpdateWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        private const val WORK_NAME = "widget_update_work"
        
        /**
         * Schedule periodic widget updates
         */
        fun schedule(context: Context, intervalMinutes: Int = 30) {
            val constraints = Constraints.Builder()
                .setRequiresBatteryNotLow(true)
                .build()
            
            val workRequest = PeriodicWorkRequestBuilder<WidgetUpdateWorker>(
                intervalMinutes.toLong(),
                TimeUnit.MINUTES
            )
                .setConstraints(constraints)
                .build()
            
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
        }
        
        /**
         * Cancel scheduled widget updates
         */
        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
        
        /**
         * Force immediate widget update
         */
        suspend fun updateNow(context: Context) {
            val manager = GlanceAppWidgetManager(context)
            
            // Update Balance Widgets
            manager.getGlanceIds(BalanceWidget::class.java).forEach { glanceId ->
                BalanceWidget().update(context, glanceId)
            }
            
            // Update Quick Add Widgets
            manager.getGlanceIds(QuickAddWidget::class.java).forEach { glanceId ->
                QuickAddWidget().update(context, glanceId)
            }
            
            // Update Transactions Widgets
            manager.getGlanceIds(TransactionsWidget::class.java).forEach { glanceId ->
                TransactionsWidget().update(context, glanceId)
            }
        }
    }

    override suspend fun doWork(): Result {
        return try {
            updateNow(applicationContext)
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
