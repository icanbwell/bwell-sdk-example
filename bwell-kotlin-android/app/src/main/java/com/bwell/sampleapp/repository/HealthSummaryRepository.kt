package com.bwell.sampleapp.repository

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.bwell.common.models.domain.healthdata.common.Binary
import com.bwell.common.models.domain.healthdata.healthsummary.documentreference.DocumentReference
import com.bwell.sampleapp.singletons.BWellSdk
import com.bwell.common.models.domain.healthdata.healthsummary.healthsummary.HealthSummary
import com.bwell.common.models.domain.healthdata.healthsummary.healthsummary.enums.HealthSummaryCategory
import com.bwell.common.models.responses.BWellResult
import com.bwell.healthdata.healthsummary.requests.allergyintolerance.AllergyIntoleranceGroupsRequest
import com.bwell.healthdata.healthsummary.requests.allergyintolerance.AllergyIntoleranceRequest
import com.bwell.healthdata.healthsummary.requests.careplan.CarePlanGroupsRequest
import com.bwell.healthdata.healthsummary.requests.careplan.CarePlanRequest
import com.bwell.healthdata.healthsummary.requests.condition.ConditionGroupsRequest
import com.bwell.healthdata.healthsummary.requests.condition.ConditionRequest
import com.bwell.healthdata.healthsummary.requests.documentReference.DocumentReferencesRequest
import com.bwell.healthdata.healthsummary.requests.encounter.EncounterGroupsRequest
import com.bwell.healthdata.healthsummary.requests.encounter.EncounterRequest
import com.bwell.healthdata.healthsummary.requests.immunization.ImmunizationGroupsRequest
import com.bwell.healthdata.healthsummary.requests.immunization.ImmunizationRequest
import com.bwell.healthdata.healthsummary.requests.procedure.ProcedureGroupsRequest
import com.bwell.healthdata.healthsummary.requests.procedure.ProcedureRequest
import com.bwell.healthdata.healthsummary.requests.vitalsign.VitalSignGroupsRequest
import com.bwell.healthdata.healthsummary.requests.vitalsign.VitalSignsRequest
import com.bwell.healthdata.requests.binary.BinaryRequest
import com.bwell.sampleapp.R
import com.bwell.sampleapp.model.HealthSummaryList
import com.bwell.sampleapp.model.HealthSummaryListItems
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext

class HealthSummaryRepository(private val applicationContext: Context) {

    private val TAG = HealthSummaryRepository::class.java.simpleName
    private val repositoryScope = CoroutineScope(Dispatchers.IO)

    fun cancelScope(){
        repositoryScope.cancel()
    }


    suspend fun getDocumentReference(documentReferencesRequest: DocumentReferencesRequest): Flow<BWellResult<DocumentReference>?> = flow {
        try {
            val documentReferenceResult = BWellSdk.health.getDocumentReferences(documentReferencesRequest)
            emit(documentReferenceResult)
        } catch (e: Exception) {
            emit(null)
        }
    }


    suspend fun getBinary(binaryRequest: BinaryRequest): Flow<BWellResult<Binary>?> = flow {
        try {
            val binaryResult = BWellSdk.health.getBinary(binaryRequest)
            emit(binaryResult)
        } catch (e: Exception) {
            emit(null)
        }
    }

    suspend fun <T : Any> getHealthSummaryGroupData(request: T?, category: HealthSummaryCategory?): Flow<BWellResult<Any>?> = flow {
        try {
            val healthSummaryResult:BWellResult<Any>? = when (category) {
                HealthSummaryCategory.CARE_PLAN -> {
                    BWellSdk.health.getCarePlanGroups(request as? CarePlanGroupsRequest)
                }
                HealthSummaryCategory.IMMUNIZATION -> {
                    BWellSdk.health.getImmunizationGroups(request as? ImmunizationGroupsRequest)
                }
                HealthSummaryCategory.PROCEDURE -> {
                    BWellSdk.health.getProcedureGroups(request as? ProcedureGroupsRequest)
                }
                HealthSummaryCategory.VITAL_SIGNS -> {
                    BWellSdk.health.getVitalSignGroups(request as? VitalSignGroupsRequest)
                }
                HealthSummaryCategory.ENCOUNTER -> {
                    BWellSdk.health.getEncounterGroups(request as? EncounterGroupsRequest)
                }
                HealthSummaryCategory.ALLERGY_INTOLERANCE -> {
                    BWellSdk.health.getAllergyIntoleranceGroups(request as AllergyIntoleranceGroupsRequest)
                }
                HealthSummaryCategory.CONDITION -> {
                    BWellSdk.health.getConditionGroups(request as ConditionGroupsRequest)
                }
//                HealthSummaryCategory.DOCUMENT_REFERENCE -> {
//                    BWellSdk.health.getDocumentReferences(request as DocumentReferencesRequest)
//                }
                else -> {
                    null
                }
            }
            emit(healthSummaryResult)
        } catch (e: Exception) {
            emit(null)
        }
    }

    suspend fun <T : Any> getHealthSummaryData(request: T?, category: HealthSummaryCategory?): Flow<BWellResult<Any>?> = flow {
        try {
            val healthSummaryResult:BWellResult<Any>? = when (category) {
                HealthSummaryCategory.CARE_PLAN -> {
                    BWellSdk.health.getCarePlans(request as? CarePlanRequest)
                }
                HealthSummaryCategory.IMMUNIZATION -> {
                    BWellSdk.health.getImmunizations(request as? ImmunizationRequest)
                }
                HealthSummaryCategory.PROCEDURE -> {
                    BWellSdk.health.getProcedures(request as? ProcedureRequest)
                }
                HealthSummaryCategory.VITAL_SIGNS -> {
                    BWellSdk.health.getVitalSigns(request as? VitalSignsRequest)
                }
                HealthSummaryCategory.ENCOUNTER -> {
                    BWellSdk.health.getEncounters(request as? EncounterRequest)
                }
                HealthSummaryCategory.ALLERGY_INTOLERANCE -> {
                    BWellSdk.health.getAllergyIntolerances(request as AllergyIntoleranceRequest)
                }
                HealthSummaryCategory.CONDITION -> {
                    BWellSdk.health.getConditions(request as ConditionRequest)
                }
                else -> {
                    null
                }
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
        try{
            // use withContext to 'await' the getHealthSummary call
            val healthSummaryResult = withContext(Dispatchers.IO) {
                BWellSdk.health.getHealthSummary()
            }

            // Update and post to live data
            Log.d(TAG, "Retrieved HealthSummary from Bwell SDK")

            val healthSummaryList = (healthSummaryResult as? BWellResult.ResourceCollection<HealthSummary>)?.data


            val healthSummaryCategoryList = mutableListOf<HealthSummaryListItems>()

            // create HealthSummaryListItems with friendly names
            for(healthSummary in healthSummaryList ?: throw Exception("Could not retrieve health summaries")){
                if(healthSummary.category == HealthSummaryCategory.LABS || healthSummary.category == HealthSummaryCategory.MEDICATIONS){
                    continue
                    // Skip as not included in EA-766
                }

                healthSummaryCategoryList.add(
                    HealthSummaryListItems(
                        R.drawable.baseline_person_24,
                        R.drawable.baseline_keyboard_arrow_right_24,
                        healthSummary.category ?: throw Exception("Invalid health summary category"),
                        getCategoryFriendlyName(healthSummary.category),
                        healthSummary.total
                    )
                )

                Log.d(TAG, "Added Health Summary Category: " + healthSummary.category.toString())
            }
            
            // Currently we do not have support for document reference 
//            healthSummaryCategoryList.add(
//                HealthSummaryListItems(
//                    R.drawable.baseline_person_24,
//                    R.drawable.baseline_keyboard_arrow_right_24,
//                    HealthSummaryCategory.DOCUMENT_REFERENCE,
//                    getCategoryFriendlyName(category = HealthSummaryCategory.DOCUMENT_REFERENCE),
//                    10 // Not yet Integrated
//                )
//            )


            Log.d(TAG, "Posting HealthSummary live data")
            // Post live data
            healthSummaryLiveData.postValue(HealthSummaryList(healthSummaryCategoryList))

        } catch (e: Exception){
            // do nothing for now
        }
    }

    private fun getCategoryFriendlyName(category: HealthSummaryCategory?): String {

        when (category) {
            HealthSummaryCategory.ALLERGY_INTOLERANCE -> {
                return applicationContext.getString(R.string.allergies)
            }
            HealthSummaryCategory.CARE_PLAN -> {
                return applicationContext.getString(R.string.care_plans)
            }
            HealthSummaryCategory.CONDITION -> {
                return applicationContext.getString(R.string.conditions)
            }
            HealthSummaryCategory.ENCOUNTER -> {
                return applicationContext.getString(R.string.visit_history)
            }
            HealthSummaryCategory.IMMUNIZATION -> {
                return applicationContext.getString(R.string.immunizations)
            }
            HealthSummaryCategory.PROCEDURE -> {
                return applicationContext.getString(R.string.procedures)
            }
            HealthSummaryCategory.VITAL_SIGNS -> {
                return applicationContext.getString(R.string.vitals)
            }
//            HealthSummaryCategory.DOCUMENT_REFERENCE -> {
//                return applicationContext.getString(R.string.document_references)
//            }
            else -> {
                throw IllegalStateException("Invalid category row")
            }
        }
    }
}