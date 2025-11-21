package com.bwell.sampleapp.repository

import android.content.Context
import android.util.Log
import com.bwell.common.models.domain.healthdata.common.observation.Observation
import com.bwell.common.models.domain.healthdata.healthsummary.diagnosticreport.DiagnosticReport
import com.bwell.common.models.domain.healthdata.healthsummary.diagnosticreportlab.DiagnosticReportLabGroup
import com.bwell.sampleapp.singletons.BWellSdk
import com.bwell.common.models.domain.healthdata.lab.LabGroup
import com.bwell.common.models.domain.healthdata.lab.LabKnowledge
import com.bwell.common.models.responses.BWellResult
import com.bwell.healthdata.healthsummary.requests.diagnosticreport.DiagnosticReportRequest
import com.bwell.healthdata.healthsummary.requests.diagnosticreportlab.DiagnosticReportLabGroupsRequest
import com.bwell.healthdata.lab.requests.LabGroupsRequest
import com.bwell.healthdata.lab.requests.LabKnowledgeRequest
import com.bwell.healthdata.lab.requests.LabsRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class LabsRepository(private val applicationContext: Context) {

    suspend fun getLabGroups(labGroupsRequest: LabGroupsRequest?): Flow<BWellResult<LabGroup>?> = flow {
        try {
            val labGroupsResult = BWellSdk.health?.getLabGroups(labGroupsRequest)
            emit(labGroupsResult)
        } catch (e: Exception) {
            emit(null)
        }
    }

    suspend fun getLabs(labsRequest: LabsRequest): Flow<BWellResult<Observation>?> = flow {
        try {
            val labsResult = BWellSdk.health?.getLabs(labsRequest)
            emit(labsResult)
        } catch (e: Exception) {
            emit(null)
        }
    }

    suspend fun getLabKnowledge(labKnowledgeRequest: LabKnowledgeRequest): Flow<BWellResult<LabKnowledge>?> = flow {
        try {
            val labKnowledgeResults = BWellSdk.health?.getLabKnowledge(labKnowledgeRequest)
            emit(labKnowledgeResults)
        } catch (e: Exception) {
            emit(null)
        }
    }
<<<<<<< Updated upstream

    /**
     * Fetch diagnostic report groups using the provided DiagnosticReportLabGroupsRequest
     * @param diagnosticReportGroupRequest Request parameters for fetching DiagnosticReportLabGroups
     * @return Flow of BWellResult with DiagnosticReportLabGroup data
     */
    suspend fun getDiagnosticReportGroups(diagnosticReportGroupRequest: DiagnosticReportLabGroupsRequest): Flow<BWellResult<DiagnosticReportLabGroup>?> = flow {
        try {
            val diagnosticReportGroupResult = BWellSdk.health.getDiagnosticReportLabGroups(diagnosticReportGroupRequest)
            emit(diagnosticReportGroupResult)
        } catch (e: Exception) {
            emit(null)
        }
    }
=======
g
>>>>>>> Stashed changes

    /**
     * Fetch diagnostic reports using the provided DiagnosticReportRequest
     * @param diagnosticReportRequest Request parameters for fetching diagnostic reports
     * @return Flow of BWellResult with DiagnosticReport data
     */
    suspend fun getDiagnosticReports(diagnosticReportRequest: DiagnosticReportRequest): Flow<BWellResult<DiagnosticReport>?> = flow {
        try {
            val diagnosticReportResult = BWellSdk.health.getDiagnosticReports(diagnosticReportRequest)
            emit(diagnosticReportResult)
        } catch (e: Exception) {
            emit(null)
            Log.e("Error", "Failed to get diagnostic reports: ${e.message} ")
        }
    }

}