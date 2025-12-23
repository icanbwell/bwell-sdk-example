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
            // 1) Initialize the SDK.
            try await BWellSDKManager.shared.initilize(apiKey)

            // 2) Attempt to authenticate from storage (rehydrate tokens).
            do {
                try await BWellSDKManager.shared.login(credentials: .storage)
                // 3) If storage auth succeeded, validate the session.
                await BWellSDKManager.shared.validateSession()
            } catch {
                // Storage auth failed - no valid tokens exist.
                // Clear the stored API key so user can enter fresh credentials.
                print("LaunchViewModel: Storage authentication failed: \(error)")
                APIKeyService.shared.clear()
                BWellSDKManager.shared.reset()
            }
        } catch {
            // SDK initialization failed - clear stored key and reset.
            print("LaunchViewModel: SDK initialization failed: \(error)")
            APIKeyService.shared.clear()
            BWellSDKManager.shared.reset()
        }

        // 4) Show RootView after session check completes so RootView can react to the final state.
        isCheckingSession = false
    }
}
