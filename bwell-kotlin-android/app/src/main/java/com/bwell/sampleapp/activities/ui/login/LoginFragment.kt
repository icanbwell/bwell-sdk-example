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
import com.bwell.core.config.BWellConfig
import com.bwell.core.config.KeyStoreConfig
import com.bwell.core.config.LogLevel
import com.bwell.core.config.RetryPolicy
import com.bwell.core.network.auth.Credentials
import com.bwell.device.requests.deviceToken.DevicePlatform
import com.bwell.device.requests.deviceToken.RegisterDeviceTokenRequest
import com.bwell.sampleapp.BWellSampleApplication
import com.bwell.sampleapp.R
import com.bwell.sampleapp.singletons.BWellSdk
import kotlinx.coroutines.launch
import java.io.IOException
import java.io.InputStream
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
        "eyJyIjoiNWV4b3d2N2RqZzVtbWpyb2JlaiIsImVudiI6ImNsaWVudC1zYW5kYm94Iiwia2lkIjoic2Ftc3VuZy1jbGllbnQtc2FuZGJveCJ9"

    private var oAuthCredentials: String? = null

    private val deviceKey = "34cb23e2f562dbb5"


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

        // Example: Show a toast message
//        Toast.makeText(context, "Button pressed!", Toast.LENGTH_SHORT).show()

        findNavController().navigate(R.id.nav_home)
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
                .retryPolicy(RetryPolicy(maxRetries = 5, retryInterval = 500))
                .keystore(keystore)
                .build()

            BWellSdk.initialize(config = config)
            val credentials =
                Credentials.OAuthCredentials(oAuthCredentials)
            Log.d(TAG, credentials.token)

            BWellSdk.authenticate(credentials)

            val registerDeviceTokenRequest: RegisterDeviceTokenRequest =
                RegisterDeviceTokenRequest.Builder()
                    .deviceToken(deviceKey)
                    .applicationName("com.sec.android.app.shealth")
                    .platform(DevicePlatform.ANDROID)
                    .build()
            registerDeviceToken(registerDeviceTokenRequest)

            //deregisterDeviceToken(deviceKey)

            Log.i(TAG, "Finished initializing SDK")
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