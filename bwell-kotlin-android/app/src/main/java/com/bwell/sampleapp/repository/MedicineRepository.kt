package com.bwell.sampleapp.repository

import android.content.Context
import com.bwell.BWellSdk
import com.bwell.common.models.domain.healthdata.medication.MedicationComposition
import com.bwell.common.models.domain.healthdata.medication.MedicationKnowledge
import com.bwell.common.models.domain.healthdata.medication.MedicationPricing
import com.bwell.common.models.domain.healthdata.medication.MedicationSummary
import com.bwell.common.models.responses.BWellResult
import com.bwell.healthdata.medication.MedicationKnowledgeRequest
import com.bwell.healthdata.medication.MedicationListRequest
import com.bwell.healthdata.medication.MedicationPricingRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class MedicineRepository(private val applicationContext: Context) {

    suspend fun getMedicationList(medicationRequest: MedicationListRequest): Flow<BWellResult<MedicationSummary>?> = flow {
        try {
            val carePlanResult = BWellSdk.health?.getMedicationList(medicationRequest)
            emit(carePlanResult)
        } catch (e: Exception) {
            emit(null)
        }
    }

    suspend fun getMedicationOverview(medicationId: String): Flow<BWellResult<MedicationComposition>?> = flow {
        try {
            val carePlanResult = BWellSdk.health?.getMedication(medicationId)
            emit(carePlanResult)
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