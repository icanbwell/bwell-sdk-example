package com.bwell.sampleapp.repository

import android.content.Context
import com.bwell.BWellSdk
import com.bwell.common.models.domain.search.Provider
import com.bwell.common.models.responses.BWellResult
import com.bwell.common.models.responses.OperationOutcome
import com.bwell.connections.requests.ConnectionCreateRequest
import com.bwell.search.requests.connection.ConnectionRequest
import com.bwell.search.requests.provider.ProviderSearchRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class ProviderRepository {

    suspend fun searchProviders(providerSearchRequest: ProviderSearchRequest): Flow<BWellResult<Provider>?> = flow {
        try {
            val searchResult = BWellSdk.search.searchProviders(providerSearchRequest)
            emit(searchResult)
        } catch (e: Exception) {
            emit(null)
        }
    }

    suspend fun requestConnection(connectionRequest: ConnectionRequest): Flow<OperationOutcome?> = flow {
        try {
            val connectionOutcome = BWellSdk.search?.requestConnection(connectionRequest)
            emit(connectionOutcome)
        } catch (e: Exception) {
            // Handle exceptions, if any
            emit(null)
        }
    }

}