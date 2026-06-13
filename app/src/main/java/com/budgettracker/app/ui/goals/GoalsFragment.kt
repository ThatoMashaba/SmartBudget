package com.budgettracker.app.ui.goals

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.budgettracker.app.data.database.BudgetDatabase
import com.budgettracker.app.data.entities.MonthlyGoal
import com.budgettracker.app.databinding.FragmentGoalsBinding
import com.budgettracker.app.utils.SessionManager
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Calendar
import java.util.Locale

class GoalsFragment : Fragment() {

    // Tag used for Logcat filtering
    private val TAG = "GoalsFragment"

    // ViewBinding
    private var _binding: FragmentGoalsBinding? = null
    private val binding get() = _binding!!

    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "ZA"))

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGoalsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val db = BudgetDatabase.getDatabase(requireContext())
        val userId = SessionManager(requireContext()).getUserId()

        Log.d(TAG, "Goals screen initialized for userId: $userId")

        // Set current month as default
        val cal = Calendar.getInstance()
        val currentMonth = "${cal.get(Calendar.YEAR)}-${
            (cal.get(Calendar.MONTH) + 1).toString().padStart(2, '0')
        }"
        binding.etMonth.setText(currentMonth)

        // Load existing goal for current month
        loadExistingGoal(db, userId, currentMonth)

        // --- MIN SEEKBAR ---
        binding.seekMin.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                // Update display label as user drags
                binding.tvMinValue.text = currencyFormat.format(progress)
                if (fromUser) {
                    binding.etMinAmount.setText(progress.toString())
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        // --- MAX SEEKBAR ---
        binding.seekMax.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                // Update display label as user drags
                binding.tvMaxValue.text = currencyFormat.format(progress)
                if (fromUser) {
                    binding.etMaxAmount.setText(progress.toString())
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        // --- MIN TEXT INPUT syncs with seekbar ---
        binding.etMinAmount.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val value = binding.etMinAmount.text.toString().toDoubleOrNull()
                if (value != null) {
                    val intValue = value.toInt().coerceIn(0, 50000)
                    binding.seekMin.progress = intValue
                    binding.tvMinValue.text = currencyFormat.format(value)
                }
            }
        }

        // --- MAX TEXT INPUT syncs with seekbar ---
        binding.etMaxAmount.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val value = binding.etMaxAmount.text.toString().toDoubleOrNull()
                if (value != null) {
                    val intValue = value.toInt().coerceIn(0, 50000)
                    binding.seekMax.progress = intValue
                    binding.tvMaxValue.text = currencyFormat.format(value)
                }
            }
        }

        // --- SAVE BUTTON ---
        binding.btnSaveGoal.setOnClickListener {
            saveGoal(db, userId)
        }

        // Reload goal when month field loses focus
        binding.etMonth.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val month = binding.etMonth.text.toString().trim()
                if (month.matches(Regex("\\d{4}-\\d{2}"))) {
                    loadExistingGoal(db, userId, month)
                }
            }
        }
    }

    // Loads existing goal for a given month and pre-fills the UI
    private fun loadExistingGoal(
        db: BudgetDatabase,
        userId: Int,
        month: String
    ) {
        lifecycleScope.launch {
            db.goalDao().getGoalForMonth(userId, month).collect { goal ->
                requireActivity().runOnUiThread {
                    if (goal != null) {
                        Log.d(TAG, "Found existing goal for $month: min=${goal.minGoal}, max=${goal.maxGoal}")

                        // Load existing values into UI
                        binding.seekMin.progress = goal.minGoal.toInt().coerceIn(0, 50000)
                        binding.seekMax.progress = goal.maxGoal.toInt().coerceIn(0, 50000)
                        binding.tvMinValue.text = currencyFormat.format(goal.minGoal)
                        binding.tvMaxValue.text = currencyFormat.format(goal.maxGoal)
                        binding.etMinAmount.setText(goal.minGoal.toInt().toString())
                        binding.etMaxAmount.setText(goal.maxGoal.toInt().toString())

                        // Update summary card
                        binding.tvGoalSummary.text =
                            "Month: $month\n" +
                                    "Min: ${currencyFormat.format(goal.minGoal)}\n" +
                                    "Max: ${currencyFormat.format(goal.maxGoal)}"
                    } else {
                        Log.d(TAG, "No existing goal found for $month")
                        binding.tvGoalSummary.text = "No goals set for $month yet"
                    }
                }
            }
        }
    }

    // Validates inputs and saves the monthly goal to the database
    private fun saveGoal(db: BudgetDatabase, userId: Int) {
        val month = binding.etMonth.text.toString().trim()

        // Validate month format
        if (month.isEmpty()) {
            binding.etMonth.error = "Please enter a month"
            return
        }
        if (!month.matches(Regex("\\d{4}-\\d{2}"))) {
            binding.etMonth.error = "Format must be yyyy-MM e.g. 2024-03"
            return
        }

        // Get values — prefer text input if filled, else use seekbar
        val minInput = binding.etMinAmount.text.toString().toDoubleOrNull()
        val maxInput = binding.etMaxAmount.text.toString().toDoubleOrNull()

        val minGoal = minInput ?: binding.seekMin.progress.toDouble()
        val maxGoal = maxInput ?: binding.seekMax.progress.toDouble()

        // Validate goal values
        if (minGoal <= 0 && maxGoal <= 0) {
            Toast.makeText(
                requireContext(),
                "Please set at least one goal amount",
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        if (minGoal >= maxGoal) {
            Toast.makeText(
                requireContext(),
                "Minimum goal must be less than maximum goal",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        Log.d(TAG, "Saving goal for $month - min: $minGoal, max: $maxGoal")

        // Save to database
        lifecycleScope.launch {
            db.goalDao().insertGoal(
                MonthlyGoal(
                    minGoal = minGoal,
                    maxGoal = maxGoal,
                    month = month,
                    userId = userId
                )
            )
            requireActivity().runOnUiThread {
                // Update summary display
                binding.tvGoalSummary.text =
                    "Month: $month\n" +
                            "Min: ${currencyFormat.format(minGoal)}\n" +
                            "Max: ${currencyFormat.format(maxGoal)}"

                Toast.makeText(
                    requireContext(),
                    "Goals saved for $month!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}