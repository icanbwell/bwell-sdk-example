package com.bwell.sampleapp.activities.ui.healthsync

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bwell.sampleapp.BWellSampleApplication
import com.bwell.sampleapp.healthsync.HealthSyncAdapterBridge
import com.bwell.sampleapp.ui.theme.MyTestAppTheme
import com.bwell.sampleapp.viewmodel.HealthSyncViewModelFactory

/**
 * Hosts [HealthSyncRootScreen] in Compose - this app's Fragment/XML-nav-graph
 * architecture is otherwise unchanged; this is the one entry point where a
 * Fragment renders Compose content instead of a ViewBinding layout.
 */
class HealthSyncPlaygroundFragment : Fragment() {

    init {
        // Must run before CREATED - see HealthSyncAdapterBridge's doc.
        HealthSyncAdapterBridge.registerPermissionLauncher(this)
    }

    private lateinit var playgroundViewModel: HealthSyncPlaygroundViewModel
    private lateinit var dashboardViewModel: HealthSyncDashboardViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        HealthSyncAdapterBridge.configureIfAvailable(requireContext().applicationContext)

        val repository = (requireActivity().application as BWellSampleApplication).healthSyncRepository
        val factory = HealthSyncViewModelFactory(repository)
        playgroundViewModel = ViewModelProvider(this, factory)[HealthSyncPlaygroundViewModel::class.java]
        dashboardViewModel = ViewModelProvider(this, factory)[HealthSyncDashboardViewModel::class.java]

        return ComposeView(requireContext()).apply {
            setContent {
                MyTestAppTheme {
                    HealthSyncRootScreen(playgroundViewModel, dashboardViewModel, repository)
                }
            }
        }
    }
}
