package com.budgettracker.app.ui.expenses

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.budgettracker.app.data.database.BudgetDatabase
import com.budgettracker.app.databinding.FragmentExpensesBinding
import com.budgettracker.app.utils.SessionManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Calendar
import java.util.Locale

class ExpensesFragment : Fragment() {

    private val TAG = "ExpensesFragment"

    // ViewBinding
    private var _binding: FragmentExpensesBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: ExpenseAdapter

    // Default date range shows all expenses
    private var startDate = "2000-01-01"
    private var endDate = "2999-12-31"

    // Track the current collection job
    private var collectJob: Job? = null

    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "ZA"))

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExpensesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val db = BudgetDatabase.getDatabase(requireContext())
        val userId = SessionManager(requireContext()).getUserId()

        // Setup RecyclerView - tapping an item opens it for editing
        adapter = ExpenseAdapter(emptyList()) { expense ->
            val intent = Intent(requireContext(), AddExpenseActivity::class.java)
            intent.putExtra("EXPENSE_ID", expense.expenseId)
            startActivity(intent)
        }
        binding.rvExpenses.layoutManager = LinearLayoutManager(context)
        binding.rvExpenses.adapter = adapter

        // Load all expenses by default
        loadExpenses(db, userId)

        // From date picker
        binding.etFilterStart.setOnClickListener {
            showDatePicker { selectedDate ->
                startDate = selectedDate
                binding.etFilterStart.setText(selectedDate)
            }
        }

        // To date picker
        binding.etFilterEnd.setOnClickListener {
            showDatePicker { selectedDate ->
                endDate = selectedDate
                binding.etFilterEnd.setText(selectedDate)
            }
        }

        // Filter button
        binding.btnFilter.setOnClickListener {
            if (startDate == "2000-01-01" && endDate == "2999-12-31") {
                Toast.makeText(
                    requireContext(),
                    "Please select a date range first",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            if (startDate > endDate) {
                Toast.makeText(
                    requireContext(),
                    "Start date cannot be after end date",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            loadExpenses(db, userId)
        }

        // Clear filter button
        binding.btnClearFilter.setOnClickListener {
            startDate = "2000-01-01"
            endDate = "2999-12-31"
            binding.etFilterStart.setText("")
            binding.etFilterEnd.setText("")
            loadExpenses(db, userId)
            Toast.makeText(requireContext(), "Filter cleared", Toast.LENGTH_SHORT).show()
        }

        // FAB — go to Add Expense screen
        binding.fabAddExpense.setOnClickListener {
            val intent = Intent(requireContext(), AddExpenseActivity::class.java)
            startActivity(intent)
        }
    }

    // Reload expenses when coming back from AddExpenseActivity
    override fun onResume() {
        super.onResume()
        val db = BudgetDatabase.getDatabase(requireContext())
        val userId = SessionManager(requireContext()).getUserId()
        loadExpenses(db, userId)
    }

    // Loads expenses for the current date range and updates the total spent display
    private fun loadExpenses(
        db: com.budgettracker.app.data.database.BudgetDatabase,
        userId: Int
    ) {
        // Cancel previous job before starting new one to avoid duplicate collectors
        collectJob?.cancel()
        Log.d(TAG, "Loading expenses for period: $startDate to $endDate")
        collectJob = lifecycleScope.launch {
            db.expenseDao().getExpensesByPeriod(userId, startDate, endDate)
                .collect { expenses ->
                    adapter.updateList(expenses)
                    Log.d(TAG, "Loaded ${expenses.size} expenses")

                    // Update total amount display
                    val total = expenses.sumOf { it.amount }
                    requireActivity().runOnUiThread {
                        binding.tvTotalAmount.text = currencyFormat.format(total)
                    }
                }
        }
    }

    private fun showDatePicker(onDateSelected: (String) -> Unit) {
        val cal = Calendar.getInstance()
        DatePickerDialog(
            requireContext(),
            { _, year, month, day ->
                val formatted = "$year-${(month + 1).toString()
                    .padStart(2, '0')}-${day.toString().padStart(2, '0')}"
                onDateSelected(formatted)
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        collectJob?.cancel()
        _binding = null
    }
}