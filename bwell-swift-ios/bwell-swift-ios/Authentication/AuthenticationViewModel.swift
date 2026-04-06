//
//  AuthenticationViewModel.swift
//  bwell-swift-ios
//
//  Created by Ivan Villanueva on 31/10/25.
//
import Foundation
import BWellSDK

@MainActor
final class AuthenticationViewModel: ObservableObject {
    // Authentication published properties
    @Published var apiKey: String = ""
    @Published var oauthToken: String = ""
    @Published var username: String = ""
    @Published var password: String = ""

    // Auto-login reads from .env file (copy .env.example to .env and fill in values)
    #if DEBUG
    static let autoLogin = true
    #endif

    func autoLoginIfDebug(sdkManager: SDKManager) {
        #if DEBUG
        guard Self.autoLogin else { return }
        let env = EnvConfig.load()
        guard env.hasCredentials, let clientKey = env.clientKey else {
            NSLog("[Auth] Auto-login: no credentials in .env — showing login screen")
            return
        }
        NSLog("[Auth] Auto-login: initializing SDK from .env")
        apiKey = clientKey
        Task {
            do {
                try await sdkManager.initialize(clientKey)
            } catch {
                NSLog("[Auth] Auto-init failed: %@", error.localizedDescription)
            }
        }
        #endif
    }

    func autoAuthenticateIfDebug(sdkManager: SDKManager) {
        #if DEBUG
        guard Self.autoLogin else { return }
        let env = EnvConfig.load()
        guard let jwt = env.jwtToken, !jwt.isEmpty else {
            NSLog("[Auth] Auto-login: no JWT in .env — showing login screen")
            return
        }
        NSLog("[Auth] Auto-login: authenticating with JWT from .env")
        oauthToken = jwt
        loginWithOAuthToken(sdkManager: sdkManager)
        #endif
    }

    // Published properties
    @Published var isLoading: Bool = false
    @Published var apiKeyValidated: Bool = false

    @Published var usernameErrorMessage: String?
    @Published var passwordErrorMessage: String?
    @Published var errorMessage: String?

    func initializeSDK(sdkManager: SDKManager) {
        isLoading = true

        guard !apiKey.isEmpty else {
            errorMessage = "API key is required."
            isLoading = false
            return
        }

        Task {
            do {
                try await sdkManager.initialize(apiKey)
            } catch {
                NSLog("SDK initialization error: \(error)")
                errorMessage = "SDK initialization failed: \(error.localizedDescription)"
                isLoading = false
            }
        }
    }

    func handleSDKStateChange(_ state: SDKState) {
        switch state {
        case .initializing:
            isLoading = true
            errorMessage = nil
        case .initialized:
            APIKeyService.shared.save(apiKey)
            apiKeyValidated = true
            isLoading = false
        case .authenticating:
            isLoading = true
            errorMessage = nil
        case .authenticated:
            apiKeyValidated = true
            isLoading = false
        case .uninitialized:
            apiKeyValidated = false
            isLoading = false
        case .failed(let error):
            errorMessage = "SDK error: \(error.localizedDescription)"
            isLoading = false
        }
    }

    func loginWithOAuthToken(sdkManager: SDKManager) {
        isLoading = true

        guard !oauthToken.isEmpty else {
            errorMessage = "OAuth token is required."
            isLoading = false
            return
        }

        Task {
            do {
                let credentials = BWell.Credentials.oauth(token: oauthToken)
                try await sdkManager.authenticate(credentials: credentials)
                isLoading = false
            } catch {
                errorMessage = "Login failed"
                isLoading = false
            }
        }
    }

    func loginWithUsernameAndPassword(sdkManager: SDKManager) {
        isLoading = true

        if username.isEmpty {
            isLoading = false
            usernameErrorMessage = "Username is required."
            return
        }

        if password.isEmpty {
            isLoading = false
            passwordErrorMessage = "Password is required."
            return
        }

        Task {
            do {
                let credentials = BWell.Credentials.usernamePassword(username: username, password: password)
                try await sdkManager.authenticate(credentials: credentials)
                isLoading = false
            } catch {
                errorMessage = "Invalid username or password"
                isLoading = false
            }
        }
    }
}
