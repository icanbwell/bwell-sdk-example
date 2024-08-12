//
//  ContentView.swift
//  bwell-swift-iOS
//
//  Created by deric kramer on 7/9/24.
//

import SwiftUI
import bwell_sdk_swift

struct HelloWorldView: View {
    var body: some View {
        VStack {
            Spacer()
            
            Image(systemName: "globe")
                .imageScale(.large)
                .foregroundStyle(.tint)
            
            Text(BWellSdk().hello())
            
            Spacer()
            
            LogoutButton()
        }
        .padding()
    }
}

#Preview {
    HelloWorldView()
}
