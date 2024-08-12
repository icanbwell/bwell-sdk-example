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
    var clientKey: String = ""
    
    var email: String?
    var password: String?
    var oauthToken: String?
    
    func initialize(clientKey: String) {
        self.clientKey = clientKey;
    }

    func login(oauthToken: String? = nil, email: String? = nil, password: String? = nil) throws {
        guard (oauthToken != nil || (email != nil && password != nil)) else {
            throw LoginError.missingCredentials
        }
        
        self.isLoggedIn = true
    }

    func logout() {
        self.email = ""
        self.password = ""
        self.clientKey = ""
        self.oauthToken = ""
        self.isLoggedIn = false
    }
}
