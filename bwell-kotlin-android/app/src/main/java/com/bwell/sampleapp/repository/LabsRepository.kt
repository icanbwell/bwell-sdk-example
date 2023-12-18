package com.bwell.sampleapp.repository

import android.content.Context
import com.bwell.BWellSdk
import com.bwell.common.models.domain.healthdata.observation.Observation
import com.bwell.common.models.responses.BWellResult
import com.bwell.healthdata.lab.LabDetailRequest
import com.bwell.healthdata.lab.LabKnowledgeRequest
import com.bwell.healthdata.lab.LabRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class LabsRepository(private val applicationContext: Context) {

    suspend fun getLabsList(labsRequest: LabRequest): Flow<BWellResult<Observation>?> = flow {
        try {
            val labsResult = BWellSdk.health?.getLabs(labsRequest)
            emit(labsResult)
        } catch (e: Exception) {
            emit(null)
        }
    }

    suspend fun getLabDetails(labsDetailRequest: LabDetailRequest): Flow<BWellResult<Observation>?> = flow {
        try {
            val labsResult = BWellSdk.health?.getLabDetails(labsDetailRequest)
            emit(labsResult)
        } catch (e: Exception) {
            emit(null)
        }
    }

    suspend fun getLabKnowledge(labKnowledgeRequest: LabKnowledgeRequest): Flow<BWellResult<String>?> = flow {
        try {
            val labKnowledgeResults = BWellSdk.health?.getLabKnowledge(labKnowledgeRequest)
            emit(labKnowledgeResults)
        } catch (e: Exception) {
            emit(null)
        }
    }



}