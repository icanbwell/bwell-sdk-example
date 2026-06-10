package com.bwell.sampleapp.repository

import com.bwell.sampleapp.singletons.BWellSdk
import com.bwell.common.models.responses.BWellResult
import com.bwell.provider.requests.organization.OrganizationRequest
import com.bwell.provider.requests.practitioner.PractitionerRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class ProviderResourcesRepository {

    suspend fun getPractitioners(practitionerRequest: PractitionerRequest): Flow<BWellResult<Any>?> = flow {
        try {
            val practitionerResult = BWellSdk.provider.getPractitioners(practitionerRequest)
            emit(practitionerResult)
        } catch (e: Exception) {
            emit(null)
        }
    }

    suspend fun getOrganizations(organizationRequest: OrganizationRequest): Flow<BWellResult<Any>?> = flow {
        try {
            val organizationResult = BWellSdk.provider.getOrganizations(organizationRequest)
            emit(organizationResult)
        } catch (e: Exception) {
            emit(null)
        }
    }
}
