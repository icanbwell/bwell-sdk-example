//
//  PlaygroundFeature.swift
//  bwell-swift-ios
//
//  Registry that makes adding a new Playground zero-touch for navigation.
//  A feature module defines its own `static let <name>: PlaygroundFeature`
//  (see HealthSync/HealthSyncPlaygroundFeature.swift) and appends itself to
//  `PlaygroundRegistry.all` — that's the only registration point. Router.swift,
//  BrowseView.swift, and MainTabView.swift never need another edit for a
//  future second, third, etc. Playground.
//

import SwiftUI

struct PlaygroundFeature: Identifiable {
    let id: String
    let title: String
    let icon: String
    let description: String
    let destination: () -> AnyView
}

enum PlaygroundRegistry {
    static let all: [PlaygroundFeature] = [
        .healthSync
    ]
}
