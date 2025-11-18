package com.bwell.sampleapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.bwell.sampleapp.repository.HealthSummaryRepository
import com.bwell.sampleapp.repository.ProviderResourcesRepository

class HealthSummaryViewModelFactory(
    private val repository: HealthSummaryRepository?,
    private val providerResourcesRepository: ProviderResourcesRepository?
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(HealthSummaryViewModel::class.java) -> {
                HealthSummaryViewModel(repository, providerResourcesRepository) as T
            }
            // Add more ViewModel classes as needed
            else -> throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}