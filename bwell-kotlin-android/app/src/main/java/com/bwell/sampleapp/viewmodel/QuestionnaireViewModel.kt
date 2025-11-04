package com.bwell.sampleapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bwell.sampleapp.activities.ui.questionnaire.QuestionnaireItem
import com.bwell.sampleapp.repository.QuestionnaireRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * ViewModel for managing questionnaire data
 */
class QuestionnaireViewModel(private val repository: QuestionnaireRepository?) : ViewModel() {

    // Explicit call from Fragment (mirrors other explicit getX() usages elsewhere)
    fun getQuestionnaireResponses() {
        viewModelScope.launch(Dispatchers.IO) {
            repository?.getQuestionnaireList()
        }
    }

    val questionnaireData: LiveData<List<QuestionnaireItem>>
        get() = repository?.questionnaireData ?: throw IllegalStateException("Repository is null")
}
