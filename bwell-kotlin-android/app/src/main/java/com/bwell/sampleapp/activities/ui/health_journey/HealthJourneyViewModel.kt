package com.bwell.sampleapp.activities.ui.health_journey

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bwell.sampleapp.model.HealthJourneyList
import com.bwell.sampleapp.repository.Repository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HealthJourneyViewModel (private val repository: Repository?) : ViewModel() {

    init {
        viewModelScope.launch(Dispatchers.IO){
            repository?.getHealthJourneyList()
        }
    }

    val healthJourneyData : LiveData<HealthJourneyList>
        get() = repository?.healthJourney!!
}