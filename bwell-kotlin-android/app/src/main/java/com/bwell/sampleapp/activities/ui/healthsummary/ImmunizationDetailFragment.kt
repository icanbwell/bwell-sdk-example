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
import com.bwell.common.models.domain.healthdata.healthsummary.immunization.Immunization
import com.bwell.common.models.domain.healthdata.healthsummary.healthsummary.enums.HealthSummaryCategory
import com.bwell.common.models.requests.searchtoken.SearchDate
import com.bwell.common.models.responses.BWellResult
import com.bwell.healthdata.healthsummary.requests.immunization.ImmunizationRequest
import com.bwell.sampleapp.BWellSampleApplication
import com.bwell.sampleapp.R
import com.bwell.sampleapp.databinding.ImmunizationDetailViewBinding
import com.bwell.sampleapp.utils.formatDate
import com.bwell.sampleapp.viewmodel.HealthSummaryViewModel
import com.bwell.sampleapp.viewmodel.HealthSummaryViewModelFactory
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class ImmunizationDetailFragment : Fragment(), View.OnClickListener {

    private var _binding: ImmunizationDetailViewBinding? = null
    private lateinit var healthSummaryViewModel: HealthSummaryViewModel
    private lateinit var immunizationId: String
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
        _binding = ImmunizationDetailViewBinding.inflate(inflater, container, false)
        val root: View = binding.root
        val repository = (activity?.application as? BWellSampleApplication)?.healthSummaryRepository
        val providerResourcesRepository = (activity?.application as? BWellSampleApplication)?.providerResourcesRepository
        healthSummaryViewModel = ViewModelProvider(this, HealthSummaryViewModelFactory(repository, providerResourcesRepository))[HealthSummaryViewModel::class.java]
        binding.leftArrowImageView.setOnClickListener(this)
        immunizationId = arguments?.getString("id").toString()
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
                val parentFrag: HealthSummaryFragment = this@ImmunizationDetailFragment.parentFragment as HealthSummaryFragment
                parentFrag.showHealthSummaryList()
            }
            R.id.overviewTextView -> {
                showOverView()
            }
        }
    }

    private fun showOverView() {
        binding.immunizationOverviewView.immunizationOverviewView.visibility = View.VISIBLE
        binding.overviewTextView.setTextColor(resources.getColor(R.color.medicine_tabs_selected_color))
        binding.overviewunderline.setBackgroundColor(resources.getColor(R.color.medicine_tabs_selected_color))
        binding.overviewTextView.setOnClickListener(null)

        val lastUpdateSearchDate = SearchDate.Builder()
            .greaterThanOrEqualTo(SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2020-01-12 22:00:00"))
            .build()

        val request = ImmunizationRequest.Builder()
            .groupCode(listOf(Coding(code = groupCode, system = groupSystem)))
            .lastUpdated(lastUpdateSearchDate)
            .build()

        healthSummaryViewModel.getHealthSummaryData(request, category = HealthSummaryCategory.IMMUNIZATION)
        viewLifecycleOwner.lifecycleScope.launch {
            healthSummaryViewModel.healthSummaryResults.collect { result ->
                if (result != null && result is BWellResult.ResourceCollection<*>) {
                    val immunization = (result.data as? List<Immunization>)?.firstOrNull()
                    binding.immunizationOverviewView.immunizationTitleTextView.text = name
                    binding.immunizationOverviewView.vaccineCodeValueTextView.text = immunization?.vaccineCode?.coding?.firstOrNull()?.display ?: immunization?.vaccineCode?.coding?.firstOrNull()?.code?.capitalize(
                        Locale.ROOT)
                    binding.immunizationOverviewView.statusValueTextView.text = immunization?.status?.display
                    binding.immunizationOverviewView.occuranceDateValueTextView.text = formatDate(immunization?.occurrenceDateTime?.toString())
                    binding.immunizationOverviewView.doseQuantityValueTextView.text = immunization?.doseQuantity?.value?.toString()
                    if (!from.isNullOrBlank()) {
                        binding.immunizationOverviewView.organizationName.text = "from " + from.toString()
                    } else {
                        binding.immunizationOverviewView.organizationLl.visibility = View.GONE
                    }
                }
            }
        }
    }
}