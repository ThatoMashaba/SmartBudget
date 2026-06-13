package com.budgettracker.app.ui.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.budgettracker.app.data.database.BudgetDatabase
import com.budgettracker.app.data.entities.User
import com.budgettracker.app.databinding.ActivityLoginBinding
import com.budgettracker.app.ui.main.MainActivity
import com.budgettracker.app.utils.SessionManager
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    // Tag used for Logcat filtering - helps identify logs from this screen
    private val TAG = "LoginActivity"

    private lateinit var binding: ActivityLoginBinding
    private lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        session = SessionManager(this)

        Log.d(TAG, "onCreate: Login screen loaded")

        // If already logged in go straight to main screen
        if (session.isLoggedIn()) {
            goToMain()
            return
        }

        val db = BudgetDatabase.getDatabase(this)

        // Login Button
        binding.btnLogin.setOnClickListener {
            val username = binding.etUsername.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            // Validate inputs before attempting login
            if (username.isEmpty()) {
                binding.etUsername.error = "Please enter your username"
                return@setOnClickListener
            }
            if (password.isEmpty()) {
                binding.etPassword.error = "Please enter your password"
                return@setOnClickListener
            }

            Log.d(TAG, "Attempting login for username: $username")

            lifecycleScope.launch {
                val user = db.userDao().login(username, password)
                if (user != null) {
                    Log.d(TAG, "Login successful for userId: ${user.userId}")
                    session.saveUserId(user.userId)
                    session.saveUsername(user.username)
                    runOnUiThread { goToMain() }
                } else {
                    Log.w(TAG, "Login failed: invalid credentials for $username")
                    runOnUiThread {
                        Toast.makeText(
                            this@LoginActivity,
                            "Incorrect username or password",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }

        // Register Button
        binding.btnRegister.setOnClickListener {
            val username = binding.etUsername.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            // Validate inputs
            if (username.length < 3) {
                binding.etUsername.error = "Username must be at least 3 characters"
                return@setOnClickListener
            }
            if (password.length < 4) {
                binding.etPassword.error = "Password must be at least 4 characters"
                return@setOnClickListener
            }

            Log.d(TAG, "Attempting registration for username: $username")

            lifecycleScope.launch {
                val existing = db.userDao().getUserByUsername(username)
                if (existing != null) {
                    Log.w(TAG, "Registration failed: username already exists")
                    runOnUiThread {
                        Toast.makeText(
                            this@LoginActivity,
                            "Username already exists. Please choose another.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    val newUser = User(username = username, password = password)
                    val id = db.userDao().insertUser(newUser)
                    Log.d(TAG, "Registration successful, new userId: $id")
                    session.saveUserId(id.toInt())
                    session.saveUsername(username)
                    runOnUiThread {
                        Toast.makeText(
                            this@LoginActivity,
                            "Account created! Welcome $username",
                            Toast.LENGTH_SHORT
                        ).show()
                        goToMain()
                    }
                }
            }
        }
    }

    private fun goToMain() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}