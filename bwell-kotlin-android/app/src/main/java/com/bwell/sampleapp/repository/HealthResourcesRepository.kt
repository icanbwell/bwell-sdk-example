package com.bwell.sampleapp.repository

import com.bwell.sampleapp.singletons.BWellSdk
import com.bwell.common.models.domain.search.HealthResource
import com.bwell.common.models.responses.BWellResult
import com.bwell.search.requests.healthresource.HealthResourceSearchRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class HealthResourcesRepository {

    suspend fun searchHealthResources(request: HealthResourceSearchRequest): Flow<BWellResult<HealthResource>?> = flow {
        try {
            val searchResult = BWellSdk.search.searchHealthResources(request)
            emit(searchResult)
        } catch (e: Exception) {
            emit(null)
        }
    }
}
