package com.bwell.sampleapp.activities.ui.data_connections.labs

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
import com.bwell.sampleapp.databinding.FragmentDataConnectionsLabsBinding
import com.bwell.sampleapp.utils.hideKeyboard
import com.bwell.sampleapp.viewmodel.DataConnectionLabsViewModel
import com.bwell.sampleapp.viewmodel.DataConnectionsLabsViewModelFactory
import com.bwell.search.ProviderSearchQuery
import com.bwell.search.requests.ProviderSearchRequest
import com.bwell.search.type.OrganizationType
import kotlinx.coroutines.launch

class LabsSearchFragment : Fragment(),View.OnClickListener {

    private var _binding: FragmentDataConnectionsLabsBinding? = null
    private lateinit var dataConnectionLabsViewModel: DataConnectionLabsViewModel
    private lateinit var dataConnectionsLabsListAdapter: DataConnectionsLabsListAdapter

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDataConnectionsLabsBinding.inflate(inflater, container, false)
        val root: View = binding.root
        val repository = (activity?.application as? BWellSampleApplication)?.dataConnectionLabsRepository
        dataConnectionLabsViewModel = ViewModelProvider(this, DataConnectionsLabsViewModelFactory(repository))[DataConnectionLabsViewModel::class.java]

        getConnections()
        binding.leftArrowImageView.setOnClickListener(this)
        return root
    }

    private fun getConnections() {
        val searchTerm = ""
        val request = ProviderSearchRequest.Builder()
            .searchTerm(searchTerm)
            .organizationTypeFilters(listOf(OrganizationType.Laboratory))
            .build()

        dataConnectionLabsViewModel.searchConnections(request)

        viewLifecycleOwner.lifecycleScope.launch {
            dataConnectionLabsViewModel.searchResults.collect { searchResult ->
                if (searchResult?.size ?: 0 > 0) {
                    binding.noDataLl.visibility = View.GONE;
                    binding.dataLl.visibility = View.VISIBLE;
                    setDataConnectionLabsAdapter(searchResult)
                }else{
                    binding.noDataLl.visibility = View.VISIBLE;
                    binding.dataLl.visibility = View.GONE;
                }
            }
        }
    }

    private fun addSearchTextListeners() {
        binding.searchView.searchText.addTextChangedListener(object :
            TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {
                dataConnectionLabsViewModel.filterDataConnectionsClinics(charSequence.toString())
                viewLifecycleOwner.lifecycleScope.launch {
                    dataConnectionLabsViewModel.filteredResults.collect { filteredList ->
                        if(filteredList!!.isNotEmpty())
                        {
                            binding.noDataLl.visibility = View.GONE;
                            binding.dataLl.visibility = View.VISIBLE;
                        }else{
                            binding.noDataLl.visibility = View.VISIBLE;
                            binding.dataLl.visibility = View.GONE;
                        }
                        dataConnectionsLabsListAdapter.updateList(filteredList)
                    }
                }
            }

            override fun afterTextChanged(editable: Editable?) {}
        })
    }



    @SuppressLint("SetTextI18n")
    private fun setDataConnectionLabsAdapter(filteredList: List<ProviderSearchQuery.Organization?>?) {
        dataConnectionsLabsListAdapter = DataConnectionsLabsListAdapter(filteredList)
        dataConnectionsLabsListAdapter.onItemClicked = { organization ->
            // Handle item click, perform UI changes here
            binding.searchView.searchText.setText("")
            hideKeyboard(requireContext(),binding.searchView.searchText.windowToken)
            val organizationFragment = OrganizationInfoFragment(organization)
            val transaction = parentFragmentManager.beginTransaction()
            transaction.hide(this@LabsSearchFragment)
            transaction.add(R.id.container_layout, organizationFragment)
            transaction.addToBackStack(null)
            transaction.commit()
        }
        binding.rvDataConnectionsLabs.layoutManager = LinearLayoutManager(requireContext())
        binding.rvDataConnectionsLabs.adapter = dataConnectionsLabsListAdapter
        addSearchTextListeners()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.leftArrowImageView -> {
                parentFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
                val parentFrag: DataConnectionsFragment = this@LabsSearchFragment.parentFragment as DataConnectionsFragment
                parentFrag.showDataConnectionCategories()
            }
        }
    }
}
