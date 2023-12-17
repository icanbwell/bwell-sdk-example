package com.bwell.sampleapp.repository

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.bwell.BWellSdk
import com.bwell.common.models.domain.user.Person
import com.bwell.common.models.responses.BWellResult
import com.bwell.common.models.responses.OperationOutcome
import com.bwell.common.models.responses.Status
import com.bwell.sampleapp.R
import com.bwell.sampleapp.model.ActivityListItems
import com.bwell.sampleapp.model.SuggestedActivitiesLIst
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class Repository(private val applicationContext: Context) {

    private val suggestedActivitiesLiveData = MutableLiveData<SuggestedActivitiesLIst>()

    val suggestedActivities: LiveData<SuggestedActivitiesLIst>
        get() = suggestedActivitiesLiveData

    suspend fun saveUserProfile(person: Person): Flow<BWellResult<Person>?> = flow {
        var operationOutcome: BWellResult<Person>? = BWellSdk.user?.updateProfile(person)
        emit(operationOutcome)
    }

    suspend fun fetchUserProfile(): Flow<BWellResult<Person>?> = flow {
        val profileData = BWellSdk.user?.getProfile()
        if(profileData?.operationOutcome?.status==Status.SUCCESS){
            emit(profileData)
        }
    }

    suspend fun registerDeviceToken(deviceToken: String): Flow<OperationOutcome?> = flow {
        try {
            val outcome: OperationOutcome? = BWellSdk.device?.registerDeviceToken(deviceToken)
            emit(outcome)
        } catch (e: Exception) {
            // Handle exceptions
        }
    }

    suspend fun unregisterDeviceToken(deviceToken: String): Flow<OperationOutcome?> = flow {
        try {
            val outcome: OperationOutcome? = BWellSdk.device?.deregisterDeviceToken(deviceToken)
            emit(outcome)
        } catch (e: Exception) {
            // Handle exceptions
        }
    }

    suspend fun getActivitiesSuggestionList() {

        val suggestionsList = mutableListOf<ActivityListItems>()
        suggestionsList.add(
            ActivityListItems(
                applicationContext.getString(R.string.connect_your_care_providers),
                applicationContext.getString(
                    R.string.your_healthcare_providers_store_lots_of_important_information_about_your_health
                ), R.drawable.circle
            )
        )
        suggestionsList.add(
            ActivityListItems(
                applicationContext.getString(R.string.build_your_care_team),
                applicationContext.getString(
                    R.string.having_a_team_of_healthcare_providers_you_trust_helps_you_get_the_best_care_possible
                ), R.drawable.plus_icon
            )
        )
        suggestionsList.add(
            ActivityListItems(
                applicationContext.getString(R.string.build_your_care_team),
                applicationContext.getString(
                    R.string.having_a_team_of_healthcare_providers_you_trust_helps_you_get_the_best_care_possible
                ), R.drawable.ic_placeholder
            )
        )
        suggestionsList.add(
            ActivityListItems(
                applicationContext.getString(R.string.build_your_care_team),
                applicationContext.getString(
                    R.string.having_a_team_of_healthcare_providers_you_trust_helps_you_get_the_best_care_possible
                ), R.drawable.vaccine_icon
            )
        )
        suggestionsList.add(
            ActivityListItems(
                applicationContext.getString(R.string.build_your_care_team),
                applicationContext.getString(
                    R.string.having_a_team_of_healthcare_providers_you_trust_helps_you_get_the_best_care_possible
                ), R.drawable.circle
            )
        )
        suggestionsList.add(
            ActivityListItems(
                applicationContext.getString(R.string.find_a_primary_care_provider_you),
                applicationContext.getString(
                    R.string.a_primary_care_provider_is_a_healthcare_professional_who_helps
                ), R.drawable.vaccine_icon
            )
        )
        val activityList = SuggestedActivitiesLIst(suggestionsList)
        suggestedActivitiesLiveData.postValue(activityList)
    }

}