package com.bwell.sampleapp.activities.ui.data_connections

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bwell.common.models.domain.consent.enums.ConsentCategoryCode
import com.bwell.common.models.domain.consent.enums.ConsentProvisionType
import com.bwell.common.models.domain.consent.enums.ConsentStatus
import com.bwell.common.models.domain.data.Connection
import com.bwell.common.models.responses.Status
import com.bwell.connections.requests.ConnectionCreateRequest
import com.bwell.sampleapp.BWellSampleApplication
import com.bwell.sampleapp.R
import com.bwell.sampleapp.databinding.FragmentDataConnectionsParentBinding
import com.bwell.sampleapp.model.DataConnectionCategoriesListItems
import com.bwell.sampleapp.model.DataConnectionListItems
import com.bwell.sampleapp.model.DataConnectionsClinicsListItems
import com.bwell.sampleapp.viewmodel.DataConnectionsViewModel
import com.bwell.sampleapp.activities.ui.popup.PopupFragment
import com.bwell.sampleapp.viewmodel.DataConnectionsViewModelFactory
import com.bwell.user.consents.requests.ConsentUpdateRequest
import com.bwell.user.consents.requests.ConsentRequest
import kotlinx.coroutines.launch

class DataConnectionsFragment : Fragment(), View.OnClickListener, PopupFragment.PopupListener {

    private var _binding: FragmentDataConnectionsParentBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var dataConnectionsViewModel: DataConnectionsViewModel

    @SuppressLint("SuspiciousIndentation")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDataConnectionsParentBinding.inflate(inflater, container, false)
        val root: View = binding.root
        val repository = (activity?.application as? BWellSampleApplication)?.dataConnectionsRepository

        dataConnectionsViewModel = ViewModelProvider(this, DataConnectionsViewModelFactory(repository))[DataConnectionsViewModel::class.java]

        binding.includeHomeView.header.setText(resources.getString(R.string.connect_health_records))
        binding.includeHomeView.subText.setText(resources.getString(R.string.connect_health_records_sub_txt))
        binding.includeHomeView.btnGetStarted.setText(resources.getString(R.string.lets_go))
        binding.includeHomeView.btnGetStarted.setOnClickListener(this)
        binding.clinicInfoView.cancelTxt.setOnClickListener(this)

        binding.clinicInfoView.checkbox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                val drawable = ContextCompat.getDrawable(requireContext(), R.drawable.rounded_rectangle_green)
                binding.clinicInfoView.frameLayoutProceed.background = drawable
                binding.clinicInfoView.frameLayoutProceed.setOnClickListener(this)
                    lifecycleScope.launch {
                        try {
                            val consentsRequest = ConsentRequest.Builder()
                                .category(ConsentCategoryCode.TOS)
                                .status(ConsentStatus.ACTIVE)
                                .build()
                            dataConnectionsViewModel.fetchConsents(consentsRequest)

                            // Update user consent
                            val consentUpdateRequest = ConsentUpdateRequest.Builder()
                                .provisionType(ConsentProvisionType.PERMIT) // PERMIT | DENY
                                .category(ConsentCategoryCode.TOS)// Terms of Service
                                .status(ConsentStatus.ACTIVE)
                                .build()

                            dataConnectionsViewModel.updateConsent(consentUpdateRequest)

                        } catch (_: Exception) {
                        }
                    }
            } else {
                val drawable = ContextCompat.getDrawable(requireContext(), R.drawable.rounded_rectangle_grey)
                binding.clinicInfoView.frameLayoutProceed.background = drawable
                binding.clinicInfoView.frameLayoutProceed.setOnClickListener(null)
            }
        }

        // Observe changes in consent LiveData
        viewLifecycleOwner.lifecycleScope.launch {
            dataConnectionsViewModel.consentsData.collect { consentsResult ->
                consentsResult?.let {
                    Log.d("Consents ", "Consent >>>$consentsResult")
                }
            }
        }

        // Observe changes in creating a connection
        viewLifecycleOwner.lifecycleScope.launch {
            dataConnectionsViewModel.createConnectionData.collect { connectionOutcome ->
                connectionOutcome?.let {
                    if (connectionOutcome.status == Status.SUCCESS) {
                        // Connection created successfully, do something
                        //  calling getConnections
                        lifecycleScope.launch {
                            try {
                                dataConnectionsViewModel.getConnectionsAndObserve()
                            } catch (e: Exception) {
                                // Handle exceptions
                            }
                        }
                    } else {
                        // Connection creation failed, handle the error
                    }
                }
            }
        }

        // Observe changes in disconnecting a connection
        viewLifecycleOwner.lifecycleScope.launch {
            dataConnectionsViewModel.disconnectConnectionData.collect { disconnectOutcome ->
                disconnectOutcome?.let {
                    if (disconnectOutcome.status == Status.SUCCESS) {
                        // Disconnection done successfully, do something
                    } else {
                        // Disconnection failed, handle the error
                    }
                }
            }
        }

        // Observe changes in dataConnectionsListItems LiveData
        dataConnectionsViewModel.connectionsList.observe(viewLifecycleOwner) { connectionListItems ->
            // Update UI with the new list of DataConnectionListItems
            setDataConnectionsAdapter(connectionListItems)
        }

        displayDataConnectionsHomeInfo()

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    @SuppressLint("SetTextI18n")
    private fun setDataConnectionClinicsAdapter(dataConnectionsList: List<DataConnectionsClinicsListItems>) {
        val adapter = DataConnectionsClinicsListAdapter(dataConnectionsList)
        adapter.onItemClicked = { selectedDataConnection ->
            // Handle item click, perform UI changes here
            binding.includeDataConnectionsClinics.searchView.searchText.setText("")
            // Close the keyboard
            val inputMethodManager = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(binding.includeDataConnectionsClinics.searchView.searchText.windowToken, 0)
            displayIndividualClinicInfo()
            binding.clinicInfoView.clinicNametxt.text =
                "${resources.getString(R.string.connect_to)} ${selectedDataConnection.clinicName}"
            binding.clinicInfoView.clinicDiscriptionTxt.text =
                "${selectedDataConnection.clinicName} ${resources.getString(R.string.clinic_discription)}"

        }
        binding.includeDataConnectionsClinics.clinicsAfterSearchDataBodyView.rvClinics.layoutManager = LinearLayoutManager(requireContext())
        binding.includeDataConnectionsClinics.clinicsAfterSearchDataBodyView.rvClinics.adapter = adapter

        dataConnectionsViewModel.filteredDataConnectionsClinics.observe(viewLifecycleOwner) {
            if(it.isNotEmpty())
            {
                displayClinicsAfterDataSearchView(it.size)
            }else{
                displayClinicsAfterNoDataSearchView()
            }
            adapter.updateList(it)
        }
    }

    private fun setDataConnectionsAdapter(suggestedActivitiesLIst: List<Connection>) {
        val adapter = DataConnectionsListAdapter(suggestedActivitiesLIst)
        binding.includeDataConnections.dataConnectionFragment.visibility = View.VISIBLE;
        binding.includeDataConnectionCategory.dataConnectionFragment.visibility = View.GONE;
        binding.includeDataConnectionsClinics.dataConnectionsClinics.visibility = View.GONE;
        binding.clinicInfoView.clinicInfoView.visibility = View.GONE;
        binding.includeHomeView.headerView.visibility = View.GONE;
        binding.includeDataConnections.rvSuggestedDataConnections.layoutManager = LinearLayoutManager(requireContext())
        binding.includeDataConnections.rvSuggestedDataConnections.adapter = adapter
    }

    private fun setDataConnectionsCategoryAdapter(suggestedActivitiesLIst: List<DataConnectionCategoriesListItems>) {
        val adapter = DataConnectionsCategoriesListAdapter(suggestedActivitiesLIst)
        adapter.onItemClicked = { selectedDataConnection ->
            // Handle item click, perform UI changes here
            binding.includeDataConnectionsClinics.searchView.searchText.setText("")
            displayClinicsBeforeSearchView()
            addSearchTextListeners()
        }
        binding.includeDataConnectionCategory.dataConnectionFragment.visibility = View.VISIBLE;
        binding.includeDataConnections.dataConnectionFragment.visibility = View.GONE;
        binding.includeDataConnectionCategory.rvSuggestedDataConnections.layoutManager = LinearLayoutManager(requireContext())
        binding.includeDataConnectionCategory.rvSuggestedDataConnections.adapter = adapter
    }

    private fun addSearchTextListeners() {
        // Add a TextWatcher to the searchText EditText
        binding.includeDataConnectionsClinics.searchView.searchText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {
                // Update the filtered list when text changes
                val numberOfCharacters = charSequence?.length ?: 0
                if(numberOfCharacters > 2)
                {
                    dataConnectionsViewModel.filterDataConnectionsClinics(charSequence.toString())
                    dataConnectionsViewModel.dataConnectionsClinics.observe(viewLifecycleOwner) {
                        setDataConnectionClinicsAdapter(it.dataConnectionsClinicsList)
                    }
                }else{
                    if (charSequence?.isNotEmpty() == true)
                        displayClinicsBeforeSearchView()
                }
            }

            override fun afterTextChanged(editable: Editable?) {}
        })
    }

    private fun displayIndividualClinicInfo() {
        binding.includeDataConnectionCategory.dataConnectionFragment.visibility = View.GONE;
        binding.includeDataConnectionsClinics.dataConnectionsClinics.visibility = View.GONE;
        binding.clinicInfoView.clinicInfoView.visibility = View.VISIBLE;
    }

    private fun displayDataConnectionsHomeInfo() {
        binding.includeDataConnectionCategory.dataConnectionFragment.visibility = View.GONE;
        binding.includeDataConnectionsClinics.dataConnectionsClinics.visibility = View.GONE;
        binding.clinicInfoView.clinicInfoView.visibility = View.GONE;
        binding.includeDataConnections.dataConnectionFragment.visibility = View.GONE;
        binding.includeHomeView.headerView.visibility = View.VISIBLE;
    }

    private fun displayDataConnectionsCategoriesList() {
        binding.includeDataConnectionCategory.dataConnectionFragment.visibility = View.VISIBLE;
        binding.includeDataConnectionsClinics.dataConnectionsClinics.visibility = View.GONE;
        binding.clinicInfoView.clinicInfoView.visibility = View.GONE;
    }

    private fun displayClinicsBeforeSearchView() {
        binding.includeDataConnectionCategory.dataConnectionFragment.visibility = View.GONE;
        binding.includeDataConnectionsClinics.dataConnectionsClinics.visibility = View.VISIBLE;
        binding.includeDataConnectionsClinics.clinicsBeforeSearchBodyView.clinicsBeforeSearchBodyView.visibility = View.VISIBLE;
        binding.includeDataConnectionsClinics.clinicsAfterSearchNoDataBodyView.clinicsAfterSearchNoDataBodyView.visibility = View.GONE;
        binding.includeDataConnectionsClinics.clinicsAfterSearchDataBodyView.clinicsAfterSearchDataBodyView.visibility = View.GONE;
        binding.clinicInfoView.clinicInfoView.visibility = View.GONE;
    }

    private fun displayClinicsAfterNoDataSearchView() {
        binding.includeDataConnectionCategory.dataConnectionFragment.visibility = View.GONE;
        binding.includeDataConnectionsClinics.dataConnectionsClinics.visibility = View.VISIBLE;
        binding.includeDataConnectionsClinics.clinicsBeforeSearchBodyView.clinicsBeforeSearchBodyView.visibility = View.GONE;
        binding.includeDataConnectionsClinics.clinicsAfterSearchNoDataBodyView.clinicsAfterSearchNoDataBodyView.visibility = View.VISIBLE;
        binding.includeDataConnectionsClinics.clinicsAfterSearchDataBodyView.clinicsAfterSearchDataBodyView.visibility = View.GONE;
        binding.clinicInfoView.clinicInfoView.visibility = View.GONE;
    }

    private fun displayClinicsAfterDataSearchView(resultCount:Int) {
        binding.includeDataConnectionCategory.dataConnectionFragment.visibility = View.GONE;
        binding.includeDataConnectionsClinics.dataConnectionsClinics.visibility = View.VISIBLE;
        binding.includeDataConnectionsClinics.clinicsBeforeSearchBodyView.clinicsBeforeSearchBodyView.visibility = View.GONE;
        binding.includeDataConnectionsClinics.clinicsAfterSearchNoDataBodyView.clinicsAfterSearchNoDataBodyView.visibility = View.GONE;
        binding.includeDataConnectionsClinics.clinicsAfterSearchDataBodyView.clinicsAfterSearchDataBodyView.visibility = View.VISIBLE;
        binding.includeDataConnectionsClinics.clinicsAfterSearchDataBodyView.resultsText.setText("Results ("+resultCount+")");
        binding.clinicInfoView.clinicInfoView.visibility = View.GONE;
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.btn_get_started -> {
                binding.includeDataConnectionCategory.dataConnectionFragment.visibility = View.VISIBLE
                binding.includeHomeView.headerView.visibility = View.GONE
                dataConnectionsViewModel.suggestedDataConnectionsCategories.observe(viewLifecycleOwner) {
                    setDataConnectionsCategoryAdapter(it.suggestedDataConnectionsCategoriesList)
                }
            }
            R.id.cancel_txt -> {
                displayDataConnectionsCategoriesList()
                // Call the disconnect method
                lifecycleScope.launch {
                    try {
                        // Disconnect the data connection
                        val connectionId = "456"
                        // Call the disconnect method
                        dataConnectionsViewModel.disconnectConnection(connectionId)
                    } catch (e: Exception) {
                        // Handle the exception, e.g., show an error message
                        e.printStackTrace()
                    }
                }
            }
            R.id.frameLayoutProceed -> {
                // Assuming  have a connection request ready
                lifecycleScope.launch {
                    val connectionRequest = ConnectionCreateRequest.Builder()
                        .connectionId("connection_id")
                        .username("username")
                        .password("password")
                        .build()
                    dataConnectionsViewModel.createConnection(connectionRequest)
                }
            }
        }
    }

    override fun onGetDataButtonClicked() {
         //displayDataConnectionsCategoriesList()
        dataConnectionsViewModel.getConnectionsAndObserve()
    }

}