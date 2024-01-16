package com.bwell.sampleapp.activities.ui.home

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bwell.BWellSdk
import com.bwell.core.config.BWellConfig
import com.bwell.core.config.KeyStoreConfig
import com.bwell.core.config.LogLevel
import com.bwell.core.config.RetryPolicy
import com.bwell.core.network.auth.Credentials
import com.bwell.device.requests.deviceToken.DevicePlatform
import com.bwell.device.requests.deviceToken.RegisterDeviceTokenRequest
import com.bwell.sampleapp.BWellSampleApplication
import com.bwell.sampleapp.R
import com.bwell.sampleapp.databinding.FragmentHomeBinding
import com.bwell.sampleapp.model.ActivityListItems
import com.bwell.sampleapp.viewmodel.SharedViewModel
import com.bwell.sampleapp.viewmodel.SharedViewModelFactory
import kotlinx.coroutines.launch


class HomeFragment : Fragment(), View.OnClickListener {

    private var _binding: FragmentHomeBinding? = null

    private val binding get() = _binding!!

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        initializeBWellSDK()

        val root: View = binding.root
        val repository = (activity?.application as? BWellSampleApplication)?.bWellRepository

        val mainViewModel = ViewModelProvider(this, SharedViewModelFactory(repository))[SharedViewModel::class.java]
        mainViewModel.suggestedActivities.observe(viewLifecycleOwner) {
            setAdapter(it.suggestedActivitiesLIst)
        }

        binding.seeMore.setOnClickListener(this)
        binding.homeView.btnGetStarted.setOnClickListener(this)

        mainViewModel.fetchUserProfile()
        viewLifecycleOwner.lifecycleScope.launch {
            mainViewModel.userData.collect{
                binding.userName.text = resources.getString(R.string.welcome_bwell)+" "+it?.firstName+"!"
            }
        }

        return root
    }

    private fun initializeBWellSDK() {
        lifecycleScope.launch {

            val keystore: KeyStoreConfig = KeyStoreConfig.Builder()
                .path(requireContext().filesDir.absolutePath)
                .build()

            val config: BWellConfig = BWellConfig.Builder()
                .clientKey("eyJyIjoiNWV4b3d2N2RqZzVtbWpyb2JlaiIsImVudiI6ImNsaWVudC1zYW5kYm94Iiwia2lkIjoic2Ftc3VuZy1jbGllbnQtc2FuZGJveCJ9")
                .logLevel(LogLevel.DEBUG)
                .timeout(20000)
                .retryPolicy(RetryPolicy(maxRetries = 5, retryInterval = 500))
                .keystore(keystore)
                .build()

            BWellSdk.initialize(config = config)
            val credentials = Credentials.OAuthCredentials("eyJhbGciOiJFUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6ImJ3ZWxsLXRlc3QifQ.eyJndWlkIjoiYndlbGwtdGVzdF95czhpbE5NU0Rvd2h3ZklQZk1PMi9nPT0iLCJvdGlkIjpmYWxzZSwiZXhwIjoyNjk4MjM0MzcxLCJpYXQiOjE3MDM4ODkwNTZ9.2rFiyAfvqiQ_CxWM8P_AUvpGxHbXTdBAzAQuUQLSUiFq3HRXBmtjjxvHDJzhKhSIP_rU9BrAo14PNvRqKW1c6g")
            Log.d("BWell Sample App", credentials.token)

            BWellSdk.authenticate(credentials)

            val registerDeviceTokenRequest: RegisterDeviceTokenRequest = RegisterDeviceTokenRequest.Builder()
                .deviceToken("34cb23e2f562dbb5")
                .applicationName("com.icanbwell.bwelldemo.staging")
                .platform(DevicePlatform.ANDROID)
                .build()
            registerDeviceToken(registerDeviceTokenRequest)

            deregisterDeviceToken("34cb23e2f562dbb5")
        }
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
            R.id.see_more ->
            {
                findNavController().popBackStack(R.id.nav_home, true)
                findNavController().navigate(R.id.nav_health_journey)
            }
            R.id.btn_get_started ->
            {
                findNavController().popBackStack(R.id.nav_home, true)
                findNavController().navigate(R.id.nav_data_connections)
            }
        }
    }

    private fun registerDeviceToken(registerDeviceTokenRequest: RegisterDeviceTokenRequest) {
        lifecycleScope.launch {
            val repository = (activity?.application as? BWellSampleApplication)?.bWellRepository
            val registerOutcome = repository?.registerDeviceToken(registerDeviceTokenRequest)
            registerOutcome?.collect { outcome ->
                outcome?.let {
                    if (outcome.success()) {
                        //device registered successfully
                    } else {
                        //device not registered
                    }
                }
            }
        }
    }

    private fun deregisterDeviceToken(deviceToken: String) {
        lifecycleScope.launch {
            val repository = (activity?.application as? BWellSampleApplication)?.bWellRepository
            val registerOutcome = repository?.unregisterDeviceToken(deviceToken)
            registerOutcome?.collect { outcome ->
                outcome?.let {
                    if (outcome.success()) {
                        //device registered successfully
                    } else {
                        //device not registered
                    }
                }
            }
        }
    }
}