package com.bwell.sampleapp.activities.ui.healthsummary

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bwell.common.models.domain.common.Coding
import com.bwell.common.models.domain.healthdata.healthsummary.condition.Condition
import com.bwell.common.models.domain.healthdata.healthsummary.healthsummary.enums.HealthSummaryCategory
import com.bwell.common.models.requests.searchtoken.SearchDate
import com.bwell.common.models.responses.BWellResult
import com.bwell.healthdata.healthsummary.requests.condition.ConditionRequest
import com.bwell.sampleapp.BWellSampleApplication
import com.bwell.sampleapp.R
import com.bwell.sampleapp.databinding.ConditionDetailViewBinding
import com.bwell.sampleapp.utils.formatDate
import com.bwell.sampleapp.viewmodel.HealthSummaryViewModel
import com.bwell.sampleapp.viewmodel.HealthSummaryViewModelFactory
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class ConditionDetailFragment : Fragment(), View.OnClickListener {

    private var _binding: ConditionDetailViewBinding? = null
    private lateinit var healthSummaryViewModel: HealthSummaryViewModel
    private lateinit var conditionId: String
    private lateinit var groupCode: String
    private lateinit var groupSystem: String
    private lateinit var name: String
    private var from: String? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ConditionDetailViewBinding.inflate(inflater, container, false)
        val root: View = binding.root
        val repository = (activity?.application as? BWellSampleApplication)?.healthSummaryRepository
        healthSummaryViewModel = ViewModelProvider(this, HealthSummaryViewModelFactory(repository))[HealthSummaryViewModel::class.java]
        binding.leftArrowImageView.setOnClickListener(this)
        conditionId = arguments?.getString("id").toString()
        groupCode = arguments?.getString("groupCode").toString()
        groupSystem = arguments?.getString("groupSystem").toString()
        name = arguments?.getString("name").toString()
        from = arguments?.getString("from")

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
                val parentFrag: HealthSummaryFragment = this@ConditionDetailFragment.parentFragment as HealthSummaryFragment
                parentFrag.showHealthSummaryList()
            }
            R.id.overviewTextView -> {
                showOverView()
            }
        }
    }

    private fun showOverView() {
        binding.conditionOverviewView.conditionOverviewView.visibility = View.VISIBLE
        binding.overviewTextView.setTextColor(resources.getColor(R.color.medicine_tabs_selected_color))
        binding.overviewunderline.setBackgroundColor(resources.getColor(R.color.medicine_tabs_selected_color))
        binding.overviewTextView.setOnClickListener(null)

        val lastUpdatedSearchDate = SearchDate.Builder()
            .greaterThan(SimpleDateFormat("yyyy-MM-dd").parse("2020-01-12"))
            .build()

        val request = ConditionRequest.Builder()
            .groupCode(listOf(Coding(code = groupCode, system = groupSystem)))
            .lastUpdated(lastUpdatedSearchDate)
            .build()

        healthSummaryViewModel.getHealthSummaryData(request, category = HealthSummaryCategory.CONDITION)
        viewLifecycleOwner.lifecycleScope.launch {
            healthSummaryViewModel.healthSummaryResults.collect { result ->
                if (result != null && result is BWellResult.ResourceCollection<*>) {
                    val condition = (result.data as? List<Condition>)?.firstOrNull()
                    binding.conditionOverviewView.conditionTitleTextView.text = name
                    binding.conditionOverviewView.severityValueTextView.text = condition?.severity?.coding?.firstOrNull()?.display ?: condition?.severity?.coding?.firstOrNull()?.code?.capitalize(
                        Locale.ROOT)
                    binding.conditionOverviewView.clinicalStatusValueTextView.text = condition?.clinicalStatus?.coding?.firstOrNull()?.display ?: condition?.clinicalStatus?.coding?.firstOrNull()?.code?.capitalize(
                        Locale.ROOT)
                    binding.conditionOverviewView.recordedValueTextView.text = formatDate(condition?.recordedDate?.toString())
                    binding.conditionOverviewView.recorderValueTextView.text = condition?.recorder?.name?.firstOrNull()?.text
                    if (!from.isNullOrBlank()) {
                        binding.conditionOverviewView.organizationName.text = "from " + from.toString()
                    } else {
                        binding.conditionOverviewView.organizationLl.visibility = View.GONE
                    }
                }
            }
        }
    }
}