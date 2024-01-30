package com.bwell.sampleapp.activities.ui.data_connections

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bwell.common.models.domain.common.Organization
import com.bwell.common.models.domain.data.Connection
import com.bwell.common.models.responses.BWellResult
import com.bwell.connections.requests.ConnectionCreateRequest
import com.bwell.sampleapp.BWellSampleApplication
import com.bwell.sampleapp.R
import com.bwell.sampleapp.activities.ui.data_connections.clinics.ClinicsSearchFragment
import com.bwell.sampleapp.activities.ui.data_connections.labs.LabsSearchFragment
import com.bwell.sampleapp.activities.ui.data_connections.proa.WebFragment
import com.bwell.sampleapp.databinding.FragmentDataConnectionsParentBinding
import com.bwell.sampleapp.model.DataConnectionCategoriesListItems
import com.bwell.sampleapp.viewmodel.DataConnectionsViewModel
import com.bwell.sampleapp.activities.ui.data_connections.providers.ProviderSearchFragment
import com.bwell.sampleapp.viewmodel.DataConnectionsViewModelFactory
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.bwell.common.models.domain.data.DataSource
import com.bwell.common.models.domain.search.Provider
import com.bwell.sampleapp.viewmodel.EntityInfoViewModel


class DataConnectionsFragment : Fragment(), View.OnClickListener,
    DataConnectionsListAdapter.DataConnectionsClickListener {

    private var mBinding: FragmentDataConnectionsParentBinding? = null
    private val binding get() = mBinding!!
    private lateinit var dataConnectionsViewModel: DataConnectionsViewModel
    private lateinit var entityInfoViewModel: EntityInfoViewModel
    private lateinit var connection: Connection
    private lateinit var frameLayoutConnectionStatus: FrameLayout

    @SuppressLint("SuspiciousIndentation")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentDataConnectionsParentBinding.inflate(inflater, container, false)
        val root: View = binding.root
        val repository =
            (activity?.application as? BWellSampleApplication)?.dataConnectionsRepository

        dataConnectionsViewModel = ViewModelProvider(
            this,
            DataConnectionsViewModelFactory(repository)
        )[DataConnectionsViewModel::class.java]

        entityInfoViewModel = ViewModelProvider(this)[EntityInfoViewModel::class.java]

        binding.includeHomeView.header.text = resources.getString(R.string.connect_health_records)
        binding.includeHomeView.subText.text =
            resources.getString(R.string.connect_health_records_sub_txt)
        binding.includeHomeView.btnGetStarted.text = resources.getString(R.string.lets_go)
        binding.includeHomeView.btnGetStarted.setOnClickListener(this)


        checkIfAnyExistingConnections()

        return root
    }

    private fun checkIfAnyExistingConnections() {
        lifecycleScope.launch {
            try {
                dataConnectionsViewModel.getConnectionsAndObserve()
            } catch (e: Exception) {
                Log.d("", "$e.message")
            }
        }

        dataConnectionsViewModel.connectionsList.observe(viewLifecycleOwner) { connectionListItems ->
            if (connectionListItems.isNotEmpty())
                setDataConnectionsAdapter(connectionListItems)
            else
                displayDataConnectionsHomeInfo()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mBinding = null
    }

    private fun setDataConnectionsAdapter(suggestedActivitiesLIst: List<Connection>) {
        val adapter = DataConnectionsListAdapter(suggestedActivitiesLIst)
        adapter.dataConnectionsClickListener = this
        binding.includeDataConnections.dataConnectionFragment.visibility = View.VISIBLE
        binding.includeDataConnectionCategory.dataConnectionFragment.visibility = View.GONE
        binding.includeHomeView.headerView.visibility = View.GONE
        binding.includeDataConnections.rvSuggestedDataConnections.layoutManager =
            LinearLayoutManager(requireContext())
        binding.includeDataConnections.rvSuggestedDataConnections.adapter = adapter
        binding.includeDataConnections.addConnectionsView.setOnClickListener(this)
    }

    private fun setDataConnectionsCategoryAdapter(suggestedActivitiesLIst: List<DataConnectionCategoriesListItems>) {
        val adapter = DataConnectionsCategoriesListAdapter(suggestedActivitiesLIst)
        adapter.onItemClicked = { selectedDataConnection ->
            when (selectedDataConnection.connectionCategoryName) {
                resources.getString(R.string.data_connection_category_clinics) -> {
                    binding.includeDataConnectionCategory.dataConnectionFragment.visibility =
                        View.GONE
                    val clinicsFragment = ClinicsSearchFragment()
                    binding.progressBar.visibility = View.VISIBLE
                    val transaction = childFragmentManager.beginTransaction()
                    binding.containerLayout.visibility = View.VISIBLE
                    transaction.replace(R.id.container_layout, clinicsFragment)
                    transaction.addToBackStack(null)
                    transaction.commit()
                    binding.progressBar.visibility = View.INVISIBLE
                }

                resources.getString(R.string.data_connection_category_providers) -> {
                    binding.includeDataConnectionCategory.dataConnectionFragment.visibility =
                        View.GONE
                    val providersFragment = ProviderSearchFragment()
                    val transaction = childFragmentManager.beginTransaction()
                    binding.containerLayout.visibility = View.VISIBLE
                    transaction.replace(R.id.container_layout, providersFragment)
                    transaction.addToBackStack(null)
                    transaction.commit()
                }

                resources.getString(R.string.data_connection_category_lab) -> {
                    binding.includeDataConnectionCategory.dataConnectionFragment.visibility =
                        View.GONE
                    val labsSearchFragment = LabsSearchFragment()
                    val transaction = childFragmentManager.beginTransaction()
                    binding.containerLayout.visibility = View.VISIBLE
                    transaction.replace(R.id.container_layout, labsSearchFragment)
                    transaction.addToBackStack(null)
                    transaction.commit()
                }
            }
        }
        binding.includeDataConnectionCategory.dataConnectionFragment.visibility = View.VISIBLE
        binding.includeDataConnections.dataConnectionFragment.visibility = View.GONE
        binding.includeDataConnectionCategory.rvSuggestedDataConnectionCategories.layoutManager =
            LinearLayoutManager(requireContext())
        binding.includeDataConnectionCategory.rvSuggestedDataConnectionCategories.adapter = adapter
    }

    private fun displayDataConnectionsHomeInfo() {
        binding.includeDataConnectionCategory.dataConnectionFragment.visibility = View.GONE
        binding.includeDataConnections.dataConnectionFragment.visibility = View.GONE
        binding.includeHomeView.headerView.visibility = View.VISIBLE
    }

    private fun displayDataConnectionsCategoriesList() {
        binding.includeDataConnectionCategory.dataConnectionFragment.visibility = View.VISIBLE
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.btn_get_started -> {
                showDataConnectionsCategories()
            }

            R.id.addConnectionsView -> {
                showDataConnectionsCategories()
            }

            R.id.cancel_txt -> {
                displayDataConnectionsCategoriesList()
            }

            R.id.frameLayoutProceed -> {
                lifecycleScope.launch {
                    val connectionRequest = ConnectionCreateRequest.Builder()
                        .connectionId("connection_id")
                        .username("username")
                        .password("password")
                        .build()
                    dataConnectionsViewModel.createConnection(connectionRequest)
                }
                viewLifecycleOwner.lifecycleScope.launch {
                    dataConnectionsViewModel.createConnectionData.collect { connectionOutcome ->
                        connectionOutcome?.let {

                        }
                    }
                }
            }

            R.id.frameLayoutDisconnect -> {
                binding.includeDataConnections.frameLayoutDisconnect.visibility = View.GONE
                lifecycleScope.launch {
                    try {
                        val connectionId = connection.id
                        dataConnectionsViewModel.disconnectConnection(connectionId)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                viewLifecycleOwner.lifecycleScope.launch {
                    dataConnectionsViewModel.disconnectConnectionData.collect { disconnectOutcome ->
                        disconnectOutcome?.let {
                            if (disconnectOutcome.success()) {
                                val drawable = ContextCompat.getDrawable(
                                    requireContext(),
                                    R.drawable.rounded_rectangle_grey
                                )
                                frameLayoutConnectionStatus.background = drawable
                                if (frameLayoutConnectionStatus.childCount > 0 && frameLayoutConnectionStatus.getChildAt(
                                        0
                                    ) is TextView
                                ) {
                                    val textView =
                                        frameLayoutConnectionStatus.getChildAt(0) as TextView
                                    textView.text = resources.getString(R.string.disconnected)
                                    textView.setTextColor(
                                        resources.getColor(
                                            R.color.black,
                                            context?.theme
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun showDataConnectionsCategories() {
        binding.includeDataConnectionCategory.dataConnectionFragment.visibility = View.VISIBLE
        binding.includeDataConnections.dataConnectionFragment.visibility = View.GONE
        binding.includeHomeView.headerView.visibility = View.GONE
        dataConnectionsViewModel.suggestedDataConnectionsCategories.observe(viewLifecycleOwner) {
            setDataConnectionsCategoryAdapter(it.suggestedDataConnectionsCategoriesList)
        }
    }

    @Suppress("LocalVariableName")
    override fun onChangeStatusClicked(
        connection: Connection,
        parent_view: ViewGroup,
        status_change_view: View,
        frameLayoutConnectionStatus: FrameLayout
    ) {
        Log.d("onChangeStatusClicked", "onChangeStatusClicked")
        binding.includeDataConnections.frameLayoutDisconnect.visibility = View.VISIBLE
        binding.includeDataConnections.frameLayoutDisconnect.y =
            binding.includeDataConnections.rvSuggestedDataConnections.y + parent_view.y + status_change_view.y + status_change_view.height.toFloat()
        binding.includeDataConnections.frameLayoutDisconnect.setOnClickListener(this)
        this.connection = connection
        this.frameLayoutConnectionStatus = frameLayoutConnectionStatus
    }

    fun showDataConnectionCategories() {
        binding.includeDataConnectionCategory.dataConnectionFragment.visibility = View.VISIBLE
    }
}