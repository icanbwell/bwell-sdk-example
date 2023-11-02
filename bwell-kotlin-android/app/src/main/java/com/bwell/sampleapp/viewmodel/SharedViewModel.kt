package com.bwell.sampleapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bwell.sampleapp.model.SuggestedActivitiesLIst
import com.bwell.sampleapp.repository.Repository
import com.bwell.sampleapp.model.SuggestedDataConnectionsList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SharedViewModel(private val repository: Repository?) : ViewModel() {
    init {
        viewModelScope.launch(Dispatchers.IO){
            repository?.getActivitiesSuggestionList()
            repository?.getDataConnectionsList()
        }
    }

    val suggestedActivities : LiveData<SuggestedActivitiesLIst>
        get() = repository?.suggestedActivities!!

    val suggestedDataConnections : LiveData<SuggestedDataConnectionsList>
        get() = repository?.suggestedDataConnections!!
}