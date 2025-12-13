package com.bwell.sampleapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bwell.common.models.domain.financials.explanationOfBenefit.ExplanationOfBenefit
import com.bwell.common.models.responses.BWellResult
import com.bwell.financials.requests.explanationofbenefit.ExplanationOfBenefitRequest
import com.bwell.sampleapp.repository.FinancialsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


class EOBViewModel(private val repository: FinancialsRepository?) : ViewModel() {
    private val _eobResults = MutableStateFlow<BWellResult<ExplanationOfBenefit>?>(null)
    val eobResults: StateFlow<BWellResult<ExplanationOfBenefit>?> = _eobResults

    private val _showJsonMode = MutableStateFlow(false)
    val showJsonMode: StateFlow<Boolean> = _showJsonMode

    fun toggleJsonMode() {
        _showJsonMode.value = !_showJsonMode.value
    }

    fun getEOB(eobRequest: ExplanationOfBenefitRequest) {
        viewModelScope.launch {
            try {
                repository?.getEOB(eobRequest)?.collect { result ->
                    _eobResults.emit(result)
                }
            } catch (e: Exception) {
                _eobResults.value = null
            }
        }
    }

}

