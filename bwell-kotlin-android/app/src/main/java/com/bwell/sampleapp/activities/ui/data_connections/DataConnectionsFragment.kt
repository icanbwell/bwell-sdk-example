package com.bwell.sampleapp.activities.ui.data_connections

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bwell.sampleapp.BWellSampleApplication
import com.bwell.sampleapp.databinding.FragmentDataConnectionsParentBinding
import com.bwell.sampleapp.model.DataConnectionCategoriesListItems
import com.bwell.sampleapp.model.DataConnectionListItems
import com.bwell.sampleapp.viewmodel.DataConnectionsViewModel
import com.bwell.sampleapp.viewmodel.SharedViewModelFactory

class DataConnectionsFragment : Fragment() {

    private var _binding: FragmentDataConnectionsParentBinding? = null

    private val binding get() = _binding!!
    private lateinit var dataConnectionsViewModel: DataConnectionsViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDataConnectionsParentBinding.inflate(inflater, container, false)
        val root: View = binding.root
        val repository = (activity?.application as? BWellSampleApplication)?.bWellRepository

        dataConnectionsViewModel = ViewModelProvider(this, SharedViewModelFactory(repository))[DataConnectionsViewModel::class.java]

        dataConnectionsViewModel.suggestedDataConnectionsCategories.observe(viewLifecycleOwner) {
            setDataConnectionsCategoryAdapter(it.suggestedDataConnectionsCategoriesList)
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setDataConnectionsAdapter(suggestedActivitiesLIst: List<DataConnectionListItems>) {
        val adapter = DataConnectionsListAdapter(suggestedActivitiesLIst)
        binding.includeDataConnections.dataConnectionFragment.visibility = View.VISIBLE;
        binding.includeDataConnectionCategory.dataConnectionFragment.visibility = View.GONE;
        binding.includeDataConnections.rvSuggestedDataConnections.layoutManager = LinearLayoutManager(requireContext())
        binding.includeDataConnections.rvSuggestedDataConnections.adapter = adapter
    }

    private fun setDataConnectionsCategoryAdapter(suggestedActivitiesLIst: List<DataConnectionCategoriesListItems>) {
        val adapter = DataConnectionsCategoriesListAdapter(suggestedActivitiesLIst)
        adapter.onItemClicked = { selectedDataConnection ->
            displayRelatedDataConnectionsList();
        }
        binding.includeDataConnectionCategory.dataConnectionFragment.visibility = View.VISIBLE;
        binding.includeDataConnections.dataConnectionFragment.visibility = View.GONE;
        binding.includeDataConnectionCategory.rvSuggestedDataConnections.layoutManager = LinearLayoutManager(requireContext())
        binding.includeDataConnectionCategory.rvSuggestedDataConnections.adapter = adapter
    }

    private fun displayRelatedDataConnectionsList() {
        dataConnectionsViewModel.suggestedDataConnections.observe(viewLifecycleOwner) {
            setDataConnectionsAdapter(it.suggestedDataConnectionsList)
        }
    }
}