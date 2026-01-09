//
//  _logoutButton.swift
//  bwell-swift-iOS
//
//  Created by Kyle Wade on 8/12/24.
//

import Foundation
import SwiftUI

struct LogoutButton: View {
    @EnvironmentObject var userManager: UserManager
    @EnvironmentObject var router: Router

    var body: some View {
        Button(action: {
            userManager.logout()
            router.navigate(to: .clientKeyEntry) // Redirect to the client key entry view after logout
        }) {
            Text("Logout")
                .padding()
                .background(Color.red)
                .foregroundColor(.white)
                .cornerRadius(8)
        }
    }
}

struct LogoutButton_Previews: PreviewProvider {
    static var previews: some View {
        LogoutButton()
            .environmentObject(UserManager(sdkSingleton: SdkSingleton()))
            .environmentObject(Router())
    }
}
