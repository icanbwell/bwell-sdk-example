//
//  LoginView.swift
//  bwell-swift-iOS
//
//  Created by Kyle Wade on 8/12/24.
//

import Foundation
import SwiftUI

struct UsernamePasswordLoginView: View {
    @EnvironmentObject var userManager: UserManager
    @EnvironmentObject var router: Router
    @State private var email: String = ""
    @State private var password: String = ""
    
    var body: some View {
        VStack {
            ClientKeySection()
            
            TextField("Enter Email", text: $email)
                .textFieldStyle(RoundedBorderTextFieldStyle())
                .padding()
                .keyboardType(.emailAddress)
            
            SecureField("Enter Password", text: $password)
                .textFieldStyle(RoundedBorderTextFieldStyle())
                .padding()
            
            Button("Login") {
                Task {
                    do {
                        try await userManager.login(email: email, password: password)
                        router.navigate(to: .helloWorld)
                    } catch {
                        print("Login failed: \(error)")
                    }
                }
            }
            .padding()
            
            Button("Switch to OAuth") {
                router.navigate(to: .oauthLogin)
            }
            .padding(.top)
        }
        .padding()
    }
}

struct UsernamePasswordLoginView_Previews: PreviewProvider {
    static var previews: some View {
        UsernamePasswordLoginView()
            .environmentObject(Router())
    }
}
