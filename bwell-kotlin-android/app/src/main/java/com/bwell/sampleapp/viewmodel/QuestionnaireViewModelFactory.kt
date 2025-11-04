package com.bwell.sampleapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.bwell.sampleapp.repository.QuestionnaireRepository

/**
 * Factory for creating QuestionnaireViewModel instances
 */
class QuestionnaireViewModelFactory(private val repository: QuestionnaireRepository?) :
    ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(QuestionnaireViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return QuestionnaireViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
