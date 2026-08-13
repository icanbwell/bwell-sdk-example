//
//  HealthSyncProcessingTests.swift
//  bwell-swift-iosTests
//
//  Pure-logic coverage for HealthSyncProcessing - no SDK/adapter dependency,
//  so these run in any clone regardless of vendor credentials.
//

import XCTest
@testable import bwell_swift_ios

final class HealthSyncProcessingTests: XCTestCase {
    func testComputeProgressIsInactiveWithNoSnapshot() {
        let progress = HealthSyncProcessing.computeProgress(nil, now: Date())
        XCTAssertEqual(progress, .inactive)
    }

    func testComputeProgressIsInactiveWhenStale() {
        let started = Date().addingTimeInterval(-HealthSyncProcessing.processingEstimate * HealthSyncProcessing.staleFactor - 1)
        let data = ProcessingData(total: 10, resources: [ProcessingResource(label: "Steps", count: 10)], startedAt: started)
        let progress = HealthSyncProcessing.computeProgress(data, now: Date())
        XCTAssertEqual(progress, .inactive)
    }

    func testComputeProgressAtStartShowsFirstResourceProcessing() {
        let now = Date()
        let data = ProcessingData(
            total: 10,
            resources: [ProcessingResource(label: "Steps", count: 5), ProcessingResource(label: "Sleep", count: 5)],
            startedAt: now
        )
        guard case .active(let total, let percent, let currentLabel, let currentState) = HealthSyncProcessing.computeProgress(data, now: now) else {
            return XCTFail("expected .active")
        }
        XCTAssertEqual(total, 10)
        XCTAssertEqual(percent, 0)
        XCTAssertEqual(currentLabel, "Steps")
        XCTAssertEqual(currentState, .processing)
    }

    func testComputeProgressWeightsSlicesByRecordCount() {
        // Steps has 3x Sleep's records, so it should get 3x the time slice -
        // right at Steps' 75%-of-window mark, Sleep's slice hasn't started yet.
        let now = Date()
        let data = ProcessingData(
            total: 40,
            resources: [ProcessingResource(label: "Steps", count: 30), ProcessingResource(label: "Sleep", count: 10)],
            startedAt: now
        )
        let slices = HealthSyncProcessing.computeSlices(data)
        XCTAssertEqual(slices.count, 2)
        XCTAssertEqual(slices[0].start, 0)
        XCTAssertEqual(slices[0].end, HealthSyncProcessing.processingEstimate * 0.75, accuracy: 0.001)
        XCTAssertEqual(slices[1].start, slices[0].end, accuracy: 0.001)
        XCTAssertEqual(slices[1].end, HealthSyncProcessing.processingEstimate, accuracy: 0.001)
    }

    func testComputeProgressCapsBelow100Percent() {
        // Even once elapsed time reaches the very end of the last slice, the
        // fill must stay at or below fillCapPercent - only real poll arrival
        // (owned by HealthSyncDashboardViewModel, not this pure function)
        // ever reaches 100. With a single resource it's also always "the
        // last one," so it holds at .processing rather than flipping to
        // .processed - there's nothing after it to advance to.
        let started = Date().addingTimeInterval(-HealthSyncProcessing.processingEstimate)
        let data = ProcessingData(total: 10, resources: [ProcessingResource(label: "Steps", count: 10)], startedAt: started)
        guard case .active(_, let percent, _, let state) = HealthSyncProcessing.computeProgress(data, now: Date()) else {
            return XCTFail("expected .active")
        }
        XCTAssertLessThanOrEqual(percent, HealthSyncProcessing.fillCapPercent)
        XCTAssertEqual(state, .processing)
    }

    func testComputeProgressShowsProcessedBeatForNonLastResource() {
        // Steps (not the last resource) should flip to .processed for a
        // short beat right at the end of its own slice, before Sleep's
        // slice begins.
        let now = Date()
        let data = ProcessingData(
            total: 10,
            resources: [ProcessingResource(label: "Steps", count: 5), ProcessingResource(label: "Sleep", count: 5)],
            startedAt: now
        )
        let stepsSliceEnd = HealthSyncProcessing.processingEstimate * 0.5
        let justBeforeSleep = now.addingTimeInterval(stepsSliceEnd - HealthSyncProcessing.resourceDoneBeat / 2)
        guard case .active(_, _, let currentLabel, let currentState) = HealthSyncProcessing.computeProgress(data, now: justBeforeSleep) else {
            return XCTFail("expected .active")
        }
        XCTAssertEqual(currentLabel, "Steps")
        XCTAssertEqual(currentState, .processed)
    }

    func testComputeProgressIsInactiveWithZeroTotal() {
        let data = ProcessingData(total: 0, resources: [], startedAt: Date())
        XCTAssertEqual(HealthSyncProcessing.computeProgress(data, now: Date()), .inactive)
    }

    func testFormatResourceLabelSplitsCamelCase() {
        XCTAssertEqual(HealthSyncProcessing.formatResourceLabel("activitySummary"), "Activity Summary")
        XCTAssertEqual(HealthSyncProcessing.formatResourceLabel("steps"), "Steps")
        XCTAssertEqual(HealthSyncProcessing.formatResourceLabel("heartRateVariability"), "Heart Rate Variability")
    }
}
