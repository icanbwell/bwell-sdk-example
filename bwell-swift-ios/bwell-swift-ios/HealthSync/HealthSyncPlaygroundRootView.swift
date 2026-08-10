//
//  HealthSyncPlaygroundRootView.swift
//  bwell-swift-ios
//
//  Adapted from bwell-sdk-swift/Examples/HealthSyncSampleApp/Sources/ContentView.swift.
//  Unlike the internal sample, this demo has no own login/logout - whether
//  the visitor is the current logged-in user is the app's own concern,
//  already resolved before this screen is reachable (it's only reachable
//  from inside the authenticated tab flow). This view just attaches the
//  Playground to the existing session from SDKManager.
//

import SwiftUI

struct HealthSyncPlaygroundRootView: View {
    @EnvironmentObject private var sdkManager: SDKManager
    @StateObject private var viewModel = HealthSyncPlaygroundViewModel()

    var body: some View {
        Group {
            if let client = sdkManager.sdk {
                HealthSyncPlaygroundView(viewModel: viewModel)
                    .onAppear { viewModel.attach(client: client) }
            } else {
                Text("Log in to use Health Sync.")
                    .foregroundStyle(.secondary)
            }
        }
        .navigationTitle("Health Sync")
        .navigationBarTitleDisplayMode(.inline)
    }
}
