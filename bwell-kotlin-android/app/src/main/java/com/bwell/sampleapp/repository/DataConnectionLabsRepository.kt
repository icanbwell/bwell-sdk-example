package com.bwell.sampleapp.repository

import android.content.Context
import com.bwell.BWellSdk
import com.bwell.common.models.domain.search.Provider
import com.bwell.common.models.responses.BWellResult
import com.bwell.search.requests.ProviderSearchRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class DataConnectionLabsRepository(private val applicationContext: Context) {

    suspend fun searchConnections(providerSearchRequest: ProviderSearchRequest): Flow<BWellResult<Provider>?> = flow {
        try {
            val searchResult = BWellSdk.search?.searchConnections(providerSearchRequest)
            emit(searchResult)
        } catch (e: Exception) {
            emit(null)
        }
    }

}