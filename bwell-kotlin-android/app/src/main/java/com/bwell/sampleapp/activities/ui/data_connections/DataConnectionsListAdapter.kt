package com.bwell.sampleapp.activities.ui.data_connections

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.bwell.common.models.domain.data.Connection
import com.bwell.sampleapp.R
import com.bwell.sampleapp.databinding.DataConnectionsItemsViewBinding

/*
*Display the Data Connections List in RecyclerView
* */
class DataConnectionsListAdapter(private val launches: List<Connection>) :
    RecyclerView.Adapter<DataConnectionsListAdapter.ViewHolder>() {

    class ViewHolder(val binding: DataConnectionsItemsViewBinding) : RecyclerView.ViewHolder(binding.root)

    interface DataConnectionsClickListener {
        fun onChangeStatusClicked(connection: Connection,parent_view:ViewGroup,status_change_view: View)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = DataConnectionsItemsViewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return launches.size
    }

    var onEndOfListReached: (() -> Unit)? = null
    var onItemClicked: ((Connection) -> Unit)? = null
    var dataConnectionsClickListener: DataConnectionsClickListener? = null

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val launch = launches[position]
        holder.binding.header.text = launch.id?: ""
        holder.binding.textViewStatus.text = (launch.status?: "").toString()
        holder.binding.changeStatusIv.load(R.drawable.baseline_more_vert_24) {
            placeholder(R.drawable.insurance_logo)
        }
        holder.binding.icon.load(R.drawable.baseline_person_pin_24) {
            placeholder(R.drawable.baseline_person_pin_24)
        }

        holder.binding.changeStatusIv.setOnClickListener {
            dataConnectionsClickListener?.onChangeStatusClicked(launch,holder.binding.root,holder.binding.changeStatusIv)
        }

        if (position == launches.size - 1) {
            onEndOfListReached?.invoke()
        }

        holder.binding.root.setOnClickListener {
            onItemClicked?.invoke(launch)
        }
    }
}
