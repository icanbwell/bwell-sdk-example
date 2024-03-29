package com.bwell.sampleapp.activities.ui.healthsummary

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bwell.sampleapp.singletons.BWellSdk
import com.bwell.common.models.domain.common.Coding
import com.bwell.common.models.domain.healthdata.healthsummary.allergyintolerance.AllergyIntoleranceGroup
import com.bwell.common.models.domain.healthdata.healthsummary.careplan.CarePlanGroup
import com.bwell.common.models.domain.healthdata.healthsummary.condition.ConditionGroup
import com.bwell.common.models.domain.healthdata.healthsummary.encounter.EncounterGroup
import com.bwell.common.models.domain.healthdata.healthsummary.immunization.ImmunizationGroup
import com.bwell.common.models.domain.healthdata.healthsummary.procedure.ProcedureGroup
import com.bwell.common.models.domain.healthdata.healthsummary.vitalsign.VitalSignGroup
import com.bwell.sampleapp.databinding.HealthSummaryCategoriesItemsViewBinding
import com.bwell.sampleapp.utils.formatDate

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
                holder.binding.organizationName.text = "from "+ getSource(launch)
                holder.binding.organizationLl.visibility = if (getSource(launch).isNullOrBlank()) View.GONE else View.VISIBLE
            }
            is ImmunizationGroup ->{
                holder.binding.header.text = getTitle(launch)
                val startDate = getDate(launch)
                val formattedDate = startDate?.let { formatDate(it) }
                holder.binding.textViewDate.text = "Most Recent : $formattedDate"
                holder.binding.organizationName.text = "from "+ getSource(launch)
                holder.binding.organizationLl.visibility = if (getSource(launch).isNullOrBlank()) View.GONE else View.VISIBLE
            }
            is ProcedureGroup ->{
                holder.binding.header.text = getTitle(launch)
                val startDate = getDate(launch)
                val formattedDate = startDate?.let { formatDate(it) }
                holder.binding.textViewDate.text = "Started : $formattedDate"
                holder.binding.organizationName.text = "from "+ getSource(launch)
                holder.binding.organizationLl.visibility = if (getSource(launch).isNullOrBlank()) View.GONE else View.VISIBLE
            }
            is VitalSignGroup ->{
                holder.binding.header.text = getTitle(launch)
                val startDate = getDate(launch)
                val formattedDate = startDate?.let { formatDate(it) }
                holder.binding.textViewDate.text = "Effective Date : $formattedDate"
                holder.binding.organizationName.text = "from "+ getSource(launch)
                holder.binding.organizationLl.visibility = if (getSource(launch).isNullOrBlank()) View.GONE else View.VISIBLE
            }
            is EncounterGroup ->{
                holder.binding.header.text = getTitle(launch)
                val startDate = getDate(launch)
                val formattedDate = startDate?.let { formatDate(it) }
                holder.binding.textViewDate.text = "Started : $formattedDate"
                holder.binding.organizationName.text = "from "+ getSource(launch)
                holder.binding.organizationLl.visibility = if (getSource(launch).isNullOrBlank()) View.GONE else View.VISIBLE
            }
            is AllergyIntoleranceGroup ->{
                holder.binding.header.text = getTitle(launch)
                holder.binding.textViewDate.text =launch.criticality?.display
                holder.binding.organizationName.text = "from "+ getSource(launch)
                holder.binding.organizationLl.visibility = if (getSource(launch).isNullOrBlank()) View.GONE else View.VISIBLE
            }
            is ConditionGroup ->{
                holder.binding.header.text = getTitle(launch)
                val startDate = getDate(launch)
                val formattedDate = startDate?.let { formatDate(it) }
                holder.binding.textViewDate.text = "Recorded Date : $formattedDate"
                holder.binding.organizationName.text = "from "+ getSource(launch)
                holder.binding.organizationLl.visibility = if (getSource(launch).isNullOrBlank()) View.GONE else View.VISIBLE
            }
            else -> ""
        }

        if (position == (launches?.size ?: 0) - 1) {
            onEndOfListReached?.invoke()
        }
        holder.binding.root.setOnClickListener {
            onItemClicked?.invoke(launch)
        }
    }

    fun getSource(item: T?): String {
        return when (item) {
            is CarePlanGroup -> item.source?.joinToString(", ") ?: ""
            is ImmunizationGroup -> item.source?.joinToString(", ") ?: ""
            is ProcedureGroup -> item.source?.joinToString(", ") ?: ""
            is VitalSignGroup -> item.source?.joinToString(", ") ?: ""
            is EncounterGroup -> item.source?.joinToString(", ") ?: ""
            is AllergyIntoleranceGroup -> item.source?.joinToString(", ") ?: ""
            is ConditionGroup -> item.source?.joinToString(", ") ?: ""
            else -> ""
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
            is EncounterGroup -> item.date.toString()
            is ConditionGroup -> item.recordedDate.toString()
            else -> null
        }
    }

    fun getId(item: T?): String {
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

    fun getName(item: T?): String {
        return when (item) {
            is CarePlanGroup -> item.name ?: ""
            is ImmunizationGroup -> item.name ?: ""
            is ProcedureGroup -> item.name ?: ""
            is VitalSignGroup -> item.name ?: ""
            is EncounterGroup -> item.name ?: ""
            is ConditionGroup -> item.name ?: ""
            is AllergyIntoleranceGroup -> item.name ?: ""
            else -> ""
        }
    }

    fun getGroupCodeSystem(item: T?): String {
        return when (item) {
            is CarePlanGroup -> item.coding?.system.toString()
            is ImmunizationGroup -> item.coding?.system.toString()
            is ProcedureGroup -> item.coding?.system.toString()
            is VitalSignGroup -> item.coding?.system.toString()
            is EncounterGroup -> item.coding?.system.toString()
            is ConditionGroup -> item.coding?.system.toString()
            is AllergyIntoleranceGroup -> item.coding?.system.toString()
            else -> ""
        }
    }

    fun getGroupCodeCode(item: T?): String {
        return when (item) {
            is CarePlanGroup -> item.coding?.code.toString()
            is ImmunizationGroup -> item.coding?.code.toString()
            is ProcedureGroup -> item.coding?.code.toString()
            is VitalSignGroup -> item.coding?.code.toString()
            is EncounterGroup -> item.coding?.code.toString()
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

