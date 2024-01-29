package com.bwell.sampleapp.activities.ui.data_connections.providers

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Parcelable
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
import androidx.navigation.fragment.findNavController
import com.bwell.common.models.domain.common.Organization
import com.bwell.common.models.domain.search.Provider
import com.bwell.common.models.responses.BWellResult
import com.bwell.sampleapp.R
import com.bwell.sampleapp.activities.ui.data_connections.DataConnectionsFragment
import com.bwell.sampleapp.databinding.FragmentOrganizationInfoViewBinding
import com.bwell.common.models.domain.data.DataSource
import com.bwell.common.models.domain.data.enums.ConnectionCategory
import com.bwell.connections.requests.ConnectionCreateRequest
import com.bwell.sampleapp.activities.ui.data_connections.OAuthConnectionWebViewClient
import com.bwell.sampleapp.activities.ui.data_connections.WebViewCallback
import com.bwell.sampleapp.viewmodel.DataConnectionsViewModel
import kotlinx.coroutines.launch

class OrganizationInfoFragment<T>() : Fragment(),View.OnClickListener,WebViewCallback {


    private var _binding: FragmentOrganizationInfoViewBinding? = null
    private var entity: T? = null
    private lateinit var entityName: String
    private lateinit var entityId: String
    private var authType: ConnectionCategory? = null
    private var dataSourceId: String? = null
    private lateinit var viewModel: DataConnectionsViewModel

    private val binding get() = _binding!!

    companion object {
        private const val ARG_ENTITY = "entity"

        // factory method to create a new instance of the fragment with parameters
        fun <T> newInstance(entity: T): OrganizationInfoFragment<T> {
            val fragment = OrganizationInfoFragment<T>()
            val args = Bundle()

            args.putParcelable(ARG_ENTITY, entity as Parcelable)
            fragment.arguments = args
            return fragment
        }
    }

    constructor(entity: T) : this() {
        this.entity = entity ?: throw Exception("Cannot create view without entityData")
        this.entityName = getName(entity)
        this.entityId = getId(entity)
    }

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        arguments?.let { entity= it.getParcelable(ARG_ENTITY) }

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
        binding.checkboxConsent.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                addListenersToProceed()
            } else {
                removeListenersToProceed()
            }
        }

        binding.editTextUsername.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // TODO this should be checking that s is non-empty
                // decide whether to enable or disable proceed based on ^
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.editTextPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // TODO this should be checking that s is non-empty
                // decide whether to enable or disable proceed based on ^
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

        if(!binding.checkboxConsent.isChecked) {
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
                if(binding.constraintLayout.visibility == View.VISIBLE) {
                    parentFragmentManager.popBackStack()
                }

                if(binding.constraintWebLayout.visibility == View.VISIBLE) {
                    binding.constraintLayout.visibility = View.VISIBLE
                    binding.constraintWebLayout.visibility = View.GONE
                }
            }
        }
    }

    private fun onClickProceedOAuth(){
        // do the OAuth things here
        Log.d("onClickProceedOAuth", "Hey there, would you like some OAuth today?")

        openOAuthView()
        binding.frameLayoutProceed.setOnClickListener { onClickDoneOAuth() }
    }

    private fun onClickProceedBasic(){
        // do the Basic things here
        Log.d("onClickProceedBasic", "Basically, lets proceed")

        val connectionRequest = ConnectionCreateRequest.Builder()
            .connectionId(dataSourceId ?: throw Exception("If you got here, sad things happened.")) // Use the dataSourceId
            .username(binding.editTextUsername.text.toString())
            .password(binding.editTextPassword.text.toString())
            .build()
        viewModel.createConnection(connectionRequest)
    }

    private fun onClickDoneOAuth(){
        //findNavController().navigate(R.id.action_data_connections__to__nav_home)
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

        Log.d("showOAuthLogin", "End")
    }

    @SuppressLint("SetJavaScriptEnabled")
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

                    binding.constraintLayout.visibility = View.GONE
                    binding.constraintWebLayout.visibility = View.VISIBLE
                    binding.oauthWebView.visibility = View.VISIBLE
                    binding.oauthWebView.webViewClient = OAuthConnectionWebViewClient(this@OrganizationInfoFragment)
                    binding.oauthWebView.settings.javaScriptEnabled = true
                    // TODO: Is there a setting to allow HTTP?


                    // binding.oauthWebView.loadUrl("https://google.com")
                    binding.oauthWebView.loadUrl(oauthData)
                }
            }
        }
    }

    override fun onWebViewSuccess(){
        binding.constraintWebLayout.visibility = View.GONE
        binding.constraintLayout.visibility = View.VISIBLE
        binding.checkboxConsent.visibility = View.GONE
        binding.checkboxConsentTxt.visibility = View.GONE
        binding.cancelTxt.visibility = View.GONE
        binding.textViewProceed.text = "Done"

        binding.clinicDescriptionTxt.text = "OAuth Login Successful!!!!!! You're the best!"
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
                val endpoint = nonNullEntity.endpoint?.filter {
                        ep -> ep?.identifier?.any {
                        id -> id?.system?.contains("connectionhub/clientconnections") ?: false }
                    ?: return "" }
                    ?.first() ?: throw Exception("No clientConnections endpoint present on Organization entity.")

                return endpoint.name ?: throw Exception("No name present on Organization endpoint")
            }
            is Provider -> {
                val endpoint = nonNullEntity.endpoint?.filter {
                        ep -> ep?.identifier?.any {
                        id -> id?.system?.contains("connectionhub/clientconnections") ?: false }
                    ?: return "" }
                    ?.first() ?: throw Exception("No clientConnections endpoint present on Provider entity.")

                return endpoint.name ?: throw Exception("No name present on Provider endpoint")
            }
        }

        // Should not get here
        throw IllegalStateException("Could not get entity id. Must be either Provider or Organization.")
    }
}
