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
import com.bwell.common.models.domain.data.enums.ConnectionCategory
import com.bwell.common.models.domain.data.enums.ConnectionStatus
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
import com.bwell.healthdata.healthsummary.requests.careteam.CareTeamsRequest
import com.bwell.sampleapp.viewmodel.EntityInfoViewModel
import com.bwell.common.models.domain.healthdata.healthsummary.careteam.OrganizationCareTeamParticipantMember
import com.bwell.activity.requests.TasksRequest
import com.bwell.common.models.domain.task.Task


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
        binding.recordLocationStatusTextView.visibility = View.GONE
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
        getRecordLocationStatusTask() // <-- Call the new function

        return root
    }

    private fun getRecordLocationStatusTask() {
        val taskRequest = TasksRequest.Builder()
            .activityType(listOf("network-data-retrieval"))
            .performerType(listOf("system"))
            .build()
        dataConnectionsViewModel.getTasks(taskRequest)
        viewLifecycleOwner.lifecycleScope.launch {
            dataConnectionsViewModel.taskResults.collect { result ->
                val statusTextView = binding.recordLocationStatusTextView
                if (result is BWellResult.ResourceCollection) {
                    val taskList = result.data as? List<Task>
                    if (!taskList.isNullOrEmpty()) {
                        val task = taskList.firstOrNull()
                        val statusString = task?.status?.name ?: "Not Started"
                        statusTextView.text = "Record Location Status: $statusString"
                        statusTextView.visibility = View.VISIBLE
                    } else {
                        statusTextView.visibility = View.GONE
                    }
                } else {
                    statusTextView.visibility = View.GONE
                }
            }
        }
    }

    private fun checkIfAnyExistingConnections() {
        lifecycleScope.launch {
            try {
                dataConnectionsViewModel.getConnectionsAndObserve()
            } catch (e: Exception) {
                Log.d("", "$e.message")
            }
        }

        val buildCombinedList: () -> List<Any> = {
            val connectionListItems = dataConnectionsViewModel.connectionsList.value ?: emptyList()
            val careTeamListItems = dataConnectionsViewModel.careTeamsList.value ?: emptyList()

            val usedConnectionIds = mutableSetOf<String>()
            val combinedList = mutableListOf<Any>()

            careTeamListItems.forEach { careTeam ->
                careTeam.participant?.forEach { participant ->
                    val member = participant.member
                    // Use safe cast for member.name to avoid smart cast error
                    val name = (member as? OrganizationCareTeamParticipantMember)?.name as? String
                    if (name != null) {
                        val alias = (member as? OrganizationCareTeamParticipantMember)?.alias
                        val status = "NEEDS ATTENTION"
                        combinedList.add(
                            OrganizationCareTeamParticipantMemberDisplay(
                                name = name,
                                status = status,
                                alias = alias
                            )
                        )
                    }
                }
            }

            connectionListItems.forEach { connection ->
                if (!usedConnectionIds.contains(connection.id)) {
                    combinedList.add(connection)
                }
            }

            combinedList
        }

        dataConnectionsViewModel.connectionsList.observe(viewLifecycleOwner) {
            val combinedList = buildCombinedList()
            if (combinedList.isNotEmpty()) {
                setCombinedDataConnectionsAdapter(combinedList)
            } else {
                displayDataConnectionsHomeInfo()
            }
        }
        dataConnectionsViewModel.careTeamsList.observe(viewLifecycleOwner) {
            val combinedList = buildCombinedList()
            if (combinedList.isNotEmpty()) {
                setCombinedDataConnectionsAdapter(combinedList)
            } else {
                displayDataConnectionsHomeInfo()
            }
        }
    }

    // Data class for displaying care team participant members
    data class OrganizationCareTeamParticipantMemberDisplay(
        val name: String,
        val status: String,
        val alias: String?
    )

    // Adapter for combined list of Connection and CareTeam
    private fun setCombinedDataConnectionsAdapter(combinedList: List<Any>) {
        val adapter = DataConnectionsListAdapter(combinedList)
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

            R.id.activate -> {
                binding.includeDataConnections.frameLayoutUpdateStatus.visibility = View.GONE
                lifecycleScope.launch {
                    try {
                        val connectionId = connection.id
                        dataConnectionsViewModel.activateDirectConnection(connectionId)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                viewLifecycleOwner.lifecycleScope.launch {
                    dataConnectionsViewModel.activateConnectionData.collect { activateOutcome ->
                        activateOutcome?.let {
                            if (activateOutcome.success()) {
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
                                    textView.text = resources.getString(R.string.activated)
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

            R.id.disconnect -> {
                binding.includeDataConnections.frameLayoutUpdateStatus.visibility = View.GONE
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

            R.id.delete -> {
                binding.includeDataConnections.frameLayoutUpdateStatus.visibility = View.GONE
                lifecycleScope.launch {
                    try {
                        val connectionId = connection.id
                        dataConnectionsViewModel.deleteConnection(connectionId)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                viewLifecycleOwner.lifecycleScope.launch {
                    dataConnectionsViewModel.deleteConnectionData.collect { deleteOutcome ->
                        deleteOutcome?.let {
                            if (deleteOutcome.success()) {
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
                                    textView.text = resources.getString(R.string.deleted)
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
        binding.recordLocationStatusTextView.visibility = View.GONE // Always hide on this page
        binding.includeDataConnectionCategory.dataConnectionFragment.visibility = View.VISIBLE
        binding.includeDataConnections.dataConnectionFragment.visibility = View.GONE
        binding.includeHomeView.headerView.visibility = View.GONE
        dataConnectionsViewModel.suggestedDataConnectionsCategories.observe(viewLifecycleOwner) {
            setDataConnectionsCategoryAdapter(it.suggestedDataConnectionsCategoriesList)
        }
    }

    private fun showActivateButton() {
        binding.includeDataConnections.activate.visibility = View.VISIBLE
        binding.includeDataConnections.disconnect.visibility = View.GONE
        binding.includeDataConnections.activate.setOnClickListener(this)
    }

    private fun showDisconnectButton() {
        binding.includeDataConnections.activate.visibility = View.GONE
        binding.includeDataConnections.disconnect.visibility = View.VISIBLE
        binding.includeDataConnections.disconnect.setOnClickListener(this)
    }

    @Suppress("LocalVariableName")
    override fun onChangeStatusClicked(
        item: Any, // Accepts Connection or OrganizationCareTeamParticipantMemberDisplay
        parent_view: ViewGroup,
        status_change_view: View,
        frameLayoutConnectionStatus: FrameLayout
    ) {
        Log.d("onChangeStatusClicked", "onChangeStatusClicked")
        binding.includeDataConnections.frameLayoutUpdateStatus.visibility = View.VISIBLE
        binding.includeDataConnections.frameLayoutUpdateStatus.y =
            binding.includeDataConnections.rvSuggestedDataConnections.y + parent_view.y + status_change_view.y
        binding.includeDataConnections.delete.setOnClickListener(this)

        when (item) {
            is Connection -> {
                this.connection = item
                this.frameLayoutConnectionStatus = frameLayoutConnectionStatus
                
                // Show/hide buttons based on connection category and status
                val category = item.category
                val connectionStatus = item.status
                
                if (category == ConnectionCategory.IDENTITY && connectionStatus == ConnectionStatus.DISCONNECTED) {
                    // Show activate button, hide disconnect button
                    showActivateButton()
                } else {
                    // Show disconnect button, hide activate button
                    showDisconnectButton()
                }
            }
            is OrganizationCareTeamParticipantMemberDisplay -> {
                // Do not set 'connection' for non-Connection items
                this.frameLayoutConnectionStatus = frameLayoutConnectionStatus
                // For non-Connection items, show default buttons
                showDisconnectButton()
            }
        }
    }

    fun showDataConnectionCategories() {
        binding.includeDataConnectionCategory.dataConnectionFragment.visibility = View.VISIBLE
        binding.recordLocationStatusTextView.visibility = View.GONE // Always hide on this page
    }
}