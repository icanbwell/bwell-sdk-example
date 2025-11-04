//
//  BWellSDKManager.swift
//  bwell-swift-ios
//
//  Created by Ivan Villanueva on 27/10/25.
//
import Foundation
import BWellSDK

enum SDKError: Error {
    case notInitialized
    case invalidCredentials

    var errorDescription: String? {
        switch self {
            case .notInitialized:
                return "The BWell SDK has not been initialized. Please call initializeSDK() before using any SDK functionality."
            case .invalidCredentials:
                return "The provided credentials for the BWell SDK are invalid."
        }
    }
}

@MainActor
final class BWellSDKManager: ObservableObject {
    static let shared: BWellSDKManager = BWellSDKManager()
    private var sdk: BWellSDK?

    @Published var isAuthenticated: Bool = false

    private init() {}

    /**
     * Initialized the BWell SDK with the provided client key.
     *
     * - Throws:
     *    - An error if the SDK fails to initialize.
     */
    func initilize(clientKey: String) async throws {
        do {
            let config = BWell.SDKConfig(clientKey: clientKey, logLevel: .verbose)
            let sdkInstance = try BWellSDK(config: config)

            try await sdkInstance.initialize()
            self.sdk = sdkInstance
            print("BWell SDK Initialized successfully.")
        } catch {
            print("Error initilizing SDK: \(error)")
            throw error
        }
    }

    /**
     * Authenticates the user with the given credentials.
     * On success, the `isAuthenticated` property is set to true.
     *
     * - Throws:
     *    - An error is authentication fails.
     */
    func authenticate(credentials: BWell.Credentials, clientToken: String) async throws {
        guard let sdk = sdk else { throw SDKError.notInitialized }

        do {
            try await sdk.authenticate(credentials: credentials)
            self.isAuthenticated = true
            print("Authentication successful.")
        } catch {
            print("Authentication failed with error: \(error)")
            self.isAuthenticated = false
            throw error
        }
    }

    /**
     * Logs out the user by clearing the token storage.
     *
     * This is a simplified logout.
     * Please clear all the tokens from the secure token storage provided in the config.
     */
    func logout() {
        self.isAuthenticated = false
        print("User is logged out")
    }
}

extension BWellSDKManager {
    func health() throws -> HealthDataManager {
        guard let healthManager = sdk?.health else {
            throw SDKError.notInitialized
        }

        return healthManager
    }

    func connection() throws -> ConnectionManager {
        guard let connectionManager = sdk?.connection else {
            throw SDKError.notInitialized
        }

        return connectionManager
    }

    func device() throws -> DeviceManager {
        guard let device = sdk?.device else {
            throw SDKError.notInitialized
        }

        return device
    }

    func search() throws -> SearchManager {
        guard let search = sdk?.search else {
            throw SDKError.notInitialized
        }

        return search
    }

    func user() throws -> UserManager {
        guard let user = sdk?.user else {
            throw SDKError.notInitialized
        }

        return user
    }
}
