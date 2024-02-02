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
import com.bwell.common.models.domain.common.Period
import com.bwell.common.models.domain.healthdata.medication.MedicationGroup
import com.bwell.common.models.responses.BWellResult
import com.bwell.common.models.domain.healthdata.medication.enums.MedicationStatus
import com.bwell.healthdata.medication.requests.MedicationGroupsRequest
import com.bwell.sampleapp.BWellSampleApplication
import com.bwell.sampleapp.R
import com.bwell.sampleapp.databinding.FragmentMedicinesBinding
import com.bwell.sampleapp.utils.parseDateStringToDate
import com.bwell.sampleapp.viewmodel.MedicinesViewModel
import com.bwell.sampleapp.viewmodel.MedicineViewModelFactory
import kotlinx.coroutines.launch
import java.util.Date

class MedicinesFragment : Fragment() {

    private var _binding: FragmentMedicinesBinding? = null

    private val binding get() = _binding!!
    private lateinit var medicinesViewModel: MedicinesViewModel
    private lateinit var activeMedicationListAdapter: ActiveMedicationListAdapter
    private lateinit var pastMedicationListAdapter: PastMedicationListAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMedicinesBinding.inflate(inflater, container, false)
        val root: View = binding.root
        val repository = (activity?.application as? BWellSampleApplication)?.medicineRepository
        medicinesViewModel = ViewModelProvider(this, MedicineViewModelFactory(repository))[MedicinesViewModel::class.java]

        getActiveMedicationList()
        //getPastMedicationList()
        addSearchTextListeners()
        return root
    }

    private fun setActiveMedicinesAdapter(result:BWellResult<MedicationGroup>) {
        when (result) {
            is BWellResult.ResourceCollection -> {
                val dataList = result.data
                 activeMedicationListAdapter = ActiveMedicationListAdapter(dataList)
                binding.medicineActiveView.rvActiveMedicine.layoutManager = LinearLayoutManager(requireContext())
                binding.medicineActiveView.rvActiveMedicine.adapter = activeMedicationListAdapter
                activeMedicationListAdapter.onItemClicked= { selectedMedicine ->
                    showDetailedView(selectedMedicine)
                }
            }
            else -> {}
        }
    }

    private fun showDetailedView(selectedMedicine: MedicationGroup?) {
        binding.includeHomeView.headerView.visibility = View.GONE
        binding.searchView.searchView.visibility = View.GONE
        binding.medicineActiveView.medicineActiveView.visibility = View.GONE
        binding.medicinePastView.medicinePastView.visibility = View.GONE
        val medicineDetailFragment = MedicineDetailFragment()
        val bundle = Bundle()
        bundle.putString("id", selectedMedicine?.id)
        bundle.putString("groupCode", selectedMedicine?.coding?.code.toString())
        bundle.putString("groupSystem", selectedMedicine?.coding?.system.toString())
        medicineDetailFragment.arguments = bundle
        val transaction = childFragmentManager.beginTransaction()
        binding.containerLayout.visibility = View.VISIBLE;
        transaction.replace(R.id.container_layout, medicineDetailFragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    private fun setPastMedicinesAdapter(result:BWellResult<MedicationGroup>) {
        when (result) {
            is BWellResult.ResourceCollection -> {
                val dataList = result.data
                 pastMedicationListAdapter = PastMedicationListAdapter(dataList)
                binding.medicinePastView.rvPastMedicine.layoutManager = LinearLayoutManager(requireContext())
                binding.medicinePastView.rvPastMedicine.adapter = pastMedicationListAdapter
                pastMedicationListAdapter.onItemClicked= { selectedMedicine ->
                    showDetailedView(selectedMedicine)
                }
            }
            else -> {}
        }
    }

    private fun getActiveMedicationList() {
        val name = ""
        val date = Period.Builder().start(
            parseDateStringToDate("2023-01-01", "yyyy-MM-dd")
        ).end(parseDateStringToDate("2023-12-31", "yyyy-MM-dd")).build()
        val status = MedicationStatus.ACTIVE
        /*val request = MedicationListRequest.Builder()
            .name(name)
            .date(date)
            .status(status)
            .build()
            */
         val groupsRequest: MedicationGroupsRequest = MedicationGroupsRequest.Builder()
             //.page(0)
             //.pageSize(1)
             .build()
        medicinesViewModel.getActiveMedicationGroups(groupsRequest)
            viewLifecycleOwner.lifecycleScope.launch {
                medicinesViewModel.activeMedicationResults.collect { result ->
                    if (result != null) {
                        setActiveMedicinesAdapter(result)

                }
            }
        }
    }

    private fun getPastMedicationList() {
        val name = ""
        val date = Period.Builder().start(
            parseDateStringToDate("2023-01-01", "yyyy-MM-dd")
        ).end(parseDateStringToDate("2023-12-31", "yyyy-MM-dd")).build()
        val status = MedicationStatus.INACTIVE
        /*val request = MedicationListRequest.Builder()
            .name(name)
            .date(date)
            .status(status)
            .build()
            */
        val groupsRequest: MedicationGroupsRequest = MedicationGroupsRequest.Builder()
            .build()
        medicinesViewModel.getPastMedicationList(groupsRequest)
        viewLifecycleOwner.lifecycleScope.launch {
            medicinesViewModel.pastMedicationResults.collect { result ->
                if (result != null) {
                    setPastMedicinesAdapter(result)
                }
            }
        }
    }

    private fun addSearchTextListeners() {
        binding.searchView.searchText.addTextChangedListener(object :
            TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {
                medicinesViewModel.filterActiveMedicationList(charSequence.toString())
                viewLifecycleOwner.lifecycleScope.launch {
                    medicinesViewModel.filteredActiveMedicationResults.collect { filteredList ->
                        activeMedicationListAdapter.updateList(filteredList)
                    }
                }
                medicinesViewModel.filterPastMedicationList(charSequence.toString())
                viewLifecycleOwner.lifecycleScope.launch {
                    medicinesViewModel.filteredPastMedicationResults.collect { filteredList ->
                        pastMedicationListAdapter.updateList(filteredList)
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
        binding.medicineActiveView.medicineActiveView.visibility = View.VISIBLE
        binding.medicinePastView.medicinePastView.visibility = View.VISIBLE
    }

}