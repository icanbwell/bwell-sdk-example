package com.bwell.sampleapp.activities.ui.medicines

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bwell.common.models.domain.healthdata.medication.MedicationComposition
import com.bwell.common.models.domain.healthdata.medication.MedicationKnowledge
import com.bwell.common.models.domain.user.Person
import com.bwell.common.models.responses.BWellResult
import com.bwell.healthdata.medication.MedicationKnowledgeRequest
import com.bwell.healthdata.medication.MedicationPricingRequest
import com.bwell.sampleapp.BWellSampleApplication
import com.bwell.sampleapp.R
import com.bwell.sampleapp.activities.ui.data_connections.DataConnectionsFragment
import com.bwell.sampleapp.databinding.MedicineDetailViewBinding
import com.bwell.sampleapp.utils.formatDate
import com.bwell.sampleapp.viewmodel.MedicineViewModelFactory
import com.bwell.sampleapp.viewmodel.MedicinesViewModel
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import org.json.JSONObject

class MedicineDetailFragment : Fragment(),View.OnClickListener {

    private var _binding: MedicineDetailViewBinding? = null
    private lateinit var medicinesViewModel: MedicinesViewModel
    private lateinit var medicationId: String

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = MedicineDetailViewBinding.inflate(inflater, container, false)
        val root: View = binding.root
        val repository = (activity?.application as? BWellSampleApplication)?.medicineRepository
        medicinesViewModel = ViewModelProvider(this, MedicineViewModelFactory(repository))[MedicinesViewModel::class.java]
        binding.leftArrowImageView.setOnClickListener(this)
        binding.whatIsItTextView.setOnClickListener(this)
        binding.pricingTextView.setOnClickListener(this)
        medicationId = arguments?.getString("id").toString()

        showOverView()

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.leftArrowImageView -> {
                parentFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
                val parentFrag: MedicinesFragment = this@MedicineDetailFragment.getParentFragment() as MedicinesFragment
                parentFrag.showMedicinesList()
            }
            R.id.overviewTextView -> {
              showOverView()
            }
            R.id.pricingTextView -> {
              showPriceView()
            }
            R.id.whatIsItTextView -> {
               showKnowledgeView()
            }
        }
    }

    private fun showPriceView() {
        binding.medicineOverviewView.medicineOverviewView.visibility = View.GONE
        binding.medicineKnowledgeView.medicineKnowledgeView.visibility = View.GONE
        binding.medicinePriceView.medicinePriceView.visibility = View.VISIBLE
        binding.pricingTextView.setTextColor(resources.getColor(R.color.medicine_tabs_selected_color))
        binding.pricingwunderline.setBackgroundColor(resources.getColor(R.color.medicine_tabs_selected_color))
        binding.overviewTextView.setTextColor(resources.getColor(R.color.black))
        binding.overviewunderline.setBackgroundColor(resources.getColor(R.color.medicine_tabs_non_selected_color))
        binding.whatIsItTextView.setTextColor(resources.getColor(R.color.black))
        binding.whatIsItunderline.setBackgroundColor(resources.getColor(R.color.medicine_tabs_non_selected_color))
        binding.pricingTextView.setOnClickListener(null)
        binding.overviewTextView.setOnClickListener(this)
        binding.whatIsItTextView.setOnClickListener(this)
        binding.medicineKnowledgeView.containerLayout.removeAllViews()
        binding.medicinePriceView.priceContainerLayout.removeAllViews()
        val request = MedicationPricingRequest.Builder()
            .compositionId(medicationId)
            .build()
        medicinesViewModel.getMedicationPricing(request)
        viewLifecycleOwner.lifecycleScope.launch {
            medicinesViewModel.medicationPricingResults.take(1).collect { result ->
                if (result != null) {
                    when (result) {
                        is BWellResult.ResourceCollection -> {
                            val dataList = result.data
                            for (i in 0 until (dataList?.size ?: 0)) {
                                val item = dataList?.get(i)
                                val priceValue = item?.price?.value
                                val currency = item?.price?.currency
                                val pharmacy = item?.pharmacy
                                val textView = TextView(requireContext())
                                textView.text = "$pharmacy price is $${String.format("%.2f", priceValue)} $currency"
                                binding.medicinePriceView.priceContainerLayout.addView(textView)
                            }
                        }
                        else -> {}
                    }
                }
            }
        }
    }

    private fun showOverView() {
        binding.medicineOverviewView.medicineOverviewView.visibility = View.VISIBLE
        binding.medicineKnowledgeView.medicineKnowledgeView.visibility = View.GONE
        binding.medicinePriceView.medicinePriceView.visibility = View.GONE
        binding.whatIsItTextView.setTextColor(resources.getColor(R.color.black))
        binding.whatIsItunderline.setBackgroundColor(resources.getColor(R.color.medicine_tabs_non_selected_color))
        binding.overviewTextView.setTextColor(resources.getColor(R.color.medicine_tabs_selected_color))
        binding.overviewunderline.setBackgroundColor(resources.getColor(R.color.medicine_tabs_selected_color))
        binding.pricingTextView.setTextColor(resources.getColor(R.color.black))
        binding.pricingwunderline.setBackgroundColor(resources.getColor(R.color.medicine_tabs_non_selected_color))
        binding.overviewTextView.setOnClickListener(null)
        binding.whatIsItTextView.setOnClickListener(this)
        binding.pricingTextView.setOnClickListener(this)
        binding.medicineKnowledgeView.containerLayout.removeAllViews()
        binding.medicinePriceView.priceContainerLayout.removeAllViews()
        medicinesViewModel.getMedicationOverview(medicationId)
        viewLifecycleOwner.lifecycleScope.launch {
            medicinesViewModel.medicationOverviewResults.take(1).collect { result ->
                if (result != null) {
                    if (result is BWellResult.SingleResource<MedicationComposition>){
                        val medicationOverview = result.data
                        binding.medicineOverviewView.medicineTitleTextView.text = medicationOverview?.name
                        binding.medicineOverviewView.rxValueTextView.text = medicationOverview?.prescriptionNumber
                        binding.medicineOverviewView.quantityValueTextView.text = medicationOverview?.quantity.toString()
                        binding.medicineOverviewView.lastRefilledValueTextView.text = formatDate(medicationOverview?.refills?.get(0)?.refillDate.toString())
                        binding.medicineOverviewView.refillsRemainingValueTextView.text = medicationOverview?.refillsRemaining.toString()
                        binding.medicineOverviewView.startDateValueTextView.text = formatDate(medicationOverview?.startDate.toString())
                        binding.medicineOverviewView.datePrescribedValueTextView.text = formatDate(medicationOverview?.datePrescribed.toString())
                        binding.medicineOverviewView.organizationName.text = "from "+medicationOverview?.source
                    }
                }
            }
        }
    }

    private fun showKnowledgeView() {
        binding.medicineOverviewView.medicineOverviewView.visibility = View.GONE
        binding.medicineKnowledgeView.medicineKnowledgeView.visibility = View.VISIBLE
        binding.medicinePriceView.medicinePriceView.visibility = View.GONE
        binding.whatIsItTextView.setTextColor(resources.getColor(R.color.medicine_tabs_selected_color))
        binding.whatIsItunderline.setBackgroundColor(resources.getColor(R.color.medicine_tabs_selected_color))
        binding.overviewTextView.setTextColor(resources.getColor(R.color.black))
        binding.overviewunderline.setBackgroundColor(resources.getColor(R.color.medicine_tabs_non_selected_color))
        binding.pricingTextView.setTextColor(resources.getColor(R.color.black))
        binding.pricingwunderline.setBackgroundColor(resources.getColor(R.color.medicine_tabs_non_selected_color))
        binding.overviewTextView.setOnClickListener(this)
        binding.pricingTextView.setOnClickListener(this)
        binding.whatIsItTextView.setOnClickListener(null)
        binding.medicineKnowledgeView.containerLayout.removeAllViews()
        binding.medicinePriceView.priceContainerLayout.removeAllViews()
        val request = MedicationKnowledgeRequest.Builder()
            .compositionId(medicationId)
            .build()
        medicinesViewModel.getMedicationKnowledge(request)
        viewLifecycleOwner.lifecycleScope.launch {
            medicinesViewModel.medicationKnowledgeResults.take(1).collect { result ->
                if (result != null) {
                    when (result) {
                        is BWellResult.ResourceCollection -> {
                            val dataList = result.data
                            for (i in 0 until (dataList?.size ?: 0)) {
                                val titleTextView = TextView(requireContext())
                                titleTextView.text = dataList?.get(i)?.title
                                titleTextView.textSize = 18f
                                titleTextView.setTextColor(resources.getColor(R.color.black))
                                binding.medicineKnowledgeView.containerLayout.addView(titleTextView)
                                val webView = WebView(requireContext())
                                webView.loadDataWithBaseURL(null,
                                    dataList?.get(i)?.content.toString(), "text/html", "utf-8", null)
                                binding.medicineKnowledgeView.containerLayout.addView(webView)
                            }
                        }
                        else -> {}
                    }
                }
            }
        }
    }
}
