package com.bwell.sampleapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bwell.common.models.domain.user.Person
import com.bwell.common.models.requests.searchtoken.SearchDate
import com.bwell.common.models.responses.BWellResult
import com.bwell.sampleapp.model.SuggestedActivitiesLIst
import com.bwell.sampleapp.repository.Repository
import com.bwell.user.requests.patient.PatientRequest
import com.bwell.user.requests.relatedperson.RelatedPersonRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat

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

    // Patient data StateFlow
    private val _patientResults = MutableStateFlow<BWellResult<*>?>(null)
    val patientResults: StateFlow<BWellResult<*>?> = _patientResults

    // RelatedPerson data StateFlow
    private val _relatedPersonResults = MutableStateFlow<BWellResult<*>?>(null)
    val relatedPersonResults: StateFlow<BWellResult<*>?> = _relatedPersonResults

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
     * Fetch patients using the provided PatientRequest
     * @param patientRequest Request parameters for fetching patients
     */
    fun fetchPatients(patientRequest: PatientRequest) {
        viewModelScope.launch {
            try {
                repository?.fetchPatients(patientRequest)?.collect { result ->
                    _patientResults.emit(result)
                }
            } catch (e: Exception) {
                // Handle exceptions, emit null or error state
                _patientResults.emit(null)
            }
        }
    }

    /**
     * Fetch related persons using the provided RelatedPersonRequest
     * @param relatedPersonRequest Request parameters for fetching related persons
     */
    fun fetchRelatedPersons(relatedPersonRequest: RelatedPersonRequest) {
        viewModelScope.launch {
            try {
                repository?.fetchRelatedPersons(relatedPersonRequest)?.collect { result ->
                    _relatedPersonResults.emit(result)
                }
            } catch (e: Exception) {
                // Handle exceptions, emit null or error state
                _relatedPersonResults.emit(null)
            }
        }
    }

    // Simple test functions
    fun testPatients() {
        val lastUpdatedSearchDate = SearchDate.Builder()
            .greaterThan(SimpleDateFormat("yyyy-MM-dd").parse("2020-02-12"))
            .build()
        val request = PatientRequest
                .Builder()
//               .id("dc8495fc-5403-4878-8db1-fd747daff402")
//                .lastUpdated(lastUpdatedSearchDate)
                .page(0)
                .pageSize(5)
                .build()
        fetchPatients(request)
    }

    fun testRelatedPersons() {
        val lastUpdatedSearchDate = SearchDate.Builder()
            .greaterThan(SimpleDateFormat("yyyy-MM-dd").parse("2020-02-12"))
            .build()
        val request = RelatedPersonRequest.Builder()
//            .id("a1b2c3d4-e5f6-7890-abcd-ef1234567890")
//            .lastUpdated(lastUpdatedSearchDate)
            .page(0).pageSize(5)
            .build()
        fetchRelatedPersons(request)
    }
}