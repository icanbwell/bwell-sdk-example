package com.bwell.sampleapp.activities.ui.labs

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bwell.common.models.domain.healthdata.lab.LabGroup
import com.bwell.sampleapp.databinding.LabsItemsViewBinding
import com.bwell.sampleapp.utils.formatDate

/*
*Display the Labs List in RecyclerView
* */
class LabsListAdapter(private val launches: List<LabGroup>?) :
    RecyclerView.Adapter<LabsListAdapter.ViewHolder>() {

    class ViewHolder(val binding: LabsItemsViewBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = LabsItemsViewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return launches?.size ?: 0
    }

    var onEndOfListReached: (() -> Unit)? = null
    var onItemClicked: ((LabGroup?) -> Unit)? = null

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val launch = launches?.get(position)
        holder.binding.header.text = launch?.coding?.display.toString()
        //holder.binding.textViewDate.text = launch?.effectiveDateTime?.toString()?.let { formatDate(it) } ?: "---"
        //holder.binding.textViewStatus.text = launch?.interpretation?.get(0)?.text ?: "---"
        if (position == (launches?.size ?: 0) - 1) {
            onEndOfListReached?.invoke()
        }

        holder.binding.root.setOnClickListener {
            onItemClicked?.invoke(launch)
        }
    }
}
