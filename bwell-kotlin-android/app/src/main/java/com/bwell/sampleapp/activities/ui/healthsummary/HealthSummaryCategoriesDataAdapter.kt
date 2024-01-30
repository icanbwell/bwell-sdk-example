package com.bwell.sampleapp.activities.ui.healthsummary

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bwell.BWellSdk
import com.bwell.common.models.domain.common.Coding
import com.bwell.common.models.domain.healthdata.healthsummary.allergyintolerance.AllergyIntoleranceGroup
import com.bwell.common.models.domain.healthdata.healthsummary.careplan.CarePlanGroup
import com.bwell.common.models.domain.healthdata.healthsummary.communication.Communication
import com.bwell.common.models.domain.healthdata.healthsummary.condition.ConditionGroup
import com.bwell.common.models.domain.healthdata.healthsummary.encounter.EncounterGroup
import com.bwell.common.models.domain.healthdata.healthsummary.immunization.ImmunizationGroup
import com.bwell.common.models.domain.healthdata.healthsummary.procedure.ProcedureGroup
import com.bwell.common.models.domain.healthdata.healthsummary.vitalsign.VitalSignGroup
import com.bwell.common.models.responses.BWellResult
import com.bwell.healthdata.healthsummary.requests.allergyintolerance.AllergyIntoleranceRequest
import com.bwell.healthdata.healthsummary.requests.careplan.CarePlanRequest
import com.bwell.healthdata.healthsummary.requests.condition.ConditionRequest
import com.bwell.healthdata.healthsummary.requests.encounter.EncountersRequest
import com.bwell.healthdata.healthsummary.requests.immunization.ImmunizationRequest
import com.bwell.healthdata.healthsummary.requests.procedure.ProcedureRequest
import com.bwell.healthdata.healthsummary.requests.vitalsign.VitalSignsRequest
import com.bwell.sampleapp.databinding.HealthSummaryCategoriesItemsViewBinding
import com.bwell.sampleapp.utils.formatDate
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

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
            is CarePlanGroup -> {
                holder.binding.header.text = getTitle(launch)
                val startDate = getDate(launch)
                val formattedDate = startDate?.let { formatDate(it) }
                holder.binding.textViewDate.text = ("Started :  $formattedDate")
                holder.binding.organizationName.text = "from "+ launch.source?.joinToString(", ")
            }
            is ImmunizationGroup ->{
                holder.binding.header.text = getTitle(launch)
                val startDate = getDate(launch)
                val formattedDate = startDate?.let { formatDate(it) }
                holder.binding.textViewDate.text = "Most Recent : $formattedDate"
                holder.binding.organizationName.text = "from "+ launch.source?.joinToString(", ")
            }
            is ProcedureGroup ->{
                holder.binding.header.text = getTitle(launch)
                val startDate = getDate(launch)
                val formattedDate = startDate?.let { formatDate(it) }
                holder.binding.textViewDate.text = "Started : $formattedDate"
                holder.binding.organizationName.text = "from "+ launch.source?.joinToString(", ")
            }
            is VitalSignGroup ->{
                holder.binding.header.text = getTitle(launch)
                val startDate = getDate(launch)
                val formattedDate = startDate?.let { formatDate(it) }
                holder.binding.textViewDate.text = "Effective Date : $formattedDate"
                holder.binding.organizationName.text = "from "+ launch.source?.joinToString(", ")
            }
            is EncounterGroup ->{
                holder.binding.header.text = getTitle(launch)
                val startDate = getDate(launch)
                val formattedDate = startDate?.let { formatDate(it) }
                holder.binding.textViewDate.text = "Started : $formattedDate"
                holder.binding.organizationName.text = "from "+ launch.source?.joinToString(", ")
            }
            is AllergyIntoleranceGroup ->{
                holder.binding.header.text = getTitle(launch)
                holder.binding.textViewDate.text =launch.criticality.toString()
                holder.binding.organizationName.text = "from "+ launch.source?.joinToString(", ")
            }
            is Communication ->{
                holder.binding.header.text = getTitle(launch)
                holder.binding.textViewDate.text = launch.status.toString()
                holder.binding.organizationName.text = "from "+ launch.meta?.source
            }
            is ConditionGroup ->{
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
            val groupCodeCode = getGroupCodeCode(launch)
            val groupCodeSystem = getGroupCodeSystem(launch)

            when (launch) {
                is CarePlanGroup -> {
                    val carePlanRequest = CarePlanRequest.Builder()
                        //.ids(listOf(id))
                        .groupCode(listOf(Coding(code = groupCodeCode, system = groupCodeSystem)))
                        //.page(0)
                        //.pageSize(1)
                        .build()
                    GlobalScope.launch {
                        val carePlans = BWellSdk.health.getCarePlans(carePlanRequest) as BWellResult.ResourceCollection
                        //printProperties(carePlans.data?.get(position))
                    }
                }
                is ImmunizationGroup ->{
                    val immunizationRequest = ImmunizationRequest.Builder()
                        //.ids(listOf(id))
                        .groupCode(listOf(Coding(code = groupCodeCode, system = groupCodeSystem)))
                        //.page(0)
                        //.pageSize(1)
                        .build()
                    GlobalScope.launch {
                        val immunizations = BWellSdk.health.getImmunizations(immunizationRequest) as BWellResult.ResourceCollection
                        //printProperties(immunizations.data?.get(position))
                    }
                }
                is ProcedureGroup ->{
                    val proceduresRequest = ProcedureRequest.Builder()
                        //.ids(listOf(id))
                        .groupCode(listOf(Coding(code = groupCodeCode, system = groupCodeSystem)))
                        //.page(0)
                        //.pageSize(1)
                        .build()
                    GlobalScope.launch {
                        val procedures = BWellSdk.health.getProcedures(proceduresRequest) as BWellResult.ResourceCollection
                        //printProperties(procedures.data?.get(position))
                    }
                }
                is VitalSignGroup ->{
                    val vitalSignsRequest = VitalSignsRequest.Builder()
                        //.ids(listOf(id))
                        .groupCode(listOf(Coding(code = groupCodeCode, system = groupCodeSystem)))
                        //.groupCode(listOf(Coding(code = "8302-2", system = "http://loinc.org")))
                        //.page(0)
                        //.pageSize(1)
                        .build()
                    GlobalScope.launch {
                        val vitalSigns = BWellSdk.health.getVitalSigns(vitalSignsRequest) as BWellResult.ResourceCollection
                        //printProperties(vitalSigns.data?.get(position))
                    }
                }
                is EncounterGroup ->{
                    val encountersRequest = EncountersRequest.Builder()
                        //.ids(listOf(id))
                        //.groupCode(listOf(GroupCoding(code = groupCodeCode, system = groupCodeSystem)))
                        //.page(0)
                        .pageSize(1)
                        .build()
                    GlobalScope.launch {
                        val encounters = BWellSdk.health.getEncounters(encountersRequest) as BWellResult.ResourceCollection
                        //printProperties(encounters.data?.get(position))
                    }
                }
                is AllergyIntoleranceGroup ->{
                    val allergyIntoleranceRequest = AllergyIntoleranceRequest.Builder()
                        //.ids(listOf(id))
                        .groupCode(listOf(Coding(code = groupCodeCode, system = groupCodeSystem)))
                        //.page(0)
                        //.pageSize(1)
                        .build()
                    GlobalScope.launch {
                        val allergyIntolerances = BWellSdk.health.getAllergyIntolerances(allergyIntoleranceRequest) as BWellResult.ResourceCollection
                        //printProperties(allergyIntolerances.data?.get(position))
                    }
                }
                is Communication ->{
                    // do nothing
                }
                is ConditionGroup ->{
                    val conditionsRequest = ConditionRequest.Builder()
                        //.ids(listOf(id))
                        .groupCode(listOf(Coding(code = groupCodeCode, system = groupCodeSystem)))
                        //.page(0)
                        //.pageSize(1)
                        .build()
                    GlobalScope.launch {
                        val conditions = BWellSdk.health.getConditions(conditionsRequest) as BWellResult.ResourceCollection
                        //printProperties(conditions.data?.get(position))
                    }
                }
            }
        }
    }

    private fun getTitle(item: T?): String {
        return when (item) {
            is CarePlanGroup -> item.name ?: ""
            is ImmunizationGroup -> item.name ?: ""
            is ProcedureGroup -> item.name ?: ""
            is VitalSignGroup -> item.name ?: ""
            is EncounterGroup -> item.name ?: ""
            is AllergyIntoleranceGroup -> item.name ?: ""
            is Communication -> item.category?.get(0)?.coding?.get(0)?.code ?: ""
            is ConditionGroup -> item.name ?: ""
            else -> ""
        }
    }

    private fun getDate(item: T?): String? {
        return when (item) {
            is CarePlanGroup -> item.period?.start.toString()
            is ImmunizationGroup -> item.occurrenceDateTime.toString()
            is ProcedureGroup -> item.performedDate.toString()
            is VitalSignGroup -> item.effectiveDateTime.toString()
            is EncounterGroup -> item.period?.start.toString()
            is ConditionGroup -> item.recordedDate.toString()

            else -> null
        }
    }

    private fun getId(item: T?): String {
        return when (item) {
            is CarePlanGroup -> item.id ?: ""
            is ImmunizationGroup -> item.id ?: ""
            is ProcedureGroup -> item.id ?: ""
            is VitalSignGroup -> item.id ?: ""
            is EncounterGroup -> item.id ?: ""
            is ConditionGroup -> item.id ?: ""
            is AllergyIntoleranceGroup -> item.id ?: ""
            else -> ""
        }
    }

    private fun getGroupCodeSystem(item: T?): String {
        return when (item) {
            is CarePlanGroup -> item.coding?.system.toString()
            is ImmunizationGroup -> item.coding?.system.toString()
            is ProcedureGroup -> item.coding?.system.toString()
            is VitalSignGroup -> item.coding?.system.toString()
            //is EncounterGroup -> item.coding?.system.toString()
            is ConditionGroup -> item.coding?.system.toString()
            is AllergyIntoleranceGroup -> item.coding?.system.toString()
            else -> ""
        }
    }

    private fun getGroupCodeCode(item: T?): String {
        return when (item) {
            is CarePlanGroup -> item.coding?.code.toString()
            is ImmunizationGroup -> item.coding?.code.toString()
            is ProcedureGroup -> item.coding?.code.toString()
            is VitalSignGroup -> item.coding?.code.toString()
            //is EncounterGroup -> item.coding?.code.toString()
            is ConditionGroup -> item.coding?.code.toString()
            is AllergyIntoleranceGroup -> item.coding?.code.toString()
            else -> ""
        }
    }


    fun printProperties(obj: Any?) {
        if (obj == null) {
            return
        }
        val properties = obj::class.members.joinToString(", ") { prop ->
            "${prop.name} = ${prop.call(obj)}"
        }
        println("${obj::class.simpleName}($properties)")
    }
}

