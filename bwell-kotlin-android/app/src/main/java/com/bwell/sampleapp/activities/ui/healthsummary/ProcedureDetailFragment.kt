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
import com.bwell.common.models.domain.healthdata.healthsummary.procedure.Procedure
import com.bwell.common.models.domain.healthdata.healthsummary.healthsummary.enums.HealthSummaryCategory
import com.bwell.common.models.requests.searchtoken.SearchDate
import com.bwell.common.models.responses.BWellResult
import com.bwell.healthdata.healthsummary.requests.procedure.ProcedureRequest
import com.bwell.sampleapp.BWellSampleApplication
import com.bwell.sampleapp.R
import com.bwell.sampleapp.databinding.ProcedureDetailViewBinding
import com.bwell.sampleapp.utils.formatDate
import com.bwell.sampleapp.viewmodel.HealthSummaryViewModel
import com.bwell.sampleapp.viewmodel.HealthSummaryViewModelFactory
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class ProcedureDetailFragment : Fragment(), View.OnClickListener {

    private var _binding: ProcedureDetailViewBinding? = null
    private lateinit var healthSummaryViewModel: HealthSummaryViewModel
    private lateinit var procedureId: String
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
        _binding = ProcedureDetailViewBinding.inflate(inflater, container, false)
        val root: View = binding.root
        val repository = (activity?.application as? BWellSampleApplication)?.healthSummaryRepository
        val providerResourcesRepository = (activity?.application as? BWellSampleApplication)?.providerResourcesRepository
        healthSummaryViewModel = ViewModelProvider(this, HealthSummaryViewModelFactory(repository, providerResourcesRepository))[HealthSummaryViewModel::class.java]
        binding.leftArrowImageView.setOnClickListener(this)
        procedureId = arguments?.getString("id").toString()
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
                val parentFrag: HealthSummaryFragment = this@ProcedureDetailFragment.parentFragment as HealthSummaryFragment
                parentFrag.showHealthSummaryList()
            }
            R.id.overviewTextView -> {
                showOverView()
            }
        }
    }

    private fun showOverView() {
        binding.procedureOverviewView.procedureOverviewView.visibility = View.VISIBLE
        binding.overviewTextView.setTextColor(resources.getColor(R.color.medicine_tabs_selected_color))
        binding.overviewunderline.setBackgroundColor(resources.getColor(R.color.medicine_tabs_selected_color))
        binding.overviewTextView.setOnClickListener(null)

        val lastUpdatedSearchDate = SearchDate.Builder()
            .greaterThan(SimpleDateFormat("yyyy-MM-dd").parse("2020-01-12"))
            .build()

        val request = ProcedureRequest.Builder()
            .groupCode(listOf(Coding(code = groupCode, system = groupSystem)))
            .lastUpdated(lastUpdatedSearchDate)
            .build()

        healthSummaryViewModel.getHealthSummaryData(request, category = HealthSummaryCategory.PROCEDURE)
        viewLifecycleOwner.lifecycleScope.launch {
            healthSummaryViewModel.healthSummaryResults.collect { result ->
                if (result != null && result is BWellResult.ResourceCollection<*>) {
                    val procedure = (result.data as? List<Procedure>)?.firstOrNull()
                    binding.procedureOverviewView.procedureTitleTextView.text = name
                    binding.procedureOverviewView.outcomeValueTextView.text = procedure?.outcome?.coding?.firstOrNull()?.display ?: procedure?.outcome?.coding?.firstOrNull()?.code?.capitalize(
                        Locale.ROOT)
                    binding.procedureOverviewView.statusValueTextView.text = procedure?.status?.display
                    binding.procedureOverviewView.performedDateValueTextView.text = formatDate(procedure?.performedDateTime?.toString())
                    binding.procedureOverviewView.categoryValueTextView.text = procedure?.category?.coding?.firstOrNull()?.display ?: procedure?.category?.coding?.firstOrNull()?.code?.capitalize(
                        Locale.ROOT)
                    if (!from.isNullOrBlank()) {
                        binding.procedureOverviewView.organizationName.text = "from " + from.toString()
                    } else {
                        binding.procedureOverviewView.organizationLl.visibility = View.GONE
                    }
                }
            }
        }
    }
}