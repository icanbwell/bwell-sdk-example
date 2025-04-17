package com.bwell.sampleapp.activities.ui.profile

import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bwell.common.models.domain.consent.Consent
import com.bwell.common.models.domain.consent.enums.ConsentCategoryCode
import com.bwell.common.models.domain.user.Person
import com.bwell.common.models.domain.user.VerificationStatus
import com.bwell.common.models.responses.BWellResult
import com.bwell.sampleapp.repository.Repository
import com.bwell.user.requests.consents.ConsentCreateRequest
import com.bwell.user.requests.createVerificationUrl.CreateVerificationUrlRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter

class ProfileViewModel(private val repository: Repository?) : ViewModel() {

    private val _userData = MutableSharedFlow<Person?>()
    private val _consentData = MutableSharedFlow<Consent?>()
    private val _verificationStatus = MutableSharedFlow<VerificationStatus?>()
    private val _verificationUrl = MutableSharedFlow<String?>()

    val userData: MutableSharedFlow<Person?>
        get() = _userData

    val verificationStatus: MutableSharedFlow<VerificationStatus?>
        get() = _verificationStatus

    val verificationUrl: MutableSharedFlow<String?>
        get() = _verificationUrl

    val consentData: MutableSharedFlow<Consent?>
        get() = _consentData

    companion object {
        private var storedUserData: Person? = null
    }

    fun fetchData() {
        viewModelScope.launch {
            try {
                repository?.fetchUserProfile()?.collect{
                    if (it is BWellResult.SingleResource<Person>){
                        val person = it.data
                        _userData.emit(person)
                    }
                }
            } catch (e: Exception) {
                // Handle errors
            }
        }
    }

    fun fetchVerificationStatus() {
        viewModelScope.launch {
            try {
                repository?.getVerificationStatus()?.collect({
                    if(it is BWellResult.SingleResource<VerificationStatus>) {
                        _verificationStatus.emit(it.data)
                        Log.i("result", it.data.toString())
                    }
                })
            } catch (e: Exception) {
                // Handle errors
            }
        }
    }

    fun createVerificationUrl() {
        viewModelScope.launch {
            try {
                val request = CreateVerificationUrlRequest.Builder().callbackUrl("https://app.staging.icanbwell.com/bwell_demo/#/create-account/ial2-callback").build()
                repository?.createVerificationUrl(request)?.collect({
                    if(it is BWellResult.SingleResource<String>) {
                        Log.i("result", it.data.toString())
                        _verificationUrl.emit(it.data.toString())
                    }
                })
            }catch (e: Exception) {
                // Handle errors
            }
        }
    }

    fun updatePersonData(userData: Person) {
        viewModelScope.launch {
            try {
                val operationOutcomeFlow: Flow<BWellResult<Person>?>? = repository?.saveUserProfile(userData)
                operationOutcomeFlow?.collect { operationOutcome ->
                        _userData.emit(userData)
                }
            } catch (_: Exception) {
            }
        }
    }

    // Function to calculate age
    @SuppressLint("NewApi")
    fun calculateAge(birthDate: String): String {
        val formatter   = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val parsedBirthDate = LocalDate.parse(birthDate, formatter)
        val currentDate = LocalDate.now()
        val period = Period.between(parsedBirthDate, currentDate)
        return period.years.toString()
    }

    fun createConsent() {
        val createConsentRequest = ConsentCreateRequest.Builder()
            .category(ConsentCategoryCode.TOS)
            .build()

        viewModelScope.launch {
            try {
                val operationOutcomeFlow: Flow<BWellResult<Consent>?>? = repository?.createConsent(createConsentRequest)
                operationOutcomeFlow?.collect { operationOutcome ->
                    //_consentData.emit(consentData)
                }

            } catch (_: Exception) {
            }
        }

    }

}
