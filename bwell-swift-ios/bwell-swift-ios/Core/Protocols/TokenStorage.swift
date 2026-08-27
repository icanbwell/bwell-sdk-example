//
//  TokenStorage.swift
//  bwell-swift-ios
//
//  Protocol for secure token storage abstraction.
//  Enables dependency injection and testing with mocks.
//

import Foundation

/// Protocol defining secure token storage operations
protocol TokenStorage: Sendable {
    /// Save a token securely
    func save(_ token: String) async throws

    /// Load a token from secure storage
    func load() async throws -> String?

    /// Delete a specific token
    func delete() async throws

    /// Clear all tokens managed by this storage
    func clear() async throws
}
