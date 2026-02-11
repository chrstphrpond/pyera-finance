package com.pyera.app

import android.app.Application
import com.pyera.app.worker.NetWorthSnapshotWorker
import com.pyera.app.worker.RecurringTransactionWorker
import com.pyera.app.worker.SyncWorker
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class PyeraApp : Application() {

    override fun onCreate() {
        super.onCreate()
        
        // Schedule recurring transaction worker
        // This will run daily to check for and process due recurring transactions
        RecurringTransactionWorker.schedule(this)

        // Schedule background sync and monthly net worth snapshots
        SyncWorker.schedule(this)
        NetWorthSnapshotWorker.schedule(this)
    }
}
