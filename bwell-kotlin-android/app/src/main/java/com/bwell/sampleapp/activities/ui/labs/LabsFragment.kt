package com.bwell.sampleapp.activities.ui.labs

import android.graphics.Typeface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
import com.bwell.common.models.domain.healthdata.lab.LabGroup
import com.bwell.common.models.responses.BWellResult
import com.bwell.healthdata.lab.requests.LabGroupsRequest
import com.bwell.sampleapp.BWellSampleApplication
import com.bwell.sampleapp.R
import com.bwell.sampleapp.databinding.FragmentLabsBinding
import com.bwell.sampleapp.viewmodel.LabsViewModelFactory
import com.bwell.sampleapp.viewmodel.LabsViewModel
import kotlinx.coroutines.launch

class LabsFragment : Fragment() {

    private var _binding: FragmentLabsBinding? = null

    private val binding get() = _binding!!
    private lateinit var labsViewModel: LabsViewModel
    private lateinit var groupLabListAdapter: GroupLabsListAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLabsBinding.inflate(inflater, container, false)
        val root: View = binding.root
        val repository = (activity?.application as? BWellSampleApplication)?.labsRepository
        labsViewModel = ViewModelProvider(this, LabsViewModelFactory(repository))[LabsViewModel::class.java]

        getGroupLabList()
        addSearchTextListeners()
        return root
    }

    private fun setLabsGroupsAdapter(result:BWellResult<LabGroup>) {
        when (result) {
            is BWellResult.ResourceCollection -> {
                val dataList = result.data
                groupLabListAdapter = GroupLabsListAdapter(dataList)
                binding.labGroupingView.rvGroupLab.layoutManager  = LinearLayoutManager(requireContext())
                binding.labGroupingView.rvGroupLab.adapter = groupLabListAdapter
                groupLabListAdapter.onItemClicked= { selectedLab ->
                    showDetailedView(selectedLab)
                }
            }
            else -> {}
        }
    }

    private fun showDetailedView(selectedLab: LabGroup?) {
        binding.includeHomeView.headerView.visibility = View.GONE
        binding.searchView.searchView.visibility = View.GONE
        binding.labGroupingView.labGroupingView.visibility = View.GONE
        val labDetailFragment = LabDetailsFragment()
        val bundle = Bundle()
        bundle.putString("id", selectedLab?.id)
        bundle.putString("groupCode", selectedLab?.coding?.code.toString())
        bundle.putString("groupSystem", selectedLab?.coding?.system.toString())
        bundle.putString("name", selectedLab?.name)
        labDetailFragment.arguments = bundle
        val transaction = childFragmentManager.beginTransaction()
        binding.containerLayout.visibility = View.VISIBLE;
        transaction.replace(R.id.container_layout, labDetailFragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    private fun getGroupLabList() {
        val groupsRequest: LabGroupsRequest = LabGroupsRequest.Builder()
            .build()
        labsViewModel.getLabGroups(groupsRequest)
        viewLifecycleOwner.lifecycleScope.launch {
            labsViewModel.groupLabResults.collect { result ->
                if (result != null) {
                    setLabsGroupsAdapter(result)
                }
            }
        }
    }

    private fun addSearchTextListeners() {
        binding.searchView.searchText.addTextChangedListener(object :
            TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {
                labsViewModel.filterGroupLabList(charSequence.toString())
                viewLifecycleOwner.lifecycleScope.launch {
                    labsViewModel.filteredGroupLabResults.collect { filteredList ->
                        groupLabListAdapter.updateList(filteredList)
                    }
                }
            }

            override fun afterTextChanged(editable: Editable?) {}
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun showLabsList() {
        binding.includeHomeView.headerView.visibility = View.VISIBLE
        binding.searchView.searchView.visibility = View.VISIBLE
        binding.labGroupingView.labGroupingView.visibility = View.VISIBLE
    }
}