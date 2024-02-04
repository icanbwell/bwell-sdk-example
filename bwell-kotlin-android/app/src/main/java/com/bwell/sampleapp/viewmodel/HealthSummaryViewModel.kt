package com.bwell.sampleapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bwell.common.models.domain.healthdata.healthsummary.healthsummary.enums.HealthSummaryCategory
import com.bwell.common.models.responses.BWellResult
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

    override fun onCleared() {
        super.onCleared()
        repository?.cancelScope()
    }
}