package com.bwell.sampleapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bwell.activity.requests.TasksRequest
import com.bwell.activity.requests.UpdateTaskRequest
import com.bwell.common.models.domain.task.Task
import com.bwell.common.models.domain.task.enums.TaskStatus
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

    private val _updateResults = MutableStateFlow<BWellResult<Task>?>(null)
    val updateResults: StateFlow<BWellResult<Task>?> = _updateResults

    fun updateTaskStatus(updateTaskRequest: UpdateTaskRequest) {
        viewModelScope.launch {
            try {
                repository?.updateTaskStatus(updateTaskRequest)?.collect { result ->
                    _updateResults.emit(result)
                }
            } catch (e: Exception) {
                // Handle exceptions, if any
            }
        }
    }
}