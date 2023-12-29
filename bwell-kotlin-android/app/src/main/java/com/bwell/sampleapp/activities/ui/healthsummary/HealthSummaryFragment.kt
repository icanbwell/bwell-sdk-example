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
import com.bwell.common.models.domain.healthdata.healthsummary.communication.enums.CommunicationStatus
import com.bwell.common.models.responses.BWellResult
import com.bwell.healthdata.healthsummary.allergyintolerance.AllergyIntoleranceCompositeRequest
import com.bwell.healthdata.healthsummary.careplan.CarePlanCompositeRequest
import com.bwell.healthdata.healthsummary.communication.CommunicationRequest
import com.bwell.healthdata.healthsummary.condition.ConditionCompositeRequest
import com.bwell.healthdata.healthsummary.encounter.EncounterCompositeRequest
import com.bwell.healthdata.healthsummary.immunization.ImmunizationCompositeRequest
import com.bwell.healthdata.healthsummary.procedure.ProcedureCompositeRequest
import com.bwell.healthdata.healthsummary.vitalsign.VitalSignCompositeRequest
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
                healthSummaryRequest = CarePlanCompositeRequest.Builder()
                    .build()
            }else if(selectedList.category.toString() == resources.getString(R.string.immunizations))
            {
                healthSummaryRequest = ImmunizationCompositeRequest.Builder()
                    .build()
            }else if(selectedList.category.toString() == resources.getString(R.string.procedures))
            {
                healthSummaryRequest= ProcedureCompositeRequest.Builder()
                    .build()
            }else if(selectedList.category.toString() == resources.getString(R.string.vitals))
            {
                healthSummaryRequest = VitalSignCompositeRequest.Builder()
                    .build()
            }else if(selectedList.category.toString() == resources.getString(R.string.visit_history))
            {
                healthSummaryRequest = EncounterCompositeRequest.Builder()
                    .build()
            }
            else if(selectedList.category.toString() == resources.getString(R.string.allergies))
            {
                healthSummaryRequest = AllergyIntoleranceCompositeRequest.Builder()
                    .build()
            }else if(selectedList.category.toString() == resources.getString(R.string.communications))
            {
                val basedOn = ""
                val category = selectedList.category
                val encounter = ""
                val identifier = ""
                val medium = ""
                val partOf = ""
                val patient = ""
                val received = selectedList.date
                val recipient = ""
                val sender = ""
                val sent = selectedList.date
                val status = CommunicationStatus.COMPLETED
                val subject = ""
                val page = "0"
                healthSummaryRequest = CommunicationRequest.Builder()
                    .basedOn(basedOn)
                    .category(category)
                    .encounter(encounter)
                    .identifier(identifier)
                    .medium(medium)
                    .partOf(partOf)
                    .patient(patient)
                    .received(received)
                    .recipient(recipient)
                    .sender(sender)
                    .sent(sent)
                    .status(status)
                    .subject(subject)
                    .page(page)
                    .build()
            }else if(selectedList.category.toString() == resources.getString(R.string.conditions))
            {
                healthSummaryRequest = ConditionCompositeRequest.Builder()
                    .page(1)
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