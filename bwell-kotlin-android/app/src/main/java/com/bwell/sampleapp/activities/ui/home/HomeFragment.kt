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
import com.bwell.core.config.types.BWellConfig
import com.bwell.core.config.types.KeyStoreConfig
import com.bwell.core.config.types.LogLevel
import com.bwell.core.config.types.RetryPolicy
import com.bwell.core.network.auth.Credentials
import com.bwell.BWellSdk
import com.bwell.common.models.domain.consent.enums.ConsentCategoryCode
import com.bwell.common.models.domain.consent.enums.ConsentProvisionType
import com.bwell.common.models.domain.consent.enums.ConsentStatus
import com.bwell.sampleapp.BWellSampleApplication
import com.bwell.sampleapp.R
import com.bwell.sampleapp.databinding.FragmentHomeBinding
import com.bwell.sampleapp.model.ActivityListItems
import com.bwell.sampleapp.repository.Repository
import com.bwell.sampleapp.viewmodel.SharedViewModel
import com.bwell.sampleapp.viewmodel.SharedViewModelFactory
import com.bwell.user.requests.consents.ConsentCreateRequest
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.internal.wait


class HomeFragment : Fragment(), View.OnClickListener {
    private var _binding: FragmentHomeBinding? = null

    private val TAG = "Home Fragment"

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
        binding.deleteUser.setOnClickListener(this)
        binding.getConsents.setOnClickListener(this)

        mainViewModel.fetchUserProfile()
        viewLifecycleOwner.lifecycleScope.launch {
            mainViewModel.userData.collect{
                binding.userName.text = resources.getString(R.string.welcome_bwell)+" "+(it?.firstName ?: "test" )+"!"
            }
        }
        return root
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
            R.id.delete_user -> {
                lifecycleScope.launch {
                    try {
                        repository?.deleteUser()?.collect {

                        }
                        //delay(5000);
                        tempInitAndAuth()
                    } catch (ex: Exception) {

                    }
                }
            }
            R.id.get_consents -> {
                lifecycleScope.launch {
                    try {
                        repository.getConsents().collect {
                            //do nothing
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    suspend private fun tempInitAndAuth() {
        val keystore: KeyStoreConfig = KeyStoreConfig.Builder()
            .path(requireContext().filesDir.absolutePath)
            .build()

        val config: BWellConfig = BWellConfig.Builder()
            .clientKey("eyJyIjoiaGNxNTloejgyNDB2MjZyMTkzIiwiZW52Ijoic3RhZ2luZyIsImtpZCI6InNhbXN1bmctc3RhZ2luZyJ9")
            .logLevel(LogLevel.DEBUG)
            .timeout(20000)
            .retryPolicy(
                RetryPolicy.Builder()
                    .maxRetries(5)
                    .retryInterval(500)
                    .build()
            )
            .keystore(keystore)
            .build()

        BWellSdk.initialize(config = config)
        val credentials =
            Credentials.OAuthCredentials("eyJhbGciOiJFUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6ImJ3ZWxsLXRlc3QifQ.eyJndWlkIjoiYndlbGwtdGVzdF9XRHd4OGRQQ2c0Um5BVWRIakdiOS9nPT0iLCJvdGlkIjpmYWxzZSwiZXhwIjoyNjk4MjM0MzcxLCJpYXQiOjE3MjY3NjM1Nzd9.K1OoYEIF3GW68p1J4AvmwelGt-fO6H1ClSonyXIUBOzoR-GeEzZREs06b1SC9NuluK4qQpAebkXgbge7uadUZA")


        BWellSdk.authenticate(credentials)

        val createConsentRequest: ConsentCreateRequest = ConsentCreateRequest.Builder()
            .category(ConsentCategoryCode.TOS)
            .status(ConsentStatus.ACTIVE)
            .provision(ConsentProvisionType.PERMIT)
            .build()
        createConsent(createConsentRequest)
    }

    private fun createConsent(consentCreateRequest: ConsentCreateRequest) {
        lifecycleScope.launch {
            val repository = (activity?.application as? BWellSampleApplication)?.bWellRepository
            val registerOutcome = repository?.createConsent(consentCreateRequest)
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