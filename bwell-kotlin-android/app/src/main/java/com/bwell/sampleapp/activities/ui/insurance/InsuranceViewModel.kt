package com.bwell.sampleapp.activities.ui.insurance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bwell.common.models.domain.financials.coverage.Coverage
import com.bwell.common.models.domain.financials.explanationOfBenefit.ExplanationOfBenefit
import com.bwell.common.models.responses.BWellResult
import com.bwell.sampleapp.repository.InsuranceRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class InsuranceViewModel(private val repository: InsuranceRepository?) : ViewModel() {

    private val _coverages = MutableStateFlow<BWellResult<Coverage>?>(null)
    val coverages: StateFlow<BWellResult<Coverage>?> = _coverages

    private val _claims = MutableStateFlow<BWellResult<ExplanationOfBenefit>?>(null)
    val claims: StateFlow<BWellResult<ExplanationOfBenefit>?> = _claims

    fun loadCoverages() {
        viewModelScope.launch {
            repository?.getCoverages()?.collect { _coverages.emit(it) }
        }
    }

    private var claimsJob: Job? = null

    /** @param coverageId bare Coverage id, or null for all plans. */
    fun loadClaims(coverageId: String?) {
        // Cancel any in-flight claims load so rapid plan switches can't resolve
        // out of order and render stale claims under the wrong plan.
        claimsJob?.cancel()
        claimsJob = viewModelScope.launch {
            repository?.getClaims(coverageId)?.collect { _claims.emit(it) }
        }
    }
}
