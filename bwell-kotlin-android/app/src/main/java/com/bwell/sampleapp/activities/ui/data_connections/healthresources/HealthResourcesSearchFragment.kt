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
import com.bwell.common.models.domain.common.Coding
import com.bwell.common.models.domain.common.enums.Gender
import com.bwell.common.models.domain.common.enums.SortOrder
import com.bwell.common.models.domain.healthdata.healthsummary.careteam.CareTeamMutationResult
import com.bwell.common.models.domain.search.HealthResource
import com.bwell.common.models.domain.search.HealthResourceSearchFilters
import com.bwell.common.models.domain.search.HealthResourceSearchLocation
import com.bwell.common.models.domain.search.enums.DistanceUnit
import com.bwell.common.models.domain.search.enums.FilterField
import com.bwell.common.models.domain.search.enums.HealthResourceSortField
import com.bwell.common.models.domain.search.enums.PatientAcceptance
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

    private val sortOptions = listOf("Distance", "Relevance", "Name", "Next Available")
    private val genderOptions = listOf("Any", "Male", "Female")
    private val patientAcceptanceOptions = listOf("Any", "New Patients", "Existing Patients")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHealthResourcesSearchBinding.inflate(inflater, container, false)
        val app = activity?.application as? BWellSampleApplication
        val repository = app?.healthResourcesRepository
        val careTeamRepository = app?.careTeamMembersRepository
        viewModel = ViewModelProvider(
            this,
            HealthResourcesViewModelFactory(repository, careTeamRepository)
        )[HealthResourcesViewModel::class.java]

        setupSpinners()
        setupListeners()
        performSearch(null)

        return binding.root
    }

    private fun setupSpinners() {
        val ctx = requireContext()
        binding.spinnerSort.adapter = ArrayAdapter(ctx, android.R.layout.simple_spinner_dropdown_item, sortOptions)
        binding.spinnerSort.setSelection(0)

        binding.spinnerGender.adapter = ArrayAdapter(ctx, android.R.layout.simple_spinner_dropdown_item, genderOptions)
        binding.spinnerGender.setSelection(0)

        binding.spinnerPatientAcceptance.adapter = ArrayAdapter(ctx, android.R.layout.simple_spinner_dropdown_item, patientAcceptanceOptions)
        binding.spinnerPatientAcceptance.setSelection(0)
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

        observeCareTeamMutation()
    }

    private fun observeCareTeamMutation() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.careTeamMutation.collect { result ->
                if (result == null) return@collect
                if (result.success()) {
                    val data = (result as? BWellResult.SingleResource<CareTeamMutationResult>)?.data
                    Toast.makeText(requireContext(), "Added to care team (ID: ${data?.id ?: "N/A"})", Toast.LENGTH_SHORT).show()
                    Log.i(TAG, "Care team mutation success: id=${data?.id}")
                } else {
                    Toast.makeText(requireContext(), "Failed to add: ${result.error?.message()}", Toast.LENGTH_SHORT).show()
                    Log.e(TAG, "Care team mutation error: ${result.error?.message()}")
                }
            }
        }
    }

    private fun performSearch(term: String?) {
        binding.progressBar.visibility = View.VISIBLE
        binding.statusText.text = "Searching..."

        val builder = HealthResourceSearchRequest.Builder()
        builder.page(0)
        builder.pageSize(100)

        if (!term.isNullOrEmpty()) {
            builder.searchTerm(term)
        }

        if (binding.cbProaOnly.isChecked) {
            builder.includePopulatedPROAonly()
        }

        val sortField = when (binding.spinnerSort.selectedItemPosition) {
            0 -> HealthResourceSortField.DISTANCE
            1 -> HealthResourceSortField.RELEVANCE
            2 -> HealthResourceSortField.NAME
            3 -> HealthResourceSortField.NEXT_AVAILABLE
            else -> HealthResourceSortField.RELEVANCE
        }
        builder.sortBy(sortField, SortOrder.ASC)

        if (binding.cbUseLocation.isChecked) {
            val lat = binding.etLatitude.text.toString().toDoubleOrNull() ?: 39.2848102
            val lon = binding.etLongitude.text.toString().toDoubleOrNull() ?: -76.702898
            val radius = binding.etRadius.text.toString().toDoubleOrNull()

            val loc = HealthResourceSearchLocation(
                lat = lat,
                lon = lon,
                radius = radius,
                radiusUnit = if (radius != null) DistanceUnit.MILES else null
            )
            builder.location(loc)
        }

        val gender = when (binding.spinnerGender.selectedItemPosition) {
            1 -> Gender.MALE
            2 -> Gender.FEMALE
            else -> null
        }

        val patientAcceptance = when (binding.spinnerPatientAcceptance.selectedItemPosition) {
            1 -> listOf(PatientAcceptance.NEW_PATIENTS)
            2 -> listOf(PatientAcceptance.EXISTING_PATIENTS)
            else -> null
        }

        val specialtyCodes: List<Coding>? = binding.etSpecialty.text.toString().trim().let { text ->
            if (text.isEmpty()) null
            else text.split(",").map { code ->
                Coding(system = "specialty", code = code.trim())
            }
        }

        val searchFilters = HealthResourceSearchFilters(
            gender = gender,
            includeInactive = if (binding.cbIncludeInactive.isChecked) true else null,
            specialty = specialtyCodes,
            patientAcceptance = patientAcceptance
        )
        builder.filters(searchFilters)

        val requestedFilterValues = mutableListOf<FilterField>()
        if (binding.cbFilterSpecialty.isChecked) requestedFilterValues.add(FilterField.SPECIALTY)
        if (binding.cbFilterCommunication.isChecked) requestedFilterValues.add(FilterField.COMMUNICATION)
        if (binding.cbFilterInsurance.isChecked) requestedFilterValues.add(FilterField.INSURANCE_PLAN)
        if (requestedFilterValues.isNotEmpty()) {
            builder.filterValues(requestedFilterValues)
        }

        val request = builder.build()
        viewModel.searchHealthResources(request)

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.searchResults.collect { result ->
                if (result != null) {
                    binding.progressBar.visibility = View.GONE
                    handleSearchResult(result)
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

    private fun handleSearchResult(result: BWellResult<HealthResource>) {
        when (result) {
            is BWellResult.SearchResults -> {
                val resources = result.data
                val total = resources?.size ?: 0
                binding.statusText.text = "Results: $total"

                adapter = HealthResourcesListAdapter(resources)
                adapter.onItemClicked = { resource ->
                    logResourceDetails(resource)
                }
                adapter.onAddToCareTeamClicked = { resource ->
                    val id = resource.id ?: return@onAddToCareTeamClicked
                    val type = resource.type?.name ?: "Practitioner"
                    viewModel.addCareTeamMember("$type/$id", type, resource.content)
                }
                binding.rvResults.layoutManager = LinearLayoutManager(requireContext())
                binding.rvResults.adapter = adapter

                Log.i(TAG, "=== SHR Response ===")
                Log.i(TAG, "Total results: $total")
                resources?.firstOrNull()?.let { first ->
                    Log.i(TAG, "First: id=${first.id}, content=${first.content}, type=${first.type}")
                    Log.i(TAG, "  specialty: ${first.specialty?.mapNotNull { it.display }}")
                    Log.i(TAG, "  locations: ${first.providerLocation?.size ?: 0}")
                    Log.i(TAG, "  score: ${first.score}")
                    Log.i(TAG, "  acceptingNew: ${first.acceptingNewPatients}")
                    Log.i(TAG, "  bookable: ${first.bookable}")
                    Log.i(TAG, "  virtualCare: ${first.isVirtualCare}")
                    Log.i(TAG, "  communication: ${first.communication?.mapNotNull { it.text }}")
                    Log.i(TAG, "  insurancePlan: ${first.insurancePlan}")
                    Log.i(TAG, "  partOf: ${first.partOf}")
                    Log.i(TAG, "  reviewScore: ${first.reviewScore}")
                    Log.i(TAG, "  acceptedAgeRanges: ${first.acceptedAgeRanges}")
                    Log.i(TAG, "  endpoint: ${first.endpoint?.mapNotNull { it.name }}")
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
        Log.i(TAG, "score: ${resource.score}")
        Log.i(TAG, "scores: ${resource.scores}")
        Log.i(TAG, "specialty: ${resource.specialty?.mapNotNull { it.display }}")
        Log.i(TAG, "communication: ${resource.communication?.mapNotNull { it.text }}")
        Log.i(TAG, "acceptingNew: ${resource.acceptingNewPatients}")
        Log.i(TAG, "acceptedAgeRanges: ${resource.acceptedAgeRanges}")
        Log.i(TAG, "bookable: ${resource.bookable}")
        Log.i(TAG, "virtualCare: ${resource.isVirtualCare}")
        Log.i(TAG, "insurancePlan: ${resource.insurancePlan}")
        Log.i(TAG, "partOf: ${resource.partOf}")
        Log.i(TAG, "reviewScore: ${resource.reviewScore}")
        Log.i(TAG, "nextAvailableSlot: ${resource.nextAvailableSlot}")
        Log.i(TAG, "organizations: ${resource.organization?.size ?: 0}")
        Log.i(TAG, "locations: ${resource.providerLocation?.size ?: 0}")
        Log.i(TAG, "endpoints: ${resource.endpoint?.mapNotNull { it.name }}")
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
