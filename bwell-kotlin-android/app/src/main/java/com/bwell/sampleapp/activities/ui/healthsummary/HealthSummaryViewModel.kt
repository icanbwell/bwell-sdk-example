package com.bwell.sampleapp.activities.ui.healthsummary

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bwell.sampleapp.model.HealthSummaryList
import com.bwell.sampleapp.repository.Repository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HealthSummaryViewModel(private val repository: Repository?) : ViewModel() {
    init {
        viewModelScope.launch(Dispatchers.IO){
            repository?.getHealthSummaryList()
        }
    }

    val healthSummaryData : LiveData<HealthSummaryList>
        get() = repository?.healthSummary!!
}
