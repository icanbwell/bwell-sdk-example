package com.bwell.sampleapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bwell.sampleapp.activities.ui.questionnaire.QuestionnaireItem
import com.bwell.sampleapp.repository.QuestionnaireRepository
import kotlinx.coroutines.launch

/**
 * ViewModel for managing questionnaire data
 */
class QuestionnaireViewModel(private val repository: QuestionnaireRepository?) : ViewModel() {

    val questionnaireData: LiveData<List<QuestionnaireItem>>
        get() = repository?.questionnaireData ?: throw IllegalStateException("Repository is null")

    init {
        loadQuestionnaires()
    }

    private fun loadQuestionnaires() {
        viewModelScope.launch {
            repository?.getQuestionnaireList()
        }
    }
}
