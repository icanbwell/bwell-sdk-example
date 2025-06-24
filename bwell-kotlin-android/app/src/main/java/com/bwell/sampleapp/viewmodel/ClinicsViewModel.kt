package com.bwell.sampleapp.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bwell.common.models.domain.search.HealthResource
import com.bwell.common.models.domain.search.Provider
import com.bwell.common.models.responses.BWellResult
import com.bwell.sampleapp.repository.ClinicsRepository
import com.bwell.search.requests.healthresource.HealthResourceSearchRequest
import com.bwell.search.requests.provider.ProviderSearchRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ClinicsViewModel(private val repository: ClinicsRepository?) : ViewModel() {

    private val TAG = "ClinicsViewModel"

    private val _searchResults = MutableStateFlow<BWellResult<Provider>?>(null)
    //val searchResults: StateFlow<BWellResult<Provider>?> = _searchResults

    fun searchConnections(providerSearchRequest: ProviderSearchRequest) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    repository?.searchConnections(providerSearchRequest)?.collect { searchResult ->
                        _searchResults.emit(searchResult)
                    }
                    Log.i(TAG, "Received ${getLength()} results")
                }
            } catch (e: Exception) {
                // Handle exceptions, if any
            }
        }
    }

    private val _healthResourceSearchResults = MutableStateFlow<BWellResult<HealthResource>?>(null)
    val searchResults: StateFlow<BWellResult<HealthResource>?> = _healthResourceSearchResults

    fun searchHealthResources(healthResourceSearchRequest: HealthResourceSearchRequest) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    repository?.searchHealthResources(healthResourceSearchRequest)
                        ?.collect { healthResourceSearchResult ->
                            _healthResourceSearchResults.emit(healthResourceSearchResult)
                        }
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
                        provider.content?.contains(query, ignoreCase = true) == true
                    }
                    _filteredResults.emit(filteredList)
                }

            }
        }
    }

    fun getLength(): Int? {
        val searchResult = _searchResults.value
        if (searchResult != null) {
            if (searchResult is BWellResult.SearchResults) {
                val dataConnectionsList = searchResult.data
                return dataConnectionsList?.size
            }
        }
        return 0
    }

}