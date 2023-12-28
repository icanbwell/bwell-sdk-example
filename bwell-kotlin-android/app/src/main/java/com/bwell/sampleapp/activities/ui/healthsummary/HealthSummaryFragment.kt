package com.bwell.sampleapp.activities.ui.healthsummary

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bwell.common.models.domain.common.Period
import com.bwell.common.models.domain.healthdata.healthsummary.allergyintolerance.AllergyIntoleranceClinicalStatus
import com.bwell.common.models.domain.healthdata.healthsummary.allergyintolerance.AllergyIntoleranceCriticality
import com.bwell.common.models.domain.healthdata.healthsummary.allergyintolerance.AllergyIntoleranceSeverity
import com.bwell.common.models.domain.healthdata.healthsummary.allergyintolerance.AllergyIntoleranceType
import com.bwell.common.models.domain.healthdata.healthsummary.communication.CommunicationStatus
import com.bwell.common.models.domain.healthdata.healthsummary.condition.ConditionVerificationStatus
import com.bwell.common.models.domain.healthdata.healthsummary.encounter.EncounterStatus
import com.bwell.common.models.domain.healthdata.healthsummary.immunization.ImmunizationStatus
import com.bwell.common.models.domain.healthdata.healthsummary.procedure.ProcedureStatus
import com.bwell.common.models.domain.healthdata.observation.ObservationStatus
import com.bwell.common.models.responses.BWellResult
import com.bwell.healthdata.healthsummary.allergyintolerance.AllergyIntoleranceRequest
import com.bwell.healthdata.healthsummary.careplan.CarePlanRequest
import com.bwell.healthdata.healthsummary.communication.CommunicationRequest
import com.bwell.healthdata.healthsummary.condition.ConditionRequest
import com.bwell.healthdata.healthsummary.encounter.EncounterRequest
import com.bwell.healthdata.healthsummary.immunization.ImmunizationRequest
import com.bwell.healthdata.healthsummary.procedure.ProcedureRequest
import com.bwell.healthdata.healthsummary.vitalsign.VitalSignRequest
import com.bwell.sampleapp.BWellSampleApplication
import com.bwell.sampleapp.R
import com.bwell.sampleapp.databinding.FragmentHealthSummaryParentBinding
import com.bwell.sampleapp.model.HealthSummaryListItems
import com.bwell.sampleapp.viewmodel.HealthSummaryViewModel
import com.bwell.sampleapp.viewmodel.HealthSummaryViewModelFactory
import kotlinx.coroutines.launch
import com.bwell.sampleapp.viewmodel.SharedViewModelFactory

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
            if(selectedList.category.toString().equals(resources.getString(R.string.care_plans)))
            {
                healthSummaryRequest = CarePlanRequest.Builder()
                    .category(selectedList.category)
                    .date(selectedList.date)
                    .build()
            }else if(selectedList.category.toString().equals(resources.getString(R.string.immunizations)))
            {
                val date = selectedList.date
                val status = ImmunizationStatus.COMPLETED
                val vaccineCode = ""
                val page = "0"
                healthSummaryRequest = ImmunizationRequest.Builder()
                    .date(date)
                    .status(status)
                    .vaccineCode(vaccineCode)
                    .page(page)
                    .build()
            }else if(selectedList.category.toString().equals(resources.getString(R.string.procedures)))
            {
                val category = selectedList.category
                val code = "1"
                val date = selectedList.date
                val status = ProcedureStatus.IN_PROGRESS
                val page = "0"
                healthSummaryRequest= ProcedureRequest.Builder()
                    .category(category)
                    .code(code)
                    .date(date)
                    .status(status)
                    .page(page)
                    .build()
            }else if(selectedList.category.toString().equals(resources.getString(R.string.vitals)))
            {
                val code = "1"
                val ids = listOf("1")
                val componentCode = "1"
                val date = selectedList.date
                val status = ObservationStatus.FINAL
                val page = "0"
                healthSummaryRequest = VitalSignRequest.Builder()
                    .code(code)
                    .ids(ids)
                    .componentCode(componentCode)
                    .date(date)
                    .status(status)
                    .page(page)
                    .build()
            }else if(selectedList.category.toString() == resources.getString(R.string.visit_history))
            {
                val `class` = ""
                val date = selectedList.date
                val reasonCode = ""
                val reasonReference = ""
                val status = EncounterStatus.PLANNED
                val type = ""
                val page = "0"
                healthSummaryRequest = EncounterRequest.Builder()
                    .`class`(`class`)
                    .date(date)
                    .reasonCode(reasonCode)
                    .reasonReference(reasonReference)
                    .status(status)
                    .type(type)
                    .page(page)
                    .build()
            }
            else if(selectedList.category.toString() == resources.getString(R.string.allergies))
            {
                val clinicalStatus = AllergyIntoleranceClinicalStatus.ACTIVE
                val date = selectedList.date
                val lastDate = selectedList.date
                val severity = AllergyIntoleranceSeverity.SEVERE
                val type = AllergyIntoleranceType.ALLERGY
                val criticality = AllergyIntoleranceCriticality.LOW
                val page = "0"
                healthSummaryRequest = AllergyIntoleranceRequest.Builder()
                    .clinicalStatus(clinicalStatus)
                    .date(date)
                    .lastDate(lastDate)
                    .severity(severity)
                    .type(type)
                    .criticality(criticality)
                    .page(page)
                    .build()
            }else if(selectedList.category.toString().equals(resources.getString(R.string.communications)))
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
                val category = selectedList.category
                val clinicalStatus = ""
                val onsetDate = Period.Builder().start("2023-01-01").end("2023-12-31").build()
                val recordedDate = selectedList.date
                val severity = ""
                val verificationStatus = ConditionVerificationStatus.CONFIRMED
                val page = "0"
                healthSummaryRequest = ConditionRequest.Builder()
                    .category(category)
                    .clinicalStatus(clinicalStatus)
                    .onsetDate(onsetDate)
                    .recordedDate(recordedDate)
                    .severity(severity)
                    .verificationStatus(verificationStatus)
                    .page(page)
                    .build()
            }
            healthSummaryViewModel.getHealthSummaryData(healthSummaryRequest,selectedList.category)

            viewLifecycleOwner.lifecycleScope.launch {
                healthSummaryViewModel.healthSummaryResults.collect { result ->
                    Log.d("result", "result$result")
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