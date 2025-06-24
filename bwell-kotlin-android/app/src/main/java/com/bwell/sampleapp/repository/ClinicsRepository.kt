package com.bwell.sampleapp.repository

import android.content.Context
import com.bwell.common.models.domain.search.HealthResource
import com.bwell.sampleapp.singletons.BWellSdk
import com.bwell.common.models.domain.search.Provider
import com.bwell.common.models.responses.BWellResult
import com.bwell.search.requests.healthresource.HealthResourceSearchRequest
import com.bwell.search.requests.provider.ProviderSearchRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class ClinicsRepository(private val applicationContext: Context) {

    suspend fun searchConnections(providerSearchRequest: ProviderSearchRequest): Flow<BWellResult<Provider>?> = flow {
        try {
            val searchResult = BWellSdk.search?.searchConnections(providerSearchRequest)
            emit(searchResult)
        } catch (e: Exception) {
            emit(null)
        }
    }

    suspend fun searchHealthResources(healthResourceSearchRequest: HealthResourceSearchRequest): Flow<BWellResult<HealthResource>?> = flow {
        try {
            val searchResult = BWellSdk.search?.searchHealthResources(healthResourceSearchRequest)
            emit(searchResult)
        } catch (e: Exception) {
            emit(null)
        }
    }

}