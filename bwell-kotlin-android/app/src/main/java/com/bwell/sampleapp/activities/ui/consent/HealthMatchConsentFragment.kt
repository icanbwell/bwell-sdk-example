package com.bwell.sampleapp.activities.ui.consent

import android.os.Bundle
import android.view.LayoutInflater
import androidx.navigation.fragment.findNavController
import android.view.View
import com.bwell.sampleapp.R
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.bwell.sampleapp.BWellSampleApplication
import com.bwell.sampleapp.databinding.HealthMatchConsentBinding
import com.bwell.sampleapp.viewmodel.HealthMatchConsentViewModel
import com.bwell.sampleapp.viewmodel.HealthMatchConsentViewModelFactory
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar

class HealthMatchConsentFragment : BottomSheetDialogFragment() {
    companion object {
        const val TAG = "HealthMatchConsentFragment"

        fun newInstance() = HealthMatchConsentFragment()
    }
    private lateinit var viewModel: HealthMatchConsentViewModel
    private var _binding: HealthMatchConsentBinding? = null
    // This property is only valid between onCreateView and onDestroyView
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = HealthMatchConsentBinding.inflate(inflater, container, false)
        val repository = (activity?.application as? BWellSampleApplication)?.bWellRepository
        viewModel = ViewModelProvider(this, HealthMatchConsentViewModelFactory(repository))[HealthMatchConsentViewModel::class.java]
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupBottomSheet()
        setupConsentForm()
        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.consentState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is HealthMatchConsentViewModel.ConsentState.Loading -> {
                    binding.btnSubmit.isEnabled = false
                    binding.progressBar.visibility = View.VISIBLE
                    // Show loading indicator if needed
                }
                is HealthMatchConsentViewModel.ConsentState.Success -> {
                    // Handle success (e.g., navigate away or show success message)
                    binding.progressBar.visibility = View.GONE
                    dismiss()
                    // Show feedback bottom sheet
                    if (binding.radioPermit.isChecked)
                        showFeedbackBottomSheet(binding.radioPermit.isChecked)
                    else
                        findNavController().navigate(R.id.nav_home)
                }
                is HealthMatchConsentViewModel.ConsentState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnSubmit.isEnabled = true
                    Snackbar.make(binding.root, state.message, Snackbar.LENGTH_LONG).show()
                }
                else -> { /* Handle other states */ }
            }
        }
    }

    private fun setupConsentForm() {
        binding.apply {
            // Add close button
            btnClose.setOnClickListener {
                dismiss()
            }

            radioGroupConsent.setOnCheckedChangeListener { _, checkedId ->
                btnSubmit.isEnabled = true
            }

            btnSubmit.setOnClickListener {
                val isPermitted = binding.radioPermit.isChecked
                viewModel.submitConsent(isPermitted)
            }
        }
    }

    private fun setupBottomSheet() {
        // Make the bottom sheet expandable to full screen
        val bottomSheet = dialog?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        bottomSheet?.let { sheet ->
            val behavior = BottomSheetBehavior.from(sheet)
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            behavior.skipCollapsed = true

            // Set the height to match parent
            sheet.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
        }
    }

    private fun showFeedbackBottomSheet(consentGranted: Boolean) {
        HealthMatchFeedbackFragment.newInstance(consentGranted)
            .show(parentFragmentManager, HealthMatchFeedbackFragment.TAG)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}