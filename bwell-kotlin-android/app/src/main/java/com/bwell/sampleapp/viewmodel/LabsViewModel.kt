package com.bwell.sampleapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bwell.sampleapp.repository.Repository
import com.bwell.sampleapp.model.LabsList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LabsViewModel (private val repository: Repository?) : ViewModel() {

    init {
        viewModelScope.launch(Dispatchers.IO){
            repository?.getLabsList()
        }
    }

    val labsData : LiveData<LabsList>
        get() = repository?.labs!!
}