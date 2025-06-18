package com.kinduberre.expensetracker.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kinduberre.expensetracker.data.ExpenseDatabase
import com.kinduberre.expensetracker.data.dao.ExpenseDao
import com.kinduberre.expensetracker.data.model.ExpenseEntity

class AddExpenseViewModel (val dao: ExpenseDao) : ViewModel() {

    suspend fun addExpense(expense: ExpenseEntity) : Boolean {
        return try {
            dao.insertExpense(expense)
            true
        } catch ( ex: Throwable) {
            false
        }

    }
}

class AddExpenseViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddExpenseViewModel::class.java)) {
            val dao = ExpenseDatabase.getDatabase(context).expenseDao()
            @Suppress("UNCHECKED_CAST")
            return AddExpenseViewModel(dao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")

    }

}