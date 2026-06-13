package com.budgettracker.app.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.budgettracker.app.R
import com.budgettracker.app.databinding.ActivityMainBinding
import com.budgettracker.app.ui.categories.CategoriesFragment
import com.budgettracker.app.ui.expenses.ExpensesFragment
import com.budgettracker.app.ui.goals.GoalsFragment
import com.budgettracker.app.ui.login.LoginActivity
import com.budgettracker.app.ui.reports.ReportsFragment
import com.budgettracker.app.utils.SessionManager

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Show expenses fragment by default when app opens
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, ExpensesFragment())
                .commit()
            binding.bottomNav.selectedItemId = R.id.nav_expenses
        }

        // Bottom navigation click listener
        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_expenses -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragmentContainer, ExpensesFragment())
                        .commit()
                    true
                }
                R.id.nav_categories -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragmentContainer, CategoriesFragment())
                        .commit()
                    true
                }
                R.id.nav_goals -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragmentContainer, GoalsFragment())
                        .commit()
                    true
                }
                R.id.nav_reports -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragmentContainer, ReportsFragment())
                        .commit()
                    true
                }
                else -> false
            }
        }
    }

    // This adds a logout button in the top toolbar
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.top_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                showLogoutDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    // Shows a confirmation dialog before logging out
    private fun showLogoutDialog() {
        AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Yes") { _, _ ->
                SessionManager(this).clearSession()
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}