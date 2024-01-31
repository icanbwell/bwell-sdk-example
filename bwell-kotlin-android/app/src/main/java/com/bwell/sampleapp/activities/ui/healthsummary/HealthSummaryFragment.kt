package com.bwell.sampleapp.activities.ui.healthsummary

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bwell.common.models.responses.BWellResult
import com.bwell.healthdata.healthsummary.requests.allergyintolerance.AllergyIntoleranceGroupsRequest
import com.bwell.healthdata.healthsummary.requests.careplan.CarePlanGroupsRequest
import com.bwell.healthdata.healthsummary.requests.condition.ConditionGroupsRequest
import com.bwell.healthdata.healthsummary.requests.encounter.EncounterGroupsRequest
import com.bwell.healthdata.healthsummary.requests.immunization.ImmunizationGroupsRequest
import com.bwell.healthdata.healthsummary.requests.procedure.ProcedureGroupsRequest
import com.bwell.healthdata.healthsummary.requests.vitalsign.VitalSignGroupsRequest
import com.bwell.sampleapp.BWellSampleApplication
import com.bwell.sampleapp.R
import com.bwell.sampleapp.databinding.FragmentHealthSummaryParentBinding
import com.bwell.sampleapp.model.HealthSummaryListItems
import com.bwell.sampleapp.viewmodel.HealthSummaryViewModel
import com.bwell.sampleapp.viewmodel.HealthSummaryViewModelFactory
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.take

class HealthSummaryFragment : Fragment(), View.OnClickListener {

    private var _binding: FragmentHealthSummaryParentBinding? = null

    private val binding get() = _binding!!
    private lateinit var healthSummaryViewModel: HealthSummaryViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHealthSummaryParentBinding.inflate(inflater, container, false)
        val root: View = binding.root
        val repository = (activity?.application as? BWellSampleApplication)?.healthSummaryRepository
        healthSummaryViewModel = ViewModelProvider(this, HealthSummaryViewModelFactory(repository))[HealthSummaryViewModel::class.java]
        healthSummaryViewModel.healthSummaryData.observe(viewLifecycleOwner) {
            setHealthSummaryAdapter(it.healthSummaryList)
        }
        binding.healthSummaryCategoriesDataView.leftArrowImageView.setOnClickListener(this)
        return root
    }

    private fun setHealthSummaryAdapter(suggestedActivitiesLIst: List<HealthSummaryListItems>) {
        val adapter = HealthSummaryListAdapter(suggestedActivitiesLIst)
        adapter.onItemClicked = { selectedList ->
            binding.healthSummaryCategoriesView.healthSummaryCategoriesView.visibility = View.GONE
            binding.healthSummaryCategoriesDataView.healthSummaryCategoriesDataView.visibility = View.VISIBLE
            lateinit var  healthSummaryRequest: Any
            if(selectedList.category.toString() == resources.getString(R.string.care_plans))
            {
                healthSummaryRequest = CarePlanGroupsRequest.Builder()
                    .build()
            }else if(selectedList.category.toString() == resources.getString(R.string.immunizations))
            {
                healthSummaryRequest = ImmunizationGroupsRequest.Builder()
                    .build()
            }else if(selectedList.category.toString() == resources.getString(R.string.procedures))
            {
                healthSummaryRequest= ProcedureGroupsRequest.Builder()
                    .build()
            }else if(selectedList.category.toString() == resources.getString(R.string.vitals))
            {
                healthSummaryRequest = VitalSignGroupsRequest.Builder()
                    .build()
            }else if(selectedList.category.toString() == resources.getString(R.string.visit_history))
            {
                healthSummaryRequest = EncounterGroupsRequest.Builder()
                    .build()
            }
            else if(selectedList.category.toString() == resources.getString(R.string.allergies))
            {
                healthSummaryRequest = AllergyIntoleranceGroupsRequest.Builder()
                    .build()
            }
            else if(selectedList.category.toString() == resources.getString(R.string.conditions)) {
                healthSummaryRequest = ConditionGroupsRequest.Builder()
                    .build()
            }

            healthSummaryViewModel.getHealthSummaryData(healthSummaryRequest,selectedList.category)

            viewLifecycleOwner.lifecycleScope.launch {
                healthSummaryViewModel.healthSummaryResults.take(1).collect { result ->
                    if (result != null) {
                        setDataAdapter(result,selectedList.category.toString())
                    }
                }
            }
        }
        binding.healthSummaryCategoriesView.rvHealthSummary.layoutManager = LinearLayoutManager(requireContext())
        binding.healthSummaryCategoriesView.rvHealthSummary.adapter = adapter
    }

    @SuppressLint("SetTextI18n")
    private fun <T> setDataAdapter(result: BWellResult<T>, category:String?) {
        when (result) {
            is BWellResult.ResourceCollection -> {
                val dataList = result.data
                val adapter = HealthSummaryCategoriesDataAdapter(dataList)
                binding.healthSummaryCategoriesDataView.rvHealthSummaryCategories.layoutManager = LinearLayoutManager(requireContext())
                binding.healthSummaryCategoriesDataView.rvHealthSummaryCategories.adapter = adapter
                binding.healthSummaryCategoriesDataView.titleTextView.text = category+" ("+dataList?.size+")"
            }
            else -> {}
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.leftArrowImageView -> {
                binding.healthSummaryCategoriesView.healthSummaryCategoriesView.visibility = View.VISIBLE
                binding.healthSummaryCategoriesDataView.healthSummaryCategoriesDataView.visibility = View.GONE
            }
        }
    }
}