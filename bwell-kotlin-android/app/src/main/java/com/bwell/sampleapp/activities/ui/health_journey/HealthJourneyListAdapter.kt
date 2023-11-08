package com.bwell.sampleapp.activities.ui.health_journey

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.bwell.sampleapp.R
import com.bwell.sampleapp.databinding.HealthJourneyItemsViewBinding
import com.bwell.sampleapp.model.HealthJourneyListItems

/*
*Display the Health Journey List in RecyclerView
* */
class HealthJourneyListAdapter(private val launches: List<HealthJourneyListItems>) :
    RecyclerView.Adapter<HealthJourneyListAdapter.ViewHolder>() {

    class ViewHolder(val binding: HealthJourneyItemsViewBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = HealthJourneyItemsViewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return launches.size
    }

    var onEndOfListReached: (() -> Unit)? = null
    var onItemClicked: ((HealthJourneyListItems) -> Unit)? = null

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val launch = launches[position]
        holder.binding.typeText.text = launch.type?: ""
        holder.binding.descriptionTxt.text = launch.description?: ""
        holder.binding.typeLogo.load(launch.typeLogo) {
            placeholder(R.drawable.baseline_person_24)
        }
        holder.binding.moreLogo.load(launch.moreLogo) {
            placeholder(R.drawable.baseline_keyboard_arrow_right_24)
        }

        if (position == launches.size - 1) {
            onEndOfListReached?.invoke()
        }

        holder.binding.root.setOnClickListener {
            onItemClicked?.invoke(launch)
        }
    }
}
