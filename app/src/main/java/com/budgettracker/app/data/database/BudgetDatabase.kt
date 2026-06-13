package com.budgettracker.app.data.database

import android.content.Context
import androidx.room.*
import com.budgettracker.app.data.dao.*
import com.budgettracker.app.data.entities.*

// Main Room database for the app.
// Holds 4 tables: users, categories, expenses, and monthly_goals.
// Uses a singleton pattern (getDatabase) to ensure only one instance
// exists throughout the app's lifecycle.
@Database(
    entities = [User::class, Category::class, Expense::class, MonthlyGoal::class],
    version = 1,
    exportSchema = false
)
abstract class BudgetDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun categoryDao(): CategoryDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun goalDao(): GoalDao

    companion object {
        @Volatile
        private var INSTANCE: BudgetDatabase? = null

        fun getDatabase(context: Context): BudgetDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BudgetDatabase::class.java,
                    "budget_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}