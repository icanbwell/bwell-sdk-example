package com.bwell.sampleapp.activities.ui.labs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bwell.common.models.domain.common.Coding
import com.bwell.common.models.domain.healthdata.observation.Observation
import com.bwell.common.models.responses.BWellResult
import com.bwell.healthdata.lab.requests.LabKnowledgeRequest
import com.bwell.healthdata.lab.requests.LabsRequest
import com.bwell.sampleapp.BWellSampleApplication
import com.bwell.sampleapp.R
import com.bwell.sampleapp.databinding.LabDetailViewBinding
import com.bwell.sampleapp.utils.formatDate
import com.bwell.sampleapp.viewmodel.LabsViewModelFactory
import com.bwell.sampleapp.viewmodel.LabsViewModel
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch

class LabDetailsFragment : Fragment(), View.OnClickListener {

    private var _binding: LabDetailViewBinding? = null
    private lateinit var labsViewModel: LabsViewModel
    private lateinit var labId: String
    private lateinit var groupCode: String
    private lateinit var groupSystem: String
    private lateinit var name: String

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = LabDetailViewBinding.inflate(inflater, container, false)
        val root: View = binding.root
        val repository = (activity?.application as? BWellSampleApplication)?.labsRepository
        labsViewModel = ViewModelProvider(this, LabsViewModelFactory(repository))[LabsViewModel::class.java]
        binding.leftArrowImageView.setOnClickListener(this)
        labId = arguments?.getString("id").toString()
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
                val parentFrag: LabsFragment = this@LabDetailsFragment.parentFragment as LabsFragment
                parentFrag.showLabsList()
            }
            R.id.overviewTextView -> {
                showOverView()
            }
            R.id.whatIsItTextView -> {
                showKnowledgeView()
            }
        }
    }

    private fun showOverView() {
        binding.labOverviewView.labOverviewView.visibility = View.VISIBLE
        binding.labKnowledgeView.labKnowledgeView.visibility = View.GONE
        binding.whatIsItTextView.setTextColor(resources.getColor(R.color.black))
        binding.whatIsItunderline.setBackgroundColor(resources.getColor(R.color.medicine_tabs_non_selected_color))
        binding.overviewTextView.setTextColor(resources.getColor(R.color.medicine_tabs_selected_color))
        binding.overviewunderline.setBackgroundColor(resources.getColor(R.color.medicine_tabs_selected_color))
        binding.overviewTextView.setOnClickListener(null)
        binding.whatIsItTextView.setOnClickListener(this)
        binding.labKnowledgeView.containerLayout.removeAllViews()

        val labsRequest = LabsRequest.Builder()
            .groupCode(listOf(Coding(code = groupCode, system = groupSystem)))
            .build()

        labsViewModel.getLabs(labsRequest)
        viewLifecycleOwner.lifecycleScope.launch {
            labsViewModel.labResults.collect { result ->
                if (result != null && result is BWellResult.SingleResource<Observation>) {
                    val lab = result.data
                    binding.labOverviewView.labTitleTextView.text = name
                    binding.labOverviewView.codeValueTextView.text = lab?.code?.text
                    binding.labOverviewView.effectiveStartDateValueTextView.text = formatDate(lab?.effectivePeriod?.start.toString())
                    binding.labOverviewView.effectiveEndDateValueTextView.text = formatDate(lab?.effectivePeriod?.end.toString())
                    binding.labOverviewView.encounterValueTextView.text = lab?.encounter?.location?.first()?.location?.name
                    //binding.labOverviewView.organizationName.text = "from " + lab?.?.toString()
                }
            }
        }
    }

    private fun showKnowledgeView() {
        binding.labOverviewView.labOverviewView.visibility = View.GONE
        binding.labKnowledgeView.labKnowledgeView.visibility = View.VISIBLE
        binding.whatIsItTextView.setTextColor(resources.getColor(R.color.medicine_tabs_selected_color))
        binding.whatIsItunderline.setBackgroundColor(resources.getColor(R.color.medicine_tabs_selected_color))
        binding.overviewTextView.setTextColor(resources.getColor(R.color.black))
        binding.overviewunderline.setBackgroundColor(resources.getColor(R.color.medicine_tabs_non_selected_color))
        binding.overviewTextView.setOnClickListener(this)
        binding.whatIsItTextView.setOnClickListener(null)
        binding.labKnowledgeView.containerLayout.removeAllViews()
        val request = LabKnowledgeRequest.Builder()
            .labId(labId)
            .build()
        labsViewModel.getLabKnowledge(request)
        viewLifecycleOwner.lifecycleScope.launch {
            labsViewModel.labKnowledgeResults.collect { result ->
                if (result != null) {
                    when (result) {
                        is BWellResult.ResourceCollection -> {
                            val dataList = result.data
                            for (i in 0 until (dataList?.size ?: 0)) {
                                val contextTextView = TextView(requireContext())
                                contextTextView.text = dataList?.get(i)?.content
                                contextTextView.textSize = 18f
                                contextTextView.setTextColor(resources.getColor(R.color.black))
                                binding.labKnowledgeView.containerLayout.addView(contextTextView)
                                val webView = WebView(requireContext())
                                webView.loadDataWithBaseURL(null,
                                    dataList?.get(i)?.content.toString(), "text/html", "utf-8", null)
                                binding.labKnowledgeView.containerLayout.addView(webView)
                            }
                        }
                        else -> {}
                    }
                }
            }
        }
    }
}
