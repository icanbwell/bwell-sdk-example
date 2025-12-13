package com.bwell.sampleapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bwell.common.models.domain.common.location.Location
import com.bwell.common.models.domain.healthdata.common.Binary
import com.bwell.common.models.domain.healthdata.healthsummary.documentreference.DocumentReference
import com.bwell.common.models.domain.healthdata.healthsummary.healthsummary.enums.HealthSummaryCategory
import com.bwell.common.models.responses.BWellResult
import com.bwell.healthdata.healthsummary.requests.documentReference.DocumentReferencesRequest
import com.bwell.healthdata.requests.binary.BinaryRequest
import com.bwell.provider.requests.locations.LocationRequest
import com.bwell.sampleapp.model.HealthSummaryList
import com.bwell.sampleapp.repository.HealthSummaryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HealthSummaryViewModel (private val repository: HealthSummaryRepository?) : ViewModel() {

    init {
        viewModelScope.launch(Dispatchers.IO){
            repository?.getHealthSummaryList()
        }
    }

    val healthSummaryData : LiveData<HealthSummaryList>
        get() = repository?.healthSummary!!

    private val _healthSummaryGroupResults = MutableStateFlow<BWellResult<Any>?>(null)
    val healthSummaryGroupResults: StateFlow<BWellResult<Any>?> = _healthSummaryGroupResults

    private val _healthSummaryResults = MutableStateFlow<BWellResult<Any>?>(null)
    val healthSummaryResults: StateFlow<BWellResult<Any>?> = _healthSummaryResults

    fun <T> getHealthSummaryGroupData(request: T, category: HealthSummaryCategory?) {
        viewModelScope.launch {
            try {
                repository?.getHealthSummaryGroupData(request, category)?.collect { result ->
                    _healthSummaryGroupResults.emit(result)
                }
            } catch (e: Exception) {
                // Handle exceptions, if any
            }
        }
    }

    fun <T> getHealthSummaryData(request: T, category: HealthSummaryCategory?) {
        viewModelScope.launch {
            try {
                repository?.getHealthSummaryData(request, category)?.collect { result ->
                    _healthSummaryResults.emit(result)
                }
            } catch (e: Exception) {
                // Handle exceptions, if any
            }
        }
    }

    private val _documentReferencesResults = MutableStateFlow<BWellResult<DocumentReference>?>(null)
    val documentReferenceRequestResults: StateFlow<BWellResult<DocumentReference>?> = _documentReferencesResults
    fun getDocumentReferences(documentReferenceRequest: DocumentReferencesRequest) {
        viewModelScope.launch {
            try {
                repository?.getDocumentReference(documentReferenceRequest)?.collect { result ->
                    _documentReferencesResults.emit(result)
                }
            } catch (e: Exception){
                // Handle Exceptions, if any
            }
        }
    }


    private val _binaryResults = MutableStateFlow<BWellResult<Binary>?>(null)
    val binaryResults: StateFlow<BWellResult<Binary>?> = _binaryResults
    fun getBinary(binaryRequest: BinaryRequest) {
        viewModelScope.launch {
            try {
                repository?.getBinary(binaryRequest)?.collect { result ->
                    _binaryResults.emit(result)
                }
            } catch (e: Exception){
                // Handle Exceptions, if any
            }
        }
    }


    private val _locationResults = MutableStateFlow<BWellResult<Location>?>(null)
    val locationResults: StateFlow<BWellResult<Location>?> = _locationResults
    fun getLocation(locationRequest: LocationRequest) {
        viewModelScope.launch {
            try {
                repository?.getLocation(locationRequest)?.collect { result ->
                    _locationResults.emit(result)
                }
            } catch (e: Exception){
                // Handle Exceptions, if any
            }
        }
    }



    override fun onCleared() {
        super.onCleared()
        repository?.cancelScope()
    }
}