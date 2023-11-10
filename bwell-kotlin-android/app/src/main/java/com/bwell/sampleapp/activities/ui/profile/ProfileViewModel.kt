package com.bwell.sampleapp.activities.ui.profile

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bwell.common.domain.user.Person
import com.bwell.common.models.responses.BWellResult
import com.bwell.common.models.responses.OperationOutcome
import com.bwell.common.models.responses.Status
import com.bwell.sampleapp.repository.Repository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
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
                val operationOutcomeFlow: Flow<OperationOutcome?>? = repository?.saveUserProfile(userData)
                operationOutcomeFlow?.collect { operationOutcome ->
                    if (operationOutcome?.status == Status.SUCCESS) {
                        _userData.emit(userData)
                    }
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
