package com.bwell.sampleapp.activities.ui.healthsummary

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bwell.common.models.domain.healthdata.healthsummary.allergyintolerance.AllergyIntoleranceComposite
import com.bwell.common.models.domain.healthdata.healthsummary.careplan.CarePlanComposite
import com.bwell.common.models.domain.healthdata.healthsummary.communication.Communication
import com.bwell.common.models.domain.healthdata.healthsummary.condition.ConditionComposite
import com.bwell.common.models.domain.healthdata.healthsummary.encounter.EncounterComposite
import com.bwell.common.models.domain.healthdata.healthsummary.immunization.ImmunizationComposite
import com.bwell.common.models.domain.healthdata.healthsummary.procedure.ProcedureComposite
import com.bwell.common.models.domain.healthdata.observation.ObservationComposite
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
            onItemClicked?.invoke(launch)
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
}

