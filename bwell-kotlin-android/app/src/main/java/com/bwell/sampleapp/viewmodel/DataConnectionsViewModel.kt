package com.bwell.sampleapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bwell.common.models.domain.consent.Consent
import com.bwell.common.models.domain.data.Connection
import com.bwell.common.models.responses.BWellResult
import com.bwell.common.models.responses.OperationOutcome
import com.bwell.common.models.responses.Status
import com.bwell.connections.requests.ConnectionCreateRequest
import com.bwell.sampleapp.R
import com.bwell.sampleapp.model.DataConnectionListItems
import com.bwell.sampleapp.model.DataConnectionsClinicsList
import com.bwell.sampleapp.model.DataConnectionsClinicsListItems
import com.bwell.sampleapp.model.SuggestedDataConnectionsCategoriesList
import com.bwell.sampleapp.model.SuggestedDataConnectionsList
import com.bwell.sampleapp.repository.DataConnectionsRepository
import com.bwell.user.consents.requests.ConsentUpdateRequest
import com.bwell.user.consents.requests.ConsentRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DataConnectionsViewModel(private val repository: DataConnectionsRepository?) : ViewModel() {
    init {
        viewModelScope.launch(Dispatchers.IO){
            repository?.getDataConnectionsList()
        }

        viewModelScope.launch(Dispatchers.IO){
            repository?.getDataConnectionsCategoriesList()
        }

        viewModelScope.launch(Dispatchers.IO){
            repository?.getDataConnectionsClinicsList()
        }
    }

    val suggestedDataConnections : LiveData<SuggestedDataConnectionsList>
        get() = repository?.suggestedDataConnections!!

    val suggestedDataConnectionsCategories : LiveData<SuggestedDataConnectionsCategoriesList>
        get() = repository?.suggestedDataConnectionsCategories!!

    val dataConnectionsClinics : LiveData<DataConnectionsClinicsList>
        get() = repository?.dataConnectionsClinics!!

    // Add a MutableLiveData for the filtered list
    val filteredDataConnectionsClinics = MutableLiveData<List<DataConnectionsClinicsListItems>>()

    // Modify the function to filter the list based on the search query
    fun filterDataConnectionsClinics(query: String) {
        val filteredList = dataConnectionsClinics.value?.dataConnectionsClinicsList
            ?.filter { it.clinicName.contains(query, ignoreCase = true) }
        filteredDataConnectionsClinics.postValue(filteredList!!)
    }

    private val _consentsData = MutableStateFlow<BWellResult<Consent>?>(null)
    val consentsData: StateFlow<BWellResult<Consent>?> = _consentsData

    fun fetchConsents(consentsRequest: ConsentRequest) {
        viewModelScope.launch {
            try {
                repository?.fetchUserConsents(consentsRequest)?.collect { consentsResult ->
                    _consentsData.emit(consentsResult)
                }
            } catch (_: Exception) {
            }
        }
    }

    fun updateConsent(consentUpdateRequest: ConsentUpdateRequest) {
        viewModelScope.launch {
            try {
                val updateOutcomeFlow: Flow<BWellResult<Consent>?>? = repository?.updateUserConsent(consentUpdateRequest)
                updateOutcomeFlow?.collect { updateOutcome ->
                    if (updateOutcome?.operationOutcome?.status == Status.SUCCESS) {
                        _consentsData.emit(updateOutcome)
                    }
                }
            } catch (_: Exception) {
            }
        }
    }

    private val _createConnectionData = MutableStateFlow<OperationOutcome?>(null)
    val createConnectionData: StateFlow<OperationOutcome?> = _createConnectionData

    fun createConnection(connectionRequest: ConnectionCreateRequest) {
        viewModelScope.launch {
            try {
                repository?.createConnection(connectionRequest)?.collect { connectionOutcome ->
                    _createConnectionData.emit(connectionOutcome)
                }
            } catch (_: Exception) {
            }
        }
    }

    private val _disconnectConnectionData = MutableStateFlow<OperationOutcome?>(null)
    val disconnectConnectionData: StateFlow<OperationOutcome?> = _disconnectConnectionData

    fun disconnectConnection(connectionId: String) {
        viewModelScope.launch {
            try {
                repository?.disconnectConnection(connectionId)?.collect { disconnectOutcome ->
                    _disconnectConnectionData.emit(disconnectOutcome)
                }
            } catch (_: Exception) {
            }
        }
    }

    // LiveData for the list of Connection objects
    private val _connectionsList = MutableLiveData<List<Connection>>()
    val connectionsList: LiveData<List<Connection>> get() = _connectionsList

    //  process the BWellResult and update the LiveData
    private fun processConnectionsResult(connectionsResult: BWellResult<Connection>?) {
        val connectionList = when (connectionsResult) {
            is BWellResult.ResourceCollection -> {
                connectionsResult.data ?: emptyList()
            }
            else -> emptyList()
        }
        _connectionsList.value = connectionList
    }

    //  get connections and observe changes
    fun getConnectionsAndObserve() {
        viewModelScope.launch {
            try {
                repository?.getConnections()?.collect { connectionsResult ->
                    processConnectionsResult(connectionsResult)
                }
            } catch (_: Exception) {
                // Handle exceptions
            }
        }
    }

}