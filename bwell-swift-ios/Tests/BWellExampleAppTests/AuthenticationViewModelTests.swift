//
//  AuthenticationViewModelTests.swift
//  bwell-swift-ios
//
//  Tests for authentication flow with dependency injection.
//

import XCTest
import Combine
@testable import bwell_swift_ios

@MainActor
final class AuthenticationViewModelTests: XCTestCase {
    var sut: AuthenticationViewModel!
    var mockSDKManager: SDKManager!
    var mockStorage: MockTokenStorage!
    var mockAPIKeyStorage: MockAPIKeyStorage!
    var cancellables: Set<AnyCancellable>!

    override func setUp() async throws {
        mockStorage = MockTokenStorage()
        mockSDKManager = SDKManager(tokenStorage: mockStorage)
        mockAPIKeyStorage = MockAPIKeyStorage()
        sut = AuthenticationViewModel(
            sdkManager: mockSDKManager,
            apiKeyStorage: mockAPIKeyStorage
        )
        cancellables = []
    }

    override func tearDown() async throws {
        sut = nil
        mockSDKManager = nil
        mockStorage = nil
        mockAPIKeyStorage = nil
        cancellables = nil
    }

    // MARK: - Initialization Tests

    func testInitialState() {
        XCTAssertFalse(sut.isLoading)
        XCTAssertFalse(sut.apiKeyValidated)
        XCTAssertTrue(sut.apiKey.isEmpty)
        XCTAssertTrue(sut.username.isEmpty)
        XCTAssertTrue(sut.password.isEmpty)
    }

    func testLoadsSavedAPIKey() {
        // Given
        mockAPIKeyStorage.save("saved-key")

        // When
        let vm = AuthenticationViewModel(
            sdkManager: mockSDKManager,
            apiKeyStorage: mockAPIKeyStorage
        )

        // Then
        XCTAssertEqual(vm.apiKey, "saved-key")
    }

    // MARK: - SDK Initialization Tests

    func testInitializeSDK_WithEmptyKey_ShowsError() {
        // Given
        sut.apiKey = ""

        // When
        sut.initializeSDK()

        // Then
        XCTAssertEqual(sut.errorMessage, "API key is required")
        XCTAssertFalse(sut.isLoading)
    }

    func testInitializeSDK_WithValidKey_StartsLoading() {
        // Given
        sut.apiKey = "test-key"

        // When
        sut.initializeSDK()

        // Then
        XCTAssertTrue(sut.isLoading)
    }

    // MARK: - OAuth Login Tests

    func testLoginWithOAuth_WithEmptyToken_ShowsError() {
        // Given
        sut.oauthToken = ""

        // When
        sut.loginWithOAuthToken()

        // Then
        XCTAssertEqual(sut.errorMessage, "OAuth token is required")
        XCTAssertFalse(sut.isLoading)
    }

    func testLoginWithOAuth_WithToken_StartsLoading() {
        // Given
        sut.oauthToken = "test-token"

        // When
        sut.loginWithOAuthToken()

        // Then
        XCTAssertTrue(sut.isLoading)
    }

    // MARK: - Username/Password Login Tests

    func testLoginWithCredentials_WithEmptyUsername_ShowsError() {
        // Given
        sut.username = ""
        sut.password = "password"

        // When
        sut.loginWithUsernameAndPassword()

        // Then
        XCTAssertEqual(sut.usernameErrorMessage, "Username is required")
        XCTAssertFalse(sut.isLoading)
    }

    func testLoginWithCredentials_WithEmptyPassword_ShowsError() {
        // Given
        sut.username = "user"
        sut.password = ""

        // When
        sut.loginWithUsernameAndPassword()

        // Then
        XCTAssertEqual(sut.passwordErrorMessage, "Password is required")
        XCTAssertFalse(sut.isLoading)
    }

    func testLoginWithCredentials_WithValidInputs_StartsLoading() {
        // Given
        sut.username = "user"
        sut.password = "password"

        // When
        sut.loginWithUsernameAndPassword()

        // Then
        XCTAssertTrue(sut.isLoading)
    }

    // MARK: - State Observation Tests

    func testObservesSDKStateChanges() {
        let expectation = XCTestExpectation(description: "State change observed")

        sut.$apiKeyValidated
            .dropFirst()
            .sink { validated in
                if validated {
                    expectation.fulfill()
                }
            }
            .store(in: &cancellables)

        // Simulate SDK state change
        // In real scenario, SDKManager would update its state

        wait(for: [expectation], timeout: 1.0)
    }
}

// MARK: - Mock API Key Storage

final class MockAPIKeyStorage: APIKeyStorage {
    private var storedKey: String?

    func save(_ key: String) {
        storedKey = key
    }

    func load() -> String? {
        storedKey
    }

    func clear() {
        storedKey = nil
    }
}
