package com.bwell.sampleapp.repository

import com.bwell.common.models.domain.healthdata.healthsummary.careteam.CareTeamMember
import com.bwell.common.models.domain.healthdata.healthsummary.careteam.CareTeamMutationResult
import com.bwell.common.models.domain.healthdata.healthsummary.careteam.MemberPlanIdentifiersResult
import com.bwell.common.models.responses.BWellResult
import com.bwell.healthdata.healthsummary.requests.careteam.AddCareTeamMemberRequest
import com.bwell.healthdata.healthsummary.requests.careteam.CareTeamMemberType
import com.bwell.healthdata.healthsummary.requests.careteam.CareTeamMembersRequest
import com.bwell.healthdata.healthsummary.requests.careteam.RemoveCareTeamMemberRequest
import com.bwell.healthdata.healthsummary.requests.careteam.UpdateCareTeamMemberRequest
import com.bwell.sampleapp.singletons.BWellSdk
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class CareTeamMembersRepository {

    suspend fun getCareTeamMembers(request: CareTeamMembersRequest?): Flow<BWellResult<CareTeamMember>?> = flow {
        try {
            val result = BWellSdk.health.getCareTeamMembers(request)
            emit(result)
        } catch (e: Exception) {
            emit(null)
        }
    }

    suspend fun addCareTeamMember(id: String, type: CareTeamMemberType, role: List<String>): Flow<BWellResult<CareTeamMutationResult>?> = flow {
        try {
            val request = AddCareTeamMemberRequest.Builder()
                .id(id)
                .type(type)
                .role(role)
                .build()
            val result = BWellSdk.health.addCareTeamMember(request)
            emit(result)
        } catch (e: Exception) {
            emit(null)
        }
    }

    suspend fun removeCareTeamMember(id: String, type: CareTeamMemberType): Flow<BWellResult<CareTeamMutationResult>?> = flow {
        try {
            val request = RemoveCareTeamMemberRequest.Builder()
                .id(id)
                .type(type)
                .build()
            val result = BWellSdk.health.removeCareTeamMember(request)
            emit(result)
        } catch (e: Exception) {
            emit(null)
        }
    }

    suspend fun updateCareTeamMember(id: String, type: CareTeamMemberType, role: List<String>?): Flow<BWellResult<CareTeamMutationResult>?> = flow {
        try {
            val request = UpdateCareTeamMemberRequest.Builder()
                .id(id)
                .type(type)
                .apply { if (role != null) role(role) }
                .build()
            val result = BWellSdk.health.updateCareTeamMember(request)
            emit(result)
        } catch (e: Exception) {
            emit(null)
        }
    }

    suspend fun addCareTeamMemberAsPCP(id: String, type: CareTeamMemberType): Flow<BWellResult<CareTeamMutationResult>?> = flow {
        try {
            val request = AddCareTeamMemberRequest.Builder()
                .id(id)
                .type(type)
                .role(listOf("PCP"))
                .build()
            val result = BWellSdk.health.addCareTeamMember(request)
            emit(result)
        } catch (e: Exception) {
            emit(null)
        }
    }

    suspend fun getMemberPlanIdentifiers(): Flow<BWellResult<MemberPlanIdentifiersResult>?> = flow {
        try {
            val result = BWellSdk.health.getMemberPlanIdentifiers()
            emit(result)
        } catch (e: Exception) {
            emit(null)
        }
    }
}
