package com.bwell.sampleapp.activities.ui.questionnaire

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bwell.sampleapp.databinding.QuestionnaireListItemBinding

/**
 * Adapter for displaying questionnaire items in a RecyclerView
 */
class QuestionnaireListAdapter(private val items: List<QuestionnaireItem>) :
    RecyclerView.Adapter<QuestionnaireListAdapter.ViewHolder>() {

    class ViewHolder(val binding: QuestionnaireListItemBinding) : 
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = QuestionnaireListItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.binding.itemImage.setImageResource(item.logo)
        holder.binding.itemText.text = item.name
        holder.binding.itemCount.text = item.count?.toString() ?: "0"
    }

    override fun getItemCount(): Int = items.size
}
