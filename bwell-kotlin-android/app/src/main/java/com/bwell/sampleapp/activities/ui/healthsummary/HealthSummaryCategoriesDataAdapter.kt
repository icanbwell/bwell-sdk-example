package com.bwell.sampleapp.activities.ui.healthsummary

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bwell.BWellSdk
import com.bwell.common.models.domain.healthdata.healthsummary.allergyintolerance.AllergyIntoleranceComposite
import com.bwell.common.models.domain.healthdata.healthsummary.careplan.CarePlanComposite
import com.bwell.common.models.domain.healthdata.healthsummary.communication.Communication
import com.bwell.common.models.domain.healthdata.healthsummary.condition.ConditionComposite
import com.bwell.common.models.domain.healthdata.healthsummary.encounter.EncounterComposite
import com.bwell.common.models.domain.healthdata.healthsummary.immunization.ImmunizationComposite
import com.bwell.common.models.domain.healthdata.healthsummary.procedure.ProcedureComposite
import com.bwell.common.models.domain.healthdata.observation.Observation
import com.bwell.common.models.domain.healthdata.observation.ObservationComposite
import com.bwell.common.models.responses.BWellResult
import com.bwell.healthdata.healthsummary.allergyintolerance.AllergyIntoleranceRequest
import com.bwell.healthdata.healthsummary.careplan.CarePlanRequest
import com.bwell.healthdata.healthsummary.condition.ConditionRequest
import com.bwell.healthdata.healthsummary.encounter.EncounterRequest
import com.bwell.healthdata.healthsummary.immunization.ImmunizationRequest
import com.bwell.healthdata.healthsummary.procedure.ProcedureRequest
import com.bwell.healthdata.healthsummary.vitalsign.VitalSignRequest
import com.bwell.sampleapp.databinding.HealthSummaryCategoriesItemsViewBinding
import com.bwell.sampleapp.utils.formatDate
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.reflect.full.memberProperties

/*
*Display the Labs List in RecyclerView
* */
class HealthSummaryCategoriesDataAdapter<T>(private val launches: List<T>?) :
    RecyclerView.Adapter<HealthSummaryCategoriesDataAdapter.ViewHolder>() {

    class ViewHolder(val binding: HealthSummaryCategoriesItemsViewBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = HealthSummaryCategoriesItemsViewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return launches?.size ?: 0
    }

    var onEndOfListReached: (() -> Unit)? = null
    var onItemClicked: ((T?) -> Unit)? = null

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val launch = launches?.get(position)
        when (launch) {
            is CarePlanComposite -> {
                holder.binding.header.text = getTitle(launch)
                val startDate = getDate(launch)
                val formattedDate = startDate?.let { formatDate(it) }
                holder.binding.textViewDate.text = ("Started :  $formattedDate")
                holder.binding.organizationName.text = "from "+ launch.source?.joinToString(", ")
            }
            is ImmunizationComposite ->{
                holder.binding.header.text = getTitle(launch)
                val startDate = getDate(launch)
                val formattedDate = startDate?.let { formatDate(it) }
                holder.binding.textViewDate.text = "Most Recent : $formattedDate"
                holder.binding.organizationName.text = "from "+ launch.source?.joinToString(", ")
            }
            is ProcedureComposite ->{
                holder.binding.header.text = getTitle(launch)
                val startDate = getDate(launch)
                val formattedDate = startDate?.let { formatDate(it) }
                holder.binding.textViewDate.text = "Started : $formattedDate"
                holder.binding.organizationName.text = "from "+ launch.source?.joinToString(", ")
            }
            is ObservationComposite ->{
                holder.binding.header.text = getTitle(launch)
                val startDate = getDate(launch)
                val formattedDate = startDate?.let { formatDate(it) }
                holder.binding.textViewDate.text = "Effective Date : $formattedDate"
                holder.binding.organizationName.text = "from "+ launch.source?.joinToString(", ")
            }
            is EncounterComposite ->{
                holder.binding.header.text = getTitle(launch)
                val startDate = getDate(launch)
                val formattedDate = startDate?.let { formatDate(it) }
                holder.binding.textViewDate.text = "Started : $formattedDate"
                holder.binding.organizationName.text = "from "+ launch.source?.joinToString(", ")
            }
            is AllergyIntoleranceComposite ->{
                holder.binding.header.text = getTitle(launch)
                holder.binding.textViewDate.text =launch.criticality.toString()
                holder.binding.organizationName.text = "from "+ launch.source?.joinToString(", ")
            }
            is Communication ->{
                holder.binding.header.text = getTitle(launch)
                holder.binding.textViewDate.text = launch.status.toString()
                holder.binding.organizationName.text = "from "+ launch.meta?.source
            }
            is ConditionComposite ->{
                holder.binding.header.text = getTitle(launch)
                val startDate = getDate(launch)
                val formattedDate = startDate?.let { formatDate(it) }
                holder.binding.textViewDate.text = "Recorded Date : $formattedDate"
                holder.binding.organizationName.text = "from "+ launch.source?.joinToString(", ")
            }
            else -> ""
        }

        if (position == (launches?.size ?: 0) - 1) {
            onEndOfListReached?.invoke()
        }
        holder.binding.root.setOnClickListener {
            val id = getId(launch)
            when (launch) {
                is CarePlanComposite -> {
                    val carePlanRequest = CarePlanRequest.Builder().ids(listOf(id)).build()
                    GlobalScope.launch {
                        val carePlans = BWellSdk.health.getCarePlans(carePlanRequest) as BWellResult.ResourceCollection
                        printProperties(carePlans.data?.get(position))
                    }
                }
                is ImmunizationComposite ->{
                    val immunizationRequest = ImmunizationRequest.Builder().ids(listOf(id)).build()
                    GlobalScope.launch {
                        val immunizations = BWellSdk.health.getImmunizations(immunizationRequest) as BWellResult.ResourceCollection
                        printProperties(immunizations.data?.get(position))
                    }
                }
                is ProcedureComposite ->{
                    val proceduresRequest = ProcedureRequest.Builder().ids(listOf(id)).build()
                    GlobalScope.launch {
                        val procedures = BWellSdk.health.getProcedures(proceduresRequest) as BWellResult.ResourceCollection
                        printProperties(procedures.data?.get(position))
                    }
                }
                is ObservationComposite ->{
                    val vitalSignsRequest = VitalSignRequest.Builder().ids(listOf(id)).build()
                    GlobalScope.launch {
                        val vitalSigns = BWellSdk.health.getVitalSigns(vitalSignsRequest) as BWellResult.ResourceCollection
                        printProperties(vitalSigns.data?.get(position))
                    }
                }
                is EncounterComposite ->{
                    val encountersRequest = EncounterRequest.Builder().ids(listOf(id)).build()
                    GlobalScope.launch {
                        val encounters = BWellSdk.health.getEncounters(encountersRequest) as BWellResult.ResourceCollection
                        printProperties(encounters.data?.get(position))
                    }
                }
                is AllergyIntoleranceComposite ->{
                    val allergyIntoleranceRequest = AllergyIntoleranceRequest.Builder().ids(listOf(id)).build()
                    GlobalScope.launch {
                        val allergyIntolerances = BWellSdk.health.getAllergyIntolerances(allergyIntoleranceRequest) as BWellResult.ResourceCollection
                        printProperties(allergyIntolerances.data?.get(position))
                    }
                }
                is Communication ->{
                    // do nothing
                }
                is ConditionComposite ->{
                    val conditionsRequest = ConditionRequest.Builder().ids(listOf(id)).build()
                    GlobalScope.launch {
                        val conditions = BWellSdk.health.getConditions(conditionsRequest) as BWellResult.ResourceCollection
                        printProperties(conditions.data?.get(position))
                    }
                }
            }
        }
    }

    private fun getTitle(item: T?): String {
        return when (item) {
            is CarePlanComposite -> item.name ?: ""
            is ImmunizationComposite -> item.name ?: ""
            is ProcedureComposite -> item.name ?: ""
            is ObservationComposite -> item.name ?: ""
            is EncounterComposite -> item.name ?: ""
            is AllergyIntoleranceComposite -> item.name ?: ""
            is Communication -> item.category?.get(0)?.coding?.get(0)?.code ?: ""
            is ConditionComposite -> item.name ?: ""
            else -> ""
        }
    }

    private fun getDate(item: T?): String? {
        return when (item) {
            is CarePlanComposite -> item.period?.start.toString()
            is ImmunizationComposite -> item.occurrenceDateTime.toString()
            is ProcedureComposite -> item.period?.start.toString()
            is ObservationComposite -> item.effectiveDateTime.toString()
            is EncounterComposite -> item.period?.start.toString()
            is ConditionComposite -> item.recordedDate.toString()
            else -> null
        }
    }

    private fun getId(item: T?): String {
        return when (item) {
            is CarePlanComposite -> item.id ?: ""
            is ImmunizationComposite -> item.id ?: ""
            is ProcedureComposite -> item.id ?: ""
            is ObservationComposite -> item.id ?: ""
            is EncounterComposite -> item.id ?: ""
            is ConditionComposite -> item.id ?: ""
            else -> ""
        }
    }


    fun printProperties(obj: Any?) {
        if (obj == null) {
            return
        }
        val properties = obj::class.memberProperties.joinToString(", ") { prop ->
            "${prop.name} = ${prop.call(obj)}"
        }
        println("${obj::class.simpleName}($properties)")
    }
}

