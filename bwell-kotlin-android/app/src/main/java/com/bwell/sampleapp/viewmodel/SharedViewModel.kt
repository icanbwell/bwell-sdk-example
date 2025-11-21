package com.bwell.sampleapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bwell.common.models.domain.healthdata.common.ServiceRequest
import com.bwell.common.models.domain.healthdata.common.Specimen
import com.bwell.common.models.domain.user.Person
import com.bwell.common.models.responses.BWellResult
import com.bwell.sampleapp.model.SuggestedActivitiesLIst
import com.bwell.sampleapp.repository.Repository
import com.bwell.user.requests.servicerequest.ServiceRequestRequest
import com.bwell.user.requests.specimen.SpecimenRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SharedViewModel(private val repository: Repository?) : ViewModel() {
    init {
        viewModelScope.launch(Dispatchers.IO){
            repository?.getActivitiesSuggestionList()
        }
    }

    val suggestedActivities : LiveData<SuggestedActivitiesLIst>
        get() = repository?.suggestedActivities!!


    private val _userData = MutableStateFlow<Person?>(null)
    val userData: StateFlow<Person?> = _userData

    private val _specimenData = MutableStateFlow<BWellResult<*>?>(null)
    val specimenData: StateFlow<BWellResult<*>?> = _specimenData

    private val _serviceRequestData = MutableStateFlow<BWellResult<*>?>(null)
    val serviceRequestData: StateFlow<BWellResult<*>?> = _serviceRequestData

    fun fetchUserProfile() {
        viewModelScope.launch {
            try {
                repository?.fetchUserProfile()?.collect{
                    if (it is BWellResult.SingleResource<Person>){
                        val person = it.data
                        _userData.emit(person)
                    }
                }
            } catch (_: Exception) { }
        }
    }

    /**
     * Fetch specimens using the provided SpecimensRequest
     * @param specimenRequest Request parameters for fetching specimens
     */
    fun fetchSpecimens(specimenRequest: SpecimenRequest) {
        viewModelScope.launch {
            try {
                repository?.fetchSpecimens(specimenRequest)?.collect { result ->
                    _specimenData.emit(result)
                }
            } catch (e: Exception) {
                // Handle exceptions, emit null or error state
                _specimenData.emit(null)
            }
        }
    }

    /**
     * Fetch serviceRequests using the provided ServiceRequestRequest
     * @param serviceRequestRequest Request parameters for fetching serviceRequests
     */
    fun fetchServiceRequests(serviceRequestRequest: ServiceRequestRequest) {
        viewModelScope.launch {
            try {
                repository?.fetchServiceRequests(serviceRequestRequest)?.collect { result ->
                    _serviceRequestData.emit(result)
                }
            } catch (e: Exception) {
                // Handle exceptions, emit null or error state
                _serviceRequestData.emit(null)
            }
        }
    }


    // Simple test functions
    fun testSpecimens() {
        val request = SpecimenRequest
            .Builder()
            .page(0)
            .pageSize(5)
            .build()
        fetchSpecimens(request)
    }

    fun testServiceRequests() {
        val request = ServiceRequestRequest
            .Builder()
            .page(0)
            .pageSize(5)
            .build()
        fetchServiceRequests(request)
    }

}