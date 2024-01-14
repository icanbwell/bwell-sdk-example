package com.bwell.sampleapp.activities.ui.data_connections.clinics

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bwell.common.models.domain.common.enums.SortOrder
import com.bwell.common.models.domain.search.Provider
import com.bwell.common.models.responses.BWellResult
import com.bwell.sampleapp.BWellSampleApplication
import com.bwell.sampleapp.R
import com.bwell.sampleapp.activities.ui.data_connections.DataConnectionsFragment
import com.bwell.sampleapp.activities.ui.data_connections.providers.OrganizationInfoFragment
import com.bwell.sampleapp.databinding.FragmentDataConnectionsClinicsBinding
import com.bwell.sampleapp.utils.hideKeyboard
import com.bwell.sampleapp.viewmodel.ClinicsViewModel
import com.bwell.sampleapp.viewmodel.ClinicsViewModelFactory
import com.bwell.search.requests.provider.ProviderSearchRequest
import com.bwell.common.models.domain.search.enums.OrganizationType
import com.bwell.common.models.domain.search.enums.SortField
import kotlinx.coroutines.launch

class ClinicsSearchFragment : Fragment(),View.OnClickListener {

    private var _binding: FragmentDataConnectionsClinicsBinding? = null
    private lateinit var clinicsViewModel: ClinicsViewModel
    private lateinit var dataConnectionClinicsAdapter: DataConnectionsClinicsListAdapter
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDataConnectionsClinicsBinding.inflate(inflater, container, false)
        val root: View = binding.root
        val repository = (activity?.application as? BWellSampleApplication)?.clinicsRepository
        clinicsViewModel = ViewModelProvider(this, ClinicsViewModelFactory(repository))[ClinicsViewModel::class.java]
        binding.leftArrowImageView.setOnClickListener(this)
        getConnections()
        return root
    }

    private fun getConnections() {
        val searchTerm = ""
        val request = ProviderSearchRequest.Builder()
            .searchTerm(searchTerm)
            .organizationTypeFilters(listOf(OrganizationType.PROVIDER))
            .sortBy(SortField.CONTENT, SortOrder.ASC)
            .page(1)
            .pageSize(100)
            .build()
        clinicsViewModel.searchConnections(request)
        viewLifecycleOwner.lifecycleScope.launch {
            clinicsViewModel.searchResults.collect { searchResult ->
                if (searchResult != null) {
                    setDataConnectionClinicsAdapter(searchResult)
                }
            }
        }
    }

    private fun addSearchTextListeners() {
        binding.searchView.searchText.addTextChangedListener(object :
            TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {
                clinicsViewModel.filterDataConnectionsClinics(charSequence.toString())
                viewLifecycleOwner.lifecycleScope.launch {
                    clinicsViewModel.filteredResults.collect { filteredList ->
                        if(filteredList!!.isNotEmpty())
                        {
                            displayClinicsAfterDataSearchView(filteredList.size)
                        }else{
                            displayClinicsAfterNoDataSearchView()
                        }
                        dataConnectionClinicsAdapter.updateList(filteredList)
                    }
                }
            }
            override fun afterTextChanged(editable: Editable?) {}
        })
    }

    @SuppressLint("SetTextI18n")
    private fun setDataConnectionClinicsAdapter(searchResult: BWellResult<Provider>) {
        when (searchResult) {
            is BWellResult.SearchResults -> {
                val connectionsList = searchResult.data
                dataConnectionClinicsAdapter = DataConnectionsClinicsListAdapter(connectionsList)
                displayClinicsAfterDataSearchView(connectionsList?.size ?: 0)
            }
            else -> {}
        }
        dataConnectionClinicsAdapter.onItemClicked = { selectedList ->
            hideKeyboard(requireContext(),binding.searchView.searchText.windowToken)
            if((selectedList?.endpoint?.size ?: 0) > 0)
            {
                val organizationFragment = OrganizationInfoFragment<Provider?>(selectedList)
                val transaction = parentFragmentManager.beginTransaction()
                transaction.hide(this@ClinicsSearchFragment)
                transaction.add(R.id.container_layout, organizationFragment)
                transaction.addToBackStack(null)
                transaction.commit()
            }
        }
        binding.clinicsAfterSearchDataBodyView.rvClinics.layoutManager = LinearLayoutManager(requireContext())
        binding.clinicsAfterSearchDataBodyView.rvClinics.adapter = dataConnectionClinicsAdapter
        addSearchTextListeners()
    }

    private fun displayClinicsAfterNoDataSearchView() {
        binding.clinicsBeforeSearchBodyView.clinicsBeforeSearchBodyView.visibility = View.GONE;
        binding.clinicsAfterSearchNoDataBodyView.clinicsAfterSearchNoDataBodyView.visibility = View.VISIBLE;
        binding.clinicsAfterSearchDataBodyView.clinicsAfterSearchDataBodyView.visibility = View.GONE;
    }

    @SuppressLint("SetTextI18n")
    private fun displayClinicsAfterDataSearchView(resultCount:Int) {
        binding.clinicsBeforeSearchBodyView.clinicsBeforeSearchBodyView.visibility = View.GONE;
        binding.clinicsAfterSearchNoDataBodyView.clinicsAfterSearchNoDataBodyView.visibility = View.GONE;
        binding.clinicsAfterSearchDataBodyView.clinicsAfterSearchDataBodyView.visibility = View.VISIBLE;
        binding.clinicsAfterSearchDataBodyView.resultsText.text = "Results ($resultCount)";
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.leftArrowImageView -> {
                parentFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
                val parentFrag: DataConnectionsFragment = this@ClinicsSearchFragment.getParentFragment() as DataConnectionsFragment
                parentFrag.showDataConnectionCategories()
            }
        }
    }
}
