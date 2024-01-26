package com.bwell.sampleapp.repository

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.bwell.BWellSdk
import com.bwell.common.models.domain.consent.Consent
import com.bwell.common.models.domain.data.Connection
import com.bwell.common.models.domain.data.DataSource
import com.bwell.common.models.responses.BWellResult
import com.bwell.common.models.responses.OperationOutcome
import com.bwell.connections.requests.ConnectionCreateRequest
import com.bwell.sampleapp.R
import com.bwell.sampleapp.model.DataConnectionCategoriesListItems
import com.bwell.sampleapp.model.DataConnectionListItems
import com.bwell.sampleapp.model.DataConnectionsClinicsList
import com.bwell.sampleapp.model.SuggestedDataConnectionsCategoriesList
import com.bwell.sampleapp.model.SuggestedDataConnectionsList
import com.bwell.user.requests.consents.ConsentRequest
import com.bwell.user.requests.consents.ConsentCreateRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class DataConnectionsRepository(private val applicationContext: Context) {

    private val TAG = "DataConnectionsRepository"

    private val suggestedDataConnectionsLiveData = MutableLiveData<SuggestedDataConnectionsList>()

    val suggestedDataConnections: LiveData<SuggestedDataConnectionsList>
        get() = suggestedDataConnectionsLiveData

    private val suggestedDataConnectionsCategoriesLiveData =
        MutableLiveData<SuggestedDataConnectionsCategoriesList>()

    val suggestedDataConnectionsCategories: LiveData<SuggestedDataConnectionsCategoriesList>
        get() = suggestedDataConnectionsCategoriesLiveData

    private val dataConnectionsClinicsLiveData = MutableLiveData<DataConnectionsClinicsList>()

    val dataConnectionsClinics: LiveData<DataConnectionsClinicsList>
        get() = dataConnectionsClinicsLiveData

    suspend fun getConnections(): Flow<BWellResult<Connection>?> = flow {
        try {
            val connectionsResult = BWellSdk.connections?.getMemberConnections()
            emit(connectionsResult)
        } catch (e: Exception) {
            // Handle exceptions, if any
            emit(null)
        }
    }

    suspend fun disconnectConnection(connectionId: String): Flow<OperationOutcome?> = flow {
        try {
            val disconnectOutcome = BWellSdk.connections?.disconnectConnection(connectionId)
            emit(disconnectOutcome)
        } catch (e: Exception) {
            // Handle exceptions, if any
            emit(null)
        }
    }

    suspend fun createConnection(connectionRequest: ConnectionCreateRequest): Flow<OperationOutcome?> =
        flow {
            try {
                val connectionOutcome = BWellSdk.connections?.createConnection(connectionRequest)
                emit(connectionOutcome)
            } catch (e: Exception) {
                // Handle exceptions, if any
                emit(null)
            }
        }

    suspend fun getOAuthUrl(datasourceId: String): Flow<BWellResult<String>?> = flow {
        try {
            val urlOutcome: BWellResult<String> = BWellSdk.connections.getOauthUrl(datasourceId)
            Log.i(TAG, "Received url: $urlOutcome")
            emit(urlOutcome)
        } catch (e: Exception) {
            // Handle exceptions, if any
            Log.e(TAG, e.toString())
            emit(null)
        }
    }

    suspend fun getDataSource(datasourceId: String): Flow<BWellResult<DataSource>?> = flow {
        try {
            val dataSource: BWellResult<DataSource> =
                BWellSdk.connections.getDataSource(datasourceId)
            Log.i(TAG, "Received data source: $dataSource")
            emit(dataSource)
        } catch (e: Exception) {
            // Handle exceptions, if any
            Log.e(TAG, e.toString())
            emit(null)
        }
    }

    suspend fun fetchUserConsents(consentsRequest: ConsentRequest): Flow<BWellResult<Consent>?> =
        flow {
            val consentsResult = BWellSdk.user?.getConsents(consentsRequest)
            emit(consentsResult)
        }

    suspend fun updateUserConsent(request: ConsentCreateRequest): Flow<BWellResult<Consent>?> =
        flow {
            val updateOutcome = BWellSdk.user?.createConsent(request)
            emit(updateOutcome)
        }


    suspend fun getDataConnectionsCategoriesList() {

        val suggestionsList = mutableListOf<DataConnectionCategoriesListItems>()

        // Category A
        suggestionsList.add(
            DataConnectionCategoriesListItems(
                applicationContext.getString(R.string.data_connection_category_insurance),
                R.drawable.circle,
                "Get your claims and financials, plus a record of the providers you see from common insurance plans and Medicare."
            )
        )
        suggestionsList.add(
            DataConnectionCategoriesListItems(
                applicationContext.getString(R.string.data_connection_category_providers),
                R.drawable.plus_icon,
                "See your core health info, such as provider visit summaries, diagnosis, treatment history, prescriptions, and labs."
            )
        )
        suggestionsList.add(
            DataConnectionCategoriesListItems(
                applicationContext.getString(R.string.data_connection_category_clinics),
                R.drawable.ic_placeholder,
                "See your core health info, such as your diagnosis, procedures, treatment history, prescriptions, and labs."
            )
        )
        suggestionsList.add(
            DataConnectionCategoriesListItems(
                applicationContext.getString(R.string.data_connection_category_lab),
                R.drawable.vaccine_icon,
                "View your lab results to track your numbers over time."
            )
        )

        val activityList = SuggestedDataConnectionsCategoriesList(suggestionsList)
        suggestedDataConnectionsCategoriesLiveData.postValue(activityList)
    }

    suspend fun getDataConnectionsList() {

        val suggestionsList = mutableListOf<DataConnectionListItems>()

        // Category A
        suggestionsList.add(
            DataConnectionListItems(
                "Epic Sandbox R4C",
                R.drawable.baseline_person_pin_24,
                "Pending",
                R.drawable.baseline_more_vert_24
            )
        )
        suggestionsList.add(
            DataConnectionListItems(
                "HAPI - Starfleet Medical", R.drawable.baseline_person_pin_24,
                "Disconnected", R.drawable.baseline_more_vert_24
            )
        )
        suggestionsList.add(
            DataConnectionListItems(
                "ThedaCare Ripple", R.drawable.baseline_person_pin_24,
                "Needs Reauthorization", R.drawable.baseline_more_vert_24
            )
        )

        val activityList = SuggestedDataConnectionsList(suggestionsList)
        suggestedDataConnectionsLiveData.postValue(activityList)
    }


}