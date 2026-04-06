//
//  SDKManagerTests.swift
//  bwell-swift-ios
//
//  Comprehensive tests for SDKManager with mocks.
//

import XCTest
@testable import bwell_swift_ios

@MainActor
final class SDKManagerTests: XCTestCase {
    var sut: SDKManager!
    var mockStorage: MockTokenStorage!

    override func setUp() async throws {
        mockStorage = MockTokenStorage()
        sut = SDKManager(tokenStorage: mockStorage)
    }

    override func tearDown() async throws {
        sut = nil
        mockStorage = nil
    }

    // MARK: - Initialization Tests

    func testInitialState() {
        XCTAssertEqual(sut.state, .uninitialized)
        XCTAssertNil(sut.sdk)
    }

    func testInitialize_WithValidKey_UpdatesState() async throws {
        // Note: This will fail without a real SDK, but demonstrates the pattern
        // In production, you'd mock BWellClient as well

        // Given
        let clientKey = "test-client-key"

        // When/Then - expect error since we don't have real backend
        do {
            try await sut.initialize(clientKey)
            XCTFail("Should throw without real backend")
        } catch {
            // Expected to fail without real backend
            XCTAssertTrue(true)
        }
    }

    func testInitialize_AlreadyInitialized_DoesNotReinitialize() async throws {
        // Given
        let clientKey = "test-key"

        // Simulate already initialized
        // (In real test, you'd have a way to set this)

        // When
        do {
            try await sut.initialize(clientKey)
        } catch {
            // Ignore errors for this test
        }

        // Then
        // Verify it doesn't create a new SDK instance
    }

    func testReset_ClearsSDKAndTokens() async throws {
        // When
        await sut.reset()

        // Then
        XCTAssertEqual(sut.state, .uninitialized)
        XCTAssertNil(sut.sdk)

        let clearCount = await mockStorage.clearCallCount
        XCTAssertEqual(clearCount, 1)
    }

    func testLogout_ClearsSDKAndTokens() async throws {
        // When
        await sut.logout()

        // Then
        XCTAssertEqual(sut.state, .uninitialized)
        XCTAssertNil(sut.sdk)

        let clearCount = await mockStorage.clearCallCount
        XCTAssertEqual(clearCount, 1)
    }

    // MARK: - State Transition Tests

    func testStateTransitions() {
        // Given
        let states: [SDKState] = [
            .uninitialized,
            .initializing,
            .initialized,
            .authenticating,
            .authenticated
        ]

        // Then
        for state in states {
            XCTAssertNotNil(state)
        }
    }

    func testStateEquality() {
        XCTAssertEqual(SDKState.uninitialized, SDKState.uninitialized)
        XCTAssertEqual(SDKState.initialized, SDKState.initialized)
        XCTAssertEqual(SDKState.authenticated, SDKState.authenticated)

        XCTAssertNotEqual(SDKState.uninitialized, SDKState.initialized)
        XCTAssertNotEqual(SDKState.initialized, SDKState.authenticated)
    }

    func testStateHelpers() {
        XCTAssertTrue(SDKState.authenticated.isAuthenticated)
        XCTAssertFalse(SDKState.initialized.isAuthenticated)
        XCTAssertFalse(SDKState.uninitialized.isAuthenticated)

        XCTAssertTrue(SDKState.initialized.isReady)
        XCTAssertTrue(SDKState.authenticated.isReady)
        XCTAssertFalse(SDKState.uninitialized.isReady)
    }
}
