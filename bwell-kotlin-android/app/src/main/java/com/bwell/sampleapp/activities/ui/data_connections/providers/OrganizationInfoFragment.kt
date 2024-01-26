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
import com.bwell.sampleapp.R
import com.bwell.sampleapp.activities.ui.data_connections.DataConnectionsFragment
import com.bwell.sampleapp.databinding.FragmentOrganizationInfoViewBinding
import com.bwell.common.models.domain.data.DataSource
import com.bwell.common.models.domain.data.enums.ConnectionCategory
import com.bwell.sampleapp.viewmodel.DataConnectionsViewModel
import kotlinx.coroutines.launch

class OrganizationInfoFragment<T>(entityData: T?) : Fragment(),View.OnClickListener {

    private var _binding: FragmentOrganizationInfoViewBinding? = null
    private var entity: T = entityData ?: throw Exception("Cannot create view without entityData")
    private var entityName: String = getName(entity)
    private var entityId: String = getId(entity)
    private var connectionType: String = getConnectionType(entity)
    private var authType: ConnectionCategory? = null
    private var dataSourceId: String? = null
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

        val parentFragment = requireParentFragment()
        viewModel = ViewModelProvider(parentFragment)[DataConnectionsViewModel::class.java]

        // Certain login components need customization based on entity info
        showLogin()

        // Login components that don't require customization based on entity
        binding.cancelTxt.setOnClickListener(this)
        binding.togglePassword.setOnClickListener(this)
        binding.leftArrowImageView.setOnClickListener(this)
        binding.checkbox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                val isShow = checkVisibilityOfProceed(connectionType)
                if(isShow)
                {
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
                if(isShow)
                {
                    addListenersToProceed()
                }else{
                    removeListenersToProceed()
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
                    addListenersToProceed()
                }else{
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

    private fun removeListenersToProceed() {
        val drawable = ContextCompat.getDrawable(requireContext(), R.drawable.rounded_rectangle_grey)
        binding.frameLayoutProceed.background = drawable
        binding.frameLayoutProceed.setOnClickListener(null)
    }

    private fun addListenersToProceed() {
        if(authType == null){
            Log.d("addListenersToProceed", "prevented listener add - auth type is null.")
            removeListenersToProceed()
            return
        }

        if(!binding.checkbox.isChecked) {
            Log.d("addListenersToProceed", "prevented listener add - attestation not checked")
            removeListenersToProceed()
            return
        }

        val drawable = ContextCompat.getDrawable(requireContext(), R.drawable.rounded_rectangle_green)
        binding.frameLayoutProceed.background = drawable

        when(authType) {
            ConnectionCategory.BASIC -> binding.frameLayoutProceed.setOnClickListener { onClickProceedBasic() }
            ConnectionCategory.OAUTH -> binding.frameLayoutProceed.setOnClickListener { onClickProceedOAuth() }
            null -> {
                Log.w("addListenersToProceed", "tried to set listeners with null authType")
                return
            }
        }
    }

    private fun checkVisibilityOfProceed(connectionType:String?): Boolean {
        // TODO:Question - We should probably always be able to proceed? Either with the BASIC auth or to the OAuth URL, right? Or is this connectionType not the same as AuthType?
        return if(connectionType.equals(resources.getString(R.string.hapi))) {
            !(binding.editTextUsername.text.toString() == "" || binding.editTextPassword.text.toString() == "" || !binding.checkbox.isChecked)
        }else{
            true
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
                // should never get here anymore!!
            }
        }
    }

    private fun onClickProceedOAuth(){
        // do the OAuth things here
        Log.d("onClickProceedOAuth", "Hey there, would you like some OAuth today?")

        openOAuthView()
    }

    private fun onClickProceedBasic(){
        // do the Basic things here
        Log.d("onClickProceedBasic", "Basically, lets proceed")
    }

    private fun showLogin(){
        // show different login components based on dataSource Connection type
        Log.d("showLogin", "Begin")
        viewModel.getDataSource(entityId)
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.dataSourceData.collect { result ->
                if(result != null){
                    Log.d("showLogin", "collected dataSource")
                    // based on data source connection, either show basic or oauth login
                    val dataSource = (result as? BWellResult.SingleResource<DataSource>)?.data ?: throw Exception("Collected null dataSource")

                    Log.d("showLogin", "dataSource.category: ${dataSource.category}")
                    authType = dataSource.category
                    dataSourceId = dataSource.id
                    // add Listeners to Proceed now that we know our auth type
                    addListenersToProceed()
                    if( dataSource.category == ConnectionCategory.BASIC ) {
                        showBasicLogin()
                    } else if( dataSource.category == ConnectionCategory.OAUTH ){
                        showOAuthLogin()
                    }
                }
            }
        }
    }

    private fun showBasicLogin(){
        Log.d("showBasicLogin", "Begin")
        // set text
        binding.clinicNametxt.text = resources.getString(R.string.connect_to_entity, entityName)
        binding.clinicDescriptionTxt.text = resources.getString(R.string.clinic_info_hapi, entityName)
        binding.editTextUsername.visibility = View.VISIBLE
        binding.passwordLayout.visibility = View.VISIBLE
        Log.d("showBasicLogin", "End")
    }

    private fun showOAuthLogin(){
        Log.d("showOAuthLogin", "Begin")

        binding.clinicNametxt.text = resources.getString(R.string.connect_to_entity, entityName)
        binding.clinicDescriptionTxt.text = resources.getString(R.string.clinic_description, entityName) // TODO this description references Lee Health TOS
        binding.editTextUsername.visibility = View.GONE
        binding.passwordLayout.visibility = View.GONE
        // TODO:Question - what about these?
        // binding.checkbox.visibility = View.GONE
        // binding.checkboxTxt.visibility = View.GONE

        Log.d("showOAuthLogin", "End")
    }

    private fun openOAuthView(){
        Log.d("openOAuthView", "Begin")
        viewModel.getOAuthUrl(dataSourceId ?: throw Exception("Tried to open an oauthUrl before dataSource found"))
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.oauthUrlData.collect { result ->
                if(result != null){
                    Log.d("openOAuthView", "collected oauthData")
                    // openUrl
                    val oauthData = (result as? BWellResult.SingleResource<String>)?.data ?: throw Exception("No OAuth Url available for OAuth data connection entity")

                    // do something here:
                    Log.d("OAuth Url", oauthData)
                }
            }
        }
    }

    private fun getConnectionType(entity: T?): String {
        when (val nonNullEntity = requireNotNull(entity) { "Entity cannot be null in OrganizationInfoFragment" }) {
            is Organization -> {
                return nonNullEntity.endpoint?.get(0)?.connectionType?.code.toString()
            }
            is Provider -> {
                return nonNullEntity.endpoint?.get(0)?.connectionType?.code.toString()
            }
        }

        throw IllegalStateException("Could not get connection type. Must be either Provider or Organization.")
    }

    private fun getName(entity: T?): String {
        when (val nonNullEntity = requireNotNull(entity) { "Entity cannot be null in OrganizationInfoFragment" }) {
            is Organization -> {
                return nonNullEntity.name.toString()
            }
            is Provider -> {
                return nonNullEntity.content.toString()
            }
        }

        throw IllegalStateException("Could not get entity name. Must be either Provider or Organization.")
    }

    private fun getId(entity:T?): String {

        when (val nonNullEntity = requireNotNull(entity) { "Entity cannot be null in OrganizationInfoFragment" }) {
            is Organization -> {
                // TODO: Use the right filter here
                val endpoint = nonNullEntity.endpoint?.filter {
                        ep -> ep?.identifier?.any {
                        id -> id?.system ==  "https://integrationhub-web.prod.bwell.zone/connectionhub/clientconnections/oid" }
                    ?: return "" }
                    ?.first() ?: throw Exception("No clientConnections endpoint present on Organization entity.")

                return endpoint.name ?: throw Exception("No name present on Organization endpoint")
            }
            is Provider -> {
                // TODO: Use the right filter here
                val endpoint = nonNullEntity.endpoint?.filter {
                        ep -> ep?.identifier?.any {
                        id -> id?.system ==  "https://integrationhub-web.prod.bwell.zone/connectionhub/clientconnections/oid" }
                    ?: return "" }
                    ?.first() ?: throw Exception("No clientConnections endpoint present on Provider entity.")

                return endpoint.name ?: throw Exception("No name present on Provider endpoint")
            }
        }

        // Should not get here
        throw IllegalStateException("Could not get entity id. Must be either Provider or Organization.")
    }
}
