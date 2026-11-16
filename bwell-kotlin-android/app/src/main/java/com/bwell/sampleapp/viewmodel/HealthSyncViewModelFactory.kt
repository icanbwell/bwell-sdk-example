package com.bwell.sampleapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.bwell.sampleapp.activities.ui.healthsync.HealthSyncDashboardViewModel
import com.bwell.sampleapp.activities.ui.healthsync.HealthSyncPlaygroundViewModel
import com.bwell.sampleapp.repository.HealthSyncRepository

class HealthSyncViewModelFactory(private val repository: HealthSyncRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(HealthSyncPlaygroundViewModel::class.java) -> {
                HealthSyncPlaygroundViewModel(repository) as T
            }
            modelClass.isAssignableFrom(HealthSyncDashboardViewModel::class.java) -> {
                HealthSyncDashboardViewModel(repository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
