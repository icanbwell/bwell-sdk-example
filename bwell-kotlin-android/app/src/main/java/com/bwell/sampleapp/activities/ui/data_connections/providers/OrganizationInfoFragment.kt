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
import androidx.lifecycle.ViewModelProvider
import com.bwell.common.models.domain.common.Organization
import com.bwell.common.models.domain.search.Provider
import com.bwell.common.models.responses.BWellResult
import com.bwell.connections.requests.ConnectionCreateRequest
import com.bwell.sampleapp.R
import com.bwell.sampleapp.activities.ui.data_connections.DataConnectionsFragment
import com.bwell.sampleapp.databinding.FragmentOrganizationInfoViewBinding
import com.bwell.sampleapp.repository.DataConnectionsRepository
import com.bwell.sampleapp.viewmodel.DataConnectionsViewModel
import com.bwell.sampleapp.viewmodel.DataConnectionsViewModelFactory
import com.bwell.common.models.domain.data.DataSource

class OrganizationInfoFragment<T>(organizationData: T?) : Fragment(),View.OnClickListener {

    private var _binding: FragmentOrganizationInfoViewBinding? = null
    private var organization: T? = organizationData
    private var connectionType: String = ""
    private var connectionId: String = ""
    private lateinit var viewModel: DataConnectionsViewModel


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
        var name = ""
        when (organization) {
            is Organization?->{
                connectionType = organization?.endpoint?.get(0)?.connectionType?.code.toString()
                name = organization?.name.toString()
            }
            is Provider?->{
                connectionType = organization?.endpoint?.get(0)?.connectionType?.code.toString()
                name = organization?.content.toString()
            }
        }

        Log.d("OrganizationInfoFragment", "Connection Type: $organization?.endpoint?.get")
//        connectionId = organization?.endpoint?.get(0)?.name?.toString()

        binding.clinicNametxt.text =
            "${resources.getString(R.string.connect_to)} ${name}"
        if(connectionType.equals(resources.getString(R.string.open)))
        {
            binding.clinicDiscriptionTxt.text ="By providing  my "+
                "${name} ${resources.getString(R.string.clinic_info_hapi)}"
            binding.editTextUsername.visibility = View.VISIBLE;
            binding.passwordLayout.visibility = View.VISIBLE;
        }else{
            binding.clinicDiscriptionTxt.text =
                "${name} ${resources.getString(R.string.clinic_discription)}"
            binding.editTextUsername.visibility = View.GONE;
            binding.passwordLayout.visibility = View.GONE;
        }
        binding.cancelTxt.setOnClickListener(this)
        binding.togglePassword.setOnClickListener(this)
        binding.leftArrowImageView.setOnClickListener(this)
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

        // Initialize your ViewModel using the factory
        val repository = DataConnectionsRepository(requireContext()) // Pass the context
        val factory = DataConnectionsViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory).get(DataConnectionsViewModel::class.java)

        // Observe dataSourceLiveData
//        viewModel.dataSourceLiveData.observe(viewLifecycleOwner) { dataSourceResult ->
//            // Handle the result here
//            if (dataSourceResult is BWellResult.success) {
//                val dataSource: DataSource? = dataSourceResult.data
//                // Now you can use the dataSource in your UI or perform any other actions
//                // For example, update UI components based on the dataSource
//            } else if (dataSourceResult is BWellResult.error) {
//                // Handle the error case
//                val errorMessage: String? = dataSourceResult.error?.localizedMessage
//                // Display an error message or take appropriate action
//            }
//        }

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
            return !(binding.editTextUsername.text.toString() == "" || binding.editTextPassword.text.toString().equals("") || !binding.checkbox.isChecked)
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
                val parentFrag: DataConnectionsFragment = this@OrganizationInfoFragment.parentFragment as DataConnectionsFragment
                parentFrag.showDataConnectionCategories()
            }
            R.id.togglePassword -> {
                togglePasswordVisibility()
            }
            R.id.leftArrowImageView -> {
                parentFragmentManager.popBackStack()
            }
            R.id.frameLayoutProceed -> {
                val parentFrag: DataConnectionsFragment = this@OrganizationInfoFragment.parentFragment as DataConnectionsFragment
//                Log.d("OrganizationInfoFragment", "Connection Type: $connectionType")

                parentFrag.getDataSource("55a83bdc8d1eb1420aa1a71b")
                // category Basic or OAuth

                if (connectionType.equals("Open", ignoreCase = true)) {
                    val connectionCreateRequest: ConnectionCreateRequest = ConnectionCreateRequest.Builder()
                        //.connectionId(connectionId)
                        .connectionId("55a83bdc8d1eb1420aa1a71b")
                        .username(binding.editTextUsername.text.toString())
                        .password(binding.editTextPassword.text.toString())
                        .build()

                    parentFrag.createConnection(connectionCreateRequest)

                } else if (connectionType.equals("OAuth", ignoreCase = true)) {
                    // Handle the case when connectionType is "oAuth"
                    parentFrag.getOAuthUrl("epic_sandbox_r4c")
                    //openUrl()
                } else {
                    // Handle other cases or provide a default behavior

                }

//                Log.d("OrganizationInfoFragment", "Organization details: $organization")



            }
        }
//        Log.d("OrganizationInfoFragment", "Organization details: $organization")
        println("here")
    }
}
