package com.bwell.sampleapp.activities.ui.data_connections

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.bwell.sampleapp.R
import com.bwell.sampleapp.databinding.ClinicsItemsViewBinding
import com.bwell.sampleapp.model.DataConnectionsClinicsListItems

/*
*Display the Data Connections Clinics List in RecyclerView
* */
class DataConnectionsClinicsListAdapter(private var launches: List<DataConnectionsClinicsListItems>) :
    RecyclerView.Adapter<DataConnectionsClinicsListAdapter.ViewHolder>() {


    class ViewHolder(val binding: ClinicsItemsViewBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ClinicsItemsViewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return launches.size
    }

    var onEndOfListReached: (() -> Unit)? = null
    var onItemClicked: ((DataConnectionsClinicsListItems) -> Unit)? = null

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val launch = launches[position]
        holder.binding.itemText.text = launch.clinicName?: ""
        holder.binding.itemImage.load(launch.clinicLogo) {
            placeholder(R.drawable.baseline_person_pin_24)
        }

        if (position == launches.size - 1) {
            onEndOfListReached?.invoke()
        }

        holder.binding.root.setOnClickListener {
            onItemClicked?.invoke(launch)
        }
    }

    // Add a function to update the list
    fun updateList(newList: List<DataConnectionsClinicsListItems>) {
        launches = newList
        notifyDataSetChanged()
    }

}
