package com.bwell.sampleapp.activities.ui.profile

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bwell.common.models.domain.user.Person
import com.bwell.common.models.responses.BWellResult
import com.bwell.common.models.responses.OperationOutcome
import com.bwell.sampleapp.repository.Repository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter

class ProfileViewModel(private val repository: Repository?) : ViewModel() {

    private val _userData = MutableSharedFlow<Person?>()
    val userData: MutableSharedFlow<Person?>
        get() = _userData

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

    private val _deletePersonData = MutableStateFlow<OperationOutcome?>(null)
    val deletePersonData: StateFlow<OperationOutcome?> = _deletePersonData

    fun deletePerson() {
        viewModelScope.launch {
            try {
                repository?.deleteUserProfile()?.collect { disconnectOutcome ->
                    _deletePersonData.emit(disconnectOutcome)
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

}
