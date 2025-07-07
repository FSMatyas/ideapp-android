package com.example.ideapp

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.example.ideapp.data.Idea
import com.example.ideapp.viewmodel.IdeaViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.util.*

class SubmitIdeaDialogFragment : DialogFragment() {

    private lateinit var tilIdeaTitle: TextInputLayout
    private lateinit var etIdeaTitle: TextInputEditText
    private lateinit var tilCategory: TextInputLayout
    private lateinit var actvCategory: AutoCompleteTextView
    private lateinit var tilDescription: TextInputLayout
    private lateinit var etDescription: TextInputEditText
    private lateinit var tilContact: TextInputLayout
    private lateinit var etContact: TextInputEditText
    private lateinit var tilUserName: TextInputLayout
    private lateinit var etUserName: TextInputEditText
    private lateinit var btnCancel: MaterialButton
    private lateinit var btnSubmit: MaterialButton
    private lateinit var progressBar: ProgressBar

    private val viewModel: IdeaViewModel by activityViewModels()

    private val categories = arrayOf(
        "productivity",
        "health",
        "education", 
        "social",
        "entertainment",
        "utility",
        "finance",
        "other"
    )

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_submit_idea, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        initViews(view)
        setupCategoryDropdown()
        setupClickListeners()
        observeViewModel()
    }

    private fun initViews(view: View) {
        tilIdeaTitle = view.findViewById(R.id.tilIdeaTitle)
        etIdeaTitle = view.findViewById(R.id.etIdeaTitle)
        tilCategory = view.findViewById(R.id.tilCategory)
        actvCategory = view.findViewById(R.id.actvCategory)
        tilDescription = view.findViewById(R.id.tilDescription)
        etDescription = view.findViewById(R.id.etDescription)
        tilContact = view.findViewById(R.id.tilContact)
        etContact = view.findViewById(R.id.etContact)
        
        // Check if userName fields exist in the layout, if not we'll use contact as name
        tilUserName = view.findViewById(R.id.tilUserName) ?: tilContact
        etUserName = view.findViewById(R.id.etUserName) ?: etContact
        
        btnCancel = view.findViewById(R.id.btnCancel)
        btnSubmit = view.findViewById(R.id.btnSubmit)
        progressBar = view.findViewById(R.id.progressBar) ?: run {
            // Create a progress bar if it doesn't exist in layout
            ProgressBar(requireContext())
        }
    }

    private fun setupCategoryDropdown() {
        val displayCategories = categories.map { category ->
            category.replaceFirstChar { 
                if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() 
            }
        }.toTypedArray()
        
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            displayCategories
        )
        actvCategory.setAdapter(adapter)
    }

    private fun setupClickListeners() {
        btnCancel.setOnClickListener {
            dismiss()
        }

        btnSubmit.setOnClickListener {
            if (validateForm()) {
                submitIdea()
            }
        }
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            btnSubmit.isEnabled = !isLoading
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
                viewModel.clearError()
            }
        }
    }

    private fun validateForm(): Boolean {
        var isValid = true

        // Clear previous errors
        tilIdeaTitle.error = null
        tilCategory.error = null
        tilDescription.error = null
        tilContact.error = null
        tilUserName.error = null

        // Validate title
        val title = etIdeaTitle.text.toString().trim()
        if (title.isEmpty()) {
            tilIdeaTitle.error = "Please enter a title for your idea"
            isValid = false
        }

        // Validate category
        val category = actvCategory.text.toString().trim()
        val categoryLowercase = category.lowercase()
        if (category.isEmpty()) {
            tilCategory.error = "Please select a category"
            isValid = false
        } else if (!categories.contains(categoryLowercase)) {
            tilCategory.error = "Please select a valid category"
            isValid = false
        }

        // Validate description
        val description = etDescription.text.toString().trim()
        if (description.isEmpty()) {
            tilDescription.error = "Please describe your problem"
            isValid = false
        }

        // Validate contact/email
        val contact = etContact.text.toString().trim()
        if (contact.isEmpty()) {
            tilContact.error = "Please provide your email"
            isValid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(contact).matches()) {
            tilContact.error = "Please provide a valid email address"
            isValid = false
        }

        // Validate user name
        val userName = etUserName.text.toString().trim()
        if (userName.isEmpty() && etUserName != etContact) {
            tilUserName.error = "Please provide your name"
            isValid = false
        }

        return isValid
    }

    private fun submitIdea() {
        val title = etIdeaTitle.text.toString().trim()
        val categoryDisplay = actvCategory.text.toString().trim()
        val category = categoryDisplay.lowercase()
        val description = etDescription.text.toString().trim()
        val contact = etContact.text.toString().trim()
        val userName = if (etUserName != etContact) {
            etUserName.text.toString().trim()
        } else {
            // Extract name from email if we don't have a separate name field
            contact.substringBefore("@").replace(".", " ").replaceFirstChar { 
                if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() 
            }
        }

        // Create Idea object
        val idea = Idea(
            title = title,
            description = description,
            category = category,
            submitterEmail = contact,
            submitterName = userName
        )

        // Submit through ViewModel
        viewModel.submitIdea(idea) { success, message ->
            if (success) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                dismiss()
            } else {
                Toast.makeText(requireContext(), "Error: $message", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    companion object {
        fun newInstance() = SubmitIdeaDialogFragment()
    }
}
