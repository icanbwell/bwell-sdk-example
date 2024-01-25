package com.bwell.sampleapp.activities.ui.data_connections.providers

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.bwell.common.models.domain.common.Organization
import com.bwell.common.models.domain.search.Provider
import com.bwell.sampleapp.R
import com.bwell.sampleapp.activities.ui.data_connections.DataConnectionsFragment
import com.bwell.sampleapp.activities.ui.data_connections.proa.WebFragment
import com.bwell.sampleapp.databinding.FragmentOrganizationInfoViewBinding

class OrganizationInfoFragment<T>(organizationData: T?) : Fragment(), View.OnClickListener {

    private var _binding: FragmentOrganizationInfoViewBinding? = null
    private var organization: T? = organizationData
    private var organizationId: String? = null
    private val binding get() = _binding!!

    private val TAG = "OrganizationInfoFragment"

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOrganizationInfoViewBinding.inflate(inflater, container, false)
        val root: View = binding.root
        val organization = organization
        var connectionType = ""
        var name = ""
        when (organization) {
            is Organization? -> {
                connectionType = organization?.endpoint?.get(0)?.connectionType?.code.toString()
                name = organization?.name.toString()
            }

            is Provider? -> {
                connectionType = organization?.endpoint?.get(0)?.connectionType?.code.toString()
                name = organization?.content.toString()
                organizationId = organization?.id
            }
        }
        binding.clinicNametxt.text =
            "${resources.getString(R.string.connect_to)} ${name}"
        if (connectionType.equals(resources.getString(R.string.hapi))) {
            binding.clinicDiscriptionTxt.text = "By providing  my " +
                    "$name ${resources.getString(R.string.clinic_info_hapi)}"
            binding.editTextUsername.visibility = View.VISIBLE;
            binding.passwordLayout.visibility = View.VISIBLE;
        } else {
            binding.clinicDiscriptionTxt.text =
                "$name ${resources.getString(R.string.clinic_discription)}"
            binding.editTextUsername.visibility = View.GONE;
            binding.passwordLayout.visibility = View.GONE;
        }
        binding.cancelTxt.setOnClickListener(this)
        binding.togglePassword.setOnClickListener(this)
        binding.leftArrowImageView.setOnClickListener(this)
        binding.checkboxConsent.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                val isShow = checkVisibilityOfProceed(connectionType)
                if (isShow) {
                    addListenersToProceed()
                }
            } else {
                removeListenersToProceed()
            }
        }

        binding.editTextUsername.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val isShow = checkVisibilityOfProceed(connectionType)
                if (isShow) {
                    addListenersToProceed()
                } else {
                    removeListenersToProceed()
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        binding.editTextPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val isShow = checkVisibilityOfProceed(connectionType)
                if (isShow) {
                    addListenersToProceed()
                } else {
                    removeListenersToProceed()
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        return root
    }

    private fun togglePasswordVisibility() {
        val passwordEditText = binding.editTextPassword
        val togglePasswordImageView = binding.togglePassword
        val isPasswordVisible =
            passwordEditText.inputType == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
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

    private fun removeListenersToProceed() {
        val drawable =
            ContextCompat.getDrawable(requireContext(), R.drawable.rounded_rectangle_grey)
        binding.frameLayoutProceed.background = drawable
        binding.frameLayoutProceed.setOnClickListener(null)
    }

    private fun addListenersToProceed() {
        val drawable =
            ContextCompat.getDrawable(requireContext(), R.drawable.rounded_rectangle_green)
        binding.frameLayoutProceed.background = drawable
        binding.frameLayoutProceed.setOnClickListener(this)
    }

    private fun checkVisibilityOfProceed(connectionType: String?): Boolean {
        if (connectionType.equals(resources.getString(R.string.hapi))) {
            return !(binding.editTextUsername.text.toString() == "" || binding.editTextPassword.text.toString()
                .equals("") || !binding.checkboxConsent.isChecked)
        } else {
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
                val parentFrag: DataConnectionsFragment =
                    this@OrganizationInfoFragment.parentFragment as DataConnectionsFragment
                parentFrag.showDataConnectionCategories()
            }

            R.id.togglePassword -> {
                togglePasswordVisibility()
            }

            R.id.leftArrowImageView -> {
                parentFragmentManager.popBackStack()
            }

            R.id.frameLayoutProceed -> {
                Log.i(TAG, "Proceed button pressed")

                if (organizationId != null) {
                    val parentFrag: DataConnectionsFragment =
                        this@OrganizationInfoFragment.parentFragment as DataConnectionsFragment

                    parentFrag.getDataSource(organizationId.toString())

                    parentFrag.getOauthUrl(organizationId.toString())

                    parentFrag.launchWebBrowser()

                    //openUrl()

                    //val connectionCreateRequest: ConnectionCreateRequest = ConnectionCreateRequest.Builder()
                    //.connectionId(connectionId)
                    //.connectionId("55a83bdc8d1eb1420aa1a71b")
                    //.username(binding.editTextUsername.text.toString())
                    //.password(binding.editTextPassword.text.toString())
                    //.build()

                    //parentFrag.createConnection(connectionCreateRequest)
                }
            }
        }
    }
}
