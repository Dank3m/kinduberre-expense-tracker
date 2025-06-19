package com.kinduberre.expensetracker

import android.annotation.SuppressLint
import java.text.SimpleDateFormat
import java.util.Locale

object Utils {
    fun formatDateToHumanReadableForm(dateInMillis: Long): String {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return dateFormat.format(dateInMillis)
    }

    @SuppressLint("DefaultLocale")
    fun formatToDecimalValue(d: Double): String {
        return String.format("%.2f", d);
    }
}