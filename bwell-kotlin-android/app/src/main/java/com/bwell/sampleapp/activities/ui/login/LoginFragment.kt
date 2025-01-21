package com.bwell.sampleapp.activities.ui.login

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bwell.common.models.domain.consent.enums.ConsentCategoryCode
import com.bwell.common.models.domain.consent.enums.ConsentProvisionType
import com.bwell.common.models.domain.consent.enums.ConsentStatus
import com.bwell.common.models.requests.searchtoken.SearchDate
import com.bwell.core.config.types.BWellConfig
import com.bwell.core.config.types.KeyStoreConfig
import com.bwell.core.config.types.LogLevel
import com.bwell.core.config.types.RetryPolicy
import com.bwell.core.network.auth.Credentials
import com.bwell.device.requests.deviceToken.DevicePlatform
import com.bwell.device.requests.deviceToken.RegisterDeviceTokenRequest
import com.bwell.healthdata.requests.fhir.FhirRequest
import com.bwell.healthdata.requests.fhir.GetFhirSearchDate
import com.bwell.healthdata.requests.fhir.enums.ResourceType
import com.bwell.sampleapp.BWellSampleApplication
import com.bwell.sampleapp.R
import com.bwell.sampleapp.singletons.BWellSdk
import com.bwell.user.requests.consents.ConsentCreateRequest
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.launch
import java.io.IOException
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Properties


/**
 * A simple [Fragment] subclass.
 * Use the [LoginFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class LoginFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private val ARG_PARAM1: String = "A"
    private val ARG_PARAM2: String = "B"

    private val TAG = "LoginFragment"

    private val clientKey =
        "eyJyIjoiaGNxNTloejgyNDB2MjZyMTkzIiwiZW52Ijoic3RhZ2luZyIsImtpZCI6InNhbXN1bmctc3RhZ2luZyJ9"

    private var oAuthCredentials = "eyJhbGciOiJFUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6ImJ3ZWxsLXRlc3QifQ.eyJndWlkIjoiYndlbGwtdGVzdF9id1Z5cUVReWloWmhNdlJrNlRWUy9nPT0iLCJvdGlkIjpmYWxzZSwiZXhwIjoyNjk4MjM0MzcxLCJpYXQiOjE3MjY3NTk2MjR9.3uxDjlozYbQg_VUEfhTBbNCAmMst-JRNvsk-h5pH7YTxoExSj9yy8jFI4wqFPGpg8rUqplPxxqMWUdDTVONQBQ"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val editTextClientKey: EditText = view.findViewById(R.id.editTextClientKey)
        editTextClientKey.setText(clientKey)

        val editTextOAuthCredentials: EditText = view.findViewById(R.id.editTextOAuthCredentials)
        getOAuthToken()
        editTextOAuthCredentials.setText(oAuthCredentials)

        // Initialize the button
        val button: Button = view.findViewById(R.id.buttonLogin)
        button.setOnClickListener {
            Log.i(TAG, "Login Button Clicked")
            val progressBar = view.findViewById<ProgressBar>(R.id.progressBarLogin)
            progressBar.visibility = View.VISIBLE // To show the progress bar

            // Call the function when the button is pressed
            onButtonPressed(
                clientKey = editTextClientKey.text.toString().trim(),
                oAuthCredentials = editTextOAuthCredentials.text.toString().trim()
            )

//            progressBar.visibility =View.GONE
        }
    }

    private fun getOAuthToken() {
        val properties = Properties()
        try {
            val inputStream: InputStream? =
                context?.assets?.open("env.properties")
            properties.load(inputStream)
            oAuthCredentials = properties.getProperty("authToken") ?: "MISSING OAUTH TOKEN. Please see README"
            // Use the configuration values as needed
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun onButtonPressed(
        clientKey: String,
        oAuthCredentials: String
    ) {

        // Add your function logic here
        initializeBWellSDK(
            clientKey,
            oAuthCredentials
        )
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment LoginFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            LoginFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    private fun initializeBWellSDK(clientKey: String, oAuthCredentials: String) {
        lifecycleScope.launch {
            Log.i(TAG, "Initializing SDK")
            val keystore: KeyStoreConfig = KeyStoreConfig.Builder()
                .path(requireContext().filesDir.absolutePath)
                .build()

            val config: BWellConfig = BWellConfig.Builder()
                .clientKey(clientKey)
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
                Credentials.OAuthCredentials(oAuthCredentials)
            Log.d(TAG, credentials.token)

            BWellSdk.authenticate(credentials)

            val deferred = CompletableDeferred<String>()
            FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                    return@OnCompleteListener
                }
                deferred.complete(task.result)
            })
            val deviceToken = deferred.await()
            val registerDeviceTokenRequest: RegisterDeviceTokenRequest = RegisterDeviceTokenRequest.Builder()
                .deviceToken(deviceToken)
                .applicationName("com.bwell.sampleapp")
                .platform(DevicePlatform.ANDROID)
                .build()
            registerDeviceToken(registerDeviceTokenRequest)

            val createConsentRequest: ConsentCreateRequest = ConsentCreateRequest.Builder()
                .category(ConsentCategoryCode.TOS)
                .status(ConsentStatus.ACTIVE)
                .provision(ConsentProvisionType.PERMIT)
                .build()
            createConsent(createConsentRequest)

            val dateFormat = SimpleDateFormat("yyyy")
            val request = FhirRequest.Builder()
                .resourceType(ResourceType.OBSERVATION)
                .lastUpdated(
                    GetFhirSearchDate.Builder()
                        .greaterThan(dateFormat.parse("2024"))
                        .build()
                )
                .ids(listOf(
                    "5884a0f8-3d08-4077-a7fc-1817e5b8ce35",
                    "aab81d50-e53d-45e8-a881-fc22eb2f253f",
                    "3310e4f4-4a97-47fe-b0ed-7805421aa322",
                    "fd3fb48c-3565-40fb-be32-9ae014ad2860",
                    "875ff908-2ebe-46bf-8fe4-72644d7d039f",
                    "dfd1d287-2b84-45e0-bd5f-39c8c5efca2a"
                ))
                .page(0)
                .pageSize(20)
                .build()
            val result = BWellSdk.health.getFhir(request)
            println(result.toString())

            Log.i(TAG, "Finished initializing SDK")

            findNavController().navigate(R.id.nav_home)
        }
    }

    private fun registerDeviceToken(registerDeviceTokenRequest: RegisterDeviceTokenRequest) {
        lifecycleScope.launch {
            val repository = (activity?.application as? BWellSampleApplication)?.bWellRepository
            val registerOutcome = repository?.registerDeviceToken(registerDeviceTokenRequest)
            registerOutcome?.collect { outcome ->
                outcome?.let {
                    if (outcome.success()) {
                        Log.i(TAG, "FCM_TOKEN Registered Successfully")
                    } else {
                        Log.e(TAG, "FCM_TOKEN Failed to register")
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