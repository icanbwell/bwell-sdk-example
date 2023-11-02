package com.bwell.sampleapp.activities.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bwell.BWellSdk
import com.bwell.core.auth.Credentials
import com.bwell.core.config.BWellConfig
import com.bwell.core.config.LogLevel
import com.bwell.core.config.RetryPolicy
import com.bwell.sampleapp.BWellSampleApplication
import com.bwell.sampleapp.R
import com.bwell.sampleapp.databinding.FragmentHomeBinding
import com.bwell.sampleapp.model.ActivityListItems
import com.bwell.sampleapp.viewmodel.SharedViewModel
import com.bwell.sampleapp.viewmodel.SharedViewModelFactory

class HomeFragment : Fragment(), View.OnClickListener {

    private var _binding: FragmentHomeBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root
        val repository = (activity?.application as? BWellSampleApplication)?.bWellRepository

        /*BWellSdk.initialize(config = BWellConfig(
            clientKey = "testClientKey",
            logLevel = LogLevel.DEBUG,
            timeout = 20000,
            retryPolicy = RetryPolicy(maxRetries = 5, retryInterval = 500)
        )
        )*/

        initializeBWellSDK()

        val mainViewModel = ViewModelProvider(this, SharedViewModelFactory(repository))[SharedViewModel::class.java]
        mainViewModel.suggestedActivities.observe(viewLifecycleOwner) {
            setAdapter(it.suggestedActivitiesLIst)
        }

        binding.seeMore.setOnClickListener(this)
        binding.homeView.btnGetStarted.setOnClickListener(this)

        return root
    }

    private fun initializeBWellSDK() {
        BWellSdk.initialize(config = BWellConfig(
            clientKey = "testClientKey",
            logLevel = LogLevel.DEBUG,
            timeout = 20000,
            retryPolicy = RetryPolicy(maxRetries = 5, retryInterval = 500)
        ))
        val credentials = Credentials.OAuthCredentials("token")
        Log.d("BWell Sample App", credentials.token)
        BWellSdk.authenticate(credentials)
    }

    private fun setAdapter(suggestedActivitiesLIst: List<ActivityListItems>) {
        val adapter = SuggestionActivitiesListAdapter(suggestedActivitiesLIst)
        binding.rvSuggestedActivities.layoutManager = LinearLayoutManager(requireContext())
        binding.rvSuggestedActivities.adapter = adapter
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.see_more -> findNavController().navigate(R.id.nav_health_journey)
            R.id.btn_get_started -> findNavController().navigate(R.id.nav_data_connections)
        }
    }
}