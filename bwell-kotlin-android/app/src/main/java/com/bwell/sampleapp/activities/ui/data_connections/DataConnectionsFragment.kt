package com.bwell.sampleapp.activities.ui.data_connections

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bwell.sampleapp.BWellSampleApplication
import com.bwell.sampleapp.R
import com.bwell.sampleapp.databinding.FragmentDataConnectionsParentBinding
import com.bwell.sampleapp.model.DataConnectionCategoriesListItems
import com.bwell.sampleapp.model.DataConnectionListItems
import com.bwell.sampleapp.model.DataConnectionsClinicsListItems
import com.bwell.sampleapp.viewmodel.DataConnectionsViewModel
import com.bwell.sampleapp.viewmodel.SharedViewModelFactory
import com.bwell.sampleapp.activities.ui.popup.PopupFragment

class DataConnectionsFragment : Fragment(), View.OnClickListener, PopupFragment.PopupListener {

    private var _binding: FragmentDataConnectionsParentBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var dataConnectionsViewModel: DataConnectionsViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDataConnectionsParentBinding.inflate(inflater, container, false)
        val root: View = binding.root
        val repository = (activity?.application as? BWellSampleApplication)?.bWellRepository

        dataConnectionsViewModel = ViewModelProvider(this, SharedViewModelFactory(repository))[DataConnectionsViewModel::class.java]

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
            } else {
                val drawable = ContextCompat.getDrawable(requireContext(), R.drawable.rounded_rectangle_grey)
                binding.clinicInfoView.frameLayoutProceed.background = drawable
                binding.clinicInfoView.frameLayoutProceed.setOnClickListener(null)
            }
        }

        displayDataConnectionsHomeInfo()

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setDataConnectionClinicsAdapter(dataConnectionsList: List<DataConnectionsClinicsListItems>) {
        val adapter = DataConnectionsClinicsListAdapter(dataConnectionsList)
        adapter.onItemClicked = { selectedDataConnection ->
            // Handle item click, perform UI changes here
            binding.includeDataConnectionsClinics.searchView.searchText.setText("")
            displayIndividualClinicInfo()
            binding.clinicInfoView.clinicNametxt.setText(resources.getString(R.string.connect_to)+" "+selectedDataConnection.clinicName)
            binding.clinicInfoView.clinicDiscriptionTxt.setText(selectedDataConnection.clinicName+" "+resources.getString(R.string.clinic_discription))

        }
        binding.includeDataConnectionsClinics.clinicsAfterSearchDataBodyView.rvClinics.layoutManager = LinearLayoutManager(requireContext())
        binding.includeDataConnectionsClinics.clinicsAfterSearchDataBodyView.rvClinics.adapter = adapter

        // Observe the filtered list and update the adapter
        dataConnectionsViewModel.filteredDataConnectionsClinics.observe(viewLifecycleOwner) {
            if(it.size > 0)
            {
                displayClinicsAfterDataSearchView(it.size)
            }else{
                displayClinicsAfterNoDataSearchView()
            }
            adapter.updateList(it)
        }
    }

    private fun setDataConnectionsAdapter(suggestedActivitiesLIst: List<DataConnectionListItems>) {
        val adapter = DataConnectionsListAdapter(suggestedActivitiesLIst)
        binding.includeDataConnections.dataConnectionFragment.visibility = View.VISIBLE;
        binding.includeDataConnectionCategory.dataConnectionFragment.visibility = View.GONE;
        binding.includeDataConnections.rvSuggestedDataConnections.layoutManager = LinearLayoutManager(requireContext())
        binding.includeDataConnections.rvSuggestedDataConnections.adapter = adapter
    }

    private fun setDataConnectionsCategoryAdapter(suggestedActivitiesLIst: List<DataConnectionCategoriesListItems>) {
        val adapter = DataConnectionsCategoriesListAdapter(suggestedActivitiesLIst)
        adapter.onItemClicked = { selectedDataConnection ->
            // Handle item click, perform UI changes here
            //displayRelatedDataConnectionsList();
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

    private fun displayRelatedDataConnectionsList() {
        dataConnectionsViewModel.suggestedDataConnections.observe(viewLifecycleOwner) {
            setDataConnectionsAdapter(it.suggestedDataConnectionsList)
        }
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.btn_get_started -> {
                    binding.includeDataConnectionCategory.dataConnectionFragment.visibility = View.VISIBLE;
                    binding.includeHomeView.headerView.visibility = View.GONE;
                    dataConnectionsViewModel.suggestedDataConnectionsCategories.observe(viewLifecycleOwner) {
                        setDataConnectionsCategoryAdapter(it.suggestedDataConnectionsCategoriesList)
                    }
            }
            R.id.cancel_txt -> {
                displayDataConnectionsCategoriesList()
            }
            R.id.frameLayoutProceed -> {
                val popupFragment = PopupFragment()
                popupFragment.setPopupListener(this) // Set the listener
                popupFragment.show(childFragmentManager, "popup")
            }
        }
    }

     override fun onCloseButtonClicked() {
        // Do the action you want when the close button is clicked in the popup
        // For example, perform some action in YourFragment
        // You can access the views or methods of YourFragment here
         displayDataConnectionsCategoriesList()
    }
}