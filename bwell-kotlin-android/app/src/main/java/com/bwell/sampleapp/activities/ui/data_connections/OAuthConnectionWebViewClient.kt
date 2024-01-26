package com.bwell.sampleapp.activities.ui.data_connections

import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.util.Log
import android.webkit.WebResourceResponse


class OAuthConnectionWebViewClient (private val webViewCallback: WebViewCallback) : WebViewClient() {
    private var counter = 0
    override fun shouldOverrideUrlLoading(
        view: WebView?,
        request: WebResourceRequest?
    ): Boolean {

        // Check the URL here
        val url = request?.url.toString()
        Log.d("OAuthConnectionWebViewClient", "url: $url")

        view?.loadUrl(url)
        // Add your logic to determine if the form was successfully submitted
        if (counter++ > 1) { // TODO what defines successful OAuth
            Log.d("OAuthConnectionWebViewClient", "in if! counter: $counter, url: $url")

            // Form successfully submitted
            // Notify the fragment or take appropriate action
            webViewCallback.onWebViewSuccess()
            return true  // Return true to indicate that the URL has been handled
        }

        // Continue loading the URL
        return super.shouldOverrideUrlLoading(view, request)
    }

    override fun shouldInterceptRequest(
        view: WebView?,
        request: WebResourceRequest?
    ): WebResourceResponse? {

        Log.d("shouldInterceptRequest", "${request?.url}")
        return super.shouldInterceptRequest(view, request)
    }
}