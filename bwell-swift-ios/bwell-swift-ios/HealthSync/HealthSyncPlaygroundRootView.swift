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
//  With an on-device adapter configured (BWellHealthSyncType.isConfigured),
//  this shows the polished HealthSyncDashboardView (Metrics/Body Score/
//  Playground tabs) instead of the raw Playground directly - with no
//  adapter, behavior is unchanged from before: just the Playground.
//

import BWellHealthSync
import SwiftUI

struct HealthSyncPlaygroundRootView: View {
    @EnvironmentObject private var sdkManager: SDKManager
    @StateObject private var viewModel = HealthSyncPlaygroundViewModel()

    // BWellHealthSyncType.isConfigured is a plain static SDK property, not
    // something SwiftUI observes - reading it directly in `body` means the
    // very first render (before attach() below has run) permanently decides
    // this branch, even once configure() succeeds moments later. Mirroring
    // it into @State and updating that after attach() runs is what actually
    // triggers the re-render into the Dashboard branch.
    @State private var isAdapterConfigured = BWellHealthSyncType.isConfigured

    var body: some View {
        Group {
            if let client = sdkManager.sdk {
                if isAdapterConfigured {
                    HealthSyncDashboardView(client: client)
                } else {
                    HealthSyncPlaygroundView(viewModel: viewModel)
                        .onAppear {
                            viewModel.attach(client: client)
                            isAdapterConfigured = BWellHealthSyncType.isConfigured
                        }
                }
            } else {
                Text("Log in to use Health Sync.")
                    .foregroundStyle(.secondary)
            }
        }
        .navigationTitle("Health Sync")
        .navigationBarTitleDisplayMode(.inline)
    }
}
