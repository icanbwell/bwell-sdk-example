package com.bwell.sampleapp.activities.ui.healthsummary

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
import com.bwell.common.models.domain.healthdata.healthsummary.immunization.ImmunizationStatus
import com.bwell.common.models.responses.BWellResult
import com.bwell.healthdata.healthsummary.careplan.CarePlanRequest
import com.bwell.healthdata.healthsummary.immunization.ImmunizationRequest
import com.bwell.sampleapp.BWellSampleApplication
import com.bwell.sampleapp.R
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
            lateinit var  healthSummaryRequest: Any
            if(selectedList.category.toString().equals(resources.getString(R.string.care_plans)))
            {
                healthSummaryRequest = CarePlanRequest.Builder()
                    .category(selectedList.category)
                    .date(selectedList.date)
                    .build()
            }else if(selectedList.category.toString().equals(resources.getString(R.string.immunizations)))
            {
                val date = Period.Builder().start("2023-01-01").build()
                val status = ImmunizationStatus.COMPLETED
                val vaccineCode = ""
                val page = "0"
                healthSummaryRequest = ImmunizationRequest.Builder()
                    .date(date)
                    .status(status)
                    .vaccineCode(vaccineCode)
                    .page(page)
                    .build()
            }
            healthSummaryViewModel.getHealthSummaryData(healthSummaryRequest,selectedList.category)

            viewLifecycleOwner.lifecycleScope.launch {
                healthSummaryViewModel.healthSummaryResults.collect { result ->
                    Log.d("result","result"+result)
                    if (result != null) {
                        setDataAdapter(result,selectedList.category.toString())
                    }
                }
            }
        }
        binding.healthSummaryCategoriesView.rvHealthSummary.layoutManager = LinearLayoutManager(requireContext())
        binding.healthSummaryCategoriesView.rvHealthSummary.adapter = adapter
    }

    private fun <T> setDataAdapter(result: BWellResult<T>,category:String?) {
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