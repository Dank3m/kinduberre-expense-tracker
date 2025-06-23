package com.kinduberre.expensetracker.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kinduberre.expensetracker.R
import com.kinduberre.expensetracker.Utils
import com.kinduberre.expensetracker.data.ExpenseDatabase
import com.kinduberre.expensetracker.data.dao.ExpenseDao
import com.kinduberre.expensetracker.data.model.ExpenseEntity

class HomeViewModel(dao: ExpenseDao) : ViewModel() {

    val expenses = dao.getAllExpenses()

    fun getBalance(list: List<ExpenseEntity>) : String {
        var total = 0.0
        list.forEach {
            if (it.type == "Income") {
                total += it.amount
            } else {
                total -= it.amount
            }
        }
        return "$ ${Utils.formatToDecimalValue(total)}"
    }

    fun getTotalExpense(list: List<ExpenseEntity>) : String {
        var total = 0.0
        list.forEach {
            if (it.type == "Expense") {
                total -= it.amount
            }
        }
        return "$ ${Utils.formatToDecimalValue(total)}"
    }

    fun getTotalIncome(list: List<ExpenseEntity>) : String {
        var total = 0.0
        list.forEach {
            if (it.type == "Income") {
                total += it.amount
            }
        }
        return "$ ${Utils.formatToDecimalValue(total)}"
    }
}

class HomeViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            val dao = ExpenseDatabase.getDatabase(context).expenseDao()
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(dao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")

        }

}