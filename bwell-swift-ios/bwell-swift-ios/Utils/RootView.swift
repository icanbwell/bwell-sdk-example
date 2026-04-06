//
//  RootView.swift
//  bwell-swift-ios
//
//  Created by Ivan Villanueva on 27/10/25.
//

import SwiftUI
import BWellSDK

struct RootView: View {
    @EnvironmentObject var sdkManager: SDKManager
    @StateObject private var router = NavigationRouter()
    @StateObject private var healthSummaryViewModel = HealthSummaryViewModel()
    @State private var isAuthenticated = false

    var body: some View {
        Group {
            if isAuthenticated {
                MainTabView()
            } else {
                NavigationStack(path: $router.path) {
                    AuthenticationView()
                }
            }
        }
        .environmentObject(sdkManager)
        .environmentObject(router)
        .environmentObject(healthSummaryViewModel)
        .onAppear {
            handleStateChange(sdkManager.state)
        }
        .onChange(of: sdkManager.state) { _, newState in
            handleStateChange(newState)
        }
    }

    private func handleStateChange(_ state: SDKState) {
        switch state {
        case .authenticated:
            isAuthenticated = true
        case .uninitialized, .failed:
            isAuthenticated = false
            router.popToRoot()
        case .initializing, .authenticating, .initialized:
            break
        }
    }
}
