package com.bwell.sampleapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.bwell.sampleapp.repository.DataConnectionLabsRepository

class DataConnectionsLabsViewModelFactory(private val repository: DataConnectionLabsRepository?) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(DataConnectionLabsViewModel::class.java) -> {
                DataConnectionLabsViewModel(repository) as T
            }
            // Add more ViewModel classes as needed
            else -> throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}