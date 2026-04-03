//
//  SDKManager.swift
//  bwell-swift-ios
//
//  SDK lifecycle manager with dependency injection.
//  Created at app level, injected via @EnvironmentObject.
//

import Foundation
import BWell

@MainActor
final class SDKManager: ObservableObject {
    @Published private(set) var state: SDKState = .uninitialized
    private(set) var sdk: BWellSDK?

    func initialize(_ clientKey: String) async throws {
        guard sdk == nil else {
            state = .initialized
            return
        }

        state = .initializing

        do {
            let keychainAdapter = KeychainTokenStorageAdapter()
            let config = BWell.SDKConfig(
                clientKey: clientKey.trimmingCharacters(in: .whitespacesAndNewlines),
                logLevel: .verbose,
                tokenStorage: keychainAdapter
            )
            let sdkInstance = try BWellSDK(config: config)
            try await sdkInstance.initialize()

            self.sdk = sdkInstance
            self.state = .initialized
        } catch {
            NSLog("SDKManager.initialize failed: \(error)")
            self.state = .failed(.networkError(underlying: error))
            throw SDKError.networkError(underlying: error)
        }
    }

    func authenticate(credentials: BWell.Credentials) async throws {
        guard let sdk = sdk else {
            state = .failed(.notInitialized)
            throw SDKError.notInitialized
        }

        state = .authenticating

        do {
            try await sdk.authenticate(credentials: credentials)
            try await createConsent(sdk: sdk)
            state = .authenticated
        } catch {
            state = .failed(.authenticationFailed(underlying: error))
            throw SDKError.authenticationFailed(underlying: error)
        }
    }

    func checkSession() async throws {
        guard let sdk = sdk else {
            state = .failed(.notInitialized)
            throw SDKError.notInitialized
        }

        do {
            _ = try await sdk.user.getProfile()
            state = .authenticated
        } catch {
            state = .initialized
            throw SDKError.sessionExpired
        }
    }

    func reset() {
        self.sdk = nil
        self.state = .uninitialized
    }

    func logout() {
        self.sdk = nil
        self.state = .uninitialized
    }

    // MARK: - Private

    private func createConsent(sdk: BWellSDK) async throws {
        let request = BWell.CreateConsentRequest(
            status: .active,
            provision: .init(type: .permit),
            category: .tos
        )
        _ = try await sdk.user.createConsent(request)
    }
}
