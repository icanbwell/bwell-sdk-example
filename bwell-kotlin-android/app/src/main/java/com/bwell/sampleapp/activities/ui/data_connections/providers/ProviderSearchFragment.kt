package com.bwell.sampleapp.activities.ui.data_connections.providers

import LocationAdapter
import android.os.Bundle
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.apollographql.apollo3.api.Optional
import com.bwell.common.models.domain.search.Provider
import com.bwell.common.models.responses.BWellResult
import com.bwell.sampleapp.BWellSampleApplication
import com.bwell.sampleapp.R
import com.bwell.sampleapp.databinding.FragmentProviderViewBinding
import com.bwell.sampleapp.utils.SelectedOrganizationHolder
import com.bwell.sampleapp.utils.hideKeyboard
import com.bwell.sampleapp.viewmodel.ProviderViewModel
import com.bwell.sampleapp.viewmodel.ProviderViewModelFactory
import com.bwell.search.ProviderSearchQuery
import com.bwell.search.provider.requests.ProviderSearchRequest
import com.bwell.search.type.Gender
import com.bwell.search.type.OrderBy
import com.bwell.search.type.OrganizationType
import com.bwell.search.type.SortField
import com.bwell.search.type.SortOrder
import kotlinx.coroutines.launch

class ProviderSearchFragment : Fragment(),View.OnClickListener,
    OrganizationAdapter.OrganizationClickListener {

    private var _binding: FragmentProviderViewBinding? = null
    private lateinit var providerViewModel: ProviderViewModel

    private val binding get() = _binding!!

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
        addSearchTextListeners()

        return root
    }


    override fun onOrganizationClick(organization: ProviderSearchQuery.Organization?) {
        binding.organizationsLocationsDataView.organizationsLocationsDataView.visibility = View.GONE
        SelectedOrganizationHolder.selectedOrganization = organization
        val organizationFragment = OrganizationInfoFragment()
        val transaction = parentFragmentManager.beginTransaction()
        transaction.replace(R.id.container_layout, organizationFragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    private fun addSearchTextListeners() {
        binding.providerSearchView.searchView.searchText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch(binding.providerSearchView.searchView.searchText.text.toString())
                true
            } else {
                false
            }
        }
    }

    private fun performSearch(enteredText: String) {
        hideKeyboard(requireContext(),binding.providerSearchView.searchView.searchText.windowToken)
        val searchTerm = enteredText
        val latitude = 33.33
        val longitude = 44.44
        val distance = 50.0
        val gender = Gender.male
        val page = 1
        val pageSize = 10
        val sortBy = OrderBy(
            field = Optional.present(SortField.distance),
            order = Optional.present(SortOrder.asc)
        )
        val request = ProviderSearchRequest.Builder()
            .searchTerm(searchTerm)
            .organizationTypeFilters(listOf(OrganizationType.Provider))
            .location(latitude, longitude, distance)
            .gender(gender)
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
    }

    private fun setProviderAdapter(searchResult: BWellResult<Provider>) {
        when (searchResult) {
            is BWellResult.SearchResults -> {
                val providersList = searchResult.data
                val adapter = ProvidersListAdapter(providersList)
                adapter.onItemClicked = { selectedList ->
                    binding.providerSearchView.providerSearchView.visibility = View.GONE
                    binding.providerFiltersView.providerFiltersView.visibility = View.GONE
                    binding.organizationsLocationsDataView.organizationsLocationsDataView.visibility = View.VISIBLE
                    var titleText = ""
                    if(selectedList.organization?.size!! > 0)
                    {
                        var organizationAdapter = OrganizationAdapter(requireContext(), selectedList.organization)
                        organizationAdapter.organizationClickListener = this
                        binding.organizationsLocationsDataView.organizationsListView.adapter = organizationAdapter
                        titleText = resources.getString(R.string.select_connection_for)
                    }else{
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

                    }
                       binding.organizationsLocationsDataView.headerText.text = titleText+" "+selectedList.name?.get(0)?.text.toString()+" below:"
                }
                binding.providerSearchView.constraintLayout.visibility = View.GONE
                binding.providerSearchView.providersDataView.providerDataView.visibility = View.VISIBLE
                binding.providerSearchView.providersDataView.resultsText.text = "Search Results ("+providersList?.size+")"
                binding.providerSearchView.providersDataView.rvProviders.layoutManager = LinearLayoutManager(requireContext())
                binding.providerSearchView.providersDataView.rvProviders.adapter = adapter
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
        }
    }
}