package com.bwell.sampleapp.activities.ui.data_connections.providers

import LocationAdapter
import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.text.SpannableString
import android.text.TextWatcher
import android.text.style.UnderlineSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bwell.common.models.domain.common.Organization
import com.bwell.common.models.domain.search.Provider
import com.bwell.common.models.responses.BWellResult
import com.bwell.sampleapp.BWellSampleApplication
import com.bwell.sampleapp.R
import com.bwell.sampleapp.activities.ui.data_connections.DataConnectionsFragment
import com.bwell.sampleapp.activities.ui.popup.PopupFragment
import com.bwell.sampleapp.databinding.FragmentProviderViewBinding
import com.bwell.sampleapp.utils.hideKeyboard
import com.bwell.sampleapp.viewmodel.ProviderViewModel
import com.bwell.sampleapp.viewmodel.ProviderViewModelFactory
import com.bwell.search.requests.provider.ProviderSearchRequest
import com.bwell.common.models.domain.search.enums.SortField
import com.bwell.common.models.domain.common.enums.SortOrder
import com.bwell.sampleapp.viewmodel.EntityInfoViewModel
import com.bwell.search.requests.connection.RequestConnectionRequest
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch

class ProviderSearchFragment : Fragment(),View.OnClickListener, PopupFragment.PopupListener,
    OrganizationAdapter.OrganizationClickListener {

    private val organizationInfoViewModel: EntityInfoViewModel by viewModels()

    private var _binding: FragmentProviderViewBinding? = null
    private lateinit var providerViewModel: ProviderViewModel

    private val binding get() = _binding!!
    private lateinit var providersListAdapter: ProvidersListAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProviderViewBinding.inflate(inflater, container, false)
        val root: View = binding.root
        val repository = (activity?.application as? BWellSampleApplication)?.providerRepository
        providerViewModel = ViewModelProvider(this, ProviderViewModelFactory(repository))[ProviderViewModel::class.java]
        binding.providerSearchView.applyFiltersIv.setOnClickListener(this)
        binding.providerFiltersView.frameLayoutapplyFilters.setOnClickListener(this)
        binding.providerSearchView.leftArrowImageView.setOnClickListener {
            parentFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
            val parentFrag: DataConnectionsFragment = this@ProviderSearchFragment.getParentFragment() as DataConnectionsFragment
            parentFrag.showDataConnectionCategories()
        }
        addRequestConnectionButtonListener()
        showProvidersData("")

        return root
    }


    override fun onOrganizationClick(organization: Organization?) {
        // Set the entity on the viewModel
        organizationInfoViewModel.organization = organization;

        // create the fragment
        val organizationFragment = EntityInfoFragment()
        val transaction = parentFragmentManager.beginTransaction()
        transaction.hide(this@ProviderSearchFragment)
        transaction.add(R.id.container_layout, organizationFragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    private fun addSearchTextListeners() {
        binding.providerSearchView.searchView.searchText.addTextChangedListener(object :
            TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {
                // Update the filtered list when text changes
                val numberOfCharacters = charSequence?.length ?: 0
                providerViewModel.filterDataConnectionsProviders(charSequence.toString())
                viewLifecycleOwner.lifecycleScope.launch {
                    providerViewModel.filteredResults.collect { filteredList ->
                        providersListAdapter.updateList(filteredList)
                        if(filteredList!!.isNotEmpty())
                        {
                            binding.providerSearchView.providersDataView.resultsText.text = "Search Results ("+filteredList?.size+")"
                            binding.providerSearchView.providersDataView.providerDataView.visibility = View.VISIBLE
                            binding.providerSearchView.constraintLayout.visibility = View.GONE
                        }
                        else
                        {
                            binding.providerSearchView.providersDataView.providerDataView.visibility = View.GONE
                            binding.providerSearchView.constraintLayout.visibility = View.VISIBLE
                            binding.providerSearchView.noDataTv.visibility = View.VISIBLE
                        }
                    }
                }
            }

            override fun afterTextChanged(editable: Editable?) {}
        })
    }

    private fun addRequestConnectionButtonListener() {
        binding.providerSearchView.requestConnectionButton.setOnClickListener {
            val popupFragment = PopupFragment()
            popupFragment.setPopupListener(this@ProviderSearchFragment) // Set the listener
            popupFragment.show(childFragmentManager, "popup")
        }
    }


    private fun showProvidersData(enteredText: String) {
        val searchTerm = enteredText
        val latitude = 39.2848102
        val longitude = -76.702898
        val distance = 200.0
        //val gender = Gender.MALE
        val page = 0
        val pageSize = 100

        val request = ProviderSearchRequest.Builder()
            .searchTerm(searchTerm)
            .location(latitude, longitude, distance)
            .includePopulatedPROAonly()
            .sortBy(SortField.DISTANCE, SortOrder.ASC)
            .page(page)
            .pageSize(pageSize)
            .build()

        providerViewModel.searchProviders(request)

        viewLifecycleOwner.lifecycleScope.launch {
            providerViewModel.searchResults.collect { searchResult ->
                if (searchResult != null) {
                    setProviderAdapter(searchResult)
                }
            }
        }

        addSearchTextListeners()
    }

    private fun setProviderAdapter(searchResult: BWellResult<Provider>) {
        when (searchResult) {
            is BWellResult.SearchResults -> {
                val providersList = searchResult.data
                providersListAdapter = ProvidersListAdapter(providersList)
                providersListAdapter.onItemClicked = { selectedList ->
                    hideKeyboard(requireContext(),binding.providerSearchView.searchView.searchText.windowToken)
                    binding.providerSearchView.providerSearchView.visibility = View.GONE
                    binding.providerFiltersView.providerFiltersView.visibility = View.GONE
                    binding.organizationsLocationsDataView.organizationsLocationsDataView.visibility = View.VISIBLE
                    var titleText = ""
                    if(selectedList.organization?.size!! > 0)
                    {
                        binding.organizationsLocationsDataView.organizationsListView.visibility = View.VISIBLE
                        var organizationAdapter = OrganizationAdapter(requireContext(), selectedList.organization)
                        organizationAdapter.organizationClickListener = this
                        binding.organizationsLocationsDataView.organizationsListView.adapter = organizationAdapter
                        titleText = resources.getString(R.string.select_connection_for)
                    }else{
                        binding.organizationsLocationsDataView.organizationsListView.visibility = View.GONE
                        titleText = resources.getString(R.string.request_connection_for)
                    }
                    if(selectedList.location?.size!! > 0)
                    {
                        val locationAdapter = LocationAdapter(requireContext(), selectedList.location)
                        binding.organizationsLocationsDataView.locationsListView.adapter = locationAdapter
                    }
                    val content = SpannableString(resources.getString(R.string.request_add_new_connection))
                    content.setSpan(UnderlineSpan(), 0, content.length, 0)
                    binding.organizationsLocationsDataView.requestConnection.text = content
                    if(selectedList.name?.size!! > 0)
                    {
                        binding.organizationsLocationsDataView.headerText.text = titleText+" "+selectedList.name?.get(0)?.text.toString()+" below:"
                    }
                    binding.organizationsLocationsDataView.requestConnection.setOnClickListener(this)
                }
                binding.providerSearchView.constraintLayout.visibility = View.GONE
                binding.providerSearchView.providersDataView.providerDataView.visibility = View.VISIBLE
                binding.providerSearchView.providersDataView.resultsText.text = "Search Results ("+providersList?.size+")"
                binding.providerSearchView.providersDataView.rvProviders.layoutManager = LinearLayoutManager(requireContext())
                binding.providerSearchView.providersDataView.rvProviders.adapter = providersListAdapter
                binding.organizationsLocationsDataView.leftArrowImageView.setOnClickListener(this)

            }
            else -> {}
        }

    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.apply_filters_iv -> {
                binding.providerSearchView.providerSearchView.visibility = View.GONE
                binding.providerFiltersView.providerFiltersView.visibility = View.VISIBLE
            }
            R.id.frameLayoutapplyFilters -> {
                binding.providerSearchView.providerSearchView.visibility = View.VISIBLE
                binding.providerFiltersView.providerFiltersView.visibility = View.GONE
            }
            R.id.leftArrowImageView -> {
                binding.providerSearchView.providerSearchView.visibility = View.VISIBLE
                binding.providerFiltersView.providerFiltersView.visibility = View.GONE
                binding.organizationsLocationsDataView.organizationsLocationsDataView.visibility = View.GONE
            }
            R.id.request_connection -> {
                val popupFragment = PopupFragment()
                popupFragment.setPopupListener(this@ProviderSearchFragment) // Set the listener
                popupFragment.show(childFragmentManager, "popup")
            }
        }
    }

    override fun onSubmitButtonClicked(institute: String, provider: String, city: String, state: String)
    {
        if (institute.isEmpty()) {
            showSuccessDialog(resources.getString(R.string.error),resources.getString(R.string.request_connection_institution_required))
            return
        }
        val connectionRequest = RequestConnectionRequest.Builder()
            .institution(institute)
            .provider(provider)
            .city(city)
            .state(state).build()
        providerViewModel.requestConnection(connectionRequest)
        viewLifecycleOwner.lifecycleScope.launch {
            providerViewModel.requestConnectionData.collect { connectionOutcome ->
                connectionOutcome?.let {
                    if (connectionOutcome.success()) {
                        showSuccessDialog(resources.getString(R.string.success),resources.getString(R.string.success_data))
                    }else{
                        showSuccessDialog(resources.getString(R.string.error),resources.getString(R.string.error_data))
                    }
                }
            }
        }
    }

    private fun showSuccessDialog(title: String, content: String) {
        val alertDialogBuilder = AlertDialog.Builder(requireContext())
        alertDialogBuilder.setTitle(title)
        alertDialogBuilder.setMessage(content)
        alertDialogBuilder.setPositiveButton(resources.getString(R.string.close)) { dialog, _ ->
            dialog.dismiss()
        }
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }
}
