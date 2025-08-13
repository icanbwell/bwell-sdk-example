package com.bwell.sampleapp.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bwell.common.models.domain.consent.Consent
import com.bwell.common.models.domain.data.Connection
import com.bwell.common.models.domain.data.DataSource
import com.bwell.common.models.domain.healthdata.healthsummary.careteam.CareTeam
import com.bwell.common.models.responses.BWellResult
import com.bwell.common.models.responses.OperationOutcome
import com.bwell.connections.requests.ConnectionCreateRequest
import com.bwell.healthdata.healthsummary.requests.careteam.CareTeamsRequest
import com.bwell.sampleapp.model.DataConnectionsClinicsList
import com.bwell.sampleapp.model.DataConnectionsClinicsListItems
import com.bwell.sampleapp.model.SuggestedDataConnectionsCategoriesList
import com.bwell.sampleapp.model.SuggestedDataConnectionsList
import com.bwell.sampleapp.repository.DataConnectionsRepository
import com.bwell.user.requests.consents.ConsentRequest
import com.bwell.user.requests.consents.ConsentCreateRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DataConnectionsViewModel(private val repository: DataConnectionsRepository?) : ViewModel() {

    private val TAG = "DataConnectionsViewModel"
    init {
        viewModelScope.launch(Dispatchers.IO) {
            repository?.getDataConnectionsList()
        }

        viewModelScope.launch(Dispatchers.IO) {
            repository?.getDataConnectionsCategoriesList()
        }
    }

    val suggestedDataConnections: LiveData<SuggestedDataConnectionsList>
        get() = repository?.suggestedDataConnections!!

    val suggestedDataConnectionsCategories: LiveData<SuggestedDataConnectionsCategoriesList>
        get() = repository?.suggestedDataConnectionsCategories!!

    val dataConnectionsClinics: LiveData<DataConnectionsClinicsList>
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
            } catch (ex: Exception) {
                Log.i(TAG, ex.toString())
            }
        }
    }

    fun updateConsent(request: ConsentCreateRequest) {
        viewModelScope.launch {
            try {
                val updateOutcomeFlow: Flow<BWellResult<Consent>?>? =
                    repository?.updateUserConsent(request)
                updateOutcomeFlow?.collect { updateOutcome ->
                    if (updateOutcome?.operationOutcome()?.success() == true) {
                        _consentsData.emit(updateOutcome)
                    }
                }
            } catch (ex: Exception) {
                Log.i(TAG, ex.toString())
            }
        }
    }

    private val _createConnectionData = MutableStateFlow<BWellResult<Connection>?>(null)
    val createConnectionData: StateFlow<BWellResult<Connection>?> = _createConnectionData

    fun createConnection(connectionRequest: ConnectionCreateRequest) {
        viewModelScope.launch {
            try {
                repository?.createConnection(connectionRequest)?.collect { connectionOutcome ->
                     _createConnectionData.emit(connectionOutcome)
                }
            } catch (ex: Exception) {
                Log.i(TAG, ex.toString())
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
            } catch (ex: Exception) {
                Log.i(TAG, ex.toString())
            }
        }
    }

    private val _deleteConnectionData = MutableStateFlow<OperationOutcome?>(null)
    val deleteConnectionData: StateFlow<OperationOutcome?> = _deleteConnectionData

    fun deleteConnection(connectionId: String) {
        viewModelScope.launch {
            try {
                repository?.deleteConnection(connectionId)?.collect { deleteOutcome ->
                    _deleteConnectionData.emit(deleteOutcome)
                }
            } catch (ex: Exception) {
                Log.i(TAG, ex.toString())
            }
        }
    }


    // LiveData for the list of Connection objects
    private val _connectionsList = MutableLiveData<List<Connection>>()
    val connectionsList: LiveData<List<Connection>> get() = _connectionsList

    //  process the BWellResult and update the LiveData
    private fun processMemberConnectionsResult(connectionsResult: BWellResult<Connection>?) {
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
                repository?.getMemberConnections()?.collect { connectionsResult ->
                    processMemberConnectionsResult(connectionsResult)
                }
            } catch (ex: Exception) {
                Log.i(TAG, ex.toString())
            }
        }
    }

    private val _careTeamResults = MutableStateFlow<BWellResult<CareTeam?>?>(null)
    val careTeamResults: StateFlow<BWellResult<CareTeam?>?> = _careTeamResults
    fun getCareTeams(careTeamsRequest: CareTeamsRequest?) {
        viewModelScope.launch {
            try {
                repository?.getCareTeams(careTeamsRequest)?.collect { result ->
                    _careTeamResults.emit(result)
                }
            } catch (e: Exception){
                // Handle Exceptions, if any
            }
        }
    }

    private val _dataSourceData = MutableStateFlow<BWellResult<DataSource>?>(null)
    val dataSourceData: StateFlow<BWellResult<DataSource>?> = _dataSourceData
    fun getDataSource(datasourceId: String) {
        viewModelScope.launch {
            try {
                repository?.getDataSource(datasourceId)?.collect { dataSourceResult ->
                    _dataSourceData.emit(dataSourceResult)
                }
            } catch (ex: Exception) {
                Log.i(TAG, ex.toString())
            }
        }
    }

    private val _oauthUrlData = MutableStateFlow<BWellResult<String>?>(null)
    val oauthUrlData: StateFlow<BWellResult<String>?> = _oauthUrlData

    fun getOAuthUrl(datasourceId: String) {
        viewModelScope.launch {
            try {
                repository?.getOAuthUrl(datasourceId)?.collect { oauthUrlResult ->
                    _oauthUrlData.emit(oauthUrlResult)
                }
            } catch (ex: Exception) {
                Log.i(TAG, ex.toString())
            }
        }
    }
}