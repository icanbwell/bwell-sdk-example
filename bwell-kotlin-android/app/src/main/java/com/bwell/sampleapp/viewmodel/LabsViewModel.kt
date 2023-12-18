package com.bwell.sampleapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bwell.common.models.domain.healthdata.observation.Observation
import com.bwell.common.models.responses.BWellResult
import com.bwell.healthdata.lab.LabDetailRequest
import com.bwell.healthdata.lab.LabKnowledgeRequest
import com.bwell.healthdata.lab.LabRequest
import com.bwell.sampleapp.repository.LabsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LabsViewModel (private val repository: LabsRepository?) : ViewModel() {

    private val _labsResults = MutableStateFlow<BWellResult<Observation>?>(null)
    val labsResults: StateFlow<BWellResult<Observation>?> = _labsResults

    fun getLabsList(labsRequest: LabRequest) {
        viewModelScope.launch {
            try {
                repository?.getLabsList(labsRequest)?.collect { result ->
                    _labsResults.emit(result)
                }
            } catch (e: Exception) {
                // Handle exceptions, if any
            }
        }
    }

    private val _labsDetailResults = MutableStateFlow<BWellResult<Observation>?>(null)
    val labsDetailResults: StateFlow<BWellResult<Observation>?> = _labsDetailResults

    fun getLabsDetails(labsDetailRequest: LabDetailRequest) {
        viewModelScope.launch {
            try {
                repository?.getLabDetails(labsDetailRequest)?.collect { result ->
                    _labsDetailResults.emit(result)
                }
            } catch (e: Exception) {
                // Handle exceptions, if any
            }
        }
    }

    private val _labsKnowledgeResults = MutableStateFlow<BWellResult<String>?>(null)
    val labsKnowledgeResults: StateFlow<BWellResult<String>?> = _labsKnowledgeResults

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