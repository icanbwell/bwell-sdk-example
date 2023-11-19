package com.bwell.sampleapp.activities.ui.medicines

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bwell.sampleapp.BWellSampleApplication
import com.bwell.sampleapp.activities.ui.data_connections.DataConnectionsCategoriesListAdapter
import com.bwell.sampleapp.databinding.FragmentMedicinesBinding
import com.bwell.sampleapp.viewmodel.MedicinesViewModel
import com.bwell.sampleapp.viewmodel.SharedViewModelFactory
import com.bwell.sampleapp.model.DataConnectionCategoriesListItems

class MedicinesFragment : Fragment() {

    private var _binding: FragmentMedicinesBinding? = null

    private val binding get() = _binding!!
    private lateinit var medicinesViewModel: MedicinesViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMedicinesBinding.inflate(inflater, container, false)
        val root: View = binding.root
        val repository = (activity?.application as? BWellSampleApplication)?.bWellRepository

        medicinesViewModel = ViewModelProvider(this, SharedViewModelFactory(repository))[MedicinesViewModel::class.java]

        medicinesViewModel.suggestedDataConnectionsCategories.observe(viewLifecycleOwner) {
            setAdapter(it.suggestedDataConnectionsCategoriesList)
        }
        return root
    }

    private fun setAdapter(suggestedActivitiesLIst: List<DataConnectionCategoriesListItems>) {
        val adapter = DataConnectionsCategoriesListAdapter(suggestedActivitiesLIst)
        binding.rvSuggestedDataConnections.layoutManager = LinearLayoutManager(requireContext())
        binding.rvSuggestedDataConnections.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}