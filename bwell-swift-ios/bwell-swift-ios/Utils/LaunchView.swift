//
//  LaunchView.swift
//  bwell-swift-ios
//
//  Created by Ivan Villanueva on 12/11/25.
//
import Foundation
import SwiftUI

struct LaunchView: View {
    @StateObject private var viewModel = LaunchViewModel()
    @StateObject private var sideMenuViewModel = SideMenuOptionViewModel()

    var body: some View {
        Group {
            if viewModel.isCheckingSession {
                ZStack {
                    Color.bwellPurple
                        .ignoresSafeArea()

                    Image("bwell-logo")
                        .resizable()
                        .scaledToFit()
                        .frame(width: 150)
                        .padding(.bottom, 40)
                }
            } else {
                RootView()
                    .environmentObject(sideMenuViewModel)
            }
        }.onAppear {
            Task {
                await viewModel.checkSession()
            }
        }
    }
}

@MainActor
final class LaunchViewModel: ObservableObject {
    @Published var isCheckingSession: Bool = true

    func checkSession() async {
        guard let apiKey = APIKeyService.shared.getAPIKey() else {
            BWellSDKManager.shared.state = .uninitialized
            isCheckingSession = false
            return
        }
        
        do {
            // 1) Initialize the SDK (rehydrates token manager now).
            try await BWellSDKManager.shared.initilize(apiKey)
            // 2) Validate session before revealing RootView so state can be .authenticated.
            await BWellSDKManager.shared.validateSession()
        } catch {
            // This will only catch errors from the initial setup.
            print("LaunchViewModel: SDK initialization failed.")
        }

        // 3) Show RootView after session check completes so RootView can react to the final state.
        isCheckingSession = false
    }
}
