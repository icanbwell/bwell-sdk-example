//
//  bwell_swift_iosApp.swift
//  bwell-swift-ios
//
//  App entry point. Dependencies created here and injected to child views.
//

import SwiftUI

extension Notification.Name {
    static let verificationCallback = Notification.Name("verificationCallback")
}

@main
struct bwell_swift_iosApp: App {
    @StateObject private var sdkManager = SDKManager()

    var body: some Scene {
        WindowGroup {
            LaunchView()
                .environmentObject(sdkManager)
                .onOpenURL { url in
                    if url.scheme == "bwellexample" && url.host == "verification-callback" {
                        NotificationCenter.default.post(name: .verificationCallback, object: nil)
                    }
                }
        }
    }
}
