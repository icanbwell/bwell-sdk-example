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
import com.bwell.common.models.domain.healthdata.healthsummary.allergyintolerance.AllergyIntolerance
import com.bwell.common.models.domain.healthdata.healthsummary.healthsummary.enums.HealthSummaryCategory
import com.bwell.common.models.responses.BWellResult
import com.bwell.healthdata.healthsummary.requests.allergyintolerance.AllergyIntoleranceRequest
import com.bwell.sampleapp.BWellSampleApplication
import com.bwell.sampleapp.R
import com.bwell.sampleapp.databinding.AllergyIntoleranceDetailViewBinding
import com.bwell.sampleapp.utils.formatDate
import com.bwell.sampleapp.viewmodel.HealthSummaryViewModel
import com.bwell.sampleapp.viewmodel.HealthSummaryViewModelFactory
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch

class AllergyIntoleranceDetailFragment : Fragment(), View.OnClickListener {

    private var _binding: AllergyIntoleranceDetailViewBinding? = null
    private lateinit var healthSummaryViewModel: HealthSummaryViewModel
    private lateinit var allergyIntoleranceId: String
    private lateinit var groupCode: String
    private lateinit var groupSystem: String
    private lateinit var name: String

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = AllergyIntoleranceDetailViewBinding.inflate(inflater, container, false)
        val root: View = binding.root
        val repository = (activity?.application as? BWellSampleApplication)?.healthSummaryRepository
        healthSummaryViewModel = ViewModelProvider(this, HealthSummaryViewModelFactory(repository))[HealthSummaryViewModel::class.java]
        binding.leftArrowImageView.setOnClickListener(this)
        allergyIntoleranceId = arguments?.getString("id").toString()
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
                val parentFrag: HealthSummaryFragment = this@AllergyIntoleranceDetailFragment.parentFragment as HealthSummaryFragment
                parentFrag.showHealthSummaryList()
            }
            R.id.overviewTextView -> {
                showOverView()
            }
        }
    }

    private fun showOverView() {
        binding.allergyIntoleranceOverviewView.allergyIntoleranceOverviewView.visibility = View.VISIBLE
        binding.overviewTextView.setTextColor(resources.getColor(R.color.medicine_tabs_selected_color))
        binding.overviewunderline.setBackgroundColor(resources.getColor(R.color.medicine_tabs_selected_color))
        binding.overviewTextView.setOnClickListener(null)

        val allergyIntolerancRequest = AllergyIntoleranceRequest.Builder()
            .groupCode(listOf(Coding(code = groupCode, system = groupSystem)))
            .build()

        healthSummaryViewModel.getHealthSummaryData(allergyIntolerancRequest, category = HealthSummaryCategory.ALLERGY_INTOLERANCE)
        viewLifecycleOwner.lifecycleScope.launch {
            healthSummaryViewModel.healthSummaryResults.collect { result ->
                println("ALLERGY_INTOLERANCE: result: $result")
                if (result != null && result is BWellResult.ResourceCollection<*>) {
                    val allergyIntolerance = (result.data as? List<AllergyIntolerance>)?.first()
                    binding.allergyIntoleranceOverviewView.allergyIntoleranceTitleTextView.text = name
                    binding.allergyIntoleranceOverviewView.verificationStatusValueTextView.text = allergyIntolerance?.verificationStatus?.text
                    binding.allergyIntoleranceOverviewView.criticalityValueTextView.text = allergyIntolerance?.criticality?.display
                    binding.allergyIntoleranceOverviewView.onsetValueTextView.text = formatDate(allergyIntolerance?.onsetDateTime?.toString())
                    binding.allergyIntoleranceOverviewView.lastOccuranceDateValueTextView.text = formatDate(allergyIntolerance?.lastOccurrence.toString())
                    //binding.allergyIntoleranceOverviewView.organizationName.text = "from " + allergyIntolerance?.requester?.toString()
                }
            }
        }
    }
}
