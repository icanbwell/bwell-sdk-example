package com.bwell.sampleapp.activities.ui.healthsummary

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bwell.common.models.domain.healthdata.healthsummary.allergyintolerance.AllergyIntoleranceGroup
import com.bwell.common.models.domain.healthdata.healthsummary.careplan.CarePlanGroup
import com.bwell.common.models.domain.healthdata.healthsummary.communication.Communication
import com.bwell.common.models.domain.healthdata.healthsummary.condition.ConditionGroup
import com.bwell.common.models.domain.healthdata.healthsummary.encounter.EncounterGroup
import com.bwell.common.models.domain.healthdata.healthsummary.immunization.ImmunizationGroup
import com.bwell.common.models.domain.healthdata.healthsummary.procedure.ProcedureGroup
import com.bwell.common.models.domain.healthdata.observation.ObservationComposition
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
            is ObservationComposition ->{
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
            onItemClicked?.invoke(launch)
        }
    }

    private fun getTitle(item: T?): String {
        return when (item) {
            is CarePlanGroup -> item.name ?: ""
            is ImmunizationGroup -> item.name ?: ""
            is ProcedureGroup -> item.name ?: ""
            is ObservationComposition -> item.name ?: ""
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
            is ProcedureGroup -> item.period?.start.toString()
            is ObservationComposition -> item.effectiveDateTime.toString()
            is EncounterGroup -> item.period?.start.toString()
            is ConditionGroup -> item.recordedDate.toString()
            else -> null
        }
    }
}

