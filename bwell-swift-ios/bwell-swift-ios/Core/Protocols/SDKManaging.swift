//
//  SDKManaging.swift
//  bwell-swift-ios
//
//  Protocol for SDK lifecycle management.
//  Enables dependency injection and testing with mocks.
//

import Foundation
import BWell

/// Protocol defining SDK management operations
@MainActor
protocol SDKManaging: ObservableObject {
    /// Current SDK state
    var state: SDKState { get }

    /// Initialize the SDK with a client key
    func initialize(_ clientKey: String) async throws

    /// Authenticate with credentials
    func authenticate(credentials: BWell.Credentials) async throws

    /// Check if there's an active session
    func checkSession() async throws

    /// Reset SDK to uninitialized state
    func reset() async

    /// Logout and clear tokens
    func logout() async

    /// Access the SDK instance
    var sdk: BWellSDK? { get }
}
