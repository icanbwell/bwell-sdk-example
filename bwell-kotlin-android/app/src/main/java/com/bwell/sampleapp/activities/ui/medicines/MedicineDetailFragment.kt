package com.bwell.sampleapp.activities.ui.medicines

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bwell.common.models.domain.common.Coding
import com.bwell.common.models.domain.healthdata.medication.MedicationDispense
import com.bwell.common.models.domain.healthdata.medication.MedicationGroup
import com.bwell.common.models.domain.healthdata.medication.MedicationPricing
import com.bwell.common.models.domain.healthdata.medication.MedicationStatement
import com.bwell.common.models.requests.searchtoken.SearchDate
import com.bwell.common.models.responses.BWellResult
import com.bwell.healthdata.medication.requests.MedicationDispenseRequest
import com.bwell.healthdata.medication.requests.MedicationKnowledgeRequest
import com.bwell.healthdata.medication.requests.MedicationPricingRequest
import com.bwell.healthdata.medication.requests.MedicationRequestRequest
import com.bwell.healthdata.medication.requests.MedicationStatementsRequest
import com.bwell.sampleapp.BWellSampleApplication
import com.bwell.sampleapp.R
import com.bwell.sampleapp.databinding.MedicineDetailViewBinding
import com.bwell.sampleapp.utils.formatDate
import com.bwell.sampleapp.viewmodel.MedicineViewModelFactory
import com.bwell.sampleapp.viewmodel.MedicinesViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.log

class MedicineDetailFragment : Fragment(),View.OnClickListener {

    private var _binding: MedicineDetailViewBinding? = null
    private lateinit var medicinesViewModel: MedicinesViewModel
    private lateinit var medicationId: String
    private lateinit var groupCode: String
    private lateinit var groupSystem: String
    private lateinit var name: String
    private var from: String? = null

    private var medicationStatementId: String? = null

    private lateinit var medicationPricingListAdapter: MedicationPricingListAdapter


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
        medicationId = arguments?.getString("id").toString()
        groupCode = arguments?.getString("groupCode").toString()
        groupSystem = arguments?.getString("groupSystem").toString()
        name = arguments?.getString("name").toString()
        from = arguments?.getString("from")


        /**
         * Calling the medication pricing and medication dispense
         */
        val medicationDispenseRequest = MedicationDispenseRequest.Builder()
            .build()
        val medicationRequestRequest = MedicationRequestRequest.Builder()
            .build()
        medicinesViewModel.getMedicationDispense(medicationDispenseRequest)
        medicinesViewModel.getMedicationRequest(medicationRequestRequest)



        viewLifecycleOwner.lifecycleScope.launch {
            launch {
                medicinesViewModel.medicationRequestResults.collect { result ->
                    when(result) {
                        is BWellResult.ResourceCollection -> {
                            Log.i("MedicationRequest", result.toString())
                        }

                        else -> {
                            Log.i("Medication", "MedicationRequest didn't return BwellResult.ResourceCollection")
                        }
                    }
                }
            }

            launch {
                medicinesViewModel.medicationDispenseResults.collect { result ->
                    when(result) {
                        is BWellResult.ResourceCollection -> {
                            Log.i("Medication", result.toString())
                        }

                        else -> {
                            Log.i("Medication", "MedicationDispense didn't return BwellResult.ResourceCollection")
                        }
                    }
                }
            }
        }

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
            R.id.whatIsItTextView -> {
               showKnowledgeView()
            }
            R.id.pricingTextView -> {
                showPricingView()
            }
        }
    }

    private fun showOverView() {
        binding.medicineOverviewView.medicineOverviewView.visibility = View.VISIBLE
        binding.medicineKnowledgeView.medicineKnowledgeView.visibility = View.GONE
        binding.medicinePricingView.rvPricingView.visibility = View.GONE

        binding.whatIsItTextView.setTextColor(resources.getColor(R.color.black))
        binding.whatIsItunderline.setBackgroundColor(resources.getColor(R.color.medicine_tabs_non_selected_color))
        binding.overviewTextView.setTextColor(resources.getColor(R.color.medicine_tabs_selected_color))
        binding.overviewunderline.setBackgroundColor(resources.getColor(R.color.medicine_tabs_selected_color))
        binding.pricingTextView.setTextColor(resources.getColor(R.color.black))
        binding.pricingUnderline.setBackgroundColor(resources.getColor(R.color.medicine_tabs_non_selected_color))

        binding.overviewTextView.setOnClickListener(null)
        binding.whatIsItTextView.setOnClickListener(this)
        binding.pricingTextView.setOnClickListener(this)
        binding.medicineKnowledgeView.containerLayout.removeAllViews()
        binding.medicinePricingView.rvPricingView.removeAllViews()

        val lastUpdatedSearchDate = SearchDate.Builder()
            .greaterThan(SimpleDateFormat("yyyy-MM-dd").parse("2020-02-12"))
            .build()

        val medicationStatementsRequest = MedicationStatementsRequest.Builder()
            .groupCode(listOf(Coding(code = groupCode, system = groupSystem)))
            .lastUpdated(lastUpdatedSearchDate)
            .build()

        medicinesViewModel.getMedicationStatements(medicationStatementsRequest)
        viewLifecycleOwner.lifecycleScope.launch {
            medicinesViewModel.medicationStatementsResults.collect { result ->
                if (result != null && result is BWellResult.ResourceCollection<MedicationStatement>) {
                    val medicationStatement = result.data?.firstOrNull()
                    medicationStatementId = medicationStatement?.id
                    binding.medicineOverviewView.medicineTitleTextView.text = name
                    binding.medicineOverviewView.medicationValueTextView.text = medicationStatement?.medication?.coding?.firstOrNull()?.display ?: medicationStatement?.medication?.coding?.firstOrNull()?.code?.capitalize(
                        Locale.ROOT)
                    binding.medicineOverviewView.statusValueTextView.text = medicationStatement?.status?.display
                    binding.medicineOverviewView.startDateValueTextView.text = formatDate(medicationStatement?.effectiveDate?.start.toString())
                    binding.medicineOverviewView.endDateValueTextView.text = formatDate(medicationStatement?.effectiveDate?.end.toString())
                    binding.medicineOverviewView.organizationName.text = "from " + medicationStatement?.requester?.toString()
                    if (!from.isNullOrBlank()) {
                        binding.medicineOverviewView.organizationName.text = "from " + from.toString()
                    } else {
                        binding.medicineOverviewView.organizationLl.visibility = View.GONE
                    }
                }
            }
        }
    }

    private fun showKnowledgeView() {
        binding.medicineOverviewView.medicineOverviewView.visibility = View.GONE
        binding.medicineKnowledgeView.medicineKnowledgeView.visibility = View.VISIBLE
        binding.medicinePricingView.rvPricingView.visibility = View.GONE

        binding.whatIsItTextView.setTextColor(resources.getColor(R.color.medicine_tabs_selected_color))
        binding.whatIsItunderline.setBackgroundColor(resources.getColor(R.color.medicine_tabs_selected_color))
        binding.overviewTextView.setTextColor(resources.getColor(R.color.black))
        binding.overviewunderline.setBackgroundColor(resources.getColor(R.color.medicine_tabs_non_selected_color))
        binding.pricingTextView.setTextColor(resources.getColor(R.color.black))
        binding.pricingUnderline.setBackgroundColor(resources.getColor(R.color.medicine_tabs_non_selected_color))


        binding.overviewTextView.setOnClickListener(this)
        binding.whatIsItTextView.setOnClickListener(null)
        binding.pricingTextView.setOnClickListener(this)

        binding.medicineKnowledgeView.containerLayout.removeAllViews()
        binding.medicinePricingView.rvPricingView.removeAllViews()

        val request = MedicationKnowledgeRequest.Builder()
            .medicationStatementId(medicationStatementId?:medicationId)
            .build()
        medicinesViewModel.getMedicationKnowledge(request)
        viewLifecycleOwner.lifecycleScope.launch {
            medicinesViewModel.medicationKnowledgeResults.collect { result ->
                if (result != null) {
                    when (result) {
                        is BWellResult.ResourceCollection -> {
                            val dataList = result.data
                            for (i in 0 until (dataList?.size ?: 0)) {
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

    private fun setMedicinesGroupsAdapter(result:BWellResult<MedicationPricing>) {
        when (result) {
            is BWellResult.ResourceCollection -> {
                val dataList = result.data
                medicationPricingListAdapter = MedicationPricingListAdapter(dataList)
                binding.medicinePricingView.rvPricingView.layoutManager =
                    LinearLayoutManager(requireContext())
                binding.medicinePricingView.rvPricingView.adapter = medicationPricingListAdapter
            }

            else -> {}
        }
    }



        private fun showPricingView() {
        binding.medicineOverviewView.medicineOverviewView.visibility = View.GONE
        binding.medicineKnowledgeView.medicineKnowledgeView.visibility = View.GONE
        binding.medicinePricingView.rvPricingView.visibility = View.VISIBLE

        binding.pricingTextView.setTextColor(resources.getColor(R.color.medicine_tabs_selected_color))
        binding.pricingUnderline.setBackgroundColor(resources.getColor(R.color.medicine_tabs_selected_color))

        binding.overviewTextView.setTextColor(resources.getColor(R.color.black))
        binding.overviewunderline.setBackgroundColor(resources.getColor(R.color.medicine_tabs_non_selected_color))
        binding.whatIsItTextView.setTextColor(resources.getColor(R.color.black))
        binding.whatIsItunderline.setBackgroundColor(resources.getColor(R.color.medicine_tabs_non_selected_color))

        binding.overviewTextView.setOnClickListener(this)
        binding.whatIsItTextView.setOnClickListener(this)
        binding.pricingTextView.setOnClickListener(null)

        binding.medicinePricingView.rvPricingView.removeAllViews()
        val request = MedicationPricingRequest.Builder()
            .medicationStatementId(medicationStatementId?:medicationId)
            .build()
        medicinesViewModel.getMedicationPricing(request)
        viewLifecycleOwner.lifecycleScope.launch {
            medicinesViewModel.medicationPricingResults.collect { result ->
                if (result != null) {
                    when (result) {
                        is BWellResult.ResourceCollection -> {
                            setMedicinesGroupsAdapter(result)
                        }

                        else -> {}
                    }
                }
            }
        }
    }

    }
