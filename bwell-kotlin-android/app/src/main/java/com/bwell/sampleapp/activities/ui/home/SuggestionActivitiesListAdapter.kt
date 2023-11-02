package com.bwell.sampleapp.activities.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.bwell.sampleapp.R
import com.bwell.sampleapp.databinding.LaunchItemBinding
import com.bwell.sampleapp.model.ActivityListItems

/*
*Display the Suggestion Activities List  in RecyclerView
*@argument : List<ActivityListItems>
* */
class SuggestionActivitiesListAdapter(private val launches: List<ActivityListItems>) :
    RecyclerView.Adapter<SuggestionActivitiesListAdapter.ViewHolder>() {

    class ViewHolder(val binding: LaunchItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = LaunchItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return launches.size
    }

    var onEndOfListReached: (() -> Unit)? = null
    var onItemClicked: ((ActivityListItems) -> Unit)? = null

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val launch = launches[position]
        holder.binding.header.text = launch.headerText?: ""
        holder.binding.subText.text = launch.subText
        holder.binding.icon.load(launch.icon) {
            placeholder(R.drawable.vaccine_icon)
        }

        if (position == launches.size - 1) {
            onEndOfListReached?.invoke()
        }

        holder.binding.root.setOnClickListener {
            onItemClicked?.invoke(launch)
        }
    }
}
