package com.bwell.sampleapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bwell.common.models.domain.user.Person
import com.bwell.common.models.responses.BWellResult
import com.bwell.sampleapp.model.SuggestedActivitiesLIst
import com.bwell.sampleapp.repository.Repository
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
}