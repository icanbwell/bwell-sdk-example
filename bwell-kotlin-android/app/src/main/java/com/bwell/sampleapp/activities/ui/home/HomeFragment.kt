package com.bwell.sampleapp.activities.ui.home

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bwell.device.requests.deviceToken.DevicePlatform
import com.bwell.device.requests.deviceToken.RegisterDeviceTokenRequest
import com.bwell.sampleapp.BWellSampleApplication
import com.bwell.sampleapp.R
import com.bwell.sampleapp.databinding.FragmentHomeBinding
import com.bwell.sampleapp.model.ActivityListItems
import com.bwell.sampleapp.repository.Repository
import com.bwell.sampleapp.utils.getEncryptedSharedPreferences
import com.bwell.sampleapp.viewmodel.SharedViewModel
import com.bwell.sampleapp.viewmodel.SharedViewModelFactory
import kotlinx.coroutines.launch


class HomeFragment : Fragment(), View.OnClickListener {

    private var _binding: FragmentHomeBinding? = null

    private val binding get() = _binding!!
    private lateinit var repository: Repository

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        val root: View = binding.root
        repository = (activity?.application as? BWellSampleApplication)!!.bWellRepository

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

        registerDeviceToken()

        return root
    }

    private fun registerDeviceToken() {
        val sharedPreferences = com.bwell.sampleapp.utils.getSharedPreferences(requireContext().applicationContext)
        val isRegistered = sharedPreferences.getBoolean(
            R.string.fcm_device_token_registered.toString(),
            false
        )

        val encryptedPreferences = getEncryptedSharedPreferences(requireContext().applicationContext)
        val fcmToken = encryptedPreferences.getString(
            R.string.fcm_device_token.toString(), null
        )

        println("FCM_TOKEN onCreate: fcmToken: $fcmToken")
        println("FCM_TOKEN onCreate: isRegistered: $isRegistered")
        if (fcmToken != null && !isRegistered) {
            val registerDeviceTokenRequest: RegisterDeviceTokenRequest = RegisterDeviceTokenRequest.Builder()
                .deviceToken(fcmToken)
                .applicationName("com.bwell.sampleapp")
                .platform(DevicePlatform.ANDROID)
                .build()

            lifecycleScope.launch {
                println("FCM_TOKEN onCreate lifecycleScope.launch")
                val registerOutcome = repository.registerDeviceToken(registerDeviceTokenRequest)
                registerOutcome.collect { outcome ->
                    println("FCM_TOKEN onCreate outcome: $outcome")
                    outcome?.let {
                        if (outcome.success()) {
                            println("FCM_TOKEN Registered Successfully")
                            val editor = sharedPreferences.edit()
                            editor.putBoolean(R.string.fcm_device_token_registered.toString(), true)
                            editor.apply()
                        } else {
                            println("FCM_TOKEN Failed to register")
                        }
                    }
                }
            }
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

}