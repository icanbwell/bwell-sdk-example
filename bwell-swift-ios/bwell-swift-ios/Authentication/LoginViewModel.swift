//
//  LoginViewModel.swift
//  bwell-swift-ios
//
//  Created by Ivan Villanueva on 27/10/25.
//

import Foundation
import BWellSDK

@MainActor
final class LoginViewModel: ObservableObject {
    private let sdkManager: BWellSDKManager

    @Published var email: String = ""
    @Published var password: String = ""
    @Published var errorMessage: String?

    init() {
        self.sdkManager = .shared
    }

    func login() {
        guard !email.isEmpty, !password.isEmpty else {
            errorMessage = "Email and password are required."
            isLoading = false
            return
        }

        isLoading = true
        errorMessage = nil

        Task {
            do {
                let credentials = BWell.Credentials.emailPassword(email: email, password: password)
                try await sdkManager.authenticate(credentials: credentials, clientToken: "")
                await MainActor.run {
                    isLoading = false
                }
            } catch {
                await MainActor.run {
                    errorMessage = "Login failed"
                    print(error)
                    isLoading = false
                }
            }
        }
    }
}
