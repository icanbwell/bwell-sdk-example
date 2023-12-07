package com.bwell.sampleapp.repository

import android.content.Context
import com.bwell.BWellSdk
import com.bwell.common.models.domain.search.Provider
import com.bwell.common.models.responses.BWellResult
import com.bwell.search.requests.ProviderSearchRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class ProviderRepository(private val applicationContext: Context) {

    suspend fun searchProviders(providerSearchRequest: ProviderSearchRequest): Flow<BWellResult<Provider>?> = flow {
        try {
            val searchResult = BWellSdk.search?.searchProviders(providerSearchRequest)
            emit(searchResult)
        } catch (e: Exception) {
            emit(null)
        }
    }

}