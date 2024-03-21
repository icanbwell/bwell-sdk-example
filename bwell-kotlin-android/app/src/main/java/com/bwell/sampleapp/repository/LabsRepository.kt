package com.bwell.sampleapp.repository

import android.content.Context
import com.bwell.common.models.domain.healthdata.common.observation.Observation
import com.bwell.sampleapp.singletons.BWellSdk
import com.bwell.common.models.domain.healthdata.lab.LabGroup
import com.bwell.common.models.domain.healthdata.lab.LabKnowledge
import com.bwell.common.models.responses.BWellResult
import com.bwell.healthdata.lab.requests.LabGroupsRequest
import com.bwell.healthdata.lab.requests.LabKnowledgeRequest
import com.bwell.healthdata.lab.requests.LabsRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class LabsRepository(private val applicationContext: Context) {

    suspend fun getLabGroups(labGroupsRequest: LabGroupsRequest?): Flow<BWellResult<LabGroup>?> = flow {
        try {
            val labGroupsResult = BWellSdk.health?.getLabGroups(labGroupsRequest)
            emit(labGroupsResult)
        } catch (e: Exception) {
            emit(null)
        }
    }

    suspend fun getLabs(labsRequest: LabsRequest): Flow<BWellResult<Observation>?> = flow {
        try {
            val labsResult = BWellSdk.health?.getLabs(labsRequest)
            emit(labsResult)
        } catch (e: Exception) {
            emit(null)
        }
    }

    suspend fun getLabKnowledge(labKnowledgeRequest: LabKnowledgeRequest): Flow<BWellResult<LabKnowledge>?> = flow {
        try {
            val labKnowledgeResults = BWellSdk.health?.getLabKnowledge(labKnowledgeRequest)
            emit(labKnowledgeResults)
        } catch (e: Exception) {
            emit(null)
        }
    }



}