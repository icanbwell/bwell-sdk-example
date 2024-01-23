package com.bwell.sampleapp.activities.ui.login

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
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
import kotlinx.coroutines.launch

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initializeBWellSDK()

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
            val credentials =
                Credentials.OAuthCredentials("eyJhbGciOiJFUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6ImJ3ZWxsLXRlc3QifQ.eyJndWlkIjoiYndlbGwtdGVzdF95czhpbE5NU0Rvd2h3ZklQZk1PMi9nPT0iLCJvdGlkIjpmYWxzZSwiZXhwIjoyNjk4MjM0MzcxLCJpYXQiOjE3MDM4ODkwNTZ9.2rFiyAfvqiQ_CxWM8P_AUvpGxHbXTdBAzAQuUQLSUiFq3HRXBmtjjxvHDJzhKhSIP_rU9BrAo14PNvRqKW1c6g")
            Log.d("BWell Sample App", credentials.token)

            BWellSdk.authenticate(credentials)

            val registerDeviceTokenRequest: RegisterDeviceTokenRequest =
                RegisterDeviceTokenRequest.Builder()
                    .deviceToken("34cb23e2f562dbb5")
                    .applicationName("com.icanbwell.bwelldemo.staging")
                    .platform(DevicePlatform.ANDROID)
                    .build()
            registerDeviceToken(registerDeviceTokenRequest)

            deregisterDeviceToken("34cb23e2f562dbb5")
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