package com.bwell.sampleapp.activities.ui.data_connections.providers

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.bwell.sampleapp.R
import com.bwell.sampleapp.activities.ui.data_connections.DataConnectionsFragment
import com.bwell.sampleapp.databinding.FragmentOrganizationInfoViewBinding
import com.bwell.search.ProviderSearchQuery

class OrganizationInfoFragment(organizationData: ProviderSearchQuery.Organization?) : Fragment(),View.OnClickListener {

    private var _binding: FragmentOrganizationInfoViewBinding? = null
    private var organization: ProviderSearchQuery.Organization? = organizationData

    private val binding get() = _binding!!

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOrganizationInfoViewBinding.inflate(inflater, container, false)
        val root: View = binding.root
        val organization = organization
        val connectionType = organization?.endpoint?.get(0)?.connectionType?.code
        binding.clinicNametxt.text =
            "${resources.getString(R.string.connect_to)} ${organization?.name}"
        if(connectionType.equals(resources.getString(R.string.hapi)))
        {
            binding.clinicDiscriptionTxt.text ="By providing  my "+
                "${organization?.name} ${resources.getString(R.string.clinic_info_hapi)}"
            binding.editTextUsername.visibility = View.VISIBLE;
            binding.passwordLayout.visibility = View.VISIBLE;
        }else{
            binding.clinicDiscriptionTxt.text =
                "${organization?.name} ${resources.getString(R.string.clinic_discription)}"
            binding.editTextUsername.visibility = View.GONE;
            binding.passwordLayout.visibility = View.GONE;
        }
        binding.cancelTxt.setOnClickListener(this)
        binding.togglePassword.setOnClickListener(this)
        binding.checkbox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                val isShow = checkVisibilityOfProceed(connectionType)
                if(isShow)
                {
                    addListnersToProceed()
                }
            } else {
                removeListnersToProceed()
            }
        }

        binding.editTextUsername.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val isShow = checkVisibilityOfProceed(connectionType)
                if(isShow)
                {
                    addListnersToProceed()
                }else{
                    removeListnersToProceed()
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.editTextPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val isShow = checkVisibilityOfProceed(connectionType)
                if(isShow)
                {
                    addListnersToProceed()
                }else{
                    removeListnersToProceed()
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        return root
    }

    private fun togglePasswordVisibility() {
        val passwordEditText = binding.editTextPassword
        val togglePasswordImageView = binding.togglePassword
        val isPasswordVisible = passwordEditText.inputType == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
        if (isPasswordVisible) {
            passwordEditText.inputType =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            togglePasswordImageView.setImageResource(R.drawable.baseline_visibility_off_24)
        } else {
            passwordEditText.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            togglePasswordImageView.setImageResource(R.drawable.baseline_visibility_24)
        }
        passwordEditText.setSelection(passwordEditText.text.length)
    }

    private fun removeListnersToProceed() {
        val drawable = ContextCompat.getDrawable(requireContext(), R.drawable.rounded_rectangle_grey)
        binding.frameLayoutProceed.background = drawable
        binding.frameLayoutProceed.setOnClickListener(null)
    }

    private fun addListnersToProceed() {
        val drawable = ContextCompat.getDrawable(requireContext(), R.drawable.rounded_rectangle_green)
        binding.frameLayoutProceed.background = drawable
        binding.frameLayoutProceed.setOnClickListener(this)
    }

    private fun checkVisibilityOfProceed(connectionType:String?): Boolean {
        if(connectionType.equals(resources.getString(R.string.hapi)))
        {
            if(binding.editTextUsername.text.toString().equals("") || binding.editTextPassword.text.toString().equals("") || !binding.checkbox.isChecked)
                return false
            else
                return true
        }else{
            return true
        }

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
            R.id.togglePassword -> {
                togglePasswordVisibility()
            }
        }
    }
}
