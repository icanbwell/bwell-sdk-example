package com.bwell.sampleapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bwell.common.models.domain.healthdata.lab.LabGroup
import com.bwell.common.models.domain.healthdata.lab.LabKnowledge
import com.bwell.common.models.domain.healthdata.observation.Observation
import com.bwell.common.models.responses.BWellResult
import com.bwell.healthdata.healthsummary.requests.procedure.LabGroupsRequest
import com.bwell.healthdata.lab.requests.LabKnowledgeRequest
import com.bwell.healthdata.lab.requests.LabsRequest
import com.bwell.sampleapp.repository.LabsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LabsViewModel (private val repository: LabsRepository?) : ViewModel() {

    private val _labGroupsResults = MutableStateFlow<BWellResult<LabGroup>?>(null)
    val labGroupsResults: StateFlow<BWellResult<LabGroup>?> = _labGroupsResults

    fun getLabGroups(labGroupsRequest: LabGroupsRequest) {
        viewModelScope.launch {
            try {
                repository?.getLabGroups(labGroupsRequest)?.collect { result ->
                    _labGroupsResults.emit(result)
                }
            } catch (e: Exception) {
                // Handle exceptions, if any
            }
        }
    }

    private val _labsResults = MutableStateFlow<BWellResult<Observation>?>(null)
    val labsResults: StateFlow<BWellResult<Observation>?> = _labsResults

    fun getLabs(labsRequest: LabsRequest) {
        viewModelScope.launch {
            try {
                repository?.getLabs(labsRequest)?.collect { result ->
                    _labsResults.emit(result)
                }
            } catch (e: Exception) {
                // Handle exceptions, if any
            }
        }
    }

    private val _labsKnowledgeResults = MutableStateFlow<BWellResult<LabKnowledge>?>(null)
    val labsKnowledgeResults: StateFlow<BWellResult<LabKnowledge>?> = _labsKnowledgeResults

    fun getLabKnowledge(labKnowledgeRequest: LabKnowledgeRequest) {
        viewModelScope.launch {
            try {
                repository?.getLabKnowledge(labKnowledgeRequest)?.collect { result ->
                    _labsKnowledgeResults.emit(result)
                }
            } catch (e: Exception) {
                // Handle exceptions, if any
            }
        }
    }
}