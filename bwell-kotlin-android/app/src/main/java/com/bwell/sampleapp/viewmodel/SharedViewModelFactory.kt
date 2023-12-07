package com.bwell.sampleapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.bwell.sampleapp.activities.ui.profile.ProfileViewModel
import com.bwell.sampleapp.repository.Repository

class SharedViewModelFactory(private val repository: Repository?) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(SharedViewModel::class.java) -> {
                SharedViewModel(repository) as T
            }
            modelClass.isAssignableFrom(HealthSummaryViewModel::class.java) -> {
                HealthSummaryViewModel(repository) as T
            }
            modelClass.isAssignableFrom(LabsViewModel::class.java) -> {
                LabsViewModel(repository) as T
            }
            modelClass.isAssignableFrom(HealthJourneyViewModel::class.java) -> {
                HealthJourneyViewModel(repository) as T
            }
            modelClass.isAssignableFrom(ProfileViewModel::class.java) -> {
                ProfileViewModel(repository) as T
            }
            // Add more ViewModel classes as needed
            else -> throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}