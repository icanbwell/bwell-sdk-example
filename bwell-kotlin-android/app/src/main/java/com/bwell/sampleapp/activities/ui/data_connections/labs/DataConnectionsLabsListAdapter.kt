package com.bwell.sampleapp.activities.ui.data_connections.labs

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.bwell.sampleapp.R
import com.bwell.sampleapp.databinding.DataConnectionsLabsItemsViewBinding
import com.bwell.search.ProviderSearchQuery

/*
*Display the Data Connections Clinics List in RecyclerView
* */
class DataConnectionsLabsListAdapter(private var launches: List<ProviderSearchQuery.Organization?>?) :
    RecyclerView.Adapter<DataConnectionsLabsListAdapter.ViewHolder>() {


    class ViewHolder(val binding: DataConnectionsLabsItemsViewBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = DataConnectionsLabsItemsViewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return launches?.size ?: 0
    }

    var onEndOfListReached: (() -> Unit)? = null
    var onItemClicked: ((ProviderSearchQuery.Organization?) -> Unit)? = null

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val launch = launches?.get(position)
        holder.binding.itemText.text = launch?.name?: ""
        holder.binding.itemImage.load(R.drawable.baseline_person_pin_24) {
            placeholder(R.drawable.baseline_person_pin_24)
        }

        if (position == (launches?.size ?: 0) - 1) {
            onEndOfListReached?.invoke()
        }

        holder.binding.root.setOnClickListener {
            onItemClicked?.invoke(launch)
        }
    }

    // Add a function to update the list
    fun updateList(newList:List<ProviderSearchQuery.Organization?>?) {
        launches = newList
        notifyDataSetChanged()
    }

}