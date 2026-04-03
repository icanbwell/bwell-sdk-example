//
//  HTMLContentView.swift
//  bwell-swift-ios
//
//  UIViewRepresentable wrapping WKWebView for rendering HTML strings.
//

import SwiftUI
import WebKit

struct HTMLContentView: UIViewRepresentable {
    let htmlContent: String

    func makeUIView(context: Context) -> WKWebView {
        let webView = WKWebView()
        webView.isOpaque = false
        webView.backgroundColor = .clear
        webView.scrollView.backgroundColor = .clear
        webView.navigationDelegate = context.coordinator
        return webView
    }

    func updateUIView(_ webView: WKWebView, context: Context) {
        let styledHTML = """
        <html>
        <head>
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <style>
            body {
                font-family: -apple-system, BlinkMacSystemFont, 'Helvetica Neue', sans-serif;
                font-size: 16px;
                line-height: 1.5;
                color: #1C1C1E;
                padding: 0;
                margin: 0;
                background-color: transparent;
            }
            @media (prefers-color-scheme: dark) {
                body { color: #F2F2F7; }
            }
            img { max-width: 100%; height: auto; }
            a { color: #007AFF; }
            h1, h2, h3 { margin-top: 16px; margin-bottom: 8px; }
            p { margin-bottom: 12px; }
        </style>
        </head>
        <body>\(htmlContent)</body>
        </html>
        """
        webView.loadHTMLString(styledHTML, baseURL: nil)
    }

    func makeCoordinator() -> Coordinator {
        Coordinator()
    }

    final class Coordinator: NSObject, WKNavigationDelegate {
        func webView(_ webView: WKWebView,
                     decidePolicyFor navigationAction: WKNavigationAction,
                     decisionHandler: @escaping (WKNavigationActionPolicy) -> Void) {
            if navigationAction.navigationType == .linkActivated,
               let url = navigationAction.request.url {
                UIApplication.shared.open(url)
                decisionHandler(.cancel)
            } else {
                decisionHandler(.allow)
            }
        }
    }
}
