package com.bwell.sampleapp.activities.ui.medicines

import android.os.Build
import android.text.Html
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.bwell.common.models.domain.healthdata.medication.MedicationGroup
import com.bwell.sampleapp.R
import com.bwell.sampleapp.databinding.MedicineItemsViewBinding
import com.bwell.sampleapp.utils.removeHtmlTags


class ActiveMedicationListAdapter(private var launches: List<MedicationGroup>?) :
    RecyclerView.Adapter<ActiveMedicationListAdapter.ViewHolder>() {


    class ViewHolder(val binding: MedicineItemsViewBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = MedicineItemsViewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return launches?.size ?: 0
    }

    var onEndOfListReached: (() -> Unit)? = null
    var onItemClicked: ((MedicationGroup?) -> Unit)? = null

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val launch = launches?.get(position)
        val cleanedName = launch?.name?.let { removeHtmlTags(it) } ?: ""
        holder.binding.nameText.text = cleanedName
        holder.binding.detailsLogo.load(R.drawable.baseline_keyboard_arrow_right_24) {
            placeholder(R.drawable.baseline_keyboard_arrow_right_24)
        }

        if (position == (launches?.size ?: 0) - 1) {
            onEndOfListReached?.invoke()
        }

        holder.binding.root.setOnClickListener {
            onItemClicked?.invoke(launch)
        }
    }

    // Add a function to update the list
    fun updateList(newList:List<MedicationGroup>?) {
        launches = newList
        notifyDataSetChanged()
    }

}
