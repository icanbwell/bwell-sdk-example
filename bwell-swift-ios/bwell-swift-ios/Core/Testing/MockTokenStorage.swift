//
//  MockTokenStorage.swift
//  bwell-swift-ios
//
//  Mock token storage for testing. Thread-safe with actor.
//

import Foundation

enum MockStorageError: Error {
    case saveFailureSimulated
    case loadFailureSimulated
}

actor MockTokenStorage: TokenStorage {
    private var tokens: [String: String] = [:]
    var saveCallCount = 0
    var loadCallCount = 0
    var deleteCallCount = 0
    var clearCallCount = 0

    var shouldThrowOnSave = false
    var shouldThrowOnLoad = false

    func save(_ token: String) async throws {
        saveCallCount += 1
        if shouldThrowOnSave {
            throw MockStorageError.saveFailureSimulated
        }
        tokens["default"] = token
    }

    func load() async throws -> String? {
        loadCallCount += 1
        if shouldThrowOnLoad {
            throw MockStorageError.loadFailureSimulated
        }
        return tokens["default"]
    }

    func delete() async throws {
        deleteCallCount += 1
        tokens.removeValue(forKey: "default")
    }

    func clear() async throws {
        clearCallCount += 1
        tokens.removeAll()
    }

    func reset() {
        tokens.removeAll()
        saveCallCount = 0
        loadCallCount = 0
        deleteCallCount = 0
        clearCallCount = 0
        shouldThrowOnSave = false
        shouldThrowOnLoad = false
    }
}
