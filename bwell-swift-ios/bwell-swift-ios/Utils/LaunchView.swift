//
//  LaunchView.swift
//  bwell-swift-ios
//
//  Created by Ivan Villanueva on 12/11/25.
//
import Foundation
import SwiftUI

struct LaunchView: View {
    @EnvironmentObject var sdkManager: SDKManager
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
                await viewModel.checkSession(sdkManager: sdkManager)
            }
        }
    }
}

@MainActor
final class LaunchViewModel: ObservableObject {
    @Published var isCheckingSession: Bool = true

    func checkSession(sdkManager: SDKManager) async {
        guard let apiKey = APIKeyService.shared.getAPIKey() else {
            isCheckingSession = false
            return
        }

        do {
            try await sdkManager.initialize(apiKey)
            try await sdkManager.checkSession()
        } catch {
            print("LaunchViewModel: SDK initialization failed: \(error)")
            APIKeyService.shared.clear()
            sdkManager.reset()
        }

        isCheckingSession = false
    }
}
