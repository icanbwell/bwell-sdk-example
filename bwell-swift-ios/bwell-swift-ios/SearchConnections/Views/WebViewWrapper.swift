//
//  WebViewWrapper.swift
//  bwell-swift-ios
//
//  Created by Ivan Villanueva on 07/11/25.
//
import Foundation
import SwiftUI
import WebKit

struct WebViewWrapper: UIViewRepresentable {
    let url: URL
    var onSuccess: () -> Void

    func makeUIView(context: Context) -> WKWebView {
        let webView = WKWebView()
        webView.navigationDelegate = context.coordinator

        return webView
    }

    func updateUIView(_ uiView: WKWebView, context: Context) {
        let request = URLRequest(url: url)
        uiView.load(request)
    }

    func makeCoordinator() -> Coordinator {
        Coordinator(self)
    }


    class Coordinator: NSObject, WKNavigationDelegate {
        var parent: WebViewWrapper

        init(_ parent: WebViewWrapper) {
            self.parent = parent
        }

        func webView(_ webView: WKWebView, decidePolicyFor navigationAction: WKNavigationAction, decisionHandler: @escaping (WKNavigationActionPolicy) -> Void) {
            if let url = navigationAction.request.url,
               url.absoluteString.contains("status_code=success") {
                parent.onSuccess()

                decisionHandler(.cancel)
                return
            }
            decisionHandler(.allow)
        }
    }
}
