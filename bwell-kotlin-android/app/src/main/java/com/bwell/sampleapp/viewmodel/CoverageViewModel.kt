package com.bwell.sampleapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.bwell.common.models.domain.financials.coverage.Coverage
import com.bwell.common.models.responses.BWellResult
import com.bwell.financials.requests.coverage.CoverageRequest
import com.bwell.sampleapp.repository.FinancialsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for Coverage Fragment
 */
class CoverageViewModel(private val repository: FinancialsRepository?) : ViewModel() {
    
    private val _coverageResults = MutableStateFlow<BWellResult<Coverage>?>(null)
    val coverageResults: StateFlow<BWellResult<Coverage>?> = _coverageResults

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _currentPage = MutableStateFlow(0)
    val currentPage: StateFlow<Int> = _currentPage

    private val _pageSize = MutableStateFlow(20)
    val pageSize: StateFlow<Int> = _pageSize

    private val _totalItems = MutableStateFlow(0)
    val totalItems: StateFlow<Int> = _totalItems

    private val _totalPages = MutableStateFlow(0)
    val totalPages: StateFlow<Int> = _totalPages

    private val _showJsonMode = MutableStateFlow(false)
    val showJsonMode: StateFlow<Boolean> = _showJsonMode

    /**
     * Toggle JSON view mode
     */
    fun toggleJsonMode() {
        _showJsonMode.value = !_showJsonMode.value
    }

    /**
     * Fetch coverage information from bWell SDK
     */
    fun getCoverage(coverageRequest: CoverageRequest) {
        viewModelScope.launch {
            try {
                repository?.getCoverages(coverageRequest)?.collect { result ->
                    _coverageResults.emit(result)
                    // Update total items count for paging
                    when (result) {
                        is BWellResult.ResourceCollection<*> -> {
                            val coverageList = (result as BWellResult.ResourceCollection<Coverage>).data
                            _totalItems.value = coverageList?.size ?: 0
                            // Update totalPages from paging info
                            result.pagingInfo?.let { pagingInfo ->
                                _totalPages.value = pagingInfo.totalPages
                                _totalItems.value = pagingInfo.totalItems
                            }
                        }
                        is BWellResult.SingleResource<*> -> {
                            _totalItems.value = 1
                        }
                        else -> {
                            _totalItems.value = 0
                            _totalPages.value = 0
                        }
                    }
                }
            } catch (e: Exception) {
                // Handle exceptions, if any
                _totalItems.value = 0
                _totalPages.value = 0
            }
        }
    }

    /**
     * Update search query and filter results
     */
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    /**
     * Get filtered coverage list based on search query
     */
    fun getFilteredCoverageList(): List<Coverage>? {
        val coverageResult = _coverageResults.value
        val query = _searchQuery.value
        
        return when (coverageResult) {
            is BWellResult.ResourceCollection<*> -> {
                val coverageList = (coverageResult as BWellResult.ResourceCollection<Coverage>).data
                if (query.isEmpty()) {
                    coverageList
                } else {
                    coverageList?.filter { coverage ->
                        coverage.id?.contains(query, ignoreCase = true) == true
                    }
                }
            }
            is BWellResult.SingleResource<*> -> {
                val coverage = (coverageResult as BWellResult.SingleResource<Coverage>).data
                if (coverage != null && (query.isEmpty() || coverage.id?.contains(query, ignoreCase = true) == true)) {
                    listOf(coverage)
                } else {
                    emptyList()
                }
            }
            else -> null
        }
    }
}
