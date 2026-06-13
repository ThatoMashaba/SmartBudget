package com.budgettracker.app.utils

data class Badge(
    val emoji: String,
    val title: String,
    val description: String,
    val isEarned: Boolean
)

object BadgeHelper {

    // Calculate all badges based on user's data
    fun calculateBadges(
        totalExpenses: Int,
        totalCategories: Int,
        withinGoalThisMonth: Boolean,
        spentBelowMax: Boolean
    ): List<Badge> {
        return listOf(
            Badge(
                emoji = "🏆",
                title = "Goal Achiever",
                description = "Stay within your min and max goals this month",
                isEarned = withinGoalThisMonth
            ),
            Badge(
                emoji = "💰",
                title = "Budget Master",
                description = "Spend below your maximum goal this month",
                isEarned = spentBelowMax
            ),
            Badge(
                emoji = "📝",
                title = "Getting Started",
                description = "Log your first expense",
                isEarned = totalExpenses >= 1
            ),
            Badge(
                emoji = "📊",
                title = "Consistent Tracker",
                description = "Log at least 10 expenses",
                isEarned = totalExpenses >= 10
            ),
            Badge(
                emoji = "🔥",
                title = "Power User",
                description = "Log at least 25 expenses",
                isEarned = totalExpenses >= 25
            ),
            Badge(
                emoji = "🗂️",
                title = "Organizer",
                description = "Create at least 3 categories",
                isEarned = totalCategories >= 3
            )
        )
    }
}