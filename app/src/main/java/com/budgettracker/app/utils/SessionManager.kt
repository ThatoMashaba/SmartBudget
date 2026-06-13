package com.budgettracker.app.utils

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("BudgetTrackerPrefs", Context.MODE_PRIVATE)

    fun saveUserId(userId: Int) = prefs.edit().putInt("USER_ID", userId).apply()
    fun getUserId(): Int = prefs.getInt("USER_ID", -1)
    fun saveUsername(name: String) = prefs.edit().putString("USERNAME", name).apply()
    fun getUsername(): String? = prefs.getString("USERNAME", null)
    fun clearSession() = prefs.edit().clear().apply()
    fun isLoggedIn(): Boolean = getUserId() != -1

    // --- Dark Mode ---
    fun setDarkMode(enabled: Boolean) = prefs.edit().putBoolean("DARK_MODE", enabled).apply()
    fun isDarkMode(): Boolean = prefs.getBoolean("DARK_MODE", false)
}