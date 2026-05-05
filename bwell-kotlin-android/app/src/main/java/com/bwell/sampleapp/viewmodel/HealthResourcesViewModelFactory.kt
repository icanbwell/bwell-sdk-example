package com.bwell.sampleapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.bwell.sampleapp.repository.CareTeamMembersRepository
import com.bwell.sampleapp.repository.HealthResourcesRepository

class HealthResourcesViewModelFactory(
    private val repository: HealthResourcesRepository?,
    private val careTeamRepository: CareTeamMembersRepository?
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(HealthResourcesViewModel::class.java) -> {
                @Suppress("UNCHECKED_CAST")
                HealthResourcesViewModel(repository, careTeamRepository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
