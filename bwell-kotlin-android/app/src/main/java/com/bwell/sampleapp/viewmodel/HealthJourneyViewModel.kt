package com.bwell.sampleapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bwell.activity.requests.TasksRequest
import com.bwell.activity.requests.UpdateTaskRequest
import com.bwell.common.models.domain.task.Task
import com.bwell.common.models.responses.BWellResult
import com.bwell.sampleapp.repository.HealthJourneyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HealthJourneyViewModel (private val repository: HealthJourneyRepository?) : ViewModel() {

    private val _taskResults = MutableStateFlow<BWellResult<Task>?>(null)
    val taskResults: StateFlow<BWellResult<Task>?> = _taskResults

    fun getTasks(taskRequest: TasksRequest) {
        viewModelScope.launch {
            try {
                repository?.getTasks(taskRequest)?.collect { result ->
                    _taskResults.emit(result)
                }
            } catch (e: Exception) {
                // Handle exceptions, if any
            }
        }
    }
    fun updateTask(updateTaskRequest: UpdateTaskRequest, callback: (BWellResult<Task>?) -> Unit) {
        viewModelScope.launch {
            val result = repository?.updateTask(updateTaskRequest)
            callback(result)
        }
    }

    fun getActivityName(task: Task?): String? {
        return task?.identifier?.firstOrNull { it.system == "https://www.icanbwell.com/activityName" }?.value
    }

    fun getContentDescription(task: Task?): String? {
        return task?.extension?.firstOrNull {
            it.url == "https://www.icanbwell.com/contentDescription"
        }?.valueString
    }

    fun getContentImage(task: Task?): String? {
        return task?.extension?.firstOrNull {
            it.url == "https://www.icanbwell.com/contentImage"
        }?.valueString
    }

    fun getContentButtonText(task: Task?): String? {
        return task?.extension?.firstOrNull {
            it.url == "https://www.icanbwell.com/contentButtonText"
        }?.valueString
    }

    fun getContentReferences(task: Task?): String? {
        return task?.extension?.firstOrNull {
            it.url == "https://www.icanbwell.com/contentReferences"
        }?.valueString
    }
}