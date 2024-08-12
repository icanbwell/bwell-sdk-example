//
//  _clientKeySection.swift
//  bwell-swift-iOS
//
//  Created by Kyle Wade on 8/12/24.
//

import Foundation
import SwiftUI

struct ClientKeySection: View {
    @EnvironmentObject var router: Router
    @EnvironmentObject var userManager: UserManager

    var body: some View {
        VStack {
            Text("Client Key: \(userManager.clientKey)")
                .font(.subheadline)
                .padding(.bottom, 8)

            Button("Clear Client Key") {
                userManager.clientKey = ""
                router.navigate(to: .clientKeyEntry)
            }
            .foregroundColor(.red)
        }
        .padding(.bottom, 16)
    }
}

struct ClientKeySection_Previews: PreviewProvider {
    static var previews: some View {
        ClientKeySection()
            .environmentObject(UserManager())
            .environmentObject(Router())
    }
}
