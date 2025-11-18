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
import com.bwell.common.models.domain.healthdata.healthsummary.encounter.Encounter
import com.bwell.common.models.domain.healthdata.healthsummary.healthsummary.enums.HealthSummaryCategory
import com.bwell.common.models.requests.searchtoken.SearchDate
import com.bwell.common.models.responses.BWellResult
import com.bwell.healthdata.healthsummary.requests.encounter.EncounterRequest
import com.bwell.sampleapp.BWellSampleApplication
import com.bwell.sampleapp.R
import com.bwell.sampleapp.databinding.EncounterDetailViewBinding
import com.bwell.sampleapp.utils.formatDate
import com.bwell.sampleapp.viewmodel.HealthSummaryViewModel
import com.bwell.sampleapp.viewmodel.HealthSummaryViewModelFactory
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class EncounterDetailFragment : Fragment(), View.OnClickListener {

    private var _binding: EncounterDetailViewBinding? = null
    private lateinit var healthSummaryViewModel: HealthSummaryViewModel
    private lateinit var encounterId: String
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
        _binding = EncounterDetailViewBinding.inflate(inflater, container, false)
        val root: View = binding.root
        val repository = (activity?.application as? BWellSampleApplication)?.healthSummaryRepository
        val providerResourcesRepository = (activity?.application as? BWellSampleApplication)?.providerResourcesRepository
        healthSummaryViewModel = ViewModelProvider(this, HealthSummaryViewModelFactory(repository, providerResourcesRepository))[HealthSummaryViewModel::class.java]
        binding.leftArrowImageView.setOnClickListener(this)
        encounterId = arguments?.getString("id").toString()
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
                val parentFrag: HealthSummaryFragment = this@EncounterDetailFragment.parentFragment as HealthSummaryFragment
                parentFrag.showHealthSummaryList()
            }
            R.id.overviewTextView -> {
                showOverView()
            }
        }
    }

    private fun showOverView() {
        binding.encounterOverviewView.encounterOverviewView.visibility = View.VISIBLE
        binding.overviewTextView.setTextColor(resources.getColor(R.color.medicine_tabs_selected_color))
        binding.overviewunderline.setBackgroundColor(resources.getColor(R.color.medicine_tabs_selected_color))
        binding.overviewTextView.setOnClickListener(null)

        val lastUpdatedSearchDate = SearchDate.Builder()
            .greaterThan(SimpleDateFormat("yyyy-MM-dd").parse("2020-01-12"))
            .build()

        val request = EncounterRequest.Builder()
            .groupCode(listOf(Coding(code = groupCode, system = groupSystem)))
            .lastUpdated(lastUpdatedSearchDate)
            .build()

        healthSummaryViewModel.getHealthSummaryData(request, category = HealthSummaryCategory.ENCOUNTER)
        viewLifecycleOwner.lifecycleScope.launch {
            healthSummaryViewModel.healthSummaryResults.collect { result ->
                if (result != null && result is BWellResult.ResourceCollection<*>) {
                    val encounter = (result.data as? List<Encounter>)?.firstOrNull()
                    binding.encounterOverviewView.encounterTitleTextView.text = name
                    binding.encounterOverviewView.typeValueTextView.text = encounter?.type?.firstOrNull()?.coding?.firstOrNull()?.display ?: encounter?.type?.firstOrNull()?.coding?.firstOrNull()?.code?.capitalize(
                        Locale.ROOT)
                    binding.encounterOverviewView.statusValueTextView.text = encounter?.status?.display
                    binding.encounterOverviewView.classValueTextView.text = encounter?.`class`?.display
                    binding.encounterOverviewView.participantValueTextView.text = encounter?.participant?.firstOrNull()?.individual?.name?.firstOrNull()?.text
                    if (!from.isNullOrBlank()) {
                        binding.encounterOverviewView.organizationName.text = "from " + from.toString()
                    } else {
                        binding.encounterOverviewView.organizationLl.visibility = View.GONE
                    }
                }
            }
        }
    }
}