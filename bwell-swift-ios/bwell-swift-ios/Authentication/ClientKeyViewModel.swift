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
    // private var router: NavigationRouter?

    @Published var clientKey: String = ""
    @Published var isLoading: Bool = false
    @Published var authenticated: Bool = false
    @Published var errorMessage: String?

    init() {
        sdkManager = .shared
    }

    /*func setup(router: NavigationRouter) {
        self.router = router
    }*/

    func initializeSDK() {
        // guard let router = router else { return }

        // Check the client key is not empty.
        guard !clientKey.isEmpty else {
            errorMessage = "Client key is required."
            return
        }

        isLoading = false
        errorMessage = nil

        // Call the initialization method in the SDK Manager and handle the errors.
        Task {
            do {
                try await sdkManager.initilize(clientKey: clientKey)
                self.authenticated = true
                // router.navigate(to: .login)
            } catch {
                await MainActor.run {
                    errorMessage = "SDK initilization failed: \(error)"
                }

            }
            isLoading = false
        }
    }
}
