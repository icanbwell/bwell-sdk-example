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
            return
        }

        errorMessage = nil

        Task {
            do {
                let credentials = BWell.Credentials.emailPassword(email: email, password: password)
                try await sdkManager.authenticate(credentials: credentials, clientToken: "")
            } catch {
                errorMessage = "Login failed"
            }
        }
    }
}
