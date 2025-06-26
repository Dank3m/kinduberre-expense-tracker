package com.kinduberre.expensetracker.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "transaction")
data class Transaction(

    @PrimaryKey(autoGenerate = true)
    val tranId: String,
    val type: String,
    val status: String,
    val tranAmount: Double,
    val tranCurrency: String,
    val recipient: String?,
    val recipientPhone: String?,
    val recipientAccountNumber: String?,
    val sender: String?,
    val senderPhone: String?,
    val senderAccountNumber: String?,
    val senderType: String?,
    val merchant: String?,
    val source: String?,
    val tranDateTime: Long,
    val balanceAfter: Double,
    val balanceCurrency: String,
    val tranCost: Double?,
    val tranCostCurrency: String?,
    val dailyLimitRemaining: Double,
    val dailyLimitCurrency: String,
    val sourceBalanceAfter: Double?,
    val sourceBalanceCurrency: String?,
    val tranCategory: String?,
)
