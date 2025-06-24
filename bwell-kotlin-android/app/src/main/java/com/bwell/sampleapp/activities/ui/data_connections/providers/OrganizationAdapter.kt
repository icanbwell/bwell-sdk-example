package com.bwell.sampleapp.activities.ui.data_connections.providers

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.bwell.common.models.domain.common.Organization
import com.bwell.common.models.domain.search.HealthResource
import com.bwell.sampleapp.R

class OrganizationAdapter(private val context: Context, private val data: List<Organization?>?,
                           var organizationClickListener: OrganizationClickListener? = null) : BaseAdapter() {

    interface OrganizationClickListener {
        fun onOrganizationClick(organization: HealthResource?)
    }

    override fun getCount(): Int {
        return data!!.size
    }

    override fun getItem(position: Int): Any {
        return data?.get(position) ?: 0
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var view = convertView
        val holder: ViewHolder

        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.organization_item_view, parent, false)
            holder = ViewHolder(
                view.findViewById(R.id.logoImageView),
                view.findViewById(R.id.organizationNameTextView),
                view.findViewById(R.id.connectButton)
            )
            view.tag = holder
        } else {
            holder = view.tag as ViewHolder
        }

        // Bind data to views
        val item = getItem(position) as HealthResource?
        holder.organizationNameTextView.text = item?.content
        holder.connectButton.setOnClickListener {
            organizationClickListener?.onOrganizationClick(item)
        }

        return view!!
    }

    private data class ViewHolder(
        val logoImageView: ImageView,
        val organizationNameTextView: TextView,
        val connectButton: FrameLayout
    )
}
