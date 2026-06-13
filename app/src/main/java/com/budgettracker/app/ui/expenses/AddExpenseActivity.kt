package com.budgettracker.app.ui.expenses

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.budgettracker.app.data.database.BudgetDatabase
import com.budgettracker.app.data.entities.Category
import com.budgettracker.app.data.entities.Expense
import com.budgettracker.app.databinding.ActivityAddExpenseBinding
import com.budgettracker.app.utils.SessionManager
import com.bumptech.glide.Glide
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar

class AddExpenseActivity : AppCompatActivity() {

    private val TAG = "AddExpenseActivity"

    private lateinit var binding: ActivityAddExpenseBinding
    private var categories: List<Category> = emptyList()
    private var photoUri: Uri? = null

    // If editing, this holds the existing expense
    private var editingExpense: Expense? = null
    private var expenseId: Int = -1

    private val pickImage =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                photoUri = it
                binding.ivPreview.visibility = View.VISIBLE
                Glide.with(this).load(it).into(binding.ivPreview)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddExpenseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Check if we are editing an existing expense
        expenseId = intent.getIntExtra("EXPENSE_ID", -1)
        supportActionBar?.title = if (expenseId != -1) "Edit Expense" else "Add Expense"

        val db = BudgetDatabase.getDatabase(this)
        val userId = SessionManager(this).getUserId()

        // Load categories into spinner
        lifecycleScope.launch {
            categories = db.categoryDao().getCategoriesByUser(userId).first()
            Log.d(TAG, "Loaded ${categories.size} categories")

            runOnUiThread {
                if (categories.isEmpty()) {
                    Toast.makeText(
                        this@AddExpenseActivity,
                        "Please create a category first!",
                        Toast.LENGTH_LONG
                    ).show()
                }
                val categoryNames = categories.map { it.name }
                binding.spinnerCategory.adapter = ArrayAdapter(
                    this@AddExpenseActivity,
                    android.R.layout.simple_spinner_dropdown_item,
                    categoryNames
                )

                // If editing, load the expense data AFTER categories are ready
                if (expenseId != -1) {
                    loadExpenseForEditing(db, expenseId)
                }
            }
        }

        binding.etDate.setOnClickListener { showDatePicker() }
        binding.etStartTime.setOnClickListener { showTimePicker(isStart = true) }
        binding.etEndTime.setOnClickListener { showTimePicker(isStart = false) }
        binding.btnPickPhoto.setOnClickListener { pickImage.launch("image/*") }

        binding.btnSaveExpense.setOnClickListener {
            saveExpense(db, userId)
        }
    }

    // Load existing expense data into the form for editing
    private fun loadExpenseForEditing(db: BudgetDatabase, id: Int) {
        lifecycleScope.launch {
            val expense = db.expenseDao().getExpenseById(id)
            if (expense != null) {
                editingExpense = expense
                Log.d(TAG, "Editing expense: ${expense.description}")

                runOnUiThread {
                    binding.etDate.setText(expense.date)
                    binding.etStartTime.setText(expense.startTime)
                    binding.etEndTime.setText(expense.endTime)
                    binding.etDescription.setText(expense.description)
                    binding.etAmount.setText(expense.amount.toString())

                    // Select the correct category in spinner
                    val categoryIndex = categories.indexOfFirst {
                        it.categoryId == expense.categoryId
                    }
                    if (categoryIndex != -1) {
                        binding.spinnerCategory.setSelection(categoryIndex)
                    }

                    // Show existing photo
                    if (expense.photoPath != null) {
                        photoUri = Uri.parse(expense.photoPath)
                        binding.ivPreview.visibility = View.VISIBLE
                        Glide.with(this@AddExpenseActivity)
                            .load(expense.photoPath)
                            .into(binding.ivPreview)
                    }

                    // Change button text and show delete button
                    binding.btnSaveExpense.text = "UPDATE EXPENSE"
                    binding.btnDeleteExpense.visibility = View.VISIBLE
                    binding.btnDeleteExpense.setOnClickListener {
                        showDeleteConfirmation(db, expense)
                    }
                }
            }
        }
    }

    private fun showDeleteConfirmation(db: BudgetDatabase, expense: Expense) {
        AlertDialog.Builder(this)
            .setTitle("Delete Expense")
            .setMessage("Are you sure you want to delete this expense?")
            .setPositiveButton("Delete") { _, _ ->
                lifecycleScope.launch {
                    db.expenseDao().deleteExpense(expense)
                    Log.d(TAG, "Deleted expense id=${expense.expenseId}")
                    runOnUiThread {
                        Toast.makeText(
                            this@AddExpenseActivity,
                            "Expense deleted",
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showDatePicker() {
        val cal = Calendar.getInstance()
        DatePickerDialog(
            this,
            { _, year, month, day ->
                val formatted = "$year-${(month + 1).toString()
                    .padStart(2, '0')}-${day.toString().padStart(2, '0')}"
                binding.etDate.setText(formatted)
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun showTimePicker(isStart: Boolean) {
        val cal = Calendar.getInstance()
        TimePickerDialog(
            this,
            { _, hour, minute ->
                val formatted = "${hour.toString()
                    .padStart(2, '0')}:${minute.toString().padStart(2, '0')}"
                if (isStart) binding.etStartTime.setText(formatted)
                else binding.etEndTime.setText(formatted)
            },
            cal.get(Calendar.HOUR_OF_DAY),
            cal.get(Calendar.MINUTE),
            true
        ).show()
    }

    // Validates all form fields, then either inserts a new expense
    // or updates an existing one depending on whether we're editing
    private fun saveExpense(db: BudgetDatabase, userId: Int) {
        val date = binding.etDate.text.toString().trim()
        val startTime = binding.etStartTime.text.toString().trim()
        val endTime = binding.etEndTime.text.toString().trim()
        val description = binding.etDescription.text.toString().trim()
        val amountStr = binding.etAmount.text.toString().trim()

        if (date.isEmpty()) {
            Toast.makeText(this, "Please select a date", Toast.LENGTH_SHORT).show(); return
        }
        if (startTime.isEmpty()) {
            Toast.makeText(this, "Please select a start time", Toast.LENGTH_SHORT).show(); return
        }
        if (endTime.isEmpty()) {
            Toast.makeText(this, "Please select an end time", Toast.LENGTH_SHORT).show(); return
        }
        if (description.isEmpty()) {
            binding.etDescription.error = "Please enter a description"; return
        }
        if (amountStr.isEmpty()) {
            binding.etAmount.error = "Please enter an amount"; return
        }
        if (categories.isEmpty()) {
            Toast.makeText(this, "Please create a category first!", Toast.LENGTH_SHORT).show(); return
        }

        val amount = amountStr.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            binding.etAmount.error = "Please enter a valid amount"; return
        }


        val selectedCategory = categories[binding.spinnerCategory.selectedItemPosition]

        Log.d(TAG, "Saving expense - description: $description, amount: $amount, category: ${selectedCategory.name}")

        lifecycleScope.launch {
            if (editingExpense != null) {
                // UPDATE existing expense
                val updated = editingExpense!!.copy(
                    date = date,
                    startTime = startTime,
                    endTime = endTime,
                    description = description,
                    amount = amount,
                    categoryId = selectedCategory.categoryId,
                    photoPath = photoUri?.toString() ?: editingExpense!!.photoPath
                )
                db.expenseDao().updateExpense(updated)
                Log.d(TAG, "Updated expense id=${updated.expenseId}")

                runOnUiThread {
                    Toast.makeText(this@AddExpenseActivity, "Expense updated!", Toast.LENGTH_SHORT).show()
                    finish()
                }
            } else {
                // INSERT new expense
                val expense = Expense(
                    date = date, startTime = startTime, endTime = endTime,
                    description = description, amount = amount,
                    categoryId = selectedCategory.categoryId,
                    userId = userId, photoPath = photoUri?.toString()
                )
                db.expenseDao().insertExpense(expense)
                Log.d(TAG, "Inserted new expense: $description")

                runOnUiThread {
                    Toast.makeText(this@AddExpenseActivity, "Expense saved successfully!", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}