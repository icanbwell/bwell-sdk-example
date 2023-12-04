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
import com.bwell.sampleapp.BWellSampleApplication
import com.bwell.sampleapp.R
import com.bwell.sampleapp.activities.ui.data_connections.DataConnectionsFragment
import com.bwell.sampleapp.activities.ui.data_connections.providers.OrganizationInfoFragment
import com.bwell.sampleapp.databinding.FragmentDataConnectionsClinicsBinding
import com.bwell.sampleapp.utils.hideKeyboard
import com.bwell.sampleapp.viewmodel.ClinicsViewModel
import com.bwell.sampleapp.viewmodel.ClinicsViewModelFactory
import com.bwell.search.ProviderSearchQuery
import com.bwell.search.provider.requests.ProviderSearchRequest
import com.bwell.search.type.OrganizationType
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

        getConnections()
        binding.leftArrowImageView.setOnClickListener(this)
        return root
    }

    private fun getConnections() {
        val searchTerm = ""
        val request = ProviderSearchRequest.Builder()
            .searchTerm(searchTerm)
            .organizationTypeFilters(listOf(OrganizationType.Provider))
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
                // Update the filtered list when text changes
                val numberOfCharacters = charSequence?.length ?: 0
                if(numberOfCharacters > 2)
                {
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
                }else{
                    if (charSequence?.isNotEmpty() == true)
                        displayClinicsBeforeSearchView()
                }
            }

            override fun afterTextChanged(editable: Editable?) {}
        })
    }

    private fun displayClinicsBeforeSearchView() {
        binding.clinicsBeforeSearchBodyView.clinicsBeforeSearchBodyView.visibility = View.VISIBLE;
        binding.clinicsAfterSearchNoDataBodyView.clinicsAfterSearchNoDataBodyView.visibility = View.GONE;
        binding.clinicsAfterSearchDataBodyView.clinicsAfterSearchDataBodyView.visibility = View.GONE;
    }

    @SuppressLint("SetTextI18n")
    private fun setDataConnectionClinicsAdapter(filteredList: List<ProviderSearchQuery.Organization?>?) {
        dataConnectionClinicsAdapter = DataConnectionsClinicsListAdapter(filteredList)
        dataConnectionClinicsAdapter.onItemClicked = { organization ->
            // Handle item click, perform UI changes here
            hideKeyboard(requireContext(),binding.searchView.searchText.windowToken)
            val organizationFragment = OrganizationInfoFragment(organization)
            val transaction = parentFragmentManager.beginTransaction()
            transaction.hide(this@ClinicsSearchFragment)
            transaction.add(R.id.container_layout, organizationFragment)
            transaction.addToBackStack(null)
            transaction.commit()
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

    private fun displayClinicsAfterDataSearchView(resultCount:Int) {
        binding.clinicsBeforeSearchBodyView.clinicsBeforeSearchBodyView.visibility = View.GONE;
        binding.clinicsAfterSearchNoDataBodyView.clinicsAfterSearchNoDataBodyView.visibility = View.GONE;
        binding.clinicsAfterSearchDataBodyView.clinicsAfterSearchDataBodyView.visibility = View.VISIBLE;
        binding.clinicsAfterSearchDataBodyView.resultsText.setText("Results ("+resultCount+")");
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
