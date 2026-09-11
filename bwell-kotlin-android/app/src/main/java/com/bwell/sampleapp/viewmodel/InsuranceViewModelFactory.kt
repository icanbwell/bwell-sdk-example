package com.bwell.sampleapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.bwell.sampleapp.activities.ui.insurance.InsuranceViewModel
import com.bwell.sampleapp.repository.InsuranceRepository

class InsuranceViewModelFactory(private val repository: InsuranceRepository?) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(InsuranceViewModel::class.java) -> {
                InsuranceViewModel(repository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
