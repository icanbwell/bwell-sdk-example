//
//  ContentView.swift
//  bwell-swift-iOS
//
//  Created by deric kramer on 7/9/24.
//

import SwiftUI
import bwell_sdk_swift

struct ContentView: View {
    var body: some View {
        VStack {
            Image(systemName: "globe")
                .imageScale(.large)
                .foregroundStyle(.tint)
            Text(BWellSdk().hello())
        }
        .padding()
    }
}

#Preview {
    ContentView()
}
