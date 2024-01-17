package com.bwell.sampleapp.repository

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.bwell.BWellSdk
import com.bwell.common.models.domain.common.Period
import com.bwell.common.models.domain.healthdata.healthsummary.allergyintolerance.AllergyIntoleranceComposite
import com.bwell.common.models.domain.healthdata.healthsummary.careplan.CarePlanComposite
import com.bwell.common.models.domain.healthdata.healthsummary.condition.ConditionComposite
import com.bwell.common.models.domain.healthdata.healthsummary.encounter.EncounterComposite
import com.bwell.common.models.domain.healthdata.healthsummary.immunization.ImmunizationComposite
import com.bwell.common.models.domain.healthdata.healthsummary.procedure.ProcedureComposite
import com.bwell.common.models.domain.healthdata.observation.ObservationComposite
import com.bwell.common.models.responses.BWellResult
import com.bwell.healthdata.healthsummary.allergyintolerance.AllergyIntoleranceCompositeRequest
import com.bwell.healthdata.healthsummary.allergyintolerance.AllergyIntoleranceRequest
import com.bwell.healthdata.healthsummary.careplan.CarePlanCompositeRequest
import com.bwell.healthdata.healthsummary.careplan.CarePlanRequest
import com.bwell.healthdata.healthsummary.communication.CommunicationRequest
import com.bwell.healthdata.healthsummary.condition.ConditionCompositeRequest
import com.bwell.healthdata.healthsummary.condition.ConditionRequest
import com.bwell.healthdata.healthsummary.encounter.EncounterCompositeRequest
import com.bwell.healthdata.healthsummary.encounter.EncounterRequest
import com.bwell.healthdata.healthsummary.immunization.ImmunizationCompositeRequest
import com.bwell.healthdata.healthsummary.immunization.ImmunizationRequest
import com.bwell.healthdata.healthsummary.procedure.ProcedureCompositeRequest
import com.bwell.healthdata.healthsummary.procedure.ProcedureRequest
import com.bwell.healthdata.healthsummary.vitalsign.VitalSignCompositeRequest
import com.bwell.healthdata.healthsummary.vitalsign.VitalSignRequest
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
                val carePlanComposites: BWellResult.ResourceCollection<CarePlanComposite> = BWellSdk.health.getCarePlanComposites(request as? CarePlanCompositeRequest) as BWellResult.ResourceCollection
                emit(carePlanComposites)
                val ids = carePlanComposites.data?.map { it.id ?: "" } ?: listOf()
                val carePlanRequest = CarePlanRequest.Builder().ids(ids).build()
                healthSummaryResult = BWellSdk.health.getCarePlans(carePlanRequest)
            }else if(category.equals(applicationContext.getString(R.string.immunizations)))
            {
                val immunizationComposites: BWellResult.ResourceCollection<ImmunizationComposite> = BWellSdk.health.getImmunizationComposites(request as? ImmunizationCompositeRequest) as BWellResult.ResourceCollection
                emit(immunizationComposites)
                val ids = immunizationComposites.data?.map { it.id ?: "" } ?: listOf()
                val immunizationRequest = ImmunizationRequest.Builder().ids(ids).build()
                healthSummaryResult = BWellSdk.health.getImmunizations(immunizationRequest)
            }
            else if(category.equals(applicationContext.getString(R.string.procedures)))
            {
                val procedureComposites: BWellResult.ResourceCollection<ProcedureComposite> = BWellSdk.health.getProcedureComposites(request as? ProcedureCompositeRequest) as BWellResult.ResourceCollection
                emit(procedureComposites)
                val ids = procedureComposites.data?.map { it.id ?: "" } ?: listOf()
                val proceduresRequest = ProcedureRequest.Builder().ids(ids).build()
                healthSummaryResult = BWellSdk.health.getProcedures(proceduresRequest)
            }
            else if(category.equals(applicationContext.getString(R.string.vitals)))
            {
                val vitalSignComposites: BWellResult.ResourceCollection<ObservationComposite> = BWellSdk.health.getVitalSignComposites(request as? VitalSignCompositeRequest) as BWellResult.ResourceCollection
                emit(vitalSignComposites)
                val ids = vitalSignComposites.data?.map { it.id ?: "" } ?: listOf()
                val vitalSignsRequest = VitalSignRequest.Builder().ids(ids).build()
                healthSummaryResult = BWellSdk.health.getVitalSigns(vitalSignsRequest)
            }
            else if(category.equals(applicationContext.getString(R.string.visit_history)))
            {
                val encounterComposites: BWellResult.ResourceCollection<EncounterComposite> = BWellSdk.health.getEncounterComposites(request as? EncounterCompositeRequest) as BWellResult.ResourceCollection
                emit(encounterComposites)
                val ids = encounterComposites.data?.map { it.id ?: "" } ?: listOf()
                val encountersRequest = EncounterRequest.Builder().ids(ids).build()
                healthSummaryResult = BWellSdk.health.getEncounters(encountersRequest)
            }
            else if(category.equals(applicationContext.getString(R.string.allergies)))
            {
                val allergyIntoleranceComposites: BWellResult.ResourceCollection<AllergyIntoleranceComposite> = BWellSdk.health.getAllergyIntoleranceComposites(request as? AllergyIntoleranceCompositeRequest) as BWellResult.ResourceCollection
                emit(allergyIntoleranceComposites)
                val ids = allergyIntoleranceComposites.data?.map { it.id ?: "" } ?: listOf()
                val allergyIntolerancesRequest = AllergyIntoleranceRequest.Builder().ids(ids).build()
                healthSummaryResult = BWellSdk.health.getAllergyIntolerances(allergyIntolerancesRequest)
            }
            else if(category.equals(applicationContext.getString(R.string.communications)))
            {
                healthSummaryResult = BWellSdk.health.getCommunications(request as CommunicationRequest)
            }
            else if(category.equals(applicationContext.getString(R.string.conditions)))
            {
                val conditionComposites: BWellResult.ResourceCollection<ConditionComposite> = BWellSdk.health.getConditionComposites(request as? ConditionCompositeRequest) as BWellResult.ResourceCollection
                emit(conditionComposites)
                val ids = conditionComposites.data?.map { it.id ?: "" } ?: listOf()
                val conditionsRequest = ConditionRequest.Builder().ids(ids).build()
                healthSummaryResult = BWellSdk.health.getConditions(conditionsRequest)
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