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
import com.bwell.common.models.domain.healthdata.healthsummary.allergyintolerance.AllergyIntolerance
import com.bwell.common.models.domain.healthdata.healthsummary.allergyintolerance.AllergyIntoleranceGroup
import com.bwell.common.models.domain.healthdata.healthsummary.careplan.CarePlanGroup
import com.bwell.common.models.domain.healthdata.healthsummary.condition.ConditionGroup
import com.bwell.common.models.domain.healthdata.healthsummary.encounter.EncounterGroup
import com.bwell.common.models.domain.healthdata.healthsummary.healthsummary.enums.HealthSummaryCategory
import com.bwell.common.models.domain.healthdata.healthsummary.immunization.ImmunizationGroup
import com.bwell.common.models.domain.healthdata.healthsummary.procedure.ProcedureGroup
import com.bwell.common.models.domain.healthdata.healthsummary.vitalsign.VitalSignGroup
import com.bwell.common.models.domain.healthdata.medication.MedicationGroup
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
import com.bwell.sampleapp.activities.ui.medicines.MedicineDetailFragment
import com.bwell.sampleapp.databinding.FragmentHealthSummaryParentBinding
import com.bwell.sampleapp.model.HealthSummaryListItems
import com.bwell.sampleapp.viewmodel.HealthSummaryViewModel
import com.bwell.sampleapp.viewmodel.HealthSummaryViewModelFactory
import kotlinx.coroutines.launch

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

            val  healthSummaryRequest: Any? = when (selectedList.category) {
                HealthSummaryCategory.CARE_PLAN -> {
                    CarePlanGroupsRequest.Builder().build()
                }
                HealthSummaryCategory.IMMUNIZATION -> {
                    ImmunizationGroupsRequest.Builder().build()
                }
                HealthSummaryCategory.PROCEDURE -> {
                    ProcedureGroupsRequest.Builder().build()
                }
                HealthSummaryCategory.VITAL_SIGNS -> {
                    VitalSignGroupsRequest.Builder().build()
                }
                HealthSummaryCategory.ENCOUNTER -> {
                    EncounterGroupsRequest.Builder().build()
                }
                HealthSummaryCategory.ALLERGY_INTOLERANCE -> {
                    AllergyIntoleranceGroupsRequest.Builder().build()
                }
                HealthSummaryCategory.CONDITION -> {
                    ConditionGroupsRequest.Builder().build()
                }
                else -> {
                    null
                }
            }

            if(healthSummaryRequest != null) {
                binding.healthSummaryCategoriesDataView.titleTextView.text = selectedList.categoryFriendlyName + " (" + selectedList.count + ")"
                healthSummaryViewModel.getHealthSummaryGroupData(
                    healthSummaryRequest,
                    selectedList.category
                )

                viewLifecycleOwner.lifecycleScope.launch {
                    healthSummaryViewModel.healthSummaryGroupResults.collect { result ->
                        if (result != null) {
                            setDataAdapter(result)
                        }
                    }
                }
            }
        }

        binding.healthSummaryCategoriesView.rvHealthSummary.layoutManager = LinearLayoutManager(requireContext())
        binding.healthSummaryCategoriesView.rvHealthSummary.adapter = adapter
    }

    @SuppressLint("SetTextI18n")
    private fun <T> setDataAdapter(result: BWellResult<T>) {
        when (result) {
            is BWellResult.ResourceCollection -> {
                val dataList = result.data
                val adapter = HealthSummaryCategoriesDataAdapter(dataList)
                binding.healthSummaryCategoriesDataView.rvHealthSummaryCategories.layoutManager = LinearLayoutManager(requireContext())
                binding.healthSummaryCategoriesDataView.rvHealthSummaryCategories.adapter = adapter
                adapter.onItemClicked= { selectedResource ->
                    var fragment: Fragment? = null
                    when (selectedResource) {
                        is AllergyIntoleranceGroup -> {
                            fragment = AllergyIntoleranceDetailFragment()
                        }
                        is CarePlanGroup -> {
                            fragment = CarePlanDetailFragment()
                        }
                        is ConditionGroup -> {
                            fragment = ConditionDetailFragment()
                        }
                        is EncounterGroup -> {
                            fragment = EncounterDetailFragment()
                        }
                        is ImmunizationGroup -> {
                            fragment = ImmunizationDetailFragment()
                        }
                        is ProcedureGroup -> {
                            fragment = ProcedureDetailFragment()
                        }
                        is VitalSignGroup -> {
                            fragment = VitalSignDetailFragment()
                        }
                    }
                    if (fragment != null) {
                        val bundle = Bundle()
                        bundle.putString("id", adapter.getId(selectedResource))
                        bundle.putString("groupCode", adapter.getGroupCodeCode(selectedResource))
                        bundle.putString("groupSystem", adapter.getGroupCodeSystem(selectedResource))
                        bundle.putString("name", adapter.getName(selectedResource))
                        binding.healthSummaryCategoriesDataView.healthSummaryCategoriesDataView.visibility = View.GONE
                        fragment?.arguments = bundle
                        val transaction = childFragmentManager.beginTransaction()
                        binding.containerLayout.visibility = View.VISIBLE;
                        transaction.replace(R.id.container_layout, fragment)
                        transaction.addToBackStack(null)
                        transaction.commit()
                    }
                }
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

    fun showHealthSummaryList() {
        binding.healthSummaryCategoriesView.healthSummaryCategoriesView.visibility = View.VISIBLE
    }
}