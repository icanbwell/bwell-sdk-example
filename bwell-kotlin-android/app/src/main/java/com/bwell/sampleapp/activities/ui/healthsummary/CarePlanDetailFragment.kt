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
import com.bwell.common.models.domain.healthdata.healthsummary.careplan.CarePlan
import com.bwell.common.models.domain.healthdata.healthsummary.healthsummary.enums.HealthSummaryCategory
import com.bwell.common.models.responses.BWellResult
import com.bwell.healthdata.healthsummary.requests.careplan.CarePlanRequest
import com.bwell.sampleapp.BWellSampleApplication
import com.bwell.sampleapp.R
import com.bwell.sampleapp.databinding.CarePlanDetailViewBinding
import com.bwell.sampleapp.utils.formatDate
import com.bwell.sampleapp.viewmodel.HealthSummaryViewModel
import com.bwell.sampleapp.viewmodel.HealthSummaryViewModelFactory
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch

class CarePlanDetailFragment : Fragment(), View.OnClickListener {

    private var _binding: CarePlanDetailViewBinding? = null
    private lateinit var healthSummaryViewModel: HealthSummaryViewModel
    private lateinit var carePlanId: String
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
        _binding = CarePlanDetailViewBinding.inflate(inflater, container, false)
        val root: View = binding.root
        val repository = (activity?.application as? BWellSampleApplication)?.healthSummaryRepository
        healthSummaryViewModel = ViewModelProvider(this, HealthSummaryViewModelFactory(repository))[HealthSummaryViewModel::class.java]
        binding.leftArrowImageView.setOnClickListener(this)
        carePlanId = arguments?.getString("id").toString()
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
                val parentFrag: HealthSummaryFragment = this@CarePlanDetailFragment.parentFragment as HealthSummaryFragment
                parentFrag.showHealthSummaryList()
            }
            R.id.overviewTextView -> {
                showOverView()
            }
        }
    }

    private fun showOverView() {
        binding.carePlanOverviewView.carePlanOverviewView.visibility = View.VISIBLE
        binding.overviewTextView.setTextColor(resources.getColor(R.color.medicine_tabs_selected_color))
        binding.overviewunderline.setBackgroundColor(resources.getColor(R.color.medicine_tabs_selected_color))
        binding.overviewTextView.setOnClickListener(null)

        val request = CarePlanRequest.Builder()
            .groupCode(listOf(Coding(code = groupCode, system = groupSystem)))
            .build()

        healthSummaryViewModel.getHealthSummaryData(request, category = HealthSummaryCategory.CARE_PLAN)
        viewLifecycleOwner.lifecycleScope.launch {
            healthSummaryViewModel.healthSummaryResults.collect { result ->
                if (result != null && result is BWellResult.ResourceCollection<*>) {
                    val carePlan = (result.data as? List<CarePlan>)?.firstOrNull()
                    binding.carePlanOverviewView.carePlanTitleTextView.text = name
                    binding.carePlanOverviewView.dateStartValueTextView.text = formatDate(carePlan?.period?.start?.toString())
                    binding.carePlanOverviewView.dateStartValueTextView.text = formatDate(carePlan?.period?.end?.toString())
                    binding.carePlanOverviewView.statusValueTextView.text = carePlan?.status?.display
                    binding.carePlanOverviewView.intentValueTextView.text = carePlan?.intent?.display
                    if (!from.isNullOrBlank()) {
                        binding.carePlanOverviewView.organizationName.text = "from " + from.toString()
                    } else {
                        binding.carePlanOverviewView.organizationLl.visibility = View.GONE
                    }
                }
            }
        }
    }
}
