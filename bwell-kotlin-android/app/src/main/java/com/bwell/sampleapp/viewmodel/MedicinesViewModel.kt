package com.bwell.sampleapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bwell.common.models.domain.healthdata.medication.MedicationGroup
import com.bwell.common.models.domain.healthdata.medication.MedicationKnowledge
import com.bwell.common.models.domain.healthdata.medication.MedicationStatement
import com.bwell.common.models.responses.BWellResult
import com.bwell.healthdata.medication.requests.MedicationGroupsRequest
import com.bwell.healthdata.medication.requests.MedicationKnowledgeRequest
import com.bwell.healthdata.medication.requests.MedicationStatementsRequest
import com.bwell.sampleapp.repository.MedicineRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MedicinesViewModel(private val repository: MedicineRepository?) : ViewModel() {
    private val _activeMedicationResults = MutableStateFlow<BWellResult<MedicationGroup>?>(null)
    val activeMedicationResults: StateFlow<BWellResult<MedicationGroup>?> = _activeMedicationResults

<<<<<<< Updated upstream
    fun getActiveMedicationGroups(medicationRequest: MedicationGroupsRequest) {
=======
    fun getMedicationGroups(medicationRequest: MedicationGroupsRequest?) {
>>>>>>> Stashed changes
        viewModelScope.launch {
            try {
                repository?.getMedicationGroups(medicationRequest)?.collect { result ->
                    _activeMedicationResults.emit(result)
                }
            } catch (e: Exception) {
                // Handle exceptions, if any
            }
        }
    }

    private val _pastMedicationResults = MutableStateFlow<BWellResult<MedicationGroup>?>(null)
    val pastMedicationResults: StateFlow<BWellResult<MedicationGroup>?> = _pastMedicationResults

    fun getPastMedicationList(medicationRequest: MedicationGroupsRequest) {
        viewModelScope.launch {
            try {
                repository?.getMedicationGroups(medicationRequest)?.collect { result ->
                    _pastMedicationResults.emit(result)
                }
            } catch (e: Exception) {
                // Handle exceptions, if any
            }
        }
    }

    private val _filteredActiveMedicationResults = MutableStateFlow<List<MedicationGroup>?>(null)
    val filteredActiveMedicationResults: StateFlow<List<MedicationGroup>?> = _filteredActiveMedicationResults

    fun filterActiveMedicationList(query: String) {
        viewModelScope.launch {
            val activeMedicationResult = _activeMedicationResults.value
            if (activeMedicationResult != null) {
                val filteredList = (activeMedicationResult as BWellResult.ResourceCollection).data?.filter { medication ->
                    medication.name?.contains(query, ignoreCase = true) == true
                }?.map { it } // Extracting names
                _filteredActiveMedicationResults.value = filteredList
            }
        }
    }

    private val _filteredPastMedicationResults = MutableStateFlow<List<MedicationGroup>?>(null)
    val filteredPastMedicationResults: StateFlow<List<MedicationGroup>?> = _filteredPastMedicationResults

    fun filterPastMedicationList(query: String) {
        viewModelScope.launch {
            val pastMedicationResult = _pastMedicationResults.value
            if (pastMedicationResult != null) {
                val filteredList = (pastMedicationResult as BWellResult.ResourceCollection).data?.filter { medication ->
                    medication.name?.contains(query, ignoreCase = true) == true
                }?.map { it } // Extracting names
                _filteredPastMedicationResults.value = filteredList
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
}