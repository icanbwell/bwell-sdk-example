package com.bwell.sampleapp.activities.ui.data_connections.healthresources

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bwell.common.models.domain.search.HealthResource
import com.bwell.sampleapp.databinding.HealthResourceItemViewBinding

class HealthResourcesListAdapter(private var items: List<HealthResource>?) :
    RecyclerView.Adapter<HealthResourcesListAdapter.ViewHolder>() {

    class ViewHolder(val binding: HealthResourceItemViewBinding) : RecyclerView.ViewHolder(binding.root)

    var onItemClicked: ((HealthResource) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = HealthResourceItemViewBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = items?.size ?: 0

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val resource = items?.get(position) ?: return
        val b = holder.binding

        b.tvName.text = resource.content ?: resource.id ?: "Unknown"
        b.tvType.text = resource.type?.name ?: ""

        val specialties = resource.specialty?.mapNotNull { it.display }?.joinToString(", ")
        b.tvSpecialty.text = specialties ?: ""

        val locationCount = resource.providerLocation?.size ?: resource.location?.size ?: 0
        b.tvLocations.text = if (locationCount > 0) "$locationCount locations" else ""

        val badges = mutableListOf<String>()
        if (resource.bookable?.online == true) badges.add("Bookable Online")
        if (resource.bookable?.phone == true) badges.add("Bookable Phone")
        b.tvBadges.text = badges.joinToString(" | ")

        b.root.setOnClickListener { onItemClicked?.invoke(resource) }
    }

    fun updateList(newList: List<HealthResource>?) {
        items = newList
        notifyDataSetChanged()
    }
}
