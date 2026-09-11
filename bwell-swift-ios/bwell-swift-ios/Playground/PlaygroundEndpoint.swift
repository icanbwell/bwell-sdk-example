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

    /// Optional parameter-requirement note, shown in the info sheet (see
    /// PlaygroundEndpointInfoSheet.swift). Defaults to nil - only implement
    /// for endpoints that take input.
    var parameterInfo: String? { get }

    /// Optional documentation link, shown as a button in the info sheet.
    /// Defaults to nil.
    var documentationURL: URL? { get }

    /// Optional longer, doc-sourced description for the info sheet. Falls
    /// back to `explanation` (the short, card-visible blurb) when nil - so
    /// an endpoint with no dedicated doc page still shows something
    /// sensible instead of a special case in the view.
    var documentationSummary: String? { get }
}

extension PlaygroundEndpoint {
    var parameterInfo: String? { nil }
    var documentationURL: URL? { nil }
    var documentationSummary: String? { nil }
}

enum PlaygroundCardState {
    case idle
    case loading
    case success(String)
    case error(String)
}
