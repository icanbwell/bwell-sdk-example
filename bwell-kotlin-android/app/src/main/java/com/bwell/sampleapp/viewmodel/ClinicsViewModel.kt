package com.bwell.sampleapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bwell.common.models.domain.search.Provider
import com.bwell.common.models.responses.BWellResult
import com.bwell.sampleapp.repository.ClinicsRepository
import com.bwell.search.requests.provider.ProviderSearchRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ClinicsViewModel(private val repository: ClinicsRepository?) : ViewModel() {

    private val _searchResults = MutableStateFlow<BWellResult<Provider>?>(null)
    val searchResults: StateFlow<BWellResult<Provider>?> = _searchResults

    fun searchConnections(providerSearchRequest: ProviderSearchRequest) {
        viewModelScope.launch {
            try {
                repository?.searchConnections(providerSearchRequest)?.collect { searchResult ->
                    _searchResults.emit(searchResult)
                }
            } catch (e: Exception) {
                // Handle exceptions, if any
            }
        }
    }

    private val _filteredResults = MutableStateFlow<List<Provider>?>(null)
    val filteredResults: StateFlow<List<Provider>?> = _filteredResults

    fun filterDataConnectionsClinics(query: String) {
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