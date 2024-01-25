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
import androidx.lifecycle.lifecycleScope
import com.bwell.common.models.domain.common.Organization
import com.bwell.common.models.domain.search.Provider
import com.bwell.common.models.responses.BWellResult
import com.bwell.connections.requests.ConnectionCreateRequest
import com.bwell.sampleapp.R
import com.bwell.sampleapp.activities.ui.data_connections.DataConnectionsFragment
import com.bwell.sampleapp.databinding.FragmentOrganizationInfoViewBinding
import com.bwell.common.models.domain.data.DataSource
import com.bwell.sampleapp.viewmodel.DataConnectionsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class OrganizationInfoFragment<T>(organizationData: T?) : Fragment(),View.OnClickListener {

    private var _binding: FragmentOrganizationInfoViewBinding? = null
    private var organization: T? = organizationData
    private var connectionType: String = ""
    private var connectionId: String = ""
    private var dataSource: DataSource? = null
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

        // Collect organization information
        val parentFragment = requireParentFragment()
        val viewModel = ViewModelProvider(parentFragment).get(DataConnectionsViewModel::class.java)

        val orgId = getId(organization)
            ?: throw Exception("OrgId was null. Bad things are happening here.")

        lifecycleScope.launch {
            val dataSource = getDataSource(orgId);

            Log.d("dataSource.type", dataSource.type.toString())
            Log.d("dataSource.name", dataSource.name)
            Log.d("dataSource.id", dataSource.id)
            Log.d("dataSource.category", dataSource.category.toString())

            if(dataSource.category?.toString() == "OAUTH") {
                val oauthUrl = getOAuthUrl(dataSource.id)
                Log.d("oauthUrl", oauthUrl)
            }
        }

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
                Log.d("Vict organization", "$organization")

                val orgId = getId(organization)
                    ?: throw Exception("OrgId was null. Bad things are happening here.")

//                Log.d("Organization.Id:", orgId)
//                val connectionData = parentFrag.getDataSource(orgId)//"55a83bdc8d1eb1420aa1a71b")
//                GlobalScope.launch (Dispatchers.Main) {
//                    dataSource = getDataSource(connectionData)
//
//                    Log.d("dataSource.type", dataSource?.type.toString())
//                    Log.d("dataSource.name", dataSource!!.name)
//                    Log.d("dataSource.id", dataSource!!.id)
//                    Log.d("dataSource.category", dataSource!!.category.toString())
//                }
                // category Basic or OAuth

                if (dataSource!!.category.toString().equals("OPEN", ignoreCase = true)) {
                    val connectionCreateRequest: ConnectionCreateRequest = ConnectionCreateRequest.Builder()
                        //.connectionId(connectionId)
                        .connectionId("55a83bdc8d1eb1420aa1a71b")
                        .username(binding.editTextUsername.text.toString())
                        .password(binding.editTextPassword.text.toString())
                        .build()

                    parentFrag.createConnection(connectionCreateRequest)

                } else if (connectionType.equals("OAUTH", ignoreCase = true)) {
                    // Handle the case when connectionType is "oAuth"
                    parentFrag.getOAuthUrl("epic_sandbox_r4c")
                    //openUrl()
                }
            }
        }
    }

    private suspend fun getDataSource(orgId: String): DataSource {
        viewModel.getDataSource(orgId);

        return (viewModel.dataSourceData.firstOrNull() as? BWellResult.SingleResource<DataSource>)?.data
            ?: throw Exception("Could not get dataSource. Sadness...")
    }

    private suspend fun getOAuthUrl(dataSourceId: String): String {
        viewModel.getOAuthUrl(dataSourceId);

        return (viewModel.oauthUrlData.firstOrNull() as? BWellResult.SingleResource<String>)?.data
            ?: throw Exception("Could not get oauthUrl. Sadness...")
    }


    fun getId(obj:T?): String? {
        if(obj is Organization?){
            val org = obj as Organization?

            if(org != null){
                // TODO: Use the right filter here
                val endpoint = org.endpoint?.filter {
                        ep -> ep?.identifier?.any {
                        id -> id?.system ==  "https://integrationhub-web.prod.bwell.zone/connectionhub/clientconnections/oid" }
                    ?: return "" }
                    ?.first()

                return endpoint?.name
            }
        } else if(obj is Provider?){
            val prov = obj as Provider?

            if(prov != null){
                // TODO: Use the right filter here
                val endpoint = prov.endpoint?.filter {
                        ep -> ep?.identifier?.any {
                        id -> id?.system ==  "https://integrationhub-web.prod.bwell.zone/connectionhub/clientconnections/oid" }
                    ?: return "" }
                    ?.first()

                return endpoint?.name
            }
        }

        return null
    }
}
