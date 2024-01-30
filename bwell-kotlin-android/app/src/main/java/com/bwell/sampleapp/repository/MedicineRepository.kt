package com.bwell.sampleapp.repository

import android.content.Context
import com.bwell.BWellSdk
import com.bwell.common.models.domain.healthdata.medication.MedicationGroup
import com.bwell.common.models.domain.healthdata.medication.MedicationKnowledge
import com.bwell.common.models.domain.healthdata.medication.MedicationPricing
import com.bwell.common.models.domain.healthdata.medication.MedicationStatement
import com.bwell.common.models.responses.BWellResult
import com.bwell.healthdata.medication.requests.MedicationGroupsRequest
import com.bwell.healthdata.medication.requests.MedicationKnowledgeRequest
import com.bwell.healthdata.medication.requests.MedicationPricingRequest
import com.bwell.healthdata.medication.requests.MedicationStatementsRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class MedicineRepository(private val applicationContext: Context) {

    suspend fun getMedicationGroups(medicationRequest: MedicationGroupsRequest): Flow<BWellResult<MedicationGroup>?> = flow {
        try {
            val medicationGroupsResult = BWellSdk.health?.getMedicationGroups(medicationRequest)
            emit(medicationGroupsResult)
        } catch (e: Exception) {
            emit(null)
        }
    }

    suspend fun getMedicationStatements(medicationStatementRequest: MedicationStatementsRequest): Flow<BWellResult<MedicationStatement>?> = flow {
        try {
            val medicationStatementResult = BWellSdk.health?.getMedicationStatements(medicationStatementRequest)
            emit(medicationStatementResult)
        } catch (e: Exception) {
            emit(null)
        }
    }

    suspend fun getMedicationKnowledge(medicationKnowledgeRequest: MedicationKnowledgeRequest): Flow<BWellResult<MedicationKnowledge>?> = flow {
        try {
            val medicationKnowledgeResult = BWellSdk.health?.getMedicationKnowledge(medicationKnowledgeRequest)
            emit(medicationKnowledgeResult)
        } catch (e: Exception) {
            emit(null)
        }
    }

    suspend fun getMedicationPricing(medicationPricingRequest: MedicationPricingRequest): Flow<BWellResult<MedicationPricing>?> = flow {
        try {
            val medicationPricingResult = BWellSdk.health?.getMedicationPricing(medicationPricingRequest)
            emit(medicationPricingResult)
        } catch (e: Exception) {
            emit(null)
        }
    }

}