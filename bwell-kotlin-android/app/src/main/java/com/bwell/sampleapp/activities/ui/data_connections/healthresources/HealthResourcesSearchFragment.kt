package com.bwell.sampleapp.activities.ui.data_connections.healthresources

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bwell.common.models.domain.common.enums.SortOrder
import com.bwell.common.models.domain.search.HealthResource
import com.bwell.common.models.domain.search.enums.HealthResourceSortField
import com.bwell.common.models.domain.search.enums.SearchResultType
import com.bwell.common.models.responses.BWellResult
import com.bwell.sampleapp.BWellSampleApplication
import com.bwell.sampleapp.R
import com.bwell.sampleapp.activities.ui.data_connections.DataConnectionsFragment
import com.bwell.sampleapp.databinding.FragmentHealthResourcesSearchBinding
import com.bwell.sampleapp.utils.hideKeyboard
import com.bwell.sampleapp.viewmodel.HealthResourcesViewModel
import com.bwell.sampleapp.viewmodel.HealthResourcesViewModelFactory
import com.bwell.search.requests.healthresource.HealthResourceSearchRequest
import kotlinx.coroutines.launch

class HealthResourcesSearchFragment : Fragment() {

    private var _binding: FragmentHealthResourcesSearchBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: HealthResourcesViewModel
    private lateinit var adapter: HealthResourcesListAdapter

    private val sortOptions = listOf("Distance", "Relevance", "Content", "Data Source Rank")
    private val providerTypeOptions = listOf("All", "Practitioner", "Practice", "Insurance", "Laboratory", "Pharmacy")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHealthResourcesSearchBinding.inflate(inflater, container, false)
        val repository = (activity?.application as? BWellSampleApplication)?.healthResourcesRepository
        viewModel = ViewModelProvider(
            this,
            HealthResourcesViewModelFactory(repository)
        )[HealthResourcesViewModel::class.java]

        setupSpinners()
        setupListeners()
        setupCollectors()
        performSearch(null)

        return binding.root
    }

    private var hasSearched = false

    private fun setupCollectors() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.searchResults.collect { result ->
                if (!hasSearched) return@collect
                binding.progressBar.visibility = View.GONE
                if (result != null) {
                    handleSearchResult(result)
                } else {
                    binding.statusText.text = "Search failed"
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.searchError.collect { error ->
                if (error != null) {
                    binding.progressBar.visibility = View.GONE
                    binding.statusText.text = error
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.filteredResults.collect { filteredList ->
                if (filteredList != null && ::adapter.isInitialized) {
                    adapter.updateList(filteredList)
                    binding.statusText.text = "Filtered: ${filteredList.size} results"
                }
            }
        }
    }

    private fun setupSpinners() {
        val ctx = requireContext()
        binding.spinnerSort.adapter = ArrayAdapter(ctx, android.R.layout.simple_spinner_dropdown_item, sortOptions)
        binding.spinnerSort.setSelection(0)

        binding.spinnerProviderType.adapter = ArrayAdapter(ctx, android.R.layout.simple_spinner_dropdown_item, providerTypeOptions)
        binding.spinnerProviderType.setSelection(0)
    }

    private fun setupListeners() {
        binding.leftArrowImageView.setOnClickListener {
            parentFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
            val parentFrag = this@HealthResourcesSearchFragment.parentFragment as? DataConnectionsFragment
            parentFrag?.showDataConnectionCategories()
        }

        binding.searchButton.setOnClickListener {
            val term = binding.searchView.searchText.text.toString().trim()
            hideKeyboard(requireContext(), binding.searchView.searchText.windowToken)
            performSearch(term.ifEmpty { null })
        }

        binding.searchView.searchText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (::adapter.isInitialized) {
                    viewModel.filterResults(s?.toString() ?: "")
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.cbUseLocation.setOnCheckedChangeListener { _, isChecked ->
            binding.locationInputs.visibility = if (isChecked) View.VISIBLE else View.GONE
        }
    }

    private fun performSearch(term: String?) {
        binding.progressBar.visibility = View.VISIBLE
        binding.statusText.text = "Searching..."

        val builder = HealthResourceSearchRequest.Builder()
        builder.page(0)

        if (!term.isNullOrEmpty()) {
            builder.searchTerm(term)
        }

        if (binding.cbProaOnly.isChecked) {
            builder.includePopulatedPROAonly()
        }

        val sortField = when (binding.spinnerSort.selectedItemPosition) {
            0 -> HealthResourceSortField.DISTANCE
            1 -> HealthResourceSortField.RELEVANCE
            2 -> HealthResourceSortField.CONTENT
            3 -> HealthResourceSortField.DATA_SOURCE_RANK
            else -> HealthResourceSortField.RELEVANCE
        }
        builder.sortBy(sortField, SortOrder.ASC)

        if (binding.cbUseLocation.isChecked) {
            val latText = binding.etLatitude.text.toString()
            val lonText = binding.etLongitude.text.toString()
            val lat = latText.toDoubleOrNull()
            val lon = lonText.toDoubleOrNull()

            if (lat == null && latText.isNotEmpty() || lon == null && lonText.isNotEmpty()) {
                Toast.makeText(requireContext(), "Invalid coordinates — using default location", Toast.LENGTH_SHORT).show()
            }

            builder.location(lat ?: 39.2848102, lon ?: -76.702898)
        }

        val providerType: List<SearchResultType>? = when (binding.spinnerProviderType.selectedItemPosition) {
            1 -> listOf(SearchResultType.PRACTITIONER)
            2 -> listOf(SearchResultType.PRACTICE)
            3 -> listOf(SearchResultType.INSURANCE)
            4 -> listOf(SearchResultType.LABORATORY)
            5 -> listOf(SearchResultType.PHARMACY)
            else -> null
        }
        if (providerType != null) {
            builder.searchResultType(providerType)
        }

        val request = builder.build()
        hasSearched = true
        viewModel.searchHealthResources(request)
    }

    private fun handleSearchResult(result: BWellResult<HealthResource>) {
        when (result) {
            is BWellResult.SearchResults -> {
                val resources = result.data
                val total = resources?.size ?: 0
                binding.statusText.text = "Results: $total"

                if (::adapter.isInitialized) {
                    adapter.updateList(resources)
                } else {
                    adapter = HealthResourcesListAdapter(resources)
                    adapter.onItemClicked = { resource ->
                        logResourceDetails(resource)
                    }
                    binding.rvResults.layoutManager = LinearLayoutManager(requireContext())
                    binding.rvResults.adapter = adapter
                }

                Log.i(TAG, "=== SHR Response ===")
                Log.i(TAG, "Total results: $total")
                result.filterValues?.let { fv ->
                    Log.i(TAG, "Filter values (${fv.size} fields):")
                    fv.forEach { filter ->
                        Log.i(TAG, "  ${filter.field}: ${filter.values?.take(5)?.joinToString { "${it.value}(${it.count})" }}")
                    }
                }
                resources?.firstOrNull()?.let { first ->
                    Log.i(TAG, "First: id=${first.id}, content=${first.content}, type=${first.type}")
                    Log.i(TAG, "  specialty: ${first.specialty?.mapNotNull { it.display }}")
                    Log.i(TAG, "  locations: ${first.providerLocation?.size ?: 0}")
                    Log.i(TAG, "  bookable: ${first.bookable}")
                    Log.i(TAG, "  gender: ${first.gender}")
                    Log.i(TAG, "  npi: ${first.npi}")
                    Log.i(TAG, "  endpoint: ${first.endpoint?.mapNotNull { it.name }}")
                    first.providerLocation?.firstOrNull()?.let { loc ->
                        Log.i(TAG, "  loc[0].name: ${loc.name}")
                        Log.i(TAG, "  loc[0].description: ${loc.description}")
                        Log.i(TAG, "  loc[0].identifier: ${loc.identifier?.map { "${it.system}|${it.value}" }}")
                        Log.i(TAG, "  loc[0].alias: ${loc.alias}")
                        Log.i(TAG, "  loc[0].distanceInMiles: ${loc.distanceInMiles}")
                        Log.i(TAG, "  loc[0].hoursOfOperation: ${loc.hoursOfOperation?.map { "${it.daysOfWeek} ${it.openingTime}-${it.closingTime}" }}")
                        Log.i(TAG, "  loc[0].telecom: ${loc.telecom?.map { "${it.system}:${it.value}" }}")
                    }
                }
                Log.i(TAG, "====================")
            }
            is BWellResult.SingleResource -> {
                binding.statusText.text = "Single resource returned"
            }
            is BWellResult.ResourceCollection -> {
                binding.statusText.text = "Collection returned"
            }
        }
    }

    private fun logResourceDetails(resource: HealthResource) {
        Log.i(TAG, "--- Selected Resource ---")
        Log.i(TAG, "id: ${resource.id}")
        Log.i(TAG, "content: ${resource.content}")
        Log.i(TAG, "type: ${resource.type}")
        Log.i(TAG, "gender: ${resource.gender}")
        Log.i(TAG, "npi: ${resource.npi}")
        Log.i(TAG, "specialty: ${resource.specialty?.mapNotNull { it.display }}")
        Log.i(TAG, "bookable: ${resource.bookable}")
        Log.i(TAG, "endpoints: ${resource.endpoint?.mapNotNull { it.name }}")
        Log.i(TAG, "organizations: ${resource.organization?.size ?: 0}")
        Log.i(TAG, "--- Locations (${resource.providerLocation?.size ?: 0}) ---")
        resource.providerLocation?.forEachIndexed { i, loc ->
            Log.i(TAG, "  [$i] name: ${loc.name}")
            Log.i(TAG, "  [$i] description: ${loc.description}")
            Log.i(TAG, "  [$i] identifier: ${loc.identifier?.map { "${it.system}|${it.value}" }}")
            Log.i(TAG, "  [$i] alias: ${loc.alias}")
            Log.i(TAG, "  [$i] address: ${loc.address}")
            Log.i(TAG, "  [$i] distanceInMiles: ${loc.distanceInMiles}")
            Log.i(TAG, "  [$i] telecom: ${loc.telecom?.map { "${it.system}:${it.value}" }}")
            Log.i(TAG, "  [$i] hoursOfOperation: ${loc.hoursOfOperation?.map { "${it.daysOfWeek} ${it.openingTime}-${it.closingTime}" }}")
            Log.i(TAG, "  [$i] scheduling: id=${loc.scheduling?.identifier?.mapNotNull { it?.let { id -> "${id.system}|${id.value}" } }}, bookable=${loc.scheduling?.bookable}")
        }
        Log.i(TAG, "-------------------------")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val TAG = "HealthResourcesSearch"
    }
}
