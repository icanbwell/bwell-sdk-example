package com.bwell.sampleapp.activities.ui.health_journey

import android.graphics.Color
import android.opengl.Visibility
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bwell.activity.requests.TasksRequest
import com.bwell.activity.requests.UpdateTaskRequest
import com.bwell.common.models.domain.task.enums.TaskStatus
import com.bwell.common.models.responses.BWellResult
import com.bwell.sampleapp.BWellSampleApplication
import com.bwell.sampleapp.R
import com.bwell.sampleapp.databinding.TaskDetailViewBinding
import com.bwell.sampleapp.utils.loadRemoteImageIntoImageView
import com.bwell.sampleapp.viewmodel.HealthJourneyViewModel
import com.bwell.sampleapp.viewmodel.HealthJourneyViewModelFactory
import kotlinx.coroutines.launch

class TaskDetailFragment : Fragment(), View.OnClickListener {

    private var _binding: TaskDetailViewBinding? = null
    private lateinit var healthJourneyViewModel: HealthJourneyViewModel
    private lateinit var taskId: String
    private var name: String? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = TaskDetailViewBinding.inflate(inflater, container, false)
        val root: View = binding.root
        val repository = (activity?.application as? BWellSampleApplication)?.healthJourneyRepository
        healthJourneyViewModel = ViewModelProvider(this, HealthJourneyViewModelFactory(repository))[HealthJourneyViewModel::class.java]
        binding.leftArrowImageView.setOnClickListener(this)
        taskId = arguments?.getString("id").toString()
        name = arguments?.getString("name")

        showDetails()

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
                val parentFrag: HealthJourneyFragment = this@TaskDetailFragment.parentFragment as HealthJourneyFragment
                parentFrag.showTasksList()
            }
        }
    }

    private fun showDetails() {
        val request = TasksRequest.Builder()
            .id(taskId)
            .enrichContent(true)
            .build()
        healthJourneyViewModel.getTasks(request)
        viewLifecycleOwner.lifecycleScope.launch {
            healthJourneyViewModel.taskResults.collect { result ->
                if (result != null) {
                    when (result) {
                        is BWellResult.ResourceCollection -> {
                            val dataList = result.data
                            for (i in 0 until (dataList?.size ?: 0)) {
                                val task = dataList?.get(i)

                                // set title
                                if (name == null) {
                                    name = healthJourneyViewModel.getActivityName(task)
                                }
                                binding.titleTextView.text = name

                                // set image
                                loadRemoteImageIntoImageView(binding.taskImageView, healthJourneyViewModel.getContentImage(task))

                                // set description
                                healthJourneyViewModel.getContentDescription(task)?.let {
                                    binding.taskDescriptionWebview.loadDataWithBaseURL(null,
                                        it, "text/html", "utf-8", null)
                                }

                                // set button
                                val buttonText = healthJourneyViewModel.getContentButtonText(task)
                                binding.taskButton.text = buttonText
                                if (buttonText != null) {
                                    binding.taskButton.visibility = View.VISIBLE
                                }

                                binding.taskButton.setOnClickListener {
                                    binding.taskStatusTitleTextView.visibility = View.VISIBLE

                                    val taskUpdateRequest = UpdateTaskRequest.Builder()
                                        .taskId(taskId)
                                        .newStatus(TaskStatus.COMPLETED)
                                        .build()

                                    healthJourneyViewModel.updateTask(taskUpdateRequest) {
                                        if (it?.success() == true) {
                                            binding.taskStatusTitleTextView.setTextColor(Color.GREEN)
                                            binding.taskStatusTitleTextView.text = "Success!"
                                        } else {
                                            binding.taskStatusTitleTextView.setTextColor(Color.RED)
                                            binding.taskStatusTitleTextView.text = "Error"
                                        }
                                    }
                                }

                                // set references
                                healthJourneyViewModel.getContentReferences(task)?.let {
                                    binding.taskReferencesWebview.loadDataWithBaseURL(null,
                                        it, "text/html", "utf-8", null)
                                }
                            }
                        }
                        else -> {}
                    }
                }
            }
        }
    }
}
