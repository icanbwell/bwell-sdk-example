package com.bwell.sampleapp.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bwell.common.models.domain.search.Provider
import com.bwell.common.models.responses.BWellResult
import com.bwell.common.models.responses.OperationOutcome
import com.bwell.connections.requests.ConnectionCreateRequest
import com.bwell.sampleapp.repository.ProviderRepository
import com.bwell.search.requests.ConnectionRequest
import com.bwell.search.requests.ProviderSearchRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


class ProviderViewModel(private val repository: ProviderRepository?) : ViewModel() {

    private val _searchResults = MutableStateFlow<BWellResult<Provider>?>(null)
    val searchResults: StateFlow<BWellResult<Provider>?> = _searchResults

    fun searchProviders(providerSearchRequest: ProviderSearchRequest) {
        viewModelScope.launch {
            try {
                repository?.searchProviders(providerSearchRequest)?.collect { searchResult ->
                    _searchResults.emit(searchResult)
                }
            } catch (e: Exception) {
                Log.e("Exception-","${e.message}")
            }
        }
    }

    private val _filteredResults = MutableStateFlow<List<Provider>?>(null)
    val filteredResults: StateFlow<List<Provider>?> = _filteredResults

    fun filterDataConnectionsProviders(query: String) {
        viewModelScope.launch {
            val searchResult = _searchResults.value
            if (searchResult != null) {
                if (searchResult is BWellResult.SearchResults) {
                    val dataConnectionsList = searchResult.data
                    val filteredList = dataConnectionsList?.filter { provider ->
                        provider?.name?.get(0)?.text?.contains(query, ignoreCase = true) == true
                    }
                    _filteredResults.emit(filteredList)
                }

            }
        }
    }

    private val _requestConnectionData = MutableStateFlow<OperationOutcome?>(null)
    val requestConnectionData: StateFlow<OperationOutcome?> = _requestConnectionData

    fun requestConnection(connectionRequest: ConnectionRequest) {
        viewModelScope.launch {
            try {
                repository?.requestConnection(connectionRequest)?.collect { connectionOutcome ->
                    _requestConnectionData.emit(connectionOutcome)
                }
            } catch (_: Exception) {
            }
        }
    }

}