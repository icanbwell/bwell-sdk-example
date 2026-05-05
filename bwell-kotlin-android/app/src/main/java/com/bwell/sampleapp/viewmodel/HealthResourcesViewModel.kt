package com.bwell.sampleapp.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bwell.common.models.domain.healthdata.healthsummary.careteam.CareTeamMember
import com.bwell.common.models.domain.healthdata.healthsummary.careteam.CareTeamMutationResult
import com.bwell.common.models.domain.healthdata.healthsummary.careteam.MemberPlanIdentifiersResult
import com.bwell.common.models.domain.search.HealthResource
import com.bwell.common.models.responses.BWellResult
import com.bwell.sampleapp.repository.CareTeamMembersRepository
import com.bwell.sampleapp.repository.HealthResourcesRepository
import com.bwell.search.requests.healthresource.HealthResourceSearchRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HealthResourcesViewModel(
    private val repository: HealthResourcesRepository?,
    private val careTeamRepository: CareTeamMembersRepository?
) : ViewModel() {

    private val _searchResults = MutableStateFlow<BWellResult<HealthResource>?>(null)
    val searchResults: StateFlow<BWellResult<HealthResource>?> = _searchResults

    private val _filteredResults = MutableStateFlow<List<HealthResource>?>(null)
    val filteredResults: StateFlow<List<HealthResource>?> = _filteredResults

    private val _careTeamMembers = MutableStateFlow<BWellResult<CareTeamMember>?>(null)
    val careTeamMembers: StateFlow<BWellResult<CareTeamMember>?> = _careTeamMembers

    private val _careTeamMutation = MutableStateFlow<BWellResult<CareTeamMutationResult>?>(null)
    val careTeamMutation: StateFlow<BWellResult<CareTeamMutationResult>?> = _careTeamMutation

    private val _planIdentifiers = MutableStateFlow<BWellResult<MemberPlanIdentifiersResult>?>(null)
    val planIdentifiers: StateFlow<BWellResult<MemberPlanIdentifiersResult>?> = _planIdentifiers

    fun searchHealthResources(request: HealthResourceSearchRequest) {
        viewModelScope.launch {
            try {
                repository?.searchHealthResources(request)?.collect { searchResult ->
                    _searchResults.emit(searchResult)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Search failed", e)
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

    // MARK: - Care Team Members

    fun getCareTeamMembers() {
        viewModelScope.launch {
            try {
                careTeamRepository?.getCareTeamMembers(null)?.collect { result ->
                    _careTeamMembers.emit(result)
                }
            } catch (e: Exception) {
                Log.e(TAG, "getCareTeamMembers failed", e)
            }
        }
    }

    fun addCareTeamMember(reference: String, type: String?, display: String?) {
        viewModelScope.launch {
            try {
                careTeamRepository?.addCareTeamMember(reference, type, display)?.collect { result ->
                    _careTeamMutation.emit(result)
                }
            } catch (e: Exception) {
                Log.e(TAG, "addCareTeamMember failed", e)
            }
        }
    }

    fun removeCareTeamMember(reference: String) {
        viewModelScope.launch {
            try {
                careTeamRepository?.removeCareTeamMember(reference)?.collect { result ->
                    _careTeamMutation.emit(result)
                }
            } catch (e: Exception) {
                Log.e(TAG, "removeCareTeamMember failed", e)
            }
        }
    }

    fun getMemberPlanIdentifiers() {
        viewModelScope.launch {
            try {
                careTeamRepository?.getMemberPlanIdentifiers()?.collect { result ->
                    _planIdentifiers.emit(result)
                }
            } catch (e: Exception) {
                Log.e(TAG, "getMemberPlanIdentifiers failed", e)
            }
        }
    }

    companion object {
        private const val TAG = "HealthResourcesVM"
    }
}
