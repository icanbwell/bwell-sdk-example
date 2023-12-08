package com.bwell.sampleapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bwell.common.models.responses.BWellResult
import com.bwell.sampleapp.repository.ClinicsRepository
import com.bwell.search.ProviderSearchQuery
import com.bwell.search.requests.ProviderSearchRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ClinicsViewModel(private val repository: ClinicsRepository?) : ViewModel() {

    private val _searchResults = MutableStateFlow<List<ProviderSearchQuery.Organization?>?>(null)
    val searchResults: StateFlow<List<ProviderSearchQuery.Organization?>?> = _searchResults

    fun searchConnections(providerSearchRequest: ProviderSearchRequest) {
        viewModelScope.launch {
            try {
                repository?.searchConnections(providerSearchRequest)?.collect { searchResult ->
                    if (searchResult is BWellResult.SearchResults) {
                        val dataConnectionsList = searchResult.data
                        val filteredList = dataConnectionsList?.flatMap { item ->
                            item.organization ?: emptyList()
                        }
                        _searchResults.value = filteredList
                    }
                }
            } catch (_: Exception) {
            }
        }
    }

    private val _filteredResults = MutableStateFlow<List<ProviderSearchQuery.Organization?>?>(null)
    val filteredResults: StateFlow<List<ProviderSearchQuery.Organization?>?> = _filteredResults

    fun filterDataConnectionsClinics(query: String) {
        viewModelScope.launch {
            val searchResult = _searchResults.value
            if (searchResult != null) {
                val filteredList = searchResult.filter { item ->
                    item?.name?.contains(query, ignoreCase = true) == true
                }
                _filteredResults.value = filteredList
            }
        }
    }

}