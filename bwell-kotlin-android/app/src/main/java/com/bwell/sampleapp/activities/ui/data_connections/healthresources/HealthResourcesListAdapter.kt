package com.bwell.sampleapp.activities.ui.data_connections.healthresources

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bwell.common.models.domain.search.HealthResource
import com.bwell.common.models.domain.search.enums.SearchResultType
import com.bwell.sampleapp.databinding.HealthResourceItemViewBinding

data class HealthResourceState(
    var isInCareTeam: Boolean = false,
    var isPCP: Boolean = false
)

class HealthResourcesListAdapter(private var items: List<HealthResource>?) :
    RecyclerView.Adapter<HealthResourcesListAdapter.ViewHolder>() {

    class ViewHolder(val binding: HealthResourceItemViewBinding) : RecyclerView.ViewHolder(binding.root)

    var onItemClicked: ((HealthResource) -> Unit)? = null
    var onAddToCareTeamClicked: ((HealthResource) -> Unit)? = null
    var onRemoveFromCareTeamClicked: ((HealthResource) -> Unit)? = null
    var onPCPToggled: ((HealthResource, Boolean) -> Unit)? = null

    private val stateMap = mutableMapOf<String, HealthResourceState>()

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
        val id = resource.id ?: ""
        val state = stateMap.getOrPut(id) { HealthResourceState() }

        b.tvName.text = resource.content ?: resource.id ?: "Unknown"
        b.tvType.text = resource.type?.name ?: ""

        val specialties = resource.specialty?.mapNotNull { it.display }?.joinToString(", ")
        b.tvSpecialty.text = specialties ?: ""

        b.tvScore.text = resource.score?.let { "Score: %.2f".format(it) } ?: ""

        val acceptingText = when (resource.acceptingNewPatients) {
            true -> "Accepting New"
            false -> "Not Accepting"
            null -> ""
        }
        b.tvAcceptingNew.text = acceptingText
        if (resource.acceptingNewPatients == true) {
            b.tvAcceptingNew.setTextColor(Color.parseColor("#4CAF50"))
        } else {
            b.tvAcceptingNew.setTextColor(Color.parseColor("#888888"))
        }

        val locationCount = resource.providerLocation?.size ?: resource.location?.size ?: 0
        b.tvLocations.text = if (locationCount > 0) "$locationCount locations" else ""

        val languages = resource.communication?.mapNotNull { it.text }?.joinToString(", ")
        b.tvCommunication.text = languages ?: ""

        val badges = mutableListOf<String>()
        if (resource.isVirtualCare == true) badges.add("Virtual Care")
        if (resource.bookable?.online == true) badges.add("Bookable Online")
        if (resource.bookable?.phone == true) badges.add("Bookable Phone")
        b.tvBadges.text = badges.joinToString(" | ")

        val isPractitioner = resource.type == SearchResultType.PRACTITIONER

        // Chips (only for practitioners)
        b.chipCareTeam.visibility = if (isPractitioner && state.isInCareTeam) View.VISIBLE else View.GONE
        b.chipPCP.visibility = if (isPractitioner && state.isPCP) View.VISIBLE else View.GONE

        // Care team actions (only for practitioners)
        b.careTeamActions.visibility = if (isPractitioner) View.VISIBLE else View.GONE

        if (isPractitioner) {
            if (state.isInCareTeam) {
                b.btnCareTeam.text = "- Care Team"
            } else {
                b.btnCareTeam.text = "+ Care Team"
            }

            b.btnCareTeam.setOnClickListener {
                if (state.isInCareTeam) {
                    onRemoveFromCareTeamClicked?.invoke(resource)
                } else {
                    onAddToCareTeamClicked?.invoke(resource)
                }
            }

            // PCP toggle
            b.switchPCP.setOnCheckedChangeListener(null)
            b.switchPCP.isChecked = state.isPCP
            b.switchPCP.setOnCheckedChangeListener { _, isChecked ->
                onPCPToggled?.invoke(resource, isChecked)
            }
        }

        b.root.setOnClickListener { onItemClicked?.invoke(resource) }
    }

    fun updateState(resourceId: String, isInCareTeam: Boolean, isPCP: Boolean) {
        val state = stateMap.getOrPut(resourceId) { HealthResourceState() }
        state.isInCareTeam = isInCareTeam
        state.isPCP = isPCP
        val index = items?.indexOfFirst { it.id == resourceId } ?: -1
        if (index >= 0) {
            notifyItemChanged(index)
        }
    }

    fun updateList(newList: List<HealthResource>?) {
        items = newList
        notifyDataSetChanged()
    }
}
