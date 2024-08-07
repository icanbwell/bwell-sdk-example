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
import com.bwell.activity.requests.TasksRequest
import com.bwell.common.models.domain.task.Task
import com.bwell.common.models.domain.task.enums.TaskStatus
import com.bwell.common.models.responses.BWellResult
import com.bwell.sampleapp.BWellSampleApplication
import com.bwell.sampleapp.R
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
        handleDeepLink()
        return root
    }

    private fun setAdapter(taskResult: BWellResult<Task>) {
        when (taskResult) {
            is BWellResult.ResourceCollection -> {
                val taskList = taskResult.data

                val adapter = HealthJourneyListAdapter(taskList)
                binding.rvHealthJourney.layoutManager = LinearLayoutManager(requireContext())
                binding.rvHealthJourney.adapter = adapter
                adapter.onItemClicked= { selectedTask ->
                    showDetailedView(selectedTask?.id,  healthJourneyViewModel.getActivityName(selectedTask))
                }
            }
            else -> {}
        }
    }

    private fun showDetailedView(id: String?, name: String? = null) {
        binding.titleTextView.visibility = View.GONE
        binding.descriptionTextView.visibility = View.GONE
        binding.rvHealthJourney.visibility = View.GONE

        val taskDetailsFragment = TaskDetailFragment()
        val bundle = Bundle()
        bundle.putString("id", id)
        bundle.putString("name", name)
        taskDetailsFragment.arguments = bundle
        val transaction = childFragmentManager.beginTransaction()
        binding.containerLayout.visibility = View.VISIBLE;
        transaction.replace(R.id.container_layout, taskDetailsFragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    private fun handleDeepLink() {
        arguments?.getString("task_id")?.let { taskId ->
            showDetailedView(taskId)
        }
    }

    private fun getTasks() {
        val taskRequest = TasksRequest.Builder()
            .smartSort(true)
            .status(listOf(TaskStatus.READY))
            //.activityType(listOf("care-need"))
            .page(0)
            .pageSize(100)
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

    fun showTasksList() {
        binding.titleTextView.visibility = View.VISIBLE
        binding.descriptionTextView.visibility = View.VISIBLE
        binding.rvHealthJourney.visibility = View.VISIBLE
        getTasks()
    }
}