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
    private let sdkManager: BWellSDKManager
    
    // Authentication published properties
    @Published var clientKey: String = "eyJyIjoiY2Zoa2h3ODZvNHdoNWFiOW9kaHgiLCJlbnYiOiJkZXYiLCJraWQiOiJid2VsbF9kZW1vLWRldiJ9"
    @Published var email: String = "test@test.test"
    @Published var password: String = "Password@1"
    
    // Published properties
    @Published var isLoading: Bool = false
    @Published var authenticated: Bool = false
    @Published var errorMessage: String?
    
    init() {
        sdkManager = .shared
    }
    
    func initializeSDK() {
        isLoading = true
        guard !clientKey.isEmpty else {
            errorMessage = "Client key is required."
            isLoading = false
            return
        }
        
        Task {
            do {
                try await sdkManager.initilize(clientKey: clientKey)
                
                self.authenticated = true
                self.isLoading = false
                
            } catch {
                
                errorMessage = "SDK initilization failed"
                isLoading = false
                
            }
        }
    }
    
    func login() {
        isLoading = true
        
        guard !email.isEmpty, !password.isEmpty else {
            errorMessage = "Email and password are required."
            isLoading = false
            return
        }
        
        Task {
            do {
                let credentials = BWell.Credentials.emailPassword(email: email, password: password)
                
                try await sdkManager.authenticate(credentials: credentials, clientToken: "")
                await createConsent()
                
                isLoading = false
            } catch {
                errorMessage = "Login failed"
                isLoading = false
            }
        }
    }
    
    private func createConsent() async {
        do {
            let request: BWell.CreateConsentRequest = .init(status: .active,
                                                            provision: .init(type: .permit),
                                                            category: .tos)
            
            _ = try await sdkManager.user().createConsent(request)
        } catch {
            errorMessage = "TOS consent creation failed"
        }
    }
}
