//
//  RootView.swift
//  bwell-swift-ios
//
//  Created by Ivan Villanueva on 27/10/25.
//

import SwiftUI
import BWellSDK

struct RootView: View {
    @EnvironmentObject private var bwellSDKManager: BWellSDKManager
    @EnvironmentObject private var router: NavigationRouter

    var body: some View {
        NavigationStack(path: $router.path) {
            AuthenticationView()
                .navigationDestination(for: AppView.self) { screen in
                    switch screen {
                    case .home:
                        HomeView()
                            .navigationBarBackButtonHidden()
                    case .profile:
                        ProfileView()
                            .navigationBarBackButtonHidden()
                    case .healthSummary:
                        HealthSummaryView()
                            .navigationBarBackButtonHidden()
                    case .manageConnections:
                        ManageConnectionsView()
                            .navigationBarBackButtonHidden()
                    case .searchConnections(let connection):
                        SearchConnectionsView(connection: connection)
                    }
                }
        }
        .onAppear {
            // Critical: handle the already-authenticated state when this view first appears.
            handleStateChange(bwellSDKManager.state)
        }
        .onChange(of: bwellSDKManager.state) { _, newState in
            handleStateChange(newState)
        }
    }

    private func handleStateChange(_ state: SDKState) {
        switch state {
        case .authenticated:
            if router.path.isEmpty {
                router.path = NavigationPath([AppView.home])
            }
        case .uninitialized, .unauthenticated, .failed:
            router.navigateToRoot()
        case .initializing, .authenticating, .initialized, .checkingSession:
            break
        }
    }
}
