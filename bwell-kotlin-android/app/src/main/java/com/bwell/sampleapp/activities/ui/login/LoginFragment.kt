package com.bwell.sampleapp.activities.ui.login

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.launch
import com.bwell.common.models.domain.consent.enums.ConsentCategoryCode
import com.bwell.common.models.domain.consent.enums.ConsentProvisionType
import com.bwell.common.models.domain.consent.enums.ConsentStatus
import com.bwell.core.config.types.BWellConfig
import com.bwell.core.config.types.KeyStoreConfig
import com.bwell.core.config.types.LogLevel
import com.bwell.core.config.types.RetryPolicy
import com.bwell.core.network.auth.Credentials
// Firebase notifications - uncomment to enable push notifications
// import com.bwell.device.requests.deviceToken.DevicePlatform
// import com.bwell.device.requests.deviceToken.RegisterDeviceTokenRequest
import com.bwell.healthdata.requests.fhir.FhirRequest
import com.bwell.healthdata.requests.fhir.GetFhirSearchDate
import com.bwell.healthdata.requests.fhir.enums.ResourceType
import com.bwell.sampleapp.BWellSampleApplication
import com.bwell.sampleapp.R
import com.bwell.sampleapp.singletons.BWellSdk
import com.bwell.sampleapp.utils.BWellSdkInitializer
import com.bwell.user.requests.consents.ConsentCreateRequest
// Firebase notifications - uncomment to enable push notifications
// import com.google.android.gms.tasks.OnCompleteListener
// import com.google.firebase.messaging.FirebaseMessaging
// import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException
import java.io.InputStream
import java.text.SimpleDateFormat
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

    private lateinit var webView: WebView
    private lateinit var buttonLaunchEmbeddable: Button

    private val embeddableVersion = "latest" // NOTE: Set to `latest` which can be breaking. Please pin to an exact version in production use.
    private val bootstrapApiURL = "https://api-gateway.client-sandbox.icanbwell.com/identity" // NOTE: Defaults to bwell `client-sandbox` environment
    private val clientKey = null
    private var oAuthCredentials: String? = null

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
            // Call the function when the button is pressed
            onButtonPressed(
                clientKey = editTextClientKey.text.toString().trim(),
                oAuthCredentials = editTextOAuthCredentials.text.toString().trim()
            )
        }

        // Initialize WebView
        webView = view.findViewById(R.id.embeddableWebView)
        setupWebView()

        // Initialize Launch Embeddable Button
        buttonLaunchEmbeddable = view.findViewById(R.id.buttonLaunchEmbeddable)
        buttonLaunchEmbeddable.setOnClickListener {
            launchEmbeddableWebView(
                clientKey = editTextClientKey.text.toString().trim(),
                oAuthCredentials = editTextOAuthCredentials.text.toString().trim()
            )
        }
    }

    private fun setupWebView() {
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            loadWithOverviewMode = true
            useWideViewPort = true
            builtInZoomControls = true
            displayZoomControls = false
        }

        webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                Log.d(TAG, "Page started loading: $url")
                // TODO: Show loading indicator if needed
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                Log.d(TAG, "Page finished loading: $url")
                // TODO: Hide loading indicator if needed
            }
        }

        // Optional: Add error handling
        webView.webChromeClient = object : WebChromeClient() {
            fun onReceivedError(
                view: WebView?,
                errorCode: Int,
                description: String?,
                failingUrl: String?
            ) {
                Log.e(TAG, "WebView error: $description")
                // Handle error (e.g., show error message)
            }
        }
        // Optional: Enable Webview Debugging
        WebView.setWebContentsDebuggingEnabled(true)
    }

    private fun launchEmbeddableWebView(clientKey: String, oAuthCredentials: String) {
        lifecycleScope.launch {
            // Validate credentials
            if (clientKey.isEmpty()) {
                Toast.makeText(
                    requireContext(),
                    "Please enter valid Client Key",
                    Toast.LENGTH_SHORT
                ).show()
                return@launch
            }

            // Determine the auth strategy
            var authStrategy: String? = "credential"
            val client = OkHttpClient()
            val request = Request.Builder()
                .url("$bootstrapApiURL/admin/bootstrap")
                .addHeader("Content-Type", "application/json")
                .addHeader("ClientKey", clientKey)
                .build()
            try {
                val response = withContext(Dispatchers.IO) {
                    client.newCall(request).execute()
                }
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    if (!responseBody.isNullOrBlank()) {
                        val jsonObject = JSONObject(responseBody)
                        authStrategy = jsonObject.getJSONObject("embeddableConfiguration").getString("authStrategy")

                        if (authStrategy == "jwe" && oAuthCredentials.isBlank()) {
                            Toast.makeText(
                                requireContext(),
                                "Please enter valid OAuth Credentials",
                                Toast.LENGTH_SHORT
                            ).show()
                            return@launch
                        }
                    }
                    // Handle the successful response
                    println(responseBody)
                } else {
                    throw IOException("HTTP error: ${response.code}")
                }
            } catch (e: IOException) {
                Log.e(TAG, "Error querying for client configuration.")
                Toast.makeText(
                    requireContext(),
                    "Error querying for client configuration. Make sure Client Key is valid.",
                    Toast.LENGTH_SHORT
                ).show()
                return@launch
            }

            // Make WebView visible
            webView.visibility = View.VISIBLE

            // Prepare the HTML with embedded BWell SDK script
            val embeddableHtml = """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="utf-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1">
                    <title>BWell Embeddable</title>
                    <script src="https://embeddables.prod.icanbwell.com/composite/${embeddableVersion}/loader/index.js"></script>
                </head>
                <body style="margin: 0;">
                    <script>
                        // Initialize BWell SDK
                        async function initBWell() {
                            try {
                                // Initialize the app experience
                                await bwell.init('$clientKey');
                                ${if (authStrategy == "jwe") "await bwell.setUserToken('$oAuthCredentials');" else ""}
                            } catch (error) {
                                // Handle initialization errors
                                window.Android.onError(error.toString());
                            }
                        }
        
                        // Call initialization when page loads
                        window.addEventListener('load', initBWell);
                    </script>
                    <bwell-composite />
                </body>
                </html>
            """.trimIndent()

            // Set up WebView client
            webView.webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    Log.d(TAG, "Embeddable page loaded")
                }

                override fun onReceivedError(
                    view: WebView?,
                    request: WebResourceRequest?,
                    error: WebResourceError?
                ) {
                    Log.e(TAG, "WebView error: ${error.toString()}")
                }
            }

            // Load the HTML content
            webView.loadDataWithBaseURL(
                "https://mobile.prod.icanbwell.com", // Mock URL for a valid baseUrl
                embeddableHtml,
                "text/html",
                "UTF-8",
                null
            )
        }
    }

    // Optional: Handle back navigation
    fun onBackPressed(): Boolean {
        return if (webView.visibility == View.VISIBLE && webView.canGoBack()) {
            webView.goBack()
            true
        } else {
            false
        }
    }

    // Lifecycle management
    override fun onPause() {
        super.onPause()
        webView.onPause()
    }

    override fun onResume() {
        super.onResume()
        webView.onResume()
    }

    // Clean up WebView to prevent memory leaks
    override fun onDestroyView() {
        webView.destroy()
        super.onDestroyView()
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
        Log.i(TAG, "Login Button Clicked")

        if (clientKey.isEmpty() || oAuthCredentials.isEmpty()) {
            Toast.makeText(
                requireContext(),
                "Please enter valid Client Key and OAuth Credentials",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val progressBar = view?.findViewById<ProgressBar>(R.id.progressBarLogin)
        progressBar?.visibility = View.VISIBLE // To show the progress bar

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
            BWellSdkInitializer.initialize(requireContext(), clientKey, oAuthCredentials)

            // Firebase notifications - uncomment to enable push notifications
            // Requires google-services.json file to be configured
            /*
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
            */

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

    // Firebase notifications - uncomment to enable push notifications
    /*
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
    */
}