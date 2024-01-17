package com.bwell.sampleapp.activities.ui.health_journey

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bwell.activity.requests.TaskRequest
import com.bwell.common.models.domain.task.Task
import com.bwell.common.models.domain.task.enums.TaskStatus
import com.bwell.common.models.responses.BWellResult
import com.bwell.sampleapp.BWellSampleApplication
import com.bwell.sampleapp.databinding.FragmentHealthJourneyBinding
import com.bwell.sampleapp.viewmodel.HealthJourneyViewModel
import com.bwell.sampleapp.viewmodel.HealthJourneyViewModelFactory
import kotlinx.coroutines.launch

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
        val repository = (activity?.application as? BWellSampleApplication)?.healthJourneyRepository
        healthJourneyViewModel = ViewModelProvider(this, HealthJourneyViewModelFactory(repository))[HealthJourneyViewModel::class.java]
        getTasks()
        return root
    }

    private fun setAdapter(taskResult: BWellResult<Task>) {
        when (taskResult) {
            is BWellResult.ResourceCollection -> {
                val taskList = taskResult.data

                val adapter = HealthJourneyListAdapter(taskList)
                binding.rvHealthJourney.layoutManager = LinearLayoutManager(requireContext())
                binding.rvHealthJourney.adapter = adapter
            }
            else -> {}
        }
    }

    private fun getTasks() {
        val taskRequest = TaskRequest.Builder()
            .build()
        healthJourneyViewModel.getTasks(taskRequest)
        viewLifecycleOwner.lifecycleScope.launch {
            healthJourneyViewModel.taskResults.collect { result ->
                if (result != null) {
                    Log.e("result","health journey result>>>>>>>>>>>>>>> "+result)
                    setAdapter(result)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}