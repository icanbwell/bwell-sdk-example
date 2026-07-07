package com.bwell.sampleapp.activities.ui.insurance

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bwell.common.models.domain.financials.explanationOfBenefit.ExplanationOfBenefit
import com.bwell.sampleapp.databinding.ClaimItemViewBinding
import java.text.SimpleDateFormat
import java.util.Locale

private const val CLAIM_TYPE_SYSTEM = "http://terminology.hl7.org/CodeSystem/claim-type"

/** Displays the filtered list of claims (ExplanationOfBenefit) in a RecyclerView. */
class ClaimsListAdapter(private var claims: List<ExplanationOfBenefit>) :
    RecyclerView.Adapter<ClaimsListAdapter.ViewHolder>() {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    class ViewHolder(val binding: ClaimItemViewBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ClaimItemViewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = claims.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val eob = claims[position]

        val type = eob.type?.coding
            ?.firstOrNull { it.system == CLAIM_TYPE_SYSTEM }?.code
            ?: eob.type?.text
            ?: "Claim"
        val provider = eob.provider?.display
        holder.binding.typeProviderText.text =
            if (provider.isNullOrBlank()) type.uppercase(Locale.getDefault())
            else "${type.uppercase(Locale.getDefault())} · $provider"

        val description = eob.item?.firstOrNull()?.productOrService?.text
            ?: eob.item?.firstOrNull()?.productOrService?.coding?.firstOrNull()?.display
        holder.binding.descriptionText.text = description ?: "—"

        val serviceStart = eob.billablePeriod?.start?.let { dateFormat.format(it) } ?: "—"
        holder.binding.serviceDateText.text = "Service: $serviceStart"

        holder.binding.amountsText.text =
            "Billed ${amount(eob, "submitted")}  " +
            "Ins ${amount(eob, "benefit", "paidtopatient")}  " +
            "You ${amount(eob, "paidbypatient")}"
    }

    fun updateList(newClaims: List<ExplanationOfBenefit>) {
        claims = newClaims
        notifyDataSetChanged()
    }

    /** Reads a total amount by matching any of the given category codes; "—" if absent. */
    private fun amount(eob: ExplanationOfBenefit, vararg codes: String): String {
        val money = eob.total
            ?.firstOrNull { total -> total.category?.coding?.any { it.code in codes } == true }
            ?.amount ?: return "—"
        val value = money.value ?: return "—"
        return "$$value"
    }
}
