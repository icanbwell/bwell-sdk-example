//
//  RootView.swift
//  bwell-swift-ios
//
//  Created by Ivan Villanueva on 27/10/25.
//

import SwiftUI
import BWellSDK

struct RootView: View {
    @StateObject private var bwellSDKManager = BWellSDKManager.shared
    @StateObject private var router = NavigationRouter()
    @StateObject private var viewModel = SideMenuOptionViewModel()
    @StateObject private var healthSummaryViewModel = HealthSummaryViewModel()
    @State private var showMenu: Bool = false

    var body: some View {
        ZStack {
            NavigationStack(path: $router.path) {
                AuthenticationView()
                    .navigationDestination(for: AppView.self) { screen in
                        switch screen {
                        case .home:
                            HomeView(showMenu: $showMenu)
                                .navigationBarBackButtonHidden()
                        case .profile:
                            ProfileView(showMenu: $showMenu)
                                .navigationBarBackButtonHidden()
                        case .healthSummary:
                            HealthSummaryView(showMenu: $showMenu)
                                .navigationBarBackButtonHidden()
                        case .manageConnections:
                            ManageConnectionsView(showMenu: $showMenu)
                                .navigationBarBackButtonHidden()
                        case .searchConnections(let connection):
                            SearchConnectionsView(connection: connection)
                        case .connections:
                            ConnectionsView()
                        case .healthGroupItems(let category, let groupCode):
                            // Health data summary detail views
                            HealthDataFactoryView(category, groupCode)
                        }
                    }
            }
            
            SideMenuView(isShowing: $showMenu, viewModel: viewModel)
        }
        .environmentObject(router)
        .environmentObject(viewModel)
        .environmentObject(healthSummaryViewModel)
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
