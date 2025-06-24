package com.kinduberre.expensetracker.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

class SmsScheduler {

    fun scheduleHourlyCheck(context: Context) {
        val workRequest = PeriodicWorkRequestBuilder<SMSWorker>(1, TimeUnit.HOURS)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                    .setRequiresBatteryNotLow(false)
                    .build()
            )
            .addTag("SMS_HOURLY_CHECK")
            .build()

        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(
                "SMS_HOURLY_CHECK",
                ExistingPeriodicWorkPolicy.REPLACE,
                workRequest
            )
    }

    fun cancelScheduledCheck(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork("SMS_HOURLY_CHECK")
    }
}