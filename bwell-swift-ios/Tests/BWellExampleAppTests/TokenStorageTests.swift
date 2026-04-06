//
//  TokenStorageTests.swift
//  bwell-swift-ios
//
//  Tests for token storage implementations.
//

import XCTest
@testable import bwell_swift_ios

final class TokenStorageTests: XCTestCase {
    var sut: MockTokenStorage!

    override func setUp() async throws {
        sut = MockTokenStorage()
    }

    override func tearDown() async throws {
        sut = nil
    }

    // MARK: - Save Tests

    func testSave_StoresToken() async throws {
        // Given
        let token = "test-token-123"

        // When
        try await sut.save(token)

        // Then
        let saveCount = await sut.saveCallCount
        XCTAssertEqual(saveCount, 1)

        let retrieved = try await sut.load()
        XCTAssertEqual(retrieved, token)
    }

    func testSave_ThrowsWhenConfigured() async throws {
        // Given
        await sut.setShouldThrowOnSave(true)

        // When/Then
        do {
            try await sut.save("token")
            XCTFail("Should have thrown")
        } catch {
            XCTAssertTrue(error is KeychainError)
        }
    }

    // MARK: - Load Tests

    func testLoad_ReturnsNilWhenEmpty() async throws {
        // When
        let token = try await sut.load()

        // Then
        XCTAssertNil(token)
        let loadCount = await sut.loadCallCount
        XCTAssertEqual(loadCount, 1)
    }

    func testLoad_ReturnsSavedToken() async throws {
        // Given
        let expected = "saved-token"
        try await sut.save(expected)

        // When
        let actual = try await sut.load()

        // Then
        XCTAssertEqual(actual, expected)
    }

    func testLoad_ThrowsWhenConfigured() async throws {
        // Given
        await sut.setShouldThrowOnLoad(true)

        // When/Then
        do {
            _ = try await sut.load()
            XCTFail("Should have thrown")
        } catch {
            XCTAssertTrue(error is KeychainError)
        }
    }

    // MARK: - Delete Tests

    func testDelete_RemovesToken() async throws {
        // Given
        try await sut.save("token")

        // When
        try await sut.delete()

        // Then
        let token = try await sut.load()
        XCTAssertNil(token)

        let deleteCount = await sut.deleteCallCount
        XCTAssertEqual(deleteCount, 1)
    }

    // MARK: - Clear Tests

    func testClear_RemovesAllTokens() async throws {
        // Given
        try await sut.save("token1")
        try await sut.save("token2")

        // When
        try await sut.clear()

        // Then
        let token = try await sut.load()
        XCTAssertNil(token)

        let clearCount = await sut.clearCallCount
        XCTAssertEqual(clearCount, 1)
    }

    // MARK: - Reset Tests

    func testReset_ClearsCounters() async throws {
        // Given
        try await sut.save("token")
        try await sut.load()
        try await sut.delete()

        // When
        await sut.reset()

        // Then
        let saveCount = await sut.saveCallCount
        let loadCount = await sut.loadCallCount
        let deleteCount = await sut.deleteCallCount

        XCTAssertEqual(saveCount, 0)
        XCTAssertEqual(loadCount, 0)
        XCTAssertEqual(deleteCount, 0)
    }
}

// MARK: - Helper Extensions for Testing

extension MockTokenStorage {
    func setShouldThrowOnSave(_ value: Bool) async {
        shouldThrowOnSave = value
    }

    func setShouldThrowOnLoad(_ value: Bool) async {
        shouldThrowOnLoad = value
    }
}
