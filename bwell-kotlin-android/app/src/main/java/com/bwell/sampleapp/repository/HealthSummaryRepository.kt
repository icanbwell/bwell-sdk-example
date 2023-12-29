package com.bwell.sampleapp.repository

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.bwell.BWellSdk
import com.bwell.common.models.domain.common.Period
import com.bwell.common.models.responses.BWellResult
import com.bwell.healthdata.healthsummary.allergyintolerance.AllergyIntoleranceCompositeRequest
import com.bwell.healthdata.healthsummary.careplan.CarePlanCompositeRequest
import com.bwell.healthdata.healthsummary.communication.CommunicationRequest
import com.bwell.healthdata.healthsummary.condition.ConditionCompositeRequest
import com.bwell.healthdata.healthsummary.encounter.EncounterCompositeRequest
import com.bwell.healthdata.healthsummary.immunization.ImmunizationCompositeRequest
import com.bwell.healthdata.healthsummary.procedure.ProcedureCompositeRequest
import com.bwell.healthdata.healthsummary.vitalsign.VitalSignCompositeRequest
import com.bwell.sampleapp.R
import com.bwell.sampleapp.model.HealthSummaryList
import com.bwell.sampleapp.model.HealthSummaryListItems
import com.bwell.sampleapp.utils.parseDateStringToDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.Date

class HealthSummaryRepository(private val applicationContext: Context) {

    suspend fun <T : Any> getHealthSummaryData(request: T?,category:String?): Flow<BWellResult<Any>?> = flow {
        try {
            lateinit var healthSummaryResult:BWellResult<Any>
            if(category.equals(applicationContext.getString(R.string.care_plans)))
            {
                healthSummaryResult = BWellSdk.health.getCarePlanComposites(request as? CarePlanCompositeRequest)
            }else if(category.equals(applicationContext.getString(R.string.immunizations)))
            {
                healthSummaryResult = BWellSdk.health.getImmunizationComposites(request as? ImmunizationCompositeRequest)
            }
            else if(category.equals(applicationContext.getString(R.string.procedures)))
            {
                healthSummaryResult = BWellSdk.health.getProcedureComposites(request as? ProcedureCompositeRequest)
            }
            else if(category.equals(applicationContext.getString(R.string.vitals)))
            {
                healthSummaryResult = BWellSdk.health.getVitalSignComposites(request as? VitalSignCompositeRequest)
            }
            else if(category.equals(applicationContext.getString(R.string.visit_history)))
            {
                healthSummaryResult = BWellSdk.health.getEncounterComposites(request as? EncounterCompositeRequest)
            }
            else if(category.equals(applicationContext.getString(R.string.allergies)))
            {
                healthSummaryResult = BWellSdk.health.getAllergyIntoleranceComposites(request as AllergyIntoleranceCompositeRequest)
            }
            else if(category.equals(applicationContext.getString(R.string.communications)))
            {
                healthSummaryResult = BWellSdk.health.getCommunications(request as CommunicationRequest)
            }
            else if(category.equals(applicationContext.getString(R.string.conditions)))
            {
                healthSummaryResult = BWellSdk.health.getConditionComposites(request as ConditionCompositeRequest)
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
                "Communications", Period.Builder().start(parseDateStringToDate("2023-01-01", "yyyy-MM-dd")).build()
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