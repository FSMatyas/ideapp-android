package com.example.ideapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.ideapp.viewmodel.IdeaViewModel
import com.google.android.material.card.MaterialCardView

class CategoriesFragment : Fragment() {

    private val viewModel: IdeaViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_categories, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupCategoryClickListeners(view)
        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
                viewModel.clearError()
            }
        }
    }

    private fun setupCategoryClickListeners(view: View) {
        // Get all category cards and set up click listeners
        val categoryCards = getAllCategoryCards(view)
        val categoryNames = listOf(
            "productivity", // 1st card
            "family",       // 2nd card
            "business",     // 3rd card
            "entertainment",// 4th card
            "finance",      // 5th card
            "health",       // 6th card
            "education",    // 7th card
            "utilities"     // 8th card
        )

        categoryCards.forEachIndexed { index, card ->
            card.setOnClickListener {
                if (index < categoryNames.size) {
                    openCategoryDetail(categoryNames[index])
                }
            }
        }
    }

    private fun getAllCategoryCards(view: View): List<MaterialCardView> {
        val cards = mutableListOf<MaterialCardView>()
        
        // Find all MaterialCardView elements in the layout
        findCardViewsRecursively(view as ViewGroup, cards)
        
        return cards
    }

    private fun findCardViewsRecursively(viewGroup: ViewGroup, cards: MutableList<MaterialCardView>) {
        for (i in 0 until viewGroup.childCount) {
            val child = viewGroup.getChildAt(i)
            when (child) {
                is MaterialCardView -> cards.add(child)
                is ViewGroup -> findCardViewsRecursively(child, cards)
            }
        }
    }

    private fun openCategoryDetail(categoryName: String) {
        val context = requireContext()
        val intent = android.content.Intent(context, CategoryDetailActivity::class.java)
        // Pass the string resource key for the selected category
        val categoryKey = "category_" + categoryName.lowercase()
        intent.putExtra("category_key", categoryKey)
        startActivity(intent)
    }

    companion object {
        fun newInstance() = CategoriesFragment()
    }
}
