package com.bwell.sampleapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bwell.common.models.domain.search.Provider
import com.bwell.common.models.responses.BWellResult
import com.bwell.sampleapp.repository.ProviderRepository
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
                // Handle exceptions, if any
            }
        }
    }

}