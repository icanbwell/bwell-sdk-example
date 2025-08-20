package com.bwell.sampleapp.activities.ui.data_connections

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.bwell.common.models.domain.data.Connection
import com.bwell.common.models.domain.healthdata.healthsummary.careteam.CareTeam
import com.bwell.common.models.domain.healthdata.healthsummary.careteam.OrganizationCareTeamParticipantMember
import com.bwell.sampleapp.R
import com.bwell.sampleapp.activities.ui.data_connections.DataConnectionsFragment.OrganizationCareTeamParticipantMemberDisplay
import com.bwell.sampleapp.databinding.DataConnectionsItemsViewBinding

// Sealed class for combined items
sealed class DataConnectionItem {
    data class ConnectionItem(val connection: Connection) : DataConnectionItem()
    data class CareTeamItem(val careTeam: CareTeam) : DataConnectionItem()
}

/*
* Display the Data Connections and Care Teams List in RecyclerView
*/
class DataConnectionsListAdapter(
    private val items: List<Any>
) : RecyclerView.Adapter<DataConnectionsListAdapter.ViewHolder>() {

    class ViewHolder(val binding: DataConnectionsItemsViewBinding) : RecyclerView.ViewHolder(binding.root)

    interface DataConnectionsClickListener {
        fun onChangeStatusClicked(
            item: Any, // Accepts Connection or OrganizationCareTeamParticipantMemberDisplay
            parent_view: ViewGroup,
            status_change_view: View,
            frameLayoutConnectionStatus: FrameLayout
        )
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = DataConnectionsItemsViewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = items.size

    var onEndOfListReached: (() -> Unit)? = null
    var onItemClicked: ((Any) -> Unit)? = null
    var dataConnectionsClickListener: DataConnectionsClickListener? = null

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        when (item) {
            is Connection -> {
                holder.binding.header.text = item.name ?: ""
                holder.binding.textViewStatus.text = item.status?.toString() ?: ""
                holder.binding.changeStatusIv.visibility = View.VISIBLE
                holder.binding.changeStatusIv.isEnabled = true
                holder.binding.changeStatusIv.isClickable = true
                holder.binding.changeStatusIv.bringToFront()
                holder.binding.changeStatusIv.load(R.drawable.baseline_more_vert_24) {
                    placeholder(R.drawable.insurance_logo)
                }
                holder.binding.icon.load(R.drawable.baseline_person_pin_24) {
                    placeholder(R.drawable.baseline_person_pin_24)
                }
                holder.binding.changeStatusIv.setOnClickListener {
                    dataConnectionsClickListener?.onChangeStatusClicked(
                        item,
                        holder.binding.root,
                        holder.binding.changeStatusIv,
                        holder.binding.frameLayoutConnectionStatus
                    )
                }
                holder.binding.root.setOnClickListener {
                    onItemClicked?.invoke(item)
                }
            }
            is OrganizationCareTeamParticipantMemberDisplay -> {
                holder.binding.header.text = item.name
                holder.binding.textViewStatus.text = item.status
                if (item.status.equals("NEEDS ATTENTION", ignoreCase = true)) {
                    holder.binding.changeStatusIv.visibility = View.GONE
                    holder.binding.changeStatusIv.isEnabled = false
                    holder.binding.changeStatusIv.isClickable = false
                } else {
                    holder.binding.changeStatusIv.visibility = View.VISIBLE
                    holder.binding.changeStatusIv.isEnabled = true
                    holder.binding.changeStatusIv.isClickable = true
                    holder.binding.changeStatusIv.bringToFront()
                    holder.binding.changeStatusIv.load(R.drawable.baseline_more_vert_24) {
                        placeholder(R.drawable.insurance_logo)
                    }
                    holder.binding.changeStatusIv.setOnClickListener {
                        dataConnectionsClickListener?.onChangeStatusClicked(
                            item,
                            holder.binding.root,
                            holder.binding.changeStatusIv,
                            holder.binding.frameLayoutConnectionStatus
                        )
                    }
                }
                holder.binding.icon.load(R.drawable.baseline_person_pin_24) {
                    placeholder(R.drawable.baseline_person_pin_24)
                }
                holder.binding.root.setOnClickListener {
                    onItemClicked?.invoke(item)
                }
            }
        }
        if (position == items.size - 1) {
            onEndOfListReached?.invoke()
        }
    }
}