//
//  SafariView.swift
//  bwell-swift-ios
//
//  In-app browser for any Playground doc link - opens without leaving the
//  app (SFSafariViewController), same pattern as e.g. Twitter/Instagram's
//  in-app link previews. Feature-agnostic, lives in the shared Playground
//  infra since "open a link in-app" isn't Health-Sync-specific.
//

import SafariServices
import SwiftUI

struct SafariView: UIViewControllerRepresentable {
    let url: URL

    func makeUIViewController(context: Context) -> SFSafariViewController {
        SFSafariViewController(url: url)
    }

    func updateUIViewController(_ controller: SFSafariViewController, context: Context) {}
}
