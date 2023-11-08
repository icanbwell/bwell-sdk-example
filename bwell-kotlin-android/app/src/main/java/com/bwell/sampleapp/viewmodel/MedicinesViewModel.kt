package com.bwell.sampleapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bwell.sampleapp.model.SuggestedDataConnectionsCategoriesList
import com.bwell.sampleapp.repository.Repository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MedicinesViewModel(private val repository: Repository?) : ViewModel() {
    init {
        viewModelScope.launch(Dispatchers.IO){
            repository?.getDataConnectionsCategoriesList()
        }
    }

    val suggestedDataConnectionsCategories : LiveData<SuggestedDataConnectionsCategoriesList>
        get() = repository?.suggestedDataConnectionsCategories!!
}