//
//  HealthSyncPlaygroundEndpointTests.swift
//  bwell-swift-iosTests
//
//  Covers HealthSyncPlaygroundEndpoint.isBlocked/blockedReason. This public
//  repo never links the on-device adapter (see file header on
//  HealthSyncPlaygroundViewModel.swift), so BWellHealthSyncType.isConfigured
//  is guaranteed false in every build these tests run in - that's exactly
//  the branch exercised here. The "configured" branch (all 5 gated
//  endpoints unblocked) can only be verified by a developer who has linked
//  the adapter locally; there's no way to flip that flag from this repo.
//

import XCTest
@testable import bwell_swift_ios

final class HealthSyncPlaygroundEndpointTests: XCTestCase {
    private let gatedEndpoints: Set<HealthSyncPlaygroundEndpoint> = [
        .connect, .disconnect, .requestPermissions, .sync, .getCurrentUser,
    ]

    func testGatedEndpointsAreBlockedWithoutAnAdapter() {
        for endpoint in gatedEndpoints {
            XCTAssertTrue(endpoint.isBlocked, "\(endpoint) should be blocked with no adapter linked")
            XCTAssertEqual(endpoint.blockedReason, HealthSyncPlaygroundEndpoint.notConfiguredMessage)
        }
    }

    func testNonGatedEndpointsAreNeverBlocked() {
        for endpoint in HealthSyncPlaygroundEndpoint.allCases where !gatedEndpoints.contains(endpoint) {
            XCTAssertFalse(endpoint.isBlocked, "\(endpoint) should never be blocked - it doesn't need the adapter")
            XCTAssertNil(endpoint.blockedReason)
        }
    }

    func testExactlyFiveEndpointsAreGated() {
        // Pins the count so a future case added to the enum without being
        // added to (or deliberately left out of) gatedEndpoints above shows
        // up as a failing assertion rather than silently falling into
        // whichever bucket the loops above happen to iterate.
        XCTAssertEqual(HealthSyncPlaygroundEndpoint.allCases.filter(\.isBlocked).count, 5)
    }
}
