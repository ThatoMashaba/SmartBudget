package com.budgettracker.app.ui.reports

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.budgettracker.app.data.database.BudgetDatabase
import com.budgettracker.app.databinding.FragmentReportsBinding
import com.budgettracker.app.utils.SessionManager
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Calendar
import java.util.Locale
import android.util.Log

class ReportsFragment : Fragment() {

    private val TAG = "ReportsFragment"

    private var _binding: FragmentReportsBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: CategoryTotalAdapter
    private lateinit var badgeAdapter: BadgeAdapter

    private var startDate = "2000-01-01"
    private var endDate = "2999-12-31"
    private var collectJob: Job? = null
    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "ZA"))

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReportsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val db = BudgetDatabase.getDatabase(requireContext())
        val userId = SessionManager(requireContext()).getUserId()

        // --- Dark Mode Toggle ---
        val session = SessionManager(requireContext())
        binding.switchDarkMode.isChecked = session.isDarkMode()
        binding.switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            session.setDarkMode(isChecked)
            if (isChecked) {
                androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(
                    androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
                )
            } else {
                androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(
                    androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
                )
            }
        }

        // Setup Category Totals RecyclerView
        adapter = CategoryTotalAdapter(emptyList())
        binding.rvCategoryTotals.layoutManager = LinearLayoutManager(context)
        binding.rvCategoryTotals.adapter = adapter

        // Setup Badges RecyclerView
        badgeAdapter = BadgeAdapter(emptyList())
        binding.rvBadges.layoutManager = LinearLayoutManager(context)
        binding.rvBadges.adapter = badgeAdapter

        // Load default data
        loadReport(db, userId)

        // Date pickers
        binding.etReportStart.setOnClickListener {
            showDatePicker { date ->
                startDate = date
                binding.etReportStart.setText(date)
            }
        }
        binding.etReportEnd.setOnClickListener {
            showDatePicker { date ->
                endDate = date
                binding.etReportEnd.setText(date)
            }
        }

        // Buttons
        binding.btnViewReport.setOnClickListener {
            if (startDate > endDate) {
                Toast.makeText(requireContext(),
                    "Start date cannot be after end date",
                    Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            loadReport(db, userId)
        }

        binding.btnThisMonth.setOnClickListener {
            val cal = Calendar.getInstance()
            val year = cal.get(Calendar.YEAR)
            val month = (cal.get(Calendar.MONTH) + 1).toString().padStart(2, '0')
            val lastDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
            startDate = "$year-$month-01"
            endDate = "$year-$month-${lastDay.toString().padStart(2, '0')}"
            binding.etReportStart.setText(startDate)
            binding.etReportEnd.setText(endDate)
            loadReport(db, userId)
        }

        binding.btnLast30.setOnClickListener {
            val cal = Calendar.getInstance()
            endDate = "${cal.get(Calendar.YEAR)}-${
                (cal.get(Calendar.MONTH) + 1).toString().padStart(2, '0')
            }-${cal.get(Calendar.DAY_OF_MONTH).toString().padStart(2, '0')}"
            cal.add(Calendar.DAY_OF_MONTH, -30)
            startDate = "${cal.get(Calendar.YEAR)}-${
                (cal.get(Calendar.MONTH) + 1).toString().padStart(2, '0')
            }-${cal.get(Calendar.DAY_OF_MONTH).toString().padStart(2, '0')}"
            binding.etReportStart.setText(startDate)
            binding.etReportEnd.setText(endDate)
            loadReport(db, userId)
        }

        binding.btnAllTime.setOnClickListener {
            startDate = "2000-01-01"
            endDate = "2999-12-31"
            binding.etReportStart.setText("All time")
            binding.etReportEnd.setText("")
            loadReport(db, userId)
        }
    }

    private fun loadReport(db: BudgetDatabase, userId: Int) {
        collectJob?.cancel()
        collectJob = lifecycleScope.launch {
            db.expenseDao().getCategoryTotals(userId, startDate, endDate)
                .collect { totals ->
                    adapter.updateList(totals)
                    val grandTotal = totals.sumOf { it.total }

                    // Get current month goal
                    val cal = Calendar.getInstance()
                    val currentMonth = "${cal.get(Calendar.YEAR)}-${
                        (cal.get(Calendar.MONTH) + 1).toString().padStart(2, '0')
                    }"
                    val goal = db.goalDao()
                        .getGoalForMonth(userId, currentMonth).first()

                    // Update chart
                    updateChart(
                        totals.map { it.categoryName },
                        totals.map { it.total.toFloat() },
                        goal?.minGoal?.toFloat(),
                        goal?.maxGoal?.toFloat()
                    )

                    // --- Calculate Badges ---
                    val totalExpenseCount = db.expenseDao().getExpenseCount(userId)
                    val totalCategoryCount = db.categoryDao().getCategoryCount(userId)
                    val withinGoal = goal != null &&
                            grandTotal >= goal.minGoal && grandTotal <= goal.maxGoal
                    val belowMax = goal == null || grandTotal <= goal.maxGoal

                    val badges = com.budgettracker.app.utils.BadgeHelper.calculateBadges(
                        totalExpenses = totalExpenseCount,
                        totalCategories = totalCategoryCount,
                        withinGoalThisMonth = withinGoal,
                        spentBelowMax = belowMax
                    )

                    requireActivity().runOnUiThread {
                        badgeAdapter.updateList(badges)

                        // Update total
                        binding.tvReportTotal.text = currencyFormat.format(grandTotal)

                        // Update goal status
                        if (goal != null) {
                            val status = when {
                                grandTotal < goal.minGoal ->
                                    "⬇️ Below minimum (${currencyFormat.format(goal.minGoal)})"
                                grandTotal > goal.maxGoal ->
                                    "⬆️ Exceeded maximum (${currencyFormat.format(goal.maxGoal)})"
                                else -> "✅ Within goal range"
                            }
                            binding.tvGoalStatus.text = status
                            binding.tvGoalStatus.setTextColor(
                                when {
                                    grandTotal < goal.minGoal ->
                                        resources.getColor(android.R.color.holo_orange_dark, null)
                                    grandTotal > goal.maxGoal ->
                                        resources.getColor(android.R.color.holo_red_dark, null)
                                    else ->
                                        resources.getColor(android.R.color.holo_green_dark, null)
                                }
                            )
                        } else {
                            binding.tvGoalStatus.text = "No goal set for this month"
                            binding.tvGoalStatus.setTextColor(
                                resources.getColor(android.R.color.darker_gray, null)
                            )
                        }
                    }
                }
        }
    }

    private fun updateChart(
        labels: List<String>,
        values: List<Float>,
        minGoal: Float?,
        maxGoal: Float?
    ) {
        requireActivity().runOnUiThread {
            val entries = values.mapIndexed { index, value ->
                BarEntry(index.toFloat(), value)
            }

            val dataSet = BarDataSet(entries, "Spending by Category").apply {
                color = android.graphics.Color.parseColor("#1565C0")
                valueTextColor = android.graphics.Color.BLACK
                valueTextSize = 10f
            }

            val barData = BarData(dataSet)
            binding.barChart.data = barData

            binding.barChart.xAxis.apply {
                valueFormatter = IndexAxisValueFormatter(labels)
                granularity = 1f
                setDrawGridLines(false)
                labelRotationAngle = -30f
            }

            binding.barChart.axisLeft.removeAllLimitLines()

            if (minGoal != null) {
                val minLine = LimitLine(minGoal, "Min Goal").apply {
                    lineColor = android.graphics.Color.parseColor("#2E7D32")
                    lineWidth = 2f
                    textColor = android.graphics.Color.parseColor("#2E7D32")
                    textSize = 10f
                }
                binding.barChart.axisLeft.addLimitLine(minLine)
            }

            if (maxGoal != null) {
                val maxLine = LimitLine(maxGoal, "Max Goal").apply {
                    lineColor = android.graphics.Color.parseColor("#C62828")
                    lineWidth = 2f
                    textColor = android.graphics.Color.parseColor("#C62828")
                    textSize = 10f
                }
                binding.barChart.axisLeft.addLimitLine(maxLine)
            }

            binding.barChart.apply {
                description.isEnabled = false
                setFitBars(true)
                animateY(1000)
                axisRight.isEnabled = false
                legend.isEnabled = true
                invalidate()
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