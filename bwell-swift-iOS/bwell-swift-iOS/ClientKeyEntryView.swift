//
//  ClientKeyEntryView.swift
//  bwell-swift-iOS
//
//  Created by Kyle Wade on 8/12/24.
//

import Foundation
import SwiftUI

struct ClientKeyEntryView: View {
    @EnvironmentObject var router: Router
    @EnvironmentObject var userManager: UserManager
    
    @State private var clientKey: String = ""
    
    var body: some View {
        VStack {
            Text("Welcome to b.well");
            
            TextField("Enter Client Key", text: $clientKey)
                .textFieldStyle(RoundedBorderTextFieldStyle())
                .padding()
                .keyboardType(.asciiCapable)
                .autocapitalization(/*@START_MENU_TOKEN@*/.none/*@END_MENU_TOKEN@*/)
            
            if clientKey.isEmpty {
                Text("Client Key is required.")
                    .foregroundColor(.red)
                    .padding(.bottom, 8)
            }
            
            Button("Next") {
                Task {
                    try await userManager.initialize(clientKey: clientKey);
                    router.navigate(to: .oauthLogin) // Default to OAuth login
                }
            }
            .padding()
            .disabled(clientKey.isEmpty) // Disable the button if clientKey is empty
        }
        .padding()
    }
}

struct ClientKeyEntryView_Previews: PreviewProvider {
    static var previews: some View {
        ClientKeyEntryView()
            .environmentObject(Router())
    }
}
