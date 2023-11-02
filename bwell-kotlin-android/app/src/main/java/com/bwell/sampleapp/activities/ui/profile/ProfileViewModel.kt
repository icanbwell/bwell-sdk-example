package com.bwell.sampleapp.activities.ui.profile

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bwell.sampleapp.model.UserData
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel() {

    private val _userData = MutableLiveData<UserData?>()
    val userData: MutableLiveData<UserData?>
        get() = _userData

    // Define a companion object to hold the data
    companion object {
        private var storedUserData: UserData? = null
    }

    // Function to retrieve data from the companion object
    private fun getStoredUserData(): UserData? {
        return storedUserData
    }

    // Function to save data to the companion object
    private fun saveUserData(userData: UserData) {
        storedUserData = userData
    }

    fun fetchData() {
        viewModelScope.launch {
            try {
                // Retrieve data from the companion object
                val data = getStoredUserData()
                _userData.postValue(data)
            } catch (e: Exception) {
                // Handle errors
            }
        }
    }

    // Function to save data using the companion object
    fun saveData(userData: UserData) {
        viewModelScope.launch {
            try {
                // Save data to the companion object
                saveUserData(userData)
                // Notify the UI about the change
                _userData.postValue(userData)
            } catch (e: Exception) {
                // Handle errors
            }
        }
    }
}
