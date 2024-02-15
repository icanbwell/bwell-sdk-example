package com.bwell.sampleapp.activities.ui.health_journey

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bwell.activity.requests.TasksRequest
import com.bwell.common.models.domain.task.Task
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
    private lateinit var tasksViewModel: HealthJourneyViewModel
    private lateinit var taskId: String
    private lateinit var name: String

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = TaskDetailViewBinding.inflate(inflater, container, false)
        val root: View = binding.root
        val repository = (activity?.application as? BWellSampleApplication)?.healthJourneyRepository
        tasksViewModel = ViewModelProvider(this, HealthJourneyViewModelFactory(repository))[HealthJourneyViewModel::class.java]
        binding.leftArrowImageView.setOnClickListener(this)
        taskId = arguments?.getString("id").toString()
        name = arguments?.getString("name").toString()

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
        tasksViewModel.getTasks(request)
        viewLifecycleOwner.lifecycleScope.launch {
            tasksViewModel.taskResults.collect { result ->
                if (result != null) {
                    when (result) {
                        is BWellResult.ResourceCollection -> {
                            val dataList = result.data
                            for (i in 0 until (dataList?.size ?: 0)) {
                                val task = dataList?.get(i)

                                // set title
                                binding.titleTextView.text = name

                                // set image
                                loadRemoteImageIntoImageView(binding.taskImageView, tasksViewModel.getContentImage(task))

                                // set description
                                tasksViewModel.getContentDescription(task)?.let {
                                    binding.taskDescriptionWebview.loadDataWithBaseURL(null,
                                        it, "text/html", "utf-8", null)
                                }

                                // set button
                                val buttonText = tasksViewModel.getContentButtonText(task)
                                binding.taskButton.text = buttonText

                                // set references
                                tasksViewModel.getContentReferences(task)?.let {
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
