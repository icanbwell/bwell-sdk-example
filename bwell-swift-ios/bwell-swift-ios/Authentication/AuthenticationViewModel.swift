//
//  AuthenticationViewModel.swift
//  bwell-swift-ios
//
//  Created by Ivan Villanueva on 31/10/25.
//
import Foundation
import BWellSDK
import Combine

@MainActor
final class AuthenticationViewModel: ObservableObject {
    private let sdkManager: BWellSDKManager
    private var cancellables = Set<AnyCancellable>()

    // Authentication published properties
    @Published var apiKey: String = ""
    @Published var oauthToken: String = ""
    @Published var username: String = ""
    @Published var password: String = ""

    // Published properties
    @Published var isLoading: Bool = false
    @Published var apiKeyValidated: Bool = false

    @Published var usernameErrorMessage: String? = nil
    @Published var passwordErrorMessage: String? = nil
    @Published var errorMessage: String? = nil

    init() {
        sdkManager = .shared
        sdkManager.$state
            .receive(on: DispatchQueue.main)
            .sink { [weak self] state in
                self?.handleSDKStateChange(state)
            }
            .store(in: &cancellables)
    }

    func initializeSDK() {
        isLoading = true

        guard !apiKey.isEmpty else {
            errorMessage = "API key is required."
            isLoading = false
            return
        }

        Task {
            do {
                try await sdkManager.initilize(apiKey)
            } catch {
                errorMessage = "SDK initilization failed"
                isLoading = false
            }
        }
    }
    
    private func handleSDKStateChange(_ state: SDKState) {
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
        case .uninitialized, .unauthenticated:
            apiKeyValidated = false
            isLoading = false
        case .failed(let error):
            errorMessage = "SDK error: \(error.localizedDescription)"
            isLoading = false
        case .checkingSession:
            break
        }
    }
    
    func loginWithOAuthToken() {
        isLoading = true

        guard !oauthToken.isEmpty else {
            errorMessage = "OAuth token is required."
            isLoading = false
            return
        }

        Task {
            do {
                let credentials = BWell.Credentials.oauth(token: oauthToken)

                try await sdkManager.login(credentials: credentials)
                isLoading = false
            } catch {
                errorMessage = "Login failed"
                isLoading = false
            }
        }
    }

    func loginWithUsernameAndPassword() {
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

                try await sdkManager.login(credentials: credentials)
                isLoading = false
            } catch {
                errorMessage = "Invalid username or password"
                isLoading = false
            }
        }
    }
}
