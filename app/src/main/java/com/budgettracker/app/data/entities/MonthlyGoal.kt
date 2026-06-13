package com.budgettracker.app.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "monthly_goals",
    foreignKeys = [ForeignKey(
        entity = User::class,
        parentColumns = ["userId"],
        childColumns = ["userId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class MonthlyGoal(
    @PrimaryKey(autoGenerate = true) val goalId: Int = 0,
    val minGoal: Double,
    val maxGoal: Double,
    val month: String,
    val userId: Int
)