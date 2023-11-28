package com.bwell.sampleapp.activities.ui.data_connections.providers

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.bwell.sampleapp.R
import com.bwell.sampleapp.activities.ui.data_connections.DataConnectionsFragment
import com.bwell.sampleapp.databinding.FragmentOrganizationInfoViewBinding
import com.bwell.sampleapp.utils.SelectedOrganizationHolder

class OrganizationInfoFragment : Fragment(),View.OnClickListener {

    private var _binding: FragmentOrganizationInfoViewBinding? = null


    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOrganizationInfoViewBinding.inflate(inflater, container, false)
        val root: View = binding.root
        val organization = SelectedOrganizationHolder.selectedOrganization
        binding.clinicNametxt.text =
            "${resources.getString(R.string.connect_to)} ${organization?.name}"
        binding.clinicDiscriptionTxt.text =
            "${organization?.name} ${resources.getString(R.string.clinic_discription)}"
        binding.cancelTxt.setOnClickListener(this)
        binding.checkbox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                val drawable = ContextCompat.getDrawable(requireContext(), R.drawable.rounded_rectangle_green)
                binding.frameLayoutProceed.background = drawable
                binding.frameLayoutProceed.setOnClickListener(this)
            } else {
                val drawable = ContextCompat.getDrawable(requireContext(), R.drawable.rounded_rectangle_grey)
                binding.frameLayoutProceed.background = drawable
                binding.frameLayoutProceed.setOnClickListener(null)
            }
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.cancel_txt -> {
                parentFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
                val parentFrag: DataConnectionsFragment = this@OrganizationInfoFragment.getParentFragment() as DataConnectionsFragment
                parentFrag.showDataConnectionCategories()
            }
        }
    }
}
