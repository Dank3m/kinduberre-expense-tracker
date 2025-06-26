package com.kinduberre.expensetracker

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.PowerManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.kinduberre.expensetracker.ui.theme.ExpenseTrackerTheme
import com.kinduberre.expensetracker.worker.SmsScheduler

class MainActivity : ComponentActivity() {

    private lateinit var smsScheduler: SmsScheduler

    private val batteryOptimizationLauncher = registerForActivityResult(
        BatteryOptimizationContract()
    ) { _ ->
        // Check the actual result
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        val isOptimized =
            powerManager.isIgnoringBatteryOptimizations(packageName)
        if (isOptimized) {
            Toast.makeText(this, "Battery optimization disabled!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Please disable battery optimization manually", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ExpenseTrackerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NavHostScreen()


                }
            }
        }
        smsScheduler = SmsScheduler()
        requestSMSPermissions()
    }

    private fun requestSMSPermissions() {
        val permissions = arrayOf(
            Manifest.permission.READ_SMS,
            Manifest.permission.READ_PHONE_STATE
        )

        if(!hasAllPermissions(permissions)){
            ActivityCompat.requestPermissions(this, permissions, 100)
        } else {
            startSMSScheduler()
        }
    }

    private fun hasAllPermissions(permissions: Array<String>): Boolean {
        return permissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String?>,
        grantResults: IntArray,
        deviceId: Int
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults, deviceId)

        if(requestCode == 100 && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
            startSMSScheduler()
        }
    }

    private fun startSMSScheduler() {
        smsScheduler.scheduleHourlyCheck(this)
        handleBatteryOptimizationWithDialog()
        Toast.makeText(this, "SMS monitoring started", Toast.LENGTH_SHORT).show()
    }


    private fun requestBatteryOptimization() {
        batteryOptimizationLauncher.launch(packageName)
    }

    private fun handleBatteryOptimizationWithDialog() {
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        val isOptimized =
            powerManager.isIgnoringBatteryOptimizations(packageName)
        if (isOptimized) {
            AlertDialog.Builder(this)
                .setTitle("Battery Optimization")
                .setMessage("To ensure SMS monitoring works reliably in the background, please disable battery optimization for this app.")
                .setPositiveButton("Settings") { _, _ ->
                    requestBatteryOptimization()
                }
                .setNegativeButton("Skip") { dialog, _ ->
                    dialog.dismiss()
                    showBatteryOptimizationWarning()
                }
                .setCancelable(false)
                .show()
        }
    }

    private fun showBatteryOptimizationWarning() {
        Toast.makeText(
            this,
            "Warning: SMS monitoring may be limited by battery optimization",
            Toast.LENGTH_LONG
        ).show()
    }
}

