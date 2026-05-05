package com.bwell.sampleapp.repository

import com.bwell.common.models.domain.healthdata.healthsummary.careteam.CareTeamMember
import com.bwell.common.models.domain.healthdata.healthsummary.careteam.CareTeamMutationResult
import com.bwell.common.models.domain.healthdata.healthsummary.careteam.MemberPlanIdentifiersResult
import com.bwell.common.models.responses.BWellResult
import com.bwell.healthdata.healthsummary.requests.careteam.AddCareTeamMemberRequest
import com.bwell.healthdata.healthsummary.requests.careteam.CareTeamMembersRequest
import com.bwell.healthdata.healthsummary.requests.careteam.CareTeamParticipantInput
import com.bwell.healthdata.healthsummary.requests.careteam.RemoveCareTeamMemberRequest
import com.bwell.healthdata.healthsummary.requests.careteam.UpdateCareTeamMemberRequest
import com.bwell.sampleapp.singletons.BWellSdk
import com.bwell.user.requests.consents.ReferenceInput
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

    suspend fun addCareTeamMember(reference: String, type: String?, display: String?): Flow<BWellResult<CareTeamMutationResult>?> = flow {
        try {
            val request = AddCareTeamMemberRequest.Builder()
                .participant(
                    CareTeamParticipantInput(
                        member = ReferenceInput(reference = reference, type = type, display = display)
                    )
                )
                .build()
            val result = BWellSdk.health.addCareTeamMember(request)
            emit(result)
        } catch (e: Exception) {
            emit(null)
        }
    }

    suspend fun removeCareTeamMember(reference: String): Flow<BWellResult<CareTeamMutationResult>?> = flow {
        try {
            val request = RemoveCareTeamMemberRequest.Builder()
                .participant(
                    CareTeamParticipantInput(
                        member = ReferenceInput(reference = reference)
                    )
                )
                .build()
            val result = BWellSdk.health.removeCareTeamMember(request)
            emit(result)
        } catch (e: Exception) {
            emit(null)
        }
    }

    suspend fun updateCareTeamMember(reference: String, display: String?): Flow<BWellResult<CareTeamMutationResult>?> = flow {
        try {
            val request = UpdateCareTeamMemberRequest.Builder()
                .participant(
                    CareTeamParticipantInput(
                        member = ReferenceInput(reference = reference, display = display)
                    )
                )
                .build()
            val result = BWellSdk.health.updateCareTeamMember(request)
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
