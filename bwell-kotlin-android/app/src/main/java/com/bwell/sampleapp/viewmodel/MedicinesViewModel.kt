package com.bwell.sampleapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bwell.common.models.domain.healthdata.medication.MedicationComposition
import com.bwell.common.models.domain.healthdata.medication.MedicationKnowledge
import com.bwell.common.models.domain.healthdata.medication.MedicationPricing
import com.bwell.common.models.domain.healthdata.medication.MedicationSummary
import com.bwell.common.models.responses.BWellResult
import com.bwell.healthdata.medication.MedicationKnowledgeRequest
import com.bwell.healthdata.medication.MedicationListRequest
import com.bwell.healthdata.medication.MedicationPricingRequest
import com.bwell.sampleapp.repository.MedicineRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MedicinesViewModel(private val repository: MedicineRepository?) : ViewModel() {
    private val _activeMedicationResults = MutableStateFlow<BWellResult<MedicationSummary>?>(null)
    val activeMedicationResults: StateFlow<BWellResult<MedicationSummary>?> = _activeMedicationResults

    fun getActiveMedicationList(medicationRequest: MedicationListRequest) {
        viewModelScope.launch {
            try {
                repository?.getMedicationList(medicationRequest)?.collect { result ->
                    _activeMedicationResults.emit(result)
                }
            } catch (e: Exception) {
                // Handle exceptions, if any
            }
        }
    }

    private val _pastMedicationResults = MutableStateFlow<BWellResult<MedicationSummary>?>(null)
    val pastMedicationResults: StateFlow<BWellResult<MedicationSummary>?> = _pastMedicationResults

    fun getPastMedicationList(medicationRequest: MedicationListRequest) {
        viewModelScope.launch {
            try {
                repository?.getMedicationList(medicationRequest)?.collect { result ->
                    _pastMedicationResults.emit(result)
                }
            } catch (e: Exception) {
                // Handle exceptions, if any
            }
        }
    }

    private val _filteredActiveMedicationResults = MutableStateFlow<List<MedicationSummary>?>(null)
    val filteredActiveMedicationResults: StateFlow<List<MedicationSummary>?> = _filteredActiveMedicationResults

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

    private val _filteredPastMedicationResults = MutableStateFlow<List<MedicationSummary>?>(null)
    val filteredPastMedicationResults: StateFlow<List<MedicationSummary>?> = _filteredPastMedicationResults

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

    private val _medicationOverviewResults = MutableStateFlow<BWellResult<MedicationComposition>?>(null)
    val medicationOverviewResults: StateFlow<BWellResult<MedicationComposition>?> = _medicationOverviewResults

    fun getMedicationOverview(medicationId: String) {
        viewModelScope.launch {
            try {
                repository?.getMedicationOverview(medicationId)?.collect { result ->
                    _medicationOverviewResults.emit(result)
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


}