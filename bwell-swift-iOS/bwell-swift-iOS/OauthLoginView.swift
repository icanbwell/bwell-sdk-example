//
//  OauthLoginView.swift
//  bwell-swift-iOS
//
//  Created by Kyle Wade on 8/12/24.
//

import Foundation
import SwiftUI

struct OauthLoginView: View {
    @EnvironmentObject var userManager: UserManager
    @EnvironmentObject var router: Router
    @State private var oauthToken: String = ""

    var body: some View {
        VStack {
            ClientKeySection()
            
            TextField("Enter OAuth Token", text: $oauthToken)
                .textFieldStyle(RoundedBorderTextFieldStyle())
                .padding()
                .keyboardType(.asciiCapable)

            Button("Login") {
                Task {
                    do {
                        try await userManager.login(oauthToken: oauthToken)
                        router.navigate(to: .helloWorld)
                    } catch {
                        print("Login failed: \(error)")
                    }
                }
            }
            .padding()

            Button("Switch to Username/Password") {
                router.navigate(to: .usernamePasswordLogin)
            }
            .padding(.top)
        }
        .padding()
    }
}

struct OauthLoginView_Previews: PreviewProvider {
    static var previews: some View {
        OauthLoginView()
            .environmentObject(Router())
    }
}
