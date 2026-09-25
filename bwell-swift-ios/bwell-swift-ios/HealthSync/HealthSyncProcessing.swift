//
//  HealthSyncProcessing.swift
//  bwell-swift-ios
//
//  Ported from ui-platform's libs/mfe-devices/src/lib/processing (Device
//  ProcessingState/useProcessingProgress/constants) — same port as Kotlin's
//  HealthSyncProcessing.kt. sync() only tells us the on-device record count,
//  not real server-side processing progress - this simulates it: each
//  resource type gets a slice of a fixed estimate window proportional to its
//  record count, and the fill rises through that slice as time elapses.
//  Only real data arrival (the poll in HealthSyncDashboardViewModel) ever
//  actually completes the wait - the timer alone is capped below 100%.
//

import Foundation

/// One synced resource type and how many records were submitted for it.
struct ProcessingResource {
    let label: String
    let count: Int
}

/// Snapshot captured when sync() completes, driving the simulated processing animation.
struct ProcessingData {
    let total: Int
    let resources: [ProcessingResource]
    let startedAt: Date
}

struct ProcessingSlice {
    let label: String
    let count: Int
    let start: TimeInterval
    let end: TimeInterval
    let completedBefore: Int
}

enum ProcessingResourceState: Equatable {
    case processing
    case processed
}

enum ProcessingProgress: Equatable {
    /// No snapshot, or the snapshot fell outside the processing window (stale).
    case inactive
    case active(total: Int, percent: Int, currentLabel: String, currentState: ProcessingResourceState)
}

enum HealthSyncProcessing {
    /// Processing window the animation is paced to (padded past the ~5-min average).
    static let processingEstimate: TimeInterval = 7 * 60

    /// The fill never reaches 100% on the timer alone - only the real poll
    /// arrival completes it. Keeps the animation honest if the backend runs long.
    static let fillCapPercent = 95

    /// How long a resource shows the "processed" beat before the next one.
    static let resourceDoneBeat: TimeInterval = 1.2

    /// Snapshots older than this multiple of the estimate are treated as stale.
    static let staleFactor: Double = 2

    static func isFreshSnapshot(_ data: ProcessingData, now: Date) -> Bool {
        let elapsed = now.timeIntervalSince(data.startedAt)
        return elapsed >= 0 && elapsed <= processingEstimate * staleFactor
    }

    /// Per-resource time slices of the estimate, record-weighted: a resource
    /// with twice the records of another gets twice the time slice.
    static func computeSlices(_ data: ProcessingData) -> [ProcessingSlice] {
        guard data.total > 0 else { return [] }
        var cursor: TimeInterval = 0
        var recordsBefore = 0
        return data.resources.map { resource in
            let start = cursor
            cursor += (Double(resource.count) / Double(data.total)) * processingEstimate
            let slice = ProcessingSlice(label: resource.label, count: resource.count, start: start, end: cursor, completedBefore: recordsBefore)
            recordsBefore += resource.count
            return slice
        }
    }

    /// Pure: maps a snapshot + a clock value to the current animation state.
    static func computeProgress(_ data: ProcessingData?, now: Date) -> ProcessingProgress {
        guard let data, data.total > 0, !data.resources.isEmpty, isFreshSnapshot(data, now: now) else {
            return .inactive
        }

        let slices = computeSlices(data)
        let elapsed = now.timeIntervalSince(data.startedAt)
        let index = slices.firstIndex { elapsed < $0.end } ?? slices.count - 1
        let slice = slices[index]
        let isLast = index == slices.count - 1

        let sliceDuration = max(slice.end - slice.start, 0.001)
        let sliceProgress = min(1, max(0, (elapsed - slice.start) / sliceDuration))
        let processedRecords = Double(slice.completedBefore) + sliceProgress * Double(slice.count)

        // A non-last resource shows "processed" for a short beat before the
        // next one; the last resource holds at "processing" until real data
        // arrives (there's nothing after it to advance to).
        let inDoneBeat = !isLast && (slice.end - elapsed) <= resourceDoneBeat

        return .active(
            total: data.total,
            percent: min(fillCapPercent, max(0, Int((processedRecords / Double(data.total) * 100).rounded()))),
            currentLabel: slice.label,
            currentState: inDoneBeat ? .processed : .processing
        )
    }

    /// "activitySummary" -> "Activity Summary" - HealthDataType's rawValue is
    /// camelCase (unlike Kotlin's SNAKE_CASE enum names), so this splits on
    /// uppercase boundaries instead of underscores.
    static func formatResourceLabel(_ camelCaseName: String) -> String {
        var words: [String] = []
        var current = ""
        for character in camelCaseName {
            if character.isUppercase, !current.isEmpty {
                words.append(current)
                current = String(character)
            } else {
                current.append(character)
            }
        }
        if !current.isEmpty { words.append(current) }
        return words
            .map { $0.prefix(1).uppercased() + $0.dropFirst().lowercased() }
            .joined(separator: " ")
    }
}
