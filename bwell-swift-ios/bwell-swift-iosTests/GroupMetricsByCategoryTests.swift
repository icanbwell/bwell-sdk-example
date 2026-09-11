//
//  GroupMetricsByCategoryTests.swift
//  bwell-swift-iosTests
//
//  Coverage for groupMetricsByCategory - ported from ui-platform's
//  groupMetricsByDisplayGroup. BWell.DeviceMetricsGroup/CodeableConcept/
//  Coding have no public memberwise init (only synthesized Decodable), so
//  test fixtures are built via JSONDecoder instead.
//

import BWellSDK
import XCTest
@testable import bwell_swift_ios

final class GroupMetricsByCategoryTests: XCTestCase {
    private static let displayGroupSystem = "https://www.icanbwell.com/display-group"

    private func makeGroup(system: String?, code: String?, display: String?) throws -> BWell.DeviceMetricsGroup {
        let categoryJSON: String
        if let system, let code, let display {
            categoryJSON = "[{\"coding\": [{\"system\": \"\(system)\", \"code\": \"\(code)\", \"display\": \"\(display)\"}]}]"
        } else {
            categoryJSON = "null"
        }
        let json = "{\"category\": \(categoryJSON)}"
        return try JSONDecoder().decode(BWell.DeviceMetricsGroup.self, from: Data(json.utf8))
    }

    func testGroupsByDisplayGroupCoding() throws {
        let cardio1 = try makeGroup(system: Self.displayGroupSystem, code: "cardio", display: "Cardiovascular")
        let metabolic = try makeGroup(system: Self.displayGroupSystem, code: "metabolic", display: "Metabolic")
        let cardio2 = try makeGroup(system: Self.displayGroupSystem, code: "cardio", display: "Cardiovascular")

        let grouped = groupMetricsByCategory([cardio1, metabolic, cardio2])

        XCTAssertEqual(grouped.count, 2)
        XCTAssertEqual(grouped[0].label, "Cardiovascular")
        XCTAssertEqual(grouped[0].items.count, 2)
        XCTAssertEqual(grouped[1].label, "Metabolic")
        XCTAssertEqual(grouped[1].items.count, 1)
    }

    func testFallsBackToOtherWhenNoMatchingCoding() throws {
        let uncategorized = try makeGroup(system: nil, code: nil, display: nil)
        let wrongSystem = try makeGroup(system: "https://example.com/some-other-system", code: "x", display: "X")

        let grouped = groupMetricsByCategory([uncategorized, wrongSystem])

        XCTAssertEqual(grouped.count, 1)
        XCTAssertEqual(grouped[0].id, "other")
        XCTAssertEqual(grouped[0].label, "Other")
        XCTAssertEqual(grouped[0].items.count, 2)
    }

    func testPreservesFirstSeenOrder() throws {
        let b = try makeGroup(system: Self.displayGroupSystem, code: "b", display: "B")
        let a = try makeGroup(system: Self.displayGroupSystem, code: "a", display: "A")

        let grouped = groupMetricsByCategory([b, a])

        XCTAssertEqual(grouped.map(\.id), ["b", "a"])
    }
}
