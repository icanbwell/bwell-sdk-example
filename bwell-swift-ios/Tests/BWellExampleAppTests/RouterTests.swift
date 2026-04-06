//
//  RouterTests.swift
//  bwell-swift-ios
//
//  Tests for navigation routing.
//

import XCTest
@testable import bwell_swift_ios

@MainActor
final class RouterTests: XCTestCase {
    var sut: Router!

    override func setUp() {
        sut = Router()
    }

    override func tearDown() {
        sut = nil
    }

    // MARK: - Navigation Tests

    func testInitialState() {
        XCTAssertTrue(sut.path.isEmpty)
    }

    func testNavigate_AppendsDestination() {
        // When
        sut.navigate(to: .home)

        // Then
        XCTAssertEqual(sut.path.count, 1)
    }

    func testNavigate_MultipleTimes_AppendsEach() {
        // When
        sut.navigate(to: .home)
        sut.navigate(to: .profile)
        sut.navigate(to: .healthSummary)

        // Then
        XCTAssertEqual(sut.path.count, 3)
    }

    func testNavigateAndReplace_ClearsPathFirst() {
        // Given
        sut.navigate(to: .home)
        sut.navigate(to: .profile)
        XCTAssertEqual(sut.path.count, 2)

        // When
        sut.navigateAndReplace(to: .healthSummary)

        // Then
        XCTAssertEqual(sut.path.count, 1)
    }

    func testPop_RemovesLastDestination() {
        // Given
        sut.navigate(to: .home)
        sut.navigate(to: .profile)

        // When
        sut.pop()

        // Then
        XCTAssertEqual(sut.path.count, 1)
    }

    func testPop_OnEmptyPath_DoesNothing() {
        // When
        sut.pop()

        // Then
        XCTAssertTrue(sut.path.isEmpty)
    }

    func testPopToRoot_ClearsEntirePath() {
        // Given
        sut.navigate(to: .home)
        sut.navigate(to: .profile)
        sut.navigate(to: .healthSummary)
        sut.navigate(to: .connections)

        // When
        sut.popToRoot()

        // Then
        XCTAssertTrue(sut.path.isEmpty)
    }

    // MARK: - Destination Tests

    func testDestinationsAreHashable() {
        let destinations: Set<Destination> = [
            .home,
            .profile,
            .healthSummary,
            .connections
        ]

        XCTAssertEqual(destinations.count, 4)
    }

    func testDestinationEquality() {
        XCTAssertEqual(Destination.home, Destination.home)
        XCTAssertNotEqual(Destination.home, Destination.profile)

        let connection1 = ConnectionData(id: "1", name: "Test")
        let connection2 = ConnectionData(id: "1", name: "Test")
        XCTAssertEqual(
            Destination.searchConnections(connection: connection1),
            Destination.searchConnections(connection: connection2)
        )
    }
}
