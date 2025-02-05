package com.bwell.sampleapp.activities.ui.medicines

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bwell.common.models.domain.healthdata.medication.MedicationPricing
import com.bwell.sampleapp.databinding.MedicinePricingItemsViewBinding
import java.text.NumberFormat

class MedicationPricingListAdapter(private var launches: List<MedicationPricing>?) :
    RecyclerView.Adapter<MedicationPricingListAdapter.ViewHolder>() {

    class ViewHolder(val binding: MedicinePricingItemsViewBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = MedicinePricingItemsViewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return launches?.size ?: 0
    }

    var onEndOfListReached: (() -> Unit)? = null

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val launch = launches?.get(position)
        val pharmacyName = launch?.pharmacy
        val pricing = launch?.price?.value
        holder.binding.pharmacyNameTextView.text = pharmacyName
        holder.binding.pricingTextView.text = pricing?.let {
           "$" + NumberFormat.getNumberInstance(java.util.Locale.US).format(it)
        } ?: ""

        if (position == (launches?.size ?: 0) - 1) {
            onEndOfListReached?.invoke()
        }

    }

    // Add a function to update the list
    fun updateList(newList:List<MedicationPricing>?) {
        launches = newList
        notifyDataSetChanged()
    }
}
