package com.bwell.sampleapp.activities.ui.medicines

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bwell.common.models.domain.healthdata.medication.MedicationGroup
import com.bwell.common.models.responses.BWellResult
import com.bwell.healthdata.medication.requests.MedicationGroupsRequest
import com.bwell.sampleapp.BWellSampleApplication
import com.bwell.sampleapp.R
import com.bwell.sampleapp.databinding.FragmentMedicinesBinding
import com.bwell.sampleapp.viewmodel.MedicinesViewModel
import com.bwell.sampleapp.viewmodel.MedicineViewModelFactory
import kotlinx.coroutines.launch

class MedicinesFragment : Fragment() {

    private var _binding: FragmentMedicinesBinding? = null

    private val binding get() = _binding!!
    private lateinit var medicinesViewModel: MedicinesViewModel
    private lateinit var groupMedicationListAdapter: GroupMedicationListAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMedicinesBinding.inflate(inflater, container, false)
        val root: View = binding.root
        val repository = (activity?.application as? BWellSampleApplication)?.medicineRepository
        medicinesViewModel = ViewModelProvider(this, MedicineViewModelFactory(repository))[MedicinesViewModel::class.java]

        getGroupMedicationList()
        addSearchTextListeners()
        return root
    }

    private fun setMedicinesGroupsAdapter(result:BWellResult<MedicationGroup>) {
        when (result) {
            is BWellResult.ResourceCollection -> {
                val dataList = result.data
                groupMedicationListAdapter = GroupMedicationListAdapter(dataList)
                binding.medicineGroupingView.rvGroupMedicine.layoutManager  = LinearLayoutManager(requireContext())
                binding.medicineGroupingView.rvGroupMedicine.adapter = groupMedicationListAdapter
                groupMedicationListAdapter.onItemClicked= { selectedMedicine ->
                    showDetailedView(selectedMedicine)
                }
            }
            else -> {}
        }
    }

    private fun showDetailedView(selectedMedicine: MedicationGroup?) {
        binding.includeHomeView.headerView.visibility = View.GONE
        binding.searchView.searchView.visibility = View.GONE
        binding.medicineGroupingView.medicineGroupingView.visibility = View.GONE
        val medicineDetailFragment = MedicineDetailFragment()
        val bundle = Bundle()
        bundle.putString("id", selectedMedicine?.id)
        bundle.putString("groupCode", selectedMedicine?.coding?.code.toString())
        bundle.putString("groupSystem", selectedMedicine?.coding?.system.toString())
        bundle.putString("name", selectedMedicine?.name)
        bundle.putString("from", selectedMedicine?.source?.joinToString(", ") ?: "")
        medicineDetailFragment.arguments = bundle
        val transaction = childFragmentManager.beginTransaction()
        binding.containerLayout.visibility = View.VISIBLE;
        transaction.replace(R.id.container_layout, medicineDetailFragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    private fun getGroupMedicationList() {
         val groupsRequest: MedicationGroupsRequest = MedicationGroupsRequest.Builder()
             .build()
        medicinesViewModel.getMedicationGroups(groupsRequest)
            viewLifecycleOwner.lifecycleScope.launch {
                medicinesViewModel.groupMedicationResults.collect { result ->
                    if (result != null) {
                        setMedicinesGroupsAdapter(result)
                    }
            }
        }
    }

    private fun addSearchTextListeners() {
        binding.searchView.searchText.addTextChangedListener(object :
            TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {
                medicinesViewModel.filterGroupMedicationList(charSequence.toString())
                viewLifecycleOwner.lifecycleScope.launch {
                    medicinesViewModel.filteredGroupMedicationResults.collect { filteredList ->
                        groupMedicationListAdapter.updateList(filteredList)
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

    fun showMedicinesList() {
        binding.includeHomeView.headerView.visibility = View.VISIBLE
        binding.searchView.searchView.visibility = View.VISIBLE
        binding.medicineGroupingView.medicineGroupingView.visibility = View.VISIBLE
    }
}