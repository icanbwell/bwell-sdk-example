package com.bwell.sampleapp.repository

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.bwell.BWellSdk
import com.bwell.common.models.domain.common.Period
import com.bwell.common.models.responses.BWellResult
import com.bwell.healthdata.healthsummary.allergyintolerance.AllergyIntoleranceRequest
import com.bwell.healthdata.healthsummary.careplan.CarePlanRequest
import com.bwell.healthdata.healthsummary.communication.CommunicationRequest
import com.bwell.healthdata.healthsummary.condition.ConditionRequest
import com.bwell.healthdata.healthsummary.encounter.EncounterRequest
import com.bwell.healthdata.healthsummary.immunization.ImmunizationRequest
import com.bwell.healthdata.healthsummary.procedure.ProcedureRequest
import com.bwell.healthdata.healthsummary.vitalsign.VitalSignRequest
import com.bwell.sampleapp.R
import com.bwell.sampleapp.model.HealthSummaryList
import com.bwell.sampleapp.model.HealthSummaryListItems
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class HealthSummaryRepository(private val applicationContext: Context) {

    suspend fun <T : Any> getHealthSummaryData(request: T?,category:String?): Flow<BWellResult<Any>?> = flow {
        try {
            lateinit var healthSummaryResult:BWellResult<Any>
            if(category.equals(applicationContext.getString(R.string.care_plans)))
            {
                healthSummaryResult = BWellSdk.health.getCarePlans(request as? CarePlanRequest)
            }else if(category.equals(applicationContext.getString(R.string.immunizations)))
            {
                healthSummaryResult = BWellSdk.health.getImmunizations(request as? ImmunizationRequest)
            }
            else if(category.equals(applicationContext.getString(R.string.procedures)))
            {
                healthSummaryResult = BWellSdk.health.getProcedures(request as? ProcedureRequest)
            }
            else if(category.equals(applicationContext.getString(R.string.vitals)))
            {
                healthSummaryResult = BWellSdk.health.getVitalSigns(request as? VitalSignRequest)
            }
            else if(category.equals(applicationContext.getString(R.string.visit_history)))
            {
                healthSummaryResult = BWellSdk.health.getEncounters(request as? EncounterRequest)
            }
            else if(category.equals(applicationContext.getString(R.string.allergies)))
            {
                healthSummaryResult = BWellSdk.health.getAllergyIntolerances(request as AllergyIntoleranceRequest)
            }
            else if(category.equals(applicationContext.getString(R.string.communications)))
            {
                healthSummaryResult = BWellSdk.health.getCommunications(request as CommunicationRequest)
            }
            else if(category.equals(applicationContext.getString(R.string.conditions)))
            {
                healthSummaryResult = BWellSdk.health.getConditions(request as ConditionRequest)
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
        val suggestionsList = mutableListOf<HealthSummaryListItems>()
        suggestionsList.add(
            HealthSummaryListItems(
                 R.drawable.baseline_person_24, R.drawable.baseline_keyboard_arrow_right_24,
                "Care Plans", Period.Builder().start("2023-01-01").end("2023-12-31").build()
            )
        )
        suggestionsList.add(
            HealthSummaryListItems(
                 R.drawable.baseline_person_24, R.drawable.baseline_keyboard_arrow_right_24,
                "Immunizations", Period.Builder().start("2023-01-01").build()
            )
        )
        suggestionsList.add(
            HealthSummaryListItems(
                R.drawable.baseline_person_24, R.drawable.baseline_keyboard_arrow_right_24,
                "Procedures", Period.Builder().start("2023-01-01").build()
            )
        )
        suggestionsList.add(
            HealthSummaryListItems(
                R.drawable.baseline_person_24, R.drawable.baseline_keyboard_arrow_right_24,
                "Vitals", Period.Builder().start("2023-01-01").build()
            )
        )
        suggestionsList.add(
            HealthSummaryListItems(
                R.drawable.baseline_person_24, R.drawable.baseline_keyboard_arrow_right_24,
                "Visit History", Period.Builder().start("2023-01-01").build()
            )
        )
        suggestionsList.add(
            HealthSummaryListItems(
                R.drawable.baseline_person_24, R.drawable.baseline_keyboard_arrow_right_24,
                "Allergies", Period.Builder().start("2023-01-01").build()
            )
        )
        suggestionsList.add(
            HealthSummaryListItems(
                R.drawable.baseline_person_24, R.drawable.baseline_keyboard_arrow_right_24,
                "Communications", Period.Builder().start("2023-01-01").build()
            )
        )
        suggestionsList.add(
            HealthSummaryListItems(
                R.drawable.baseline_person_24, R.drawable.baseline_keyboard_arrow_right_24,
                "Conditions", Period.Builder().start("2023-01-01").build()
            )
        )
        val activityList = HealthSummaryList(suggestionsList)
        healthSummaryLiveData.postValue(activityList)
    }

}