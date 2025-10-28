//
//  bwell_swift_iosApp.swift
//  bwell-swift-ios
//
//  Created by Ivan Villanueva on 27/10/25.
//

import SwiftUI

@main
struct bwell_swift_iosApp: App {
    @StateObject private var bwellSDKManager = BWellSDKManager.shared
    @StateObject private var router = NavigationRouter()

    var body: some Scene {
        WindowGroup {
            RootView()
                .environmentObject(bwellSDKManager)
                .environmentObject(router)
        }
    }
}
