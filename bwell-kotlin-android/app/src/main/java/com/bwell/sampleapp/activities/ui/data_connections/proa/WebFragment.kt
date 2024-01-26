package com.bwell.sampleapp.activities.ui.data_connections.proa

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import androidx.lifecycle.lifecycleScope
import com.bwell.common.models.responses.BWellResult
import com.bwell.sampleapp.BWellSampleApplication
import com.bwell.sampleapp.R
import com.bwell.sampleapp.viewmodel.DataConnectionsViewModel
import com.bwell.sampleapp.viewmodel.DataConnectionsViewModelFactory
import kotlinx.coroutines.launch

class WebFragment : Fragment() {

    private lateinit var viewModel: WebViewModel

    private lateinit var webView: WebView

    private val TAG = "WebFragment"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_web, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(WebViewModel::class.java)
        // TODO: Use the ViewModel
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        webView = view.findViewById(R.id.webview)
        webView.settings.javaScriptEnabled = true // Enable JavaScript if required

        val repository =
            (activity?.application as? BWellSampleApplication)?.dataConnectionsRepository

        val dataConnectionsViewModel = ViewModelProvider(
            this,
            DataConnectionsViewModelFactory(repository)
        )[DataConnectionsViewModel::class.java]

        lifecycleScope.launch {
            dataConnectionsViewModel.urlData.collect { urlResult ->
                if (urlResult != null) {
                    val url = urlResult.toString()
                    Log.i(TAG, "Loading url: $url")
                    // Load a web URL
                    webView.loadUrl(url)
                }
            }
        }
    }

    // Optional: Handle back navigation within the WebView
    fun canGoBack(): Boolean {
        return webView.canGoBack()
    }

    fun goBack() {
        webView.goBack()
    }


}