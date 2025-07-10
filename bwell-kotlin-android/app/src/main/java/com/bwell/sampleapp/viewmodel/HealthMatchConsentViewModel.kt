package com.bwell.sampleapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bwell.common.models.domain.consent.enums.ConsentCategoryCode
import com.bwell.common.models.domain.consent.enums.ConsentProvisionType
import com.bwell.common.models.domain.consent.enums.ConsentStatus
import com.bwell.sampleapp.repository.Repository
import com.bwell.user.requests.consents.ConsentCreateRequest
import kotlinx.coroutines.launch

class HealthMatchConsentViewModel (private val repository: Repository?) : ViewModel() {
    private val _consentState = MutableLiveData<ConsentState>()
    val consentState: LiveData<ConsentState> = _consentState

    sealed class ConsentState {
        object Initial : ConsentState()
        object Loading : ConsentState()
        object Success : ConsentState()
        data class Error(val message: String) : ConsentState()
    }

    fun submitConsent(isPermitted: Boolean) {
        viewModelScope.launch {
            _consentState.value = ConsentState.Loading
            try {
                val provision = when {
                    isPermitted -> ConsentProvisionType.PERMIT
                    else -> ConsentProvisionType.DENY
                }

                val createConsentRequest = ConsentCreateRequest.Builder()
                    .category(ConsentCategoryCode.HEALTH_MATCH)
                    .status(ConsentStatus.ACTIVE)
                    .provision(provision)
                    .build()

                repository?.createConsent(createConsentRequest)?.collect { outcome ->
                    outcome?.let {
                        if (outcome.success()) {
                            _consentState.value = ConsentState.Success
                        } else {
                            _consentState.value = ConsentState.Error("Consent creation failed")
                        }
                    }
                }
            } catch (e: Exception) {
                _consentState.value = ConsentState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }
}