package com.budgettracker.app.data.dao

import androidx.room.*
import com.budgettracker.app.data.entities.MonthlyGoal
import kotlinx.coroutines.flow.Flow

@Dao
interface GoalDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: MonthlyGoal)

    @Query("SELECT * FROM monthly_goals WHERE userId = :userId AND month = :month LIMIT 1")
    fun getGoalForMonth(userId: Int, month: String): Flow<MonthlyGoal?>
}