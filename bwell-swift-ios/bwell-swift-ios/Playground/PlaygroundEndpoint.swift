//
//  PlaygroundEndpoint.swift
//  bwell-swift-ios
//
//  Shared contract for any feature's "Playground" screen — a scrollable list
//  of independent, per-endpoint cards, each with its own Run button and
//  result. Feature modules (see HealthSync/) implement this per endpoint;
//  PlaygroundCard renders it generically.
//

import Foundation

protocol PlaygroundEndpoint: Identifiable, Hashable, CaseIterable {
    var title: String { get }
    var explanation: String { get }
    var isBlocked: Bool { get }
    var blockedReason: String? { get }
}

enum PlaygroundCardState {
    case idle
    case loading
    case success(String)
    case error(String)
}
