package com.bwell.sampleapp.activities.ui.labs

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bwell.sampleapp.databinding.LabsItemsViewBinding
import com.bwell.sampleapp.model.LabsListItems

/*
*Display the Labs List in RecyclerView
* */
class LabsListAdapter(private val launches: List<LabsListItems>) :
    RecyclerView.Adapter<LabsListAdapter.ViewHolder>() {

    class ViewHolder(val binding: LabsItemsViewBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = LabsItemsViewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return launches.size
    }

    var onEndOfListReached: (() -> Unit)? = null
    var onItemClicked: ((LabsListItems) -> Unit)? = null

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val launch = launches[position]
        holder.binding.header.text = launch.labs_type?: ""
        holder.binding.textViewDate.text = launch.labs_date?: ""
        holder.binding.textViewStatus.text = launch.labs_status?: ""

        if (position == launches.size - 1) {
            onEndOfListReached?.invoke()
        }

        holder.binding.root.setOnClickListener {
            onItemClicked?.invoke(launch)
        }
    }
}
