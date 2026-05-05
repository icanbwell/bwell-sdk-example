package com.bwell.sampleapp.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bwell.common.models.domain.search.HealthResource
import com.bwell.common.models.responses.BWellResult
import com.bwell.sampleapp.repository.HealthResourcesRepository
import com.bwell.search.requests.healthresource.HealthResourceSearchRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HealthResourcesViewModel(private val repository: HealthResourcesRepository?) : ViewModel() {

    private val _searchResults = MutableStateFlow<BWellResult<HealthResource>?>(null)
    val searchResults: StateFlow<BWellResult<HealthResource>?> = _searchResults

    private val _searchError = MutableStateFlow<String?>(null)
    val searchError: StateFlow<String?> = _searchError

    private val _filteredResults = MutableStateFlow<List<HealthResource>?>(null)
    val filteredResults: StateFlow<List<HealthResource>?> = _filteredResults

    fun searchHealthResources(request: HealthResourceSearchRequest) {
        viewModelScope.launch {
            _searchError.emit(null)
            try {
                repository?.searchHealthResources(request)?.collect { searchResult ->
                    _searchResults.emit(searchResult)
                }
            } catch (e: Exception) {
                Log.e("HealthResourcesVM", "Search failed", e)
                _searchError.emit("Search failed: ${e.message}")
            }
        }
    }

    fun filterResults(query: String) {
        viewModelScope.launch {
            val searchResult = _searchResults.value
            if (searchResult is BWellResult.SearchResults) {
                val list = searchResult.data
                val filtered = list?.filter { resource ->
                    resource.content?.contains(query, ignoreCase = true) == true
                }
                _filteredResults.emit(filtered)
            }
        }
    }
}
