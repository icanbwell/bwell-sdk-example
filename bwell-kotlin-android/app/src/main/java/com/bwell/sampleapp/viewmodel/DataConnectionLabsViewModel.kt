package com.bwell.sampleapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bwell.common.models.domain.search.HealthResource
import com.bwell.common.models.domain.search.Provider
import com.bwell.common.models.responses.BWellResult
import com.bwell.sampleapp.repository.DataConnectionLabsRepository
import com.bwell.search.requests.healthresource.HealthResourceSearchRequest
import com.bwell.search.requests.provider.ProviderSearchRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


class DataConnectionLabsViewModel(private val repository: DataConnectionLabsRepository?) : ViewModel() {

    private val _searchResults = MutableStateFlow<BWellResult<HealthResource>?>(null)
    val searchResults: StateFlow<BWellResult<HealthResource>?> = _searchResults

    /*fun searchConnections(providerSearchRequest: ProviderSearchRequest) {
        viewModelScope.launch {
            try {
                repository?.searchConnections(providerSearchRequest)?.collect { searchResult ->
                    _searchResults.emit(searchResult)
                }
            } catch (e: Exception) {
                // Handle exceptions, if any
            }
        }
    }*/

    fun searchHealthResources(healthResourceSearchRequest: HealthResourceSearchRequest) {
        viewModelScope.launch {
            try {
                repository?.searchHealthResources(healthResourceSearchRequest)?.collect { healthResourceSearchResult ->
                    _searchResults.emit(healthResourceSearchResult)
                }
            } catch (e: Exception) {
                // Handle exceptions, if any
            }
        }
    }

    private val _filteredResults = MutableStateFlow<List<HealthResource>?>(null)
    val filteredResults: StateFlow<List<HealthResource>?> = _filteredResults

    fun filterDataConnectionsLabs(query: String) {
        viewModelScope.launch {
            val searchResult = _searchResults.value
            if (searchResult != null) {
                if (searchResult is BWellResult.SearchResults) {
                    val dataConnectionsList = searchResult.data
                    val filteredList = dataConnectionsList?.filter { provider ->
                        provider?.content?.contains(query, ignoreCase = true) == true
                    }
                    _filteredResults.emit(filteredList)
                }

            }
        }
    }

}