package com.bwell.sampleapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bwell.common.models.domain.healthdata.medication.MedicationDispense
import com.bwell.common.models.domain.healthdata.medication.MedicationGroup
import com.bwell.common.models.domain.healthdata.medication.MedicationKnowledge
import com.bwell.common.models.domain.healthdata.medication.MedicationPricing
import com.bwell.common.models.domain.healthdata.medication.MedicationRequest
import com.bwell.common.models.domain.healthdata.medication.MedicationStatement
import com.bwell.common.models.responses.BWellResult
import com.bwell.healthdata.medication.requests.MedicationDispenseQueryRequest
import com.bwell.healthdata.medication.requests.MedicationGroupsRequest
import com.bwell.healthdata.medication.requests.MedicationKnowledgeRequest
import com.bwell.healthdata.medication.requests.MedicationPricingRequest
import com.bwell.healthdata.medication.requests.MedicationRequestRequest
import com.bwell.healthdata.medication.requests.MedicationStatementsRequest
import com.bwell.sampleapp.repository.MedicineRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MedicinesViewModel(private val repository: MedicineRepository?) : ViewModel() {
    private val _groupMedicationResults = MutableStateFlow<BWellResult<MedicationGroup>?>(null)
    val groupMedicationResults: StateFlow<BWellResult<MedicationGroup>?> = _groupMedicationResults

    fun getMedicationGroups(medicationRequest: MedicationGroupsRequest?) {
        viewModelScope.launch {
            try {
                repository?.getMedicationGroups(medicationRequest)?.collect { result ->
                    _groupMedicationResults.emit(result)
                }
            } catch (e: Exception) {
                // Handle exceptions, if any
            }
        }
    }

    private val _filteredGroupMedicationResults = MutableStateFlow<List<MedicationGroup>?>(null)
    val filteredGroupMedicationResults: StateFlow<List<MedicationGroup>?> = _filteredGroupMedicationResults

    fun filterGroupMedicationList(query: String) {
        viewModelScope.launch {
            val groupMedicationResult = _groupMedicationResults.value
            if (groupMedicationResult != null) {
                val filteredList = (groupMedicationResult as BWellResult.ResourceCollection).data?.filter { medication ->
                    medication.name?.contains(query, ignoreCase = true) == true
                }?.map { it } // Extracting names
                _filteredGroupMedicationResults.value = filteredList
            }
        }
    }

    private val _medicationStatementsResults = MutableStateFlow<BWellResult<MedicationStatement>?>(null)
    val medicationStatementsResults: StateFlow<BWellResult<MedicationStatement>?> = _medicationStatementsResults

    fun getMedicationStatements(medicationStatementsRequest: MedicationStatementsRequest) {
        viewModelScope.launch {
            try {
                repository?.getMedicationStatements(medicationStatementsRequest)?.collect { result ->
                    _medicationStatementsResults.emit(result)
                }
            } catch (e: Exception) {
                // Handle exceptions, if any
            }
        }
    }

    private val _medicationKnowledgeResults = MutableStateFlow<BWellResult<MedicationKnowledge>?>(null)
    val medicationKnowledgeResults: StateFlow<BWellResult<MedicationKnowledge>?> = _medicationKnowledgeResults

    fun getMedicationKnowledge(medicationKnowledgeRequest: MedicationKnowledgeRequest) {
        viewModelScope.launch {
            try {
                repository?.getMedicationKnowledge(medicationKnowledgeRequest)?.collect { result ->
                    _medicationKnowledgeResults.emit(result)
                }
            } catch (e: Exception) {
                // Handle exceptions, if any
            }
        }
    }


    private val _medicationPricingResults = MutableStateFlow<BWellResult<MedicationPricing>?>(null)
    val medicationPricingResults: StateFlow<BWellResult<MedicationPricing>?> = _medicationPricingResults

    fun getMedicationPricing(medicationPricingRequest: MedicationPricingRequest) {
        viewModelScope.launch {
            try {
                repository?.getMedicationPricing(medicationPricingRequest)?.collect { result ->
                    _medicationPricingResults.emit(result)
                }
            } catch (e: Exception) {
                // Handle exceptions, if any
            }
        }
    }

    private val _medicationDispenseResults = MutableStateFlow<BWellResult<MedicationDispense>?>(null)
    val medicationDispenseResults: StateFlow<BWellResult<MedicationDispense>?> = _medicationDispenseResults

    fun getMedicationDispense(medicationDispenseRequest: MedicationDispenseQueryRequest) {
        viewModelScope.launch {
            try {
                repository?.getMedicationDispense(medicationDispenseRequest)?.collect { result ->
                    _medicationDispenseResults.emit(result)
                }
            } catch (e: Exception) {
                // Handle exceptions, if any
            }
        }
    }


    private val _medicationRequestResults = MutableStateFlow<BWellResult<MedicationRequest>?>(null)
    val medicationRequestResults: StateFlow<BWellResult<MedicationRequest>?> = _medicationRequestResults

    fun getMedicationRequest(medicationRequestRequest: MedicationRequestRequest) {
        viewModelScope.launch {
            try {
                repository?.getMedicationRequest(medicationRequestRequest)?.collect { result ->
                    _medicationRequestResults.emit(result)
                }
            } catch (e: Exception) {
                // Handle exceptions, if any
            }
        }
    }
}
