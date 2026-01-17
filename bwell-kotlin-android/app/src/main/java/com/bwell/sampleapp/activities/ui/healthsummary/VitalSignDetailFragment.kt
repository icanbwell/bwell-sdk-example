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
import com.bwell.common.models.domain.healthdata.common.observation.Observation
import com.bwell.common.models.domain.healthdata.healthsummary.healthsummary.enums.HealthSummaryCategory
import com.bwell.common.models.requests.searchtoken.SearchDate
import com.bwell.common.models.responses.BWellResult
import com.bwell.healthdata.healthsummary.requests.vitalsign.VitalSignsRequest
import com.bwell.sampleapp.BWellSampleApplication
import com.bwell.sampleapp.R
import com.bwell.sampleapp.databinding.VitalSignDetailViewBinding
import com.bwell.sampleapp.utils.formatDate
import com.bwell.sampleapp.viewmodel.HealthSummaryViewModel
import com.bwell.sampleapp.viewmodel.HealthSummaryViewModelFactory
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class VitalSignDetailFragment : Fragment(), View.OnClickListener {

    private var _binding: VitalSignDetailViewBinding? = null
    private lateinit var healthSummaryViewModel: HealthSummaryViewModel
    private lateinit var vitalSignId: String
    private lateinit var groupCode: String
    private lateinit var groupSystem: String
    private lateinit var name: String

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = VitalSignDetailViewBinding.inflate(inflater, container, false)
        val root: View = binding.root
        val repository = (activity?.application as? BWellSampleApplication)?.healthSummaryRepository
        val providerResourcesRepository = (activity?.application as? BWellSampleApplication)?.providerResourcesRepository
        healthSummaryViewModel = ViewModelProvider(this, HealthSummaryViewModelFactory(repository, providerResourcesRepository))[HealthSummaryViewModel::class.java]
        binding.leftArrowImageView.setOnClickListener(this)
        vitalSignId = arguments?.getString("id").toString()
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
                val parentFrag: HealthSummaryFragment = this@VitalSignDetailFragment.parentFragment as HealthSummaryFragment
                parentFrag.showHealthSummaryList()
            }
            R.id.overviewTextView -> {
                showOverView()
            }
        }
    }

    private fun showOverView() {
        binding.vitalSignOverviewView.vitalSignOverviewView.visibility = View.VISIBLE
        binding.overviewTextView.setTextColor(resources.getColor(R.color.medicine_tabs_selected_color))
        binding.overviewunderline.setBackgroundColor(resources.getColor(R.color.medicine_tabs_selected_color))
        binding.overviewTextView.setOnClickListener(null)

        val lastUpdatedSearchDate = SearchDate.Builder()
            .greaterThan(SimpleDateFormat("yyyy-MM-dd").parse("2020-01-12"))
            .build()

        val request = VitalSignsRequest.Builder()
            .groupCode(listOf(Coding(code = groupCode, system = groupSystem)))
            .lastUpdated(lastUpdatedSearchDate)
            .build()

        healthSummaryViewModel.getHealthSummaryData(request, category = HealthSummaryCategory.VITAL_SIGNS)
        viewLifecycleOwner.lifecycleScope.launch {
            healthSummaryViewModel.healthSummaryResults.collect { result ->
                if (result != null && result is BWellResult.ResourceCollection<*>) {
                    val vitalSign = (result.data as? List<Observation>)?.firstOrNull()
                    binding.vitalSignOverviewView.vitalSignTitleTextView.text = name
                    binding.vitalSignOverviewView.codeValueTextView.text = vitalSign?.code?.coding?.firstOrNull()?.display ?: vitalSign?.code?.coding?.firstOrNull()?.code?.capitalize(
                        Locale.ROOT)
                    binding.vitalSignOverviewView.effectiveStartDateValueTextView.text = formatDate(vitalSign?.effectivePeriod?.start?.toString())
                    binding.vitalSignOverviewView.effectiveEndDateValueTextView.text = formatDate(vitalSign?.effectivePeriod?.end?.toString())
                    binding.vitalSignOverviewView.encounterValueTextView.text = vitalSign?.encounter?.location?.name                }
            }
        }
    }
}