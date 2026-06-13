package com.budgettracker.app

import android.app.Application
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import com.budgettracker.app.utils.SessionManager

// Custom Application class - runs once when the app process starts.
// Used here to apply the user's saved dark/light theme preference
// before any screen is shown.
class BudgetApp : Application() {

    private val TAG = "BudgetApp"

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "App starting up")

        val session = SessionManager(this)
        if (session.isDarkMode()) {
            Log.d(TAG, "Applying dark theme")
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            Log.d(TAG, "Applying light theme")
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }
}