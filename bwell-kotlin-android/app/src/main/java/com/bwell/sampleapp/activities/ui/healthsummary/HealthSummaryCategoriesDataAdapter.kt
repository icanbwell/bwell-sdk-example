package com.bwell.sampleapp.activities.ui.healthsummary

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bwell.common.models.domain.healthdata.healthsummary.careplan.CarePlan
import com.bwell.common.models.domain.healthdata.healthsummary.immunization.Immunization
import com.bwell.sampleapp.R
import com.bwell.sampleapp.databinding.HealthSummaryCategoriesItemsViewBinding
import com.bwell.sampleapp.utils.formatDate
import java.text.SimpleDateFormat
import java.util.Locale

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
                addTextField(holder, holder.binding.root.context.getString(R.string.dose_number), getDataOne(launch))
                addTextField(holder, holder.binding.root.context.getString(R.string.manufacturer_name), getDataTwo(launch))
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
            else -> ""
        }
    }

    private fun getDate(item: T?): String? {
        return when (item) {
            is CarePlan -> item.period?.start?.date
            is Immunization -> item.occurrenceDateTime?.date
            else -> null
        }
    }

    private fun getDataOne(item: T?): String {
        return when (item) {
            is CarePlan -> item.status.toString()
            is Immunization -> item.protocolApplied?.get(0)?.doseNumberString.toString()
            else -> ""
        }
    }

    private fun getDataTwo(item: T?): String {
        return when (item) {
            is CarePlan -> item.intent.toString()
            is Immunization -> item.manufacturer?.name.toString()
            else -> ""
        }
    }
}

