//
//  UserStateManager.swift
//  bwell-swift-iOS
//
//  Created by Kyle Wade on 8/12/24.
//

import Foundation

enum LoginError: Error {
    case missingCredentials
}

class UserManager: ObservableObject {
    @Published var isLoggedIn: Bool = false
    public var clientKey: String = "";
    
    var email: String?
    var password: String?
    var oauthToken: String?
    
    private var sdkSingleton: SdkSingleton
    
    init(sdkSingleton: SdkSingleton) {
        self.sdkSingleton = sdkSingleton
    }
    
    func initialize(clientKey: String) async {
        do {
            try await sdkSingleton.configure(clientKey: clientKey.trimmingCharacters(in: .whitespacesAndNewlines))
            self.clientKey = clientKey;
        } catch {
            print("Error configuring SDK: \(error)")
        }
    }
    
    func login(oauthToken: String? = nil, email: String? = nil, password: String? = nil) async throws {
        guard (oauthToken != nil || (email != nil && password != nil)) else {
            throw LoginError.missingCredentials
        }
        
        //try await sdkSingleton.getInstance().authenticate(oauthCredentials: oauthToken, email: email, password: password)
        self.isLoggedIn = true
    }
    
    func logout() {
        self.email = ""
        self.password = ""
        self.oauthToken = ""
        self.isLoggedIn = false
    }
}
