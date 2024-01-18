package com.bwell.sampleapp.repository

import com.bwell.BWellSdk
import com.bwell.activity.requests.TasksRequest
import com.bwell.activity.requests.UpdateTaskRequest
import com.bwell.common.models.domain.task.Task
import com.bwell.common.models.domain.task.enums.TaskStatus
import com.bwell.common.models.responses.BWellResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class HealthJourneyRepository {

    suspend fun getTasks(taskRequest: TasksRequest): Flow<BWellResult<Task>?> = flow {
        try {
            val taskResult = BWellSdk.activity.getTasks(taskRequest)
            emit(taskResult)
        } catch (e: Exception) {
            emit(null)
        }
    }

    suspend fun updateTaskStatus(taskUpdateRequest: UpdateTaskRequest): Flow<BWellResult<Task>?> = flow {
        try {
            val taskResult = BWellSdk.activity.updateTaskStatus(taskUpdateRequest)
            emit(taskResult)
        } catch (e: Exception) {
            emit(null)
        }
    }
}