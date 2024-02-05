package com.bwell.sampleapp.activities.ui.medicines

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bwell.common.models.domain.common.Coding
import com.bwell.common.models.domain.healthdata.medication.MedicationStatement
import com.bwell.common.models.responses.BWellResult
import com.bwell.healthdata.medication.requests.MedicationKnowledgeRequest
import com.bwell.healthdata.medication.requests.MedicationStatementsRequest
import com.bwell.sampleapp.BWellSampleApplication
import com.bwell.sampleapp.R
import com.bwell.sampleapp.databinding.MedicineDetailViewBinding
import com.bwell.sampleapp.utils.formatDate
import com.bwell.sampleapp.viewmodel.MedicineViewModelFactory
import com.bwell.sampleapp.viewmodel.MedicinesViewModel
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch

class MedicineDetailFragment : Fragment(),View.OnClickListener {

    private var _binding: MedicineDetailViewBinding? = null
    private lateinit var medicinesViewModel: MedicinesViewModel
    private lateinit var medicationId: String
    private lateinit var groupCode: String
    private lateinit var groupSystem: String
    private lateinit var name: String

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
        }
    }

    private fun showOverView() {
        binding.medicineOverviewView.medicineOverviewView.visibility = View.VISIBLE
        binding.medicineKnowledgeView.medicineKnowledgeView.visibility = View.GONE
        binding.whatIsItTextView.setTextColor(resources.getColor(R.color.black))
        binding.whatIsItunderline.setBackgroundColor(resources.getColor(R.color.medicine_tabs_non_selected_color))
        binding.overviewTextView.setTextColor(resources.getColor(R.color.medicine_tabs_selected_color))
        binding.overviewunderline.setBackgroundColor(resources.getColor(R.color.medicine_tabs_selected_color))
        binding.overviewTextView.setOnClickListener(null)
        binding.whatIsItTextView.setOnClickListener(this)
        binding.medicineKnowledgeView.containerLayout.removeAllViews()

        val medicationStatementsRequest = MedicationStatementsRequest.Builder()
            .groupCode(listOf(Coding(code = groupCode, system = groupSystem)))
            .build()

        medicinesViewModel.getMedicationStatements(medicationStatementsRequest)
        viewLifecycleOwner.lifecycleScope.launch {
            medicinesViewModel.medicationStatementsResults.collect { result ->
                println("MEDICATION_STATEMENT: result: $result")
                if (result != null && result is BWellResult.SingleResource<MedicationStatement>) {
                    val medicationStatement = result.data
                    binding.medicineOverviewView.medicineTitleTextView.text = name
                    binding.medicineOverviewView.medicationValueTextView.text = medicationStatement?.medication?.text
                    binding.medicineOverviewView.statusValueTextView.text = medicationStatement?.status?.display
                    binding.medicineOverviewView.startDateValueTextView.text = formatDate(medicationStatement?.effectiveDate?.start.toString())
                    binding.medicineOverviewView.endDateValueTextView.text = formatDate(medicationStatement?.effectiveDate?.end.toString())
                    binding.medicineOverviewView.organizationName.text = "from " + medicationStatement?.requester?.toString()
                }
            }
        }
    }

    private fun showKnowledgeView() {
        binding.medicineOverviewView.medicineOverviewView.visibility = View.GONE
        binding.medicineKnowledgeView.medicineKnowledgeView.visibility = View.VISIBLE
        binding.whatIsItTextView.setTextColor(resources.getColor(R.color.medicine_tabs_selected_color))
        binding.whatIsItunderline.setBackgroundColor(resources.getColor(R.color.medicine_tabs_selected_color))
        binding.overviewTextView.setTextColor(resources.getColor(R.color.black))
        binding.overviewunderline.setBackgroundColor(resources.getColor(R.color.medicine_tabs_non_selected_color))
        binding.overviewTextView.setOnClickListener(this)
        binding.whatIsItTextView.setOnClickListener(null)
        binding.medicineKnowledgeView.containerLayout.removeAllViews()
        val request = MedicationKnowledgeRequest.Builder()
            .medicationStatementId(medicationId)
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
}
