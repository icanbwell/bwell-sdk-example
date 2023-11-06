package com.bwell.sampleapp.activities.ui.health_journey

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bwell.sampleapp.BWellSampleApplication
import com.bwell.sampleapp.databinding.FragmentHealthJourneyBinding
import com.bwell.sampleapp.model.HealthJourneyListItems
import com.bwell.sampleapp.viewmodel.HealthJourneyViewModel
import com.bwell.sampleapp.viewmodel.SharedViewModelFactory

class HealthJourneyFragment : Fragment() {

    private var _binding: FragmentHealthJourneyBinding? = null

    private val binding get() = _binding!!
    private lateinit var healthJourneyViewModel: HealthJourneyViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHealthJourneyBinding.inflate(inflater, container, false)
        val root: View = binding.root
        val repository = (activity?.application as? BWellSampleApplication)?.bWellRepository

        healthJourneyViewModel = ViewModelProvider(this, SharedViewModelFactory(repository))[HealthJourneyViewModel::class.java]

        healthJourneyViewModel.healthJourneyData.observe(viewLifecycleOwner) {
            setAdapter(it.healthJourneyList)
        }


        return root
    }

    private fun setAdapter(suggestedActivitiesLIst: List<HealthJourneyListItems>) {
        val adapter = HealthJourneyListAdapter(suggestedActivitiesLIst)
        binding.rvHealthJourney.layoutManager = LinearLayoutManager(requireContext())
        binding.rvHealthJourney.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}