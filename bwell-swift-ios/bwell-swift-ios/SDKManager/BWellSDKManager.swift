//
//  BWellSDKManager.swift
//  bwell-swift-ios
//
//  Created by Ivan Villanueva on 27/10/25.
//
import Foundation
import BWellSDK

@MainActor
final class BWellSDKManager: ObservableObject {
    static let shared: BWellSDKManager = BWellSDKManager()
    private var sdk: BWellSDK?

    @Published var state: SDKState = .uninitialized

    private init() {}

    /**
     * Initialized the BWell SDK with the provided client key.
     *
     * - Throws:
     *    - An error if the SDK fails to initialize.
     */
    func initilize(_ apiKey: String, shouldCheckSession: Bool = false) async throws {
        if sdk != nil {
            print("SDK instance already exists.")
            return
        }

        self.state = .initializing
        do {
            let tokenStorage = KeychainTokenStorageAdapter()
            let config = BWell.SDKConfig(clientKey: apiKey, logLevel: .verbose,tokenStorage: tokenStorage)
            let sdkInstance = try BWellSDK(config: config)

            try await sdkInstance.initialize()
            self.sdk = sdkInstance
            self.state = .initialized

            print("BWell SDK Initialized successfully.")
        } catch {
            print("Error initilizing SDK: \(error)")
            self.state = .failed(error)
            throw error
        }
    }

    /**
     * Checks if the SDK has an active session by attempting to fetch the user profile.
     * If successful, the state is updated to `.authenticated`.
     */
    func checkSessionStatus() async throws {
        guard let sdk = sdk else {
            state = .unauthenticated
            throw SDKError.notInitialized
        }

        state = .checkingSession

        do {
            _ = try await sdk.user.getProfile()
            self.state = .authenticated

            print("Active session found. User is already authenticated.")
        } catch {
            print("No active session found or token expired. Proceeding to login.")
            self.state = .unauthenticated
            throw error
        }
    }

    func validateSession() async {
            let maxRetries = 3
            for attempt in 1...maxRetries {
                do {
                    try await checkSessionStatus()
                    return
                } catch {
                    if attempt < maxRetries {
                        try? await Task.sleep(for: .seconds(2))
                    }
                }
            }

            // If all retries have failed nuke the failed SDK instance and reset the state completely.
            self.sdk = nil
            self.state = .uninitialized
        }

    /**
     * Authenticates the user with the given credentials.
     * On success, the `state` property is set to `.ready`.
     *
     * - Throws:
     *    - An error is authentication fails.
     */
    func login(credentials: BWell.Credentials) async throws {
        guard let sdk = sdk else { throw SDKError.notInitialized }
        self.state = .authenticating

        do {
            try await sdk.authenticate(credentials: credentials)
            

            let request: BWell.CreateConsentRequest = .init(status: .active,
                                                            provision: .init(type: .permit),
                                                            category: .tos,)
            _ = try await sdk.user.createConsent(request)
            self.state = .authenticated

            print("Login flow complete. SDK is ready.")
        } catch {
            print("Login flow failed with error: \(error)")

            self.state = .failed(error)
            throw error
        }
    }
    
    private func createConsent() async throws {
        guard let sdk = sdk else { throw SDKError.createConsentNotCreated }

        do {
            let request: BWell.CreateConsentRequest = .init(status: .active,
                                                            provision: .init(type: .permit),
                                                            category: .tos)

            _ = try await sdk.user.createConsent(request)
        } catch {
            throw error
        }
    }

    /**
     * Logs out the user by invalidating the SDK session and clearing all local tokens
     * from the secure keychain storage.
     */
    func logout() {
        Task {
            do {
                try KeychainService.shared.clear()
                APIKeyService.shared.clear()

                await MainActor.run {
                    self.sdk = nil
                    self.state = .uninitialized
                }
            } catch {
                await MainActor.run {
                    self.state = .failed(error)
                }
            }
        }
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
