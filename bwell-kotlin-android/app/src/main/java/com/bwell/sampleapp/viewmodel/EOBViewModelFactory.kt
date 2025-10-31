package com.bwell.sampleapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.bwell.sampleapp.repository.FinancialsRepository

class EOBViewModelFactory(private val repository: FinancialsRepository?) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EOBViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EOBViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
