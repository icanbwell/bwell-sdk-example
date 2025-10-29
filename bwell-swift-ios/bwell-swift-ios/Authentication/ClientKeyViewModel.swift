//
//  ClientKeyViewModel.swift
//  bwell-swift-ios
//
//  Created by Ivan Villanueva on 27/10/25.
//
import Foundation
import BWellSDK

@MainActor
final class ClientKeyViewModel: ObservableObject {
    private let sdkManager: BWellSDKManager

    @Published var clientKey: String = "eyJyIjoiY2Zoa2h3ODZvNHdoNWFiOW9kaHgiLCJlbnYiOiJkZXYiLCJraWQiOiJid2VsbF9kZW1vLWRldiJ9"
    @Published var isLoading: Bool = false
    @Published var authenticated: Bool = false
    @Published var errorMessage: String?

    init() {
        sdkManager = .shared
    }

    func initializeSDK() {
        // Check the client key is not empty.
        guard !clientKey.isEmpty else {
            errorMessage = "Client key is required."
            isLoading = false
            return
        }

        isLoading = true
        errorMessage = nil

        Task {
            do {
                try await sdkManager.initilize(clientKey: clientKey)
                await MainActor.run {
                    self.authenticated = true
                    self.isLoading = false
                }
            } catch {
                await MainActor.run {
                    errorMessage = "SDK initilization failed: \(error)"
                    isLoading = false
                }
            }
        }
    }
}
