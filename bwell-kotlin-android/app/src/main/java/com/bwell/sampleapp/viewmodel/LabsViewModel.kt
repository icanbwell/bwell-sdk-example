package com.bwell.sampleapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bwell.common.models.domain.healthdata.common.observation.Observation
import com.bwell.common.models.domain.healthdata.healthsummary.diagnosticreport.DiagnosticReport
import com.bwell.common.models.domain.healthdata.healthsummary.diagnosticreportlab.DiagnosticReportLabGroup
import com.bwell.common.models.domain.healthdata.lab.LabGroup
import com.bwell.common.models.domain.healthdata.lab.LabKnowledge
import com.bwell.common.models.responses.BWellResult
import com.bwell.healthdata.healthsummary.requests.diagnosticreport.DiagnosticReportRequest
import com.bwell.healthdata.healthsummary.requests.diagnosticreportlab.DiagnosticReportLabGroupsRequest
import com.bwell.healthdata.lab.requests.LabGroupsRequest
import com.bwell.healthdata.lab.requests.LabKnowledgeRequest
import com.bwell.healthdata.lab.requests.LabsRequest
import com.bwell.sampleapp.repository.LabsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LabsViewModel(private val repository: LabsRepository?) : ViewModel() {
    private val _groupLabResults = MutableStateFlow<BWellResult<LabGroup>?>(null)
    val groupLabResults: StateFlow<BWellResult<LabGroup>?> = _groupLabResults

    fun getLabGroups(labRequest: LabGroupsRequest?) {
        viewModelScope.launch {
            try {
                repository?.getLabGroups(labRequest)?.collect { result ->
                    _groupLabResults.emit(result)
                }
            } catch (e: Exception) {
                // Handle exceptions, if any
            }
        }
    }

    private val _filteredGroupLabResults = MutableStateFlow<List<LabGroup>?>(null)
    val filteredGroupLabResults: StateFlow<List<LabGroup>?> = _filteredGroupLabResults

    fun filterGroupLabList(query: String) {
        viewModelScope.launch {
            val groupLabResult = _groupLabResults.value
            if (groupLabResult != null) {
                val filteredList = (groupLabResult as BWellResult.ResourceCollection).data?.filter { lab ->
                    lab.name?.contains(query, ignoreCase = true) == true
                }?.map { it } // Extracting names
                _filteredGroupLabResults.value = filteredList
            }
        }
    }

    private val _labStatementsResults = MutableStateFlow<BWellResult<Observation>?>(null)
    val labResults: StateFlow<BWellResult<Observation>?> = _labStatementsResults

    fun getLabs(labRequest: LabsRequest) {
        viewModelScope.launch {
            try {
                repository?.getLabs(labRequest)?.collect { result ->
                    _labStatementsResults.emit(result)
                }
            } catch (e: Exception) {
                // Handle exceptions, if any
            }
        }
    }

    private val _labKnowledgeResults = MutableStateFlow<BWellResult<LabKnowledge>?>(null)
    val labKnowledgeResults: StateFlow<BWellResult<LabKnowledge>?> = _labKnowledgeResults

    fun getLabKnowledge(labKnowledgeRequest: LabKnowledgeRequest) {
        viewModelScope.launch {
            try {
                repository?.getLabKnowledge(labKnowledgeRequest)?.collect { result ->
                    _labKnowledgeResults.emit(result)
                }
            } catch (e: Exception) {
                // Handle exceptions, if any
            }
        }
    }

    private val _diagnosticReportGroupResults = MutableStateFlow<BWellResult<DiagnosticReportLabGroup>?>(null)
    val diagnosticReportLabGroupResults: StateFlow<BWellResult<DiagnosticReportLabGroup>?> = _diagnosticReportGroupResults

    fun getDiagnosticReportLabGroups(diagnosticReportLabGroupRequest: DiagnosticReportLabGroupsRequest) {
        viewModelScope.launch {
            try {
                repository?.getDiagnosticReportGroups(diagnosticReportLabGroupRequest)?.collect { result ->
                        _diagnosticReportGroupResults.emit(result)
                }
            } catch (e: Exception) {
                // Handle exceptions, if any
            }
        }
    }

    private val _diagnosticReportResults = MutableStateFlow<BWellResult<DiagnosticReport>?>(null)
    val diagnosticReporportResults: StateFlow<BWellResult<DiagnosticReport>?> = _diagnosticReportResults

    fun getDiagnosticReports(diagnosticReportRequest: DiagnosticReportRequest) {
        viewModelScope.launch {
            try {
                repository?.getDiagnosticReports(diagnosticReportRequest)?.collect { result ->
                    _diagnosticReportResults.emit(result)
                }
            } catch (e: Exception) {
                // Handle exceptions, if any
            }
        }
    }
}