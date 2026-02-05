package com.pyera.app

import android.app.Application
import com.pyera.app.worker.RecurringTransactionWorker
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class PyeraApp : Application() {

    override fun onCreate() {
        super.onCreate()
        
        // Schedule recurring transaction worker
        // This will run daily to check for and process due recurring transactions
        RecurringTransactionWorker.schedule(this)
    }
}
