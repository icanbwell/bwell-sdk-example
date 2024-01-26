package com.bwell.sampleapp.repository

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.bwell.BWellSdk
import com.bwell.common.models.domain.common.Period
import com.bwell.common.models.responses.BWellResult
import com.bwell.healthdata.healthsummary.requests.allergyintolerance.AllergyIntoleranceGroupsRequest
import com.bwell.healthdata.healthsummary.requests.careplan.CarePlanGroupsRequest
import com.bwell.healthdata.healthsummary.requests.communication.CommunicationsRequest
import com.bwell.healthdata.healthsummary.requests.condition.ConditionGroupsRequest
import com.bwell.healthdata.healthsummary.requests.encounter.EncounterGroupsRequest
import com.bwell.healthdata.healthsummary.requests.immunization.ImmunizationGroupsRequest
import com.bwell.healthdata.healthsummary.requests.procedure.ProcedureGroupsRequest
import com.bwell.healthdata.healthsummary.requests.vitalsign.VitalSignGroupsRequest
import com.bwell.sampleapp.R
import com.bwell.sampleapp.model.HealthSummaryList
import com.bwell.sampleapp.model.HealthSummaryListItems
import com.bwell.sampleapp.utils.parseDateStringToDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class HealthSummaryRepository(private val applicationContext: Context) {

    suspend fun <T : Any> getHealthSummaryData(request: T?,category:String?): Flow<BWellResult<Any>?> = flow {
        try {
            lateinit var healthSummaryResult:BWellResult<Any>
            if(category.equals(applicationContext.getString(R.string.care_plans)))
            {
                healthSummaryResult = BWellSdk.health.getCarePlanGroups(request as? CarePlanGroupsRequest)
            }else if(category.equals(applicationContext.getString(R.string.immunizations)))
            {
                healthSummaryResult = BWellSdk.health.getImmunizationGroups(request as? ImmunizationGroupsRequest)
            }
            else if(category.equals(applicationContext.getString(R.string.procedures)))
            {
                healthSummaryResult = BWellSdk.health.getProcedureGroups(request as? ProcedureGroupsRequest)
            }
            else if(category.equals(applicationContext.getString(R.string.vitals)))
            {
                healthSummaryResult = BWellSdk.health.getVitalSignGroups(request as? VitalSignGroupsRequest)
            }
            else if(category.equals(applicationContext.getString(R.string.visit_history)))
            {
                healthSummaryResult = BWellSdk.health.getEncounterGroups(request as? EncounterGroupsRequest)
            }
            else if(category.equals(applicationContext.getString(R.string.allergies)))
            {
                healthSummaryResult = BWellSdk.health.getAllergyIntoleranceGroups(request as AllergyIntoleranceGroupsRequest)
            }
            else if(category.equals(applicationContext.getString(R.string.communications)))
            {
                healthSummaryResult = BWellSdk.health.getCommunications(request as CommunicationsRequest)
            }
            else if(category.equals(applicationContext.getString(R.string.conditions)))
            {
                healthSummaryResult = BWellSdk.health.getConditionGroups(request as ConditionGroupsRequest)
            }
            emit(healthSummaryResult)
        } catch (e: Exception) {
            emit(null)
        }
    }

    private val healthSummaryLiveData = MutableLiveData<HealthSummaryList>()
    val healthSummary: LiveData<HealthSummaryList>
        get() = healthSummaryLiveData

    suspend fun getHealthSummaryList() {
        val healthSummary = BWellSdk.health.getHealthSummary()
        Log.d("BWell Sample App", healthSummary.toString())
        val suggestionsList = mutableListOf<HealthSummaryListItems>()
        suggestionsList.add(
            HealthSummaryListItems(
                 R.drawable.baseline_person_24, R.drawable.baseline_keyboard_arrow_right_24,
                "Care Plans",  Period.Builder().start(parseDateStringToDate("2023-01-01", "yyyy-MM-dd"))
                    .end(parseDateStringToDate("2023-12-31", "yyyy-MM-dd")).build()
            )
        )
        suggestionsList.add(
            HealthSummaryListItems(
                 R.drawable.baseline_person_24, R.drawable.baseline_keyboard_arrow_right_24,
                "Immunizations", Period.Builder().start(parseDateStringToDate("2023-01-01", "yyyy-MM-dd")).build()
            )
        )
        suggestionsList.add(
            HealthSummaryListItems(
                R.drawable.baseline_person_24, R.drawable.baseline_keyboard_arrow_right_24,
                "Procedures", Period.Builder().start(parseDateStringToDate("2023-01-01", "yyyy-MM-dd")).build()
            )
        )
        suggestionsList.add(
            HealthSummaryListItems(
                R.drawable.baseline_person_24, R.drawable.baseline_keyboard_arrow_right_24,
                "Vitals", Period.Builder().start(parseDateStringToDate("2023-01-01", "yyyy-MM-dd")).build()
            )
        )
        suggestionsList.add(
            HealthSummaryListItems(
                R.drawable.baseline_person_24, R.drawable.baseline_keyboard_arrow_right_24,
                "Visit History", Period.Builder().start(parseDateStringToDate("2023-01-01", "yyyy-MM-dd")).build()
            )
        )
        suggestionsList.add(
            HealthSummaryListItems(
                R.drawable.baseline_person_24, R.drawable.baseline_keyboard_arrow_right_24,
                "Allergies", Period.Builder().start(parseDateStringToDate("2023-01-01", "yyyy-MM-dd")).build()
            )
        )
        suggestionsList.add(
            HealthSummaryListItems(
                R.drawable.baseline_person_24, R.drawable.baseline_keyboard_arrow_right_24,
                "Conditions", Period.Builder().start(parseDateStringToDate("2023-01-01", "yyyy-MM-dd")).build()
            )
        )
        val activityList = HealthSummaryList(suggestionsList)
        healthSummaryLiveData.postValue(activityList)
    }

}