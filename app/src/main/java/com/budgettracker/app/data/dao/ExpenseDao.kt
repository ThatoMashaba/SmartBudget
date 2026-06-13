package com.budgettracker.app.data.dao

import androidx.room.*
import com.budgettracker.app.data.entities.Expense
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {

    @Insert
    suspend fun insertExpense(expense: Expense)

    @Update
    suspend fun updateExpense(expense: Expense)

    @Delete
    suspend fun deleteExpense(expense: Expense)

    @Query("SELECT * FROM expenses WHERE expenseId = :id LIMIT 1")
    suspend fun getExpenseById(id: Int): Expense?

    @Query("""
        SELECT * FROM expenses 
        WHERE userId = :userId 
        AND date BETWEEN :startDate AND :endDate 
        ORDER BY date DESC
    """)
    fun getExpensesByPeriod(
        userId: Int,
        startDate: String,
        endDate: String
    ): Flow<List<Expense>>

    @Query("""
        SELECT e.categoryId, c.name as categoryName, SUM(e.amount) as total 
        FROM expenses e 
        INNER JOIN categories c ON e.categoryId = c.categoryId
        WHERE e.userId = :userId 
        AND e.date BETWEEN :startDate AND :endDate
        GROUP BY e.categoryId
    """)
    fun getCategoryTotals(
        userId: Int,
        startDate: String,
        endDate: String
    ): Flow<List<CategoryTotal>>

    @Query("SELECT COUNT(*) FROM expenses WHERE userId = :userId")
    suspend fun getExpenseCount(userId: Int): Int
}

data class CategoryTotal(
    val categoryId: Int,
    val categoryName: String,
    val total: Double
)