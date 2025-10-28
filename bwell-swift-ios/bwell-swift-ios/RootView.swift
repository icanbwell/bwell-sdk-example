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
                            EmptyView()
                        case .search:
                            EmptyView()
                        case .manageConnections:
                            EmptyView()
                        case .healthSummary:
                            EmptyView()
                    }
                }
        }.onChange(of: bwellSDKManager.isAuthenticated) { _, isAuthenticated in
            if isAuthenticated {
                router.path = NavigationPath([AppView.home])
            } else {
                router.navigateToRoot()
            }
        }
    }
}
