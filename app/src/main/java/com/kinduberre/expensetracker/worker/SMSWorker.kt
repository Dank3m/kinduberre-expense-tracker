package com.kinduberre.expensetracker.worker

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.Telephony
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.kinduberre.expensetracker.R

class SMSWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {
    override fun doWork(): Result {
        return try {
            checkSmsMessages()
            Result.success()
        } catch (e: Exception) {
            Log.e("SMSWorker", "Error checking SMS messages: ${e.message}")
            Result.failure()
        }
    }

    private fun checkSmsMessages() {
        // Check if we have SMS permission
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_SMS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.w("SMSWorker", "SMS permission not granted")
            return
        }

        // Get messages from last hour
        val oneHourAgo = System.currentTimeMillis() - (60 * 60 * 1000)
        val projection = arrayOf(
            Telephony.Sms._ID,
            Telephony.Sms.ADDRESS,
            Telephony.Sms.BODY,
            Telephony.Sms.DATE,
            Telephony.Sms.SUBSCRIPTION_ID,
            Telephony.Sms.TYPE,
            Telephony.Sms.READ
        )

        val cursor = context.contentResolver.query(
            Uri.parse("content://sms/inbox"),
            projection,
            "${Telephony.Sms.DATE} >= ?",
            arrayOf(oneHourAgo.toString()),
            null
        )
        cursor?.use {
            processSMSMessages(it)
        }
    }

    private fun processSMSMessages(cursor: Cursor) {
        val addressIndex = cursor.getColumnIndex(Telephony.Sms.ADDRESS)
        val bodyIndex = cursor.getColumnIndex(Telephony.Sms.BODY)
        val typeIndex = cursor.getColumnIndex(Telephony.Sms.TYPE)
        val readIndex = cursor.getColumnIndex(Telephony.Sms.READ)
        val dateIndex = cursor.getColumnIndex(Telephony.Sms.DATE)
        val subIdIndex = cursor.getColumnIndex(Telephony.Sms.SUBSCRIPTION_ID)

        val messagesBySim = mutableMapOf<Int, MutableList<SmsData>>()

        while (cursor.moveToNext()) {
            val address = cursor.getString(addressIndex)
            val body = cursor.getString(bodyIndex)
            val type = cursor.getInt(typeIndex)
            val isRead = cursor.getInt(readIndex)
            val date = cursor.getLong(dateIndex)
            val subId = cursor.getInt(subIdIndex)

            //Group messages by SIM
            if (!messagesBySim.containsKey(subId)) {
                messagesBySim[subId] = mutableListOf()
            }
            messagesBySim[subId]?.add(SmsData(address, body, date, type, subId, isRead == 1))

        }

        // Process messages by SIM
        messagesBySim.forEach { (simId, messages) ->

            processMessagesForSim(simId, messages)
        }
    }

    private fun processMessagesForSim(simId: Int, messages: List<SmsData>) {
        Log.d("SmsWorker", "Processing ${messages.size} messages for SIM $simId")

        // Filter by text senders (non-numeric)
        val textSenderMessages = messages.filter {
            !it.address.matches(Regex("^[+]?[0-9\\s\\-()]+$"))
        }

        // Filter unread messages
        val unreadMessages = messages.filter { !it.isRead }

        // Process banking/service messages
        val serviceMessages = messages.filter { message ->
            val address = message.address.uppercase()
            address.contains("BANK") || address.contains("MPESA")
        }

        // Log results
        Log.d("SmsWorker", "SIM $simId - Text senders: ${textSenderMessages.size}")
        Log.d("SmsWorker", "SIM $simId - Unread: ${unreadMessages.size}")
        Log.d("SmsWorker", "SIM $simId - Service messages: ${serviceMessages.size}")

        // You can save to database, send notifications, etc.
        saveToDatabase(simId, serviceMessages)

        // Send notification for important unread messages
        if (unreadMessages.isNotEmpty()) {
            sendNotification(simId, unreadMessages.size)
        }
    }


    private fun sendNotification(simId: Int, unreadCount: Int) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        // Create notification channel (Android 8+)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "SMS_CHANNEL",
                "SMS Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, "SMS_CHANNEL")
            .setContentTitle("New SMS Messages")
            .setContentText("$unreadCount unread messages on SIM $simId")
            .setSmallIcon(R.drawable.ic_notification)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(simId, notification)
    }


    private fun saveToDatabase(simId: Int, messages: List<SmsData>) {
        // Implement your database saving logic here
        // Example: Room database, SQLite, etc.
    }

    data class SmsData(
        val address: String,
        val body: String,
        val date: Long,
        val type: Int,
        val subscriptionId: Int,
        val isRead: Boolean
    )
}