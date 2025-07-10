package com.bwell.sampleapp.activities.ui.consent

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bwell.sampleapp.databinding.HealthMatchFeedbackBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class HealthMatchFeedbackFragment : BottomSheetDialogFragment() {
    companion object {
        const val TAG = "HealthMatchFeedbackFragment"
        private const val ARG_CONSENT_GRANTED = "consent_granted"

        fun newInstance(consentGranted: Boolean) = HealthMatchFeedbackFragment().apply {
            arguments = Bundle().apply {
                putBoolean(ARG_CONSENT_GRANTED, consentGranted)
            }
        }
    }

    private var _binding: HealthMatchFeedbackBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = HealthMatchFeedbackBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupBottomSheet()
        setupViews()
    }

    private fun setupBottomSheet() {
        val bottomSheet = dialog?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        bottomSheet?.let { sheet ->
            val behavior = BottomSheetBehavior.from(sheet)
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            behavior.skipCollapsed = true
            sheet.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
        }
    }

    private fun setupViews() {
        binding.apply {
            btnClose.setOnClickListener {
                dismiss()
                (parentFragment as? ConsentCallback)?.onConsentSubmitted(true)
            }

            btnGoHome.setOnClickListener {
                dismiss()
                (parentFragment as? ConsentCallback)?.onConsentSubmitted(true)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    interface ConsentCallback {
        fun onConsentSubmitted(granted: Boolean)
    }
}