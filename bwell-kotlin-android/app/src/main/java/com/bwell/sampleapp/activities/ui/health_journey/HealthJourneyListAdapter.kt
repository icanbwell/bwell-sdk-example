package com.bwell.sampleapp.activities.ui.health_journey

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.bwell.common.models.domain.task.Task
import com.bwell.sampleapp.R
import com.bwell.sampleapp.databinding.HealthJourneyItemsViewBinding

/*
*Display the Health Journey List in RecyclerView
* */
class HealthJourneyListAdapter(private val launches: List<Task>?) :
    RecyclerView.Adapter<HealthJourneyListAdapter.ViewHolder>() {

    class ViewHolder(val binding: HealthJourneyItemsViewBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = HealthJourneyItemsViewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return launches?.size ?: 0
    }

    var onEndOfListReached: (() -> Unit)? = null
    var onItemClicked: ((Task?) -> Unit)? = null

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val launch = launches?.get(position)
        holder.binding.typeText.text = launch?.identifier?.firstOrNull { it.system == "https://www.icanbwell.com/activityName" }?.value
        holder.binding.statusText.text = launch?.status?.toString()
        holder.binding.typeLogo.load(R.drawable.vaccine_icon) {
            placeholder(R.drawable.vaccine_icon)
        }
        holder.binding.moreLogo.load(R.drawable.baseline_keyboard_arrow_right_24) {
            placeholder(R.drawable.baseline_keyboard_arrow_right_24)
        }

        if (position == (launches?.size ?: 0) - 1) {
            onEndOfListReached?.invoke()
        }

        holder.binding.root.setOnClickListener {
            onItemClicked?.invoke(launch)
        }
    }
}
