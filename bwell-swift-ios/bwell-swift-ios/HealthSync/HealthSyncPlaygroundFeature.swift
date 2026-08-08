//
//  HealthSyncPlaygroundFeature.swift
//  bwell-swift-ios
//
//  Registers this demo with the shared Playground infrastructure (see
//  Playground/PlaygroundFeature.swift) — the only wiring point a new feature
//  playground needs beyond its own files.
//

import SwiftUI

extension PlaygroundFeature {
    static let healthSync = PlaygroundFeature(
        id: "health-sync",
        title: "Health Sync",
        icon: "heart.text.square",
        description: "Connect, sync, and query on-device health data",
        destination: { AnyView(HealthSyncPlaygroundRootView()) }
    )
}
