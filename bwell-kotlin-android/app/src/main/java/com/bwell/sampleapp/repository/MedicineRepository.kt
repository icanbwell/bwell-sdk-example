package com.bwell.sampleapp.repository

import android.content.Context
import com.bwell.common.models.domain.healthdata.medication.MedicationDispense
import com.bwell.sampleapp.singletons.BWellSdk
import com.bwell.common.models.domain.healthdata.medication.MedicationGroup
import com.bwell.common.models.domain.healthdata.medication.MedicationKnowledge
import com.bwell.common.models.domain.healthdata.medication.MedicationStatement
import com.bwell.common.models.domain.healthdata.medication.MedicationPricing
import com.bwell.common.models.domain.healthdata.medication.MedicationRequest
import com.bwell.common.models.responses.BWellResult
import com.bwell.healthdata.medication.requests.MedicationDispenseQueryRequest
import com.bwell.healthdata.medication.requests.MedicationGroupsRequest
import com.bwell.healthdata.medication.requests.MedicationKnowledgeRequest
import com.bwell.healthdata.medication.requests.MedicationStatementsRequest
import com.bwell.healthdata.medication.requests.MedicationPricingRequest
import com.bwell.healthdata.medication.requests.MedicationRequestRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class MedicineRepository(private val applicationContext: Context) {

    suspend fun getMedicationGroups(medicationRequest: MedicationGroupsRequest?): Flow<BWellResult<MedicationGroup>?> = flow {
        try {
            val medicationGroupsResult = BWellSdk.health?.getMedicationGroups(medicationRequest)
            emit(medicationGroupsResult)
        } catch (e: Exception) {
            emit(null)
        }
    }

    suspend fun getMedicationStatements(medicationStatementsRequest: MedicationStatementsRequest): Flow<BWellResult<MedicationStatement>?> = flow {
        try {
            val medicationStatementsResult = BWellSdk.health?.getMedicationStatements(medicationStatementsRequest)
            emit(medicationStatementsResult)
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


    suspend fun getMedicationDispense(medicationDispenseRequest: MedicationDispenseQueryRequest): Flow<BWellResult<MedicationDispense>?> = flow {
        try {
            val medicationDispenseResult = BWellSdk.health.getMedicationDispense(medicationDispenseRequest)
            emit(medicationDispenseResult)
        } catch (e: Exception) {
            emit(null)
        }
    }


    suspend fun getMedicationRequest(medicationRequestRequest: MedicationRequestRequest): Flow<BWellResult<MedicationRequest>?> = flow {
        try {
            val medicationRequestResult = BWellSdk.health.getMedicationRequest(medicationRequestRequest)
            emit(medicationRequestResult)
        } catch (e: Exception) {
            emit(null)
        }
    }
}
