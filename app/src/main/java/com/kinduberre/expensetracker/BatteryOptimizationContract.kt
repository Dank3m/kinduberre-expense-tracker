package com.kinduberre.expensetracker

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContract
import androidx.core.net.toUri

class BatteryOptimizationContract : ActivityResultContract<String, Boolean>() {
    override fun createIntent(
        context: Context,
        input: String
    ): Intent {
        return Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
            data = "package:$input".toUri()
        }
    }

    override fun parseResult(
        resultCode: Int,
        intent: Intent?
    ): Boolean {
        // The result code isn't reliable for this intent, so we return true
        // The actual check should be done in the callback
        return true
    }
}