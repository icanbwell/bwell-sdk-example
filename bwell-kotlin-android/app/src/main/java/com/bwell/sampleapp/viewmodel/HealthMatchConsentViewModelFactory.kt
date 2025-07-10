package com.bwell.sampleapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.bwell.sampleapp.repository.Repository

class HealthMatchConsentViewModelFactory(private val repository: Repository?) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(HealthMatchConsentViewModel::class.java) -> {
                HealthMatchConsentViewModel(repository) as T
            }

            else -> throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}