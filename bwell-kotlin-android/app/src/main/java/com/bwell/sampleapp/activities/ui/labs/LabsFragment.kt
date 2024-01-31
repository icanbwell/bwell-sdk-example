package com.bwell.sampleapp.activities.ui.labs

import android.annotation.SuppressLint
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bwell.common.models.domain.common.Coding
import com.bwell.common.models.domain.common.Period
import com.bwell.common.models.domain.healthdata.lab.LabGroup
import com.bwell.common.models.domain.healthdata.lab.LabKnowledge
import com.bwell.common.models.domain.healthdata.observation.Observation
import com.bwell.common.models.domain.healthdata.observation.performer.ObservationOrganizationPerformer
import com.bwell.common.models.responses.BWellResult
import com.bwell.healthdata.healthsummary.requests.procedure.LabGroupsRequest
import com.bwell.healthdata.lab.requests.LabKnowledgeRequest
import com.bwell.healthdata.lab.requests.LabsRequest
import com.bwell.sampleapp.BWellSampleApplication
import com.bwell.sampleapp.R
import com.bwell.sampleapp.databinding.FragmentLabsParentBinding
import com.bwell.sampleapp.utils.formatDate
import com.bwell.sampleapp.viewmodel.LabsViewModel
import com.bwell.sampleapp.viewmodel.LabsViewModelFactory
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch

import com.bwell.sampleapp.utils.parseDateStringToDate

class LabsFragment : Fragment(), View.OnClickListener {

    private var _binding: FragmentLabsParentBinding? = null

    private val binding get() = _binding!!
    private lateinit var labsViewModel: LabsViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLabsParentBinding.inflate(inflater, container, false)
        val root: View = binding.root
        val repository = (activity?.application as? BWellSampleApplication)?.labsRepository

        labsViewModel = ViewModelProvider(this, LabsViewModelFactory(repository))[LabsViewModel::class.java]

        getLabGroups()

        binding.includeLabsDetail.leftArrowImageView.setOnClickListener(this)
        return root
    }

    private fun getLabGroups() {
        val date = Period.Builder().start(
            parseDateStringToDate("2023-01-01", "yyyy-MM-dd")).build()
        val page = 0
        val pageSize = 10

        val request = LabGroupsRequest.Builder()
            .page(page)
            .pageSize(pageSize)
            .build()
        labsViewModel.getLabGroups(request)
        viewLifecycleOwner.lifecycleScope.launch {
            labsViewModel.labGroupsResults.take(1).collect { result ->
                if (result != null) {
                    Log.d("BWell Sample App", "result-$result")
                    when (result) {
                        is BWellResult.ResourceCollection -> {
                            val dataList = result.data
                            setLabsAdapter(dataList)
                        }

                        else -> {}
                    }
                }
            }
        }
    }

    private fun setLabsAdapter(dataList: List<LabGroup>?) {
        val adapter = LabsListAdapter(dataList)
        adapter.onItemClicked = { selectedLabType ->
            binding.includelabsData.labsFragment.visibility = View.GONE
            binding.includeLabsDetail.labDetailFragment.visibility = View.VISIBLE
            showLabDetailedView(selectedLabType)
        }
        binding.includelabsData.rvLabs.layoutManager = LinearLayoutManager(requireContext())
        binding.includelabsData.rvLabs.adapter = adapter
    }

    @SuppressLint("SetTextI18n")
    private fun showLabDetailedView(selectedLabType: LabGroup?) {
        binding.includeLabsDetail.labDataLl.removeAllViews()
        val groupCodeCode = selectedLabType?.coding?.code.toString()
        val groupCodeSystem = selectedLabType?.coding?.system.toString()
        val page = 0
        val pageSize = 30

        val request = LabsRequest.Builder()
            .groupCode(listOf(Coding(code = groupCodeCode, system = groupCodeSystem)))
            .page(page)
            .pageSize(pageSize)
            .build()

        labsViewModel.getLabs(request)
        viewLifecycleOwner.lifecycleScope.launch {
            labsViewModel.labsDetailResults.take(1).collect { result ->
                if (result != null) {
                    Log.d("result", "result$result")
                    when (result) {
                        is BWellResult.ResourceCollection -> {
                            val dataList = result.data
                            Log.d("dataList", "dataList$dataList")
                            val details:Observation? = dataList?.get(0)
                            binding.includeLabsDetail.typeText.text = details?.code?.coding?.first()?.display
                            binding.includeLabsDetail.dateText.text =
                                ("as of " + details?.effectiveDateTime?.toString().let { formatDate(it) })
                            //binding.includeLabsDetail.organizationName.text = "from "+(details?.performer?.get(1) as ObservationOrganizationPerformer).organizationName
                            addTextField(details?.effectiveDateTime?.toString()?.let { formatDate(it) } ?: "---",false)
                            addTextField(details?.interpretation?.get(0)?.text.toString(),false)
                            addTextField(resources.getString(R.string.healthy_range),true)
                            addTextField(details?.referenceRange?.get(0)?.text.toString(),false)
                            showLabKnowledgeView(details?.id)
                        }
                        else -> {}
                    }
                }
            }
        }
    }

    private fun addTextField(data: String, isTitle: Boolean) {
        val textView = TextView(requireContext())
        textView.text = data
        if (isTitle) {
            textView.setTypeface(null, Typeface.BOLD)
        } else {
            textView.setTypeface(null, Typeface.NORMAL)
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(0, 0, 0, 20)
            textView.layoutParams = params
        }
        binding.includeLabsDetail.labDataLl.addView(textView)
    }

    private fun showLabKnowledgeView(labId: String?) {

        val request = LabKnowledgeRequest.Builder()
            //.labId(labId.toString())
            .labId("f001-b")
            .build()

        labsViewModel.getLabKnowledge(request)
        viewLifecycleOwner.lifecycleScope.launch {
            labsViewModel.labsKnowledgeResults.take(1).collect { result ->
                if (result != null) {
                    Log.d("result", "result$result")
                    when (result) {
                        is BWellResult.ResourceCollection -> {
                            val dataList = result.data
                            val labKnowledge:LabKnowledge? = dataList?.get(0)
                            val webView = WebView(requireContext())
                            webView.loadDataWithBaseURL(null,
                                labKnowledge?.content.toString(), "text/html", "utf-8", null)
                            binding.includeLabsDetail.containerLayout.addView(webView)
                        }
                        else -> {}
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.leftArrowImageView -> {
                binding.includelabsData.labsFragment.visibility = View.VISIBLE
                binding.includeLabsDetail.labDetailFragment.visibility = View.GONE
            }
        }
    }
}