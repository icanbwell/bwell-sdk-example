package com.bwell.sampleapp.activities.ui.data_connections.providers

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.bwell.common.models.domain.common.Coding
import com.bwell.common.models.domain.common.location.Location
import com.bwell.common.models.domain.search.Provider
import com.bwell.sampleapp.R
import com.bwell.sampleapp.databinding.ProviderItemsViewBinding

/*
*Display the Providers List in RecyclerView
* */
class ProvidersListAdapter(private var launches: List<Provider>?) :
    RecyclerView.Adapter<ProvidersListAdapter.ViewHolder>() {


    class ViewHolder(val binding: ProviderItemsViewBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ProviderItemsViewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return launches?.size ?: 0
    }

    var onEndOfListReached: (() -> Unit)? = null
    var onItemClicked: ((Provider) -> Unit)? = null

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val launch = launches!![position]
        if(launch.name?.size!! > 0)
            holder.binding.name.text = launch.name?.get(0)?.text.toString()
        else
            holder.binding.name.text = "Null Data"
        if(launch.location?.size!! > 0)
        {
            val location: Location? = launch.location?.get(0)
            val city: String? = location?.address?.city
            val state: String? = location?.address?.state
            holder.binding.address.text = city+", "+state
        }else{
            holder.binding.address.text = "Null Data"
        }
        var specialtiesNames: String = "Null Data";
        if(launch.specialty!!.size > 0)
        {
            val specialties: List<Coding?>? = launch.specialty
            val specialtyDisplays: List<String> = specialties!!.map { it!!.display ?: "" }
            specialtiesNames = specialtyDisplays.joinToString(", ")
        }
        holder.binding.specialistType.text = specialtiesNames


        if(launch.organization!!.size > 0)
        {
            holder.binding.frameLayoutConnectionStatus.visibility = View.VISIBLE
        }else{
            holder.binding.frameLayoutConnectionStatus.visibility = View.GONE
        }


        holder.binding.icon.load(R.drawable.baseline_person_pin_24) {
            placeholder(R.drawable.baseline_person_pin_24)
        }

        if (position == launches!!.size - 1) {
            onEndOfListReached?.invoke()
        }

        holder.binding.root.setOnClickListener {
            onItemClicked?.invoke(launch)
        }
    }

    // Add a function to update the list
    fun updateList(newList:List<Provider>?) {
        launches = newList
        notifyDataSetChanged()
    }

}
