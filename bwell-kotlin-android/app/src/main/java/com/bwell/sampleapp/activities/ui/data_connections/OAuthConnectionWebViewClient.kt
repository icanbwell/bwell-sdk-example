package com.bwell.sampleapp.activities.ui.data_connections

import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.util.Log


class OAuthConnectionWebViewClient (private val webViewCallback: WebViewCallback) : WebViewClient() {
    private val TAG = OAuthConnectionWebViewClient::class.simpleName
    private var counter = 0

    override fun shouldOverrideUrlLoading(
        view: WebView?,
        request: WebResourceRequest?
    ): Boolean {

        // Check the URL here
        val url = request?.url.toString()
        Log.d(TAG, "url: $url")

        view?.loadUrl(url)

        // Add your logic to determine if the form was successfully submitted
        if (url == "https://proxy-pages.client-sandbox.icanbwell.com/index.html?status_code=success") {
            // TODO what defines successful OAuth
            Log.d(TAG, "in if! counter: $counter, url: $url")

            // Form successfully submitted
            // Notify the fragment or take appropriate action
            webViewCallback.onWebViewSuccess()
            return true  // Return true to indicate that the URL has been handled
        }

        // Continue loading the URL
        return super.shouldOverrideUrlLoading(view, request)
    }
}