package com.bwell.sampleapp.activities.ui.healthsummary

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bwell.common.models.domain.healthdata.healthsummary.allergyintolerance.AllergyIntolerance
import com.bwell.common.models.domain.healthdata.healthsummary.careplan.CarePlan
import com.bwell.common.models.domain.healthdata.healthsummary.communication.Communication
import com.bwell.common.models.domain.healthdata.healthsummary.condition.Condition
import com.bwell.common.models.domain.healthdata.healthsummary.immunization.Immunization
import com.bwell.common.models.domain.healthdata.healthsummary.procedure.Procedure
import com.bwell.common.models.domain.healthdata.observation.Observation
import com.bwell.common.models.domain.healthdata.healthsummary.encounter.Encounter
import com.bwell.sampleapp.R
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

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val launch = launches?.get(position)
        when (launch) {
            is CarePlan -> {
                holder.binding.header.text = getTitle(launch)
                val startDate = getDate(launch)
                val formattedDate = startDate?.let { formatDate(it) }
                holder.binding.textViewDate.text = "Started "+formattedDate ?: ""
                addTextField(holder, holder.binding.root.context.getString(R.string.status), getDataOne(launch))
                addTextField(holder, holder.binding.root.context.getString(R.string.intent), getDataTwo(launch))
            }
            is Immunization ->{
                holder.binding.header.text = getTitle(launch)
                val startDate = getDate(launch)
                val formattedDate = startDate?.let { formatDate(it) }
                holder.binding.textViewDate.text = "Most Recent: "+formattedDate ?: ""
                holder.binding.organizationName.text = "from "+launch?.performer?.get(1)?.actor?.onOrganization?.organizationName
                addTextField(holder, holder.binding.root.context.getString(R.string.dose_number), getDataOne(launch))
                addTextField(holder, holder.binding.root.context.getString(R.string.manufacturer_name), getDataTwo(launch))
            }
            is Procedure ->{
                holder.binding.header.text = getTitle(launch)
                val startDate = getDate(launch)
                val formattedDate = startDate?.let { formatDate(it) }
                holder.binding.textViewDate.text = "Performed Date: "+formattedDate ?: ""
                holder.binding.organizationName.text = "from "+launch?.performer?.get(1)?.actor?.onOrganization?.organizationName
                addTextField(holder, holder.binding.root.context.getString(R.string.bodysite), getDataOne(launch))
                addTextField(holder, holder.binding.root.context.getString(R.string.reason), getDataTwo(launch))
                addTextField(holder, holder.binding.root.context.getString(R.string.outcome), launch.outcome.toString())
                addTextField(holder, holder.binding.root.context.getString(R.string.followup), launch.followUp.toString())
                addTextField(holder, holder.binding.root.context.getString(R.string.complication), launch.complication.toString())
                addTextField(holder, holder.binding.root.context.getString(R.string.notes), launch.note.toString())
            }
            is Observation ->{
                holder.binding.header.text = getTitle(launch)
                val startDate = getDate(launch)
                val formattedDate = startDate?.let { formatDate(it) }
                holder.binding.textViewDate.text = "Effective Date: "+formattedDate ?: ""
                holder.binding.organizationName.text = "from "+launch?.performer?.get(1)?.onOrganization?.organizationName
                addTextField(holder, holder.binding.root.context.getString(R.string.result), getDataOne(launch))
                addTextField(holder, holder.binding.root.context.getString(R.string.healthy_range), getDataTwo(launch))
            }
            is Encounter ->{
                holder.binding.header.text = getTitle(launch)
                val startDate = getDate(launch)
                val formattedDate = startDate?.let { formatDate(it) }
                holder.binding.textViewDate.text =launch.participant?.get(0)?.individual?.practitionerName?.get(0)?.text.toString()+" "+formattedDate ?: ""
                addTextField(holder, holder.binding.root.context.getString(R.string.reason), getDataOne(launch))
            }
            is AllergyIntolerance ->{
                holder.binding.header.text = getTitle(launch)
                holder.binding.textViewDate.text =launch.reaction?.get(0)?.severity.toString()
                addTextField(holder, holder.binding.root.context.getString(R.string.severity), getDataOne(launch))
                addTextField(holder, holder.binding.root.context.getString(R.string.onsetdate), getDataTwo(launch))
                addTextField(holder, holder.binding.root.context.getString(R.string.reactions), launch.reaction?.get(0)?.description.toString())
                addTextField(holder, holder.binding.root.context.getString(R.string.notes), launch.reaction?.get(0)?.note?.get(0)?.text.toString())
            }
            is Communication ->{
                holder.binding.header.text = getTitle(launch)
                holder.binding.textViewDate.text ="---"
                addTextField(holder, holder.binding.root.context.getString(R.string.resourcetype), getDataOne(launch))
                addTextField(holder, holder.binding.root.context.getString(R.string.status), getDataTwo(launch))
            }
            is Condition ->{
                holder.binding.header.text = getTitle(launch)
                holder.binding.textViewDate.text =holder.binding.root.context.getString(R.string.name)+": "+launch.recorder?.onPractitioner?.practitionerName?.get(0)?.text.toString()
                addTextField(holder, holder.binding.root.context.getString(R.string.recorded_date), getDataOne(launch))
                addTextField(holder, holder.binding.root.context.getString(R.string.category), getDataTwo(launch))
                addTextField(holder, holder.binding.root.context.getString(R.string.severity), launch.severity.toString())
                addTextField(holder, holder.binding.root.context.getString(R.string.notes), launch.note.toString())
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

    private fun addTextField(holder: ViewHolder, label: String, value: String) {
        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT)
        val labelTextView = createDynamicTextView(holder.binding.root.context, "$label")
        labelTextView.setTextColor(Color.parseColor("#575881"))
        layoutParams.topMargin = 30
        labelTextView.layoutParams = layoutParams
        holder.binding.dataLl.addView(labelTextView)

        val valueTextView = createDynamicTextView(holder.binding.root.context, "$value")
        holder.binding.dataLl.addView(valueTextView)
    }



    private fun createDynamicTextView(context: Context, text: String): TextView {
        val dynamicTextView = TextView(context)
        dynamicTextView.text = text
        return dynamicTextView
    }

    private fun getTitle(item: T?): String {
        return when (item) {
            is CarePlan -> item.title ?: ""
            is Immunization -> item.vaccineCode?.text ?: ""
            is Procedure -> item.code?.text ?: ""
            is Observation -> item.code?.text ?: ""
            is Encounter -> item.reasonCode?.get(0)?.coding?.get(0)?.display ?: ""
            is AllergyIntolerance -> item.reaction?.get(0)?.manifestation?.get(0)?.text ?: ""
            is Communication -> item.category?.get(0)?.coding?.get(0)?.code ?: ""
            is Condition -> item.code?.text.toString()
            else -> ""
        }
    }

    private fun getDate(item: T?): String? {
        return when (item) {
            is CarePlan -> item.period?.start.toString()
            is Immunization -> item.occurrenceDateTime.toString()
            is Procedure -> item.performedDateTime.toString()
            is Observation -> item.effectiveDateTime.toString()
            is Encounter -> item.period?.start.toString()
            is AllergyIntolerance -> item.reaction?.get(0)?.onset.toString()
            is Condition -> item.recordedDate.toString()
            else -> null
        }
    }

    private fun getDataOne(item: T?): String {
        return when (item) {
            is CarePlan -> item.status.toString()
            is Immunization -> item.protocolApplied?.get(0)?.doseNumberString.toString()
            is Procedure -> item.bodySite.toString()
            is Observation -> item.interpretation?.get(0)?.text.toString()
            is Encounter -> item.reasonCode?.get(0)?.text.toString()
            is AllergyIntolerance ->item.reaction?.get(0)?.severity.toString()
            is Communication ->item.about?.get(0)?.resourceType.toString()
            is Condition ->getDate(item).let { formatDate(it) }
            else -> ""
        }
    }

    private fun getDataTwo(item: T?): String {
        return when (item) {
            is CarePlan -> item.intent.toString()
            is Immunization -> item.manufacturer?.name.toString()
            is Procedure -> item.reasonCode.toString()
            is Observation -> item.referenceRange?.get(0)?.text.toString()
            is AllergyIntolerance ->getDate(item).let { formatDate(it) }
            is Communication ->item.status.toString()
            is Condition ->item.category.toString()
            else -> ""
        }
    }
}

