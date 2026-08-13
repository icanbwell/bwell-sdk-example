//
//  HealthGradeTests.swift
//  bwell-swift-iosTests
//
//  Pure-logic coverage for HealthGrade.from(score:) - the A-F thresholds
//  ported from ui-platform's GRADE_THRESHOLDS.
//

import XCTest
@testable import bwell_swift_ios

final class HealthGradeTests: XCTestCase {
    func testThresholdBoundaries() {
        XCTAssertEqual(HealthGrade.from(score: 100), .a)
        XCTAssertEqual(HealthGrade.from(score: 90), .a)
        XCTAssertEqual(HealthGrade.from(score: 89.9), .b)
        XCTAssertEqual(HealthGrade.from(score: 75), .b)
        XCTAssertEqual(HealthGrade.from(score: 74.9), .c)
        XCTAssertEqual(HealthGrade.from(score: 60), .c)
        XCTAssertEqual(HealthGrade.from(score: 59.9), .d)
        XCTAssertEqual(HealthGrade.from(score: 45), .d)
        XCTAssertEqual(HealthGrade.from(score: 44.9), .f)
        XCTAssertEqual(HealthGrade.from(score: 0), .f)
    }

    func testLabelsMatchGrade() {
        XCTAssertEqual(HealthGrade.a.label, "Excellent")
        XCTAssertEqual(HealthGrade.b.label, "Good")
        XCTAssertEqual(HealthGrade.c.label, "Fair")
        XCTAssertEqual(HealthGrade.d.label, "Low")
        XCTAssertEqual(HealthGrade.f.label, "Poor")
    }
}
