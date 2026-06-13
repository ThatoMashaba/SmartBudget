package com.budgettracker.app.ui.categories

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.budgettracker.app.data.database.BudgetDatabase
import com.budgettracker.app.data.entities.Category
import com.budgettracker.app.databinding.FragmentCategoriesBinding
import com.budgettracker.app.utils.SessionManager
import kotlinx.coroutines.launch

class CategoriesFragment : Fragment() {

    private val TAG = "CategoriesFragment"

    // ViewBinding
    private var _binding: FragmentCategoriesBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: CategoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCategoriesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val db = BudgetDatabase.getDatabase(requireContext())
        val userId = SessionManager(requireContext()).getUserId()

        Log.d(TAG, "Categories screen initialized for userId: $userId")

        // Setup RecyclerView
        adapter = CategoryAdapter(emptyList()) { category ->
            showDeleteDialog(category, db)
        }
        binding.rvCategories.layoutManager = LinearLayoutManager(context)
        binding.rvCategories.adapter = adapter

        // Observe categories from database
        lifecycleScope.launch {
            db.categoryDao().getCategoriesByUser(userId).collect { categories ->
                Log.d(TAG, "Loaded ${categories.size} categories")
                adapter.updateList(categories)
            }
        }

        // Add category button
        binding.btnAddCategory.setOnClickListener {
            val name = binding.etCategoryName.text.toString().trim()

            // Validate input
            if (name.isEmpty()) {
                binding.etCategoryName.error = "Please enter a category name"
                return@setOnClickListener
            }
            if (name.length < 2) {
                binding.etCategoryName.error = "Name must be at least 2 characters"
                return@setOnClickListener
            }

            // Save to database
            Log.d(TAG, "Adding new category: $name")
            lifecycleScope.launch {
                db.categoryDao().insertCategory(
                    Category(name = name, userId = userId)
                )
                requireActivity().runOnUiThread {
                    binding.etCategoryName.text?.clear()
                    Toast.makeText(
                        requireContext(),
                        "Category '$name' added!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    // Show confirmation before deleting
    private fun showDeleteDialog(
        category: Category,
        db: com.budgettracker.app.data.database.BudgetDatabase
    ) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Category")
            .setMessage("Are you sure you want to delete '${category.name}'?")
            .setPositiveButton("Delete") { _, _ ->
                Log.d(TAG, "Deleting category: ${category.name}")
                lifecycleScope.launch {
                    db.categoryDao().deleteCategory(category)
                    requireActivity().runOnUiThread {
                        Toast.makeText(
                            requireContext(),
                            "'${category.name}' deleted",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}