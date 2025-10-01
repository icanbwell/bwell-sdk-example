//
//  bwell_swift_iOSApp.swift
//  bwell-swift-iOS
//
//  Created by deric kramer on 7/9/24.
//

import SwiftUI

@main
struct bwell_swift_iOSApp: App {
    @StateObject private var router = Router()
    @StateObject private var userManager: UserManager
    
    init() {
        let sdkSingleton = SdkSingleton()
        _userManager = StateObject(wrappedValue: UserManager(sdkSingleton: sdkSingleton))
        
        //TODO: check for stored credentials/configuration, initialize sdk singleton here if we can
    }

    var body: some Scene {
        WindowGroup {
            RouterView()
                .environmentObject(router)
                .environmentObject(userManager)
        }
    }
}
