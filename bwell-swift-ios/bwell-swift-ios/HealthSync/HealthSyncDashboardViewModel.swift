//
//  HealthSyncDashboardViewModel.swift
//  bwell-swift-ios
//
//  Backs HealthSyncDashboardView (the polished Metrics/Body Score tabs shown
//  when an on-device adapter is configured) — separate from
//  HealthSyncPlaygroundViewModel, which is about raw endpoint testing, not
//  display. Both tabs share the same "fetch, and if empty let the user
//  trigger sync() then poll until data shows up" shape, factored into one
//  generic helper below instead of duplicating it per tab.
//

import BWellHealthSync
import BWellSDK
import Foundation

/// Live progress while a sync-then-poll is in flight — shown in the UI so
/// "syncing" isn't an opaque spinner. `processingData` is nil only in the
/// brief window before sync() itself has returned (we don't know per-type
/// record counts yet) - once set, the view ticks its own simulated-
/// processing clock against it (see HealthSyncProcessing) rather than this
/// struct carrying a live percent itself.
struct SyncProgress {
    var processingData: ProcessingData?
}

enum SyncGatedState<Value> {
    case loading
    case empty(errorMessage: String? = nil)
    case syncing(SyncProgress)
    case loaded(Value)
}

/// One card's worth of grid data - `DeviceMetricsGroup.id` is optional (a
/// server-provided FHIR id, not guaranteed), so identity is index-qualified
/// rather than relying on it directly, same rationale as the old List view.
struct MetricGridItem: Identifiable {
    let id: String
    let group: BWell.DeviceMetricsGroup
}

/// `BodySystemSummary` has no `id` of its own - `bodySystemId` stands in,
/// with an index fallback in the unlikely case it's missing.
struct BodySystemGridItem: Identifiable {
    let id: String
    let system: BWell.BodySystemSummary
}

/// One category section in the Metrics grid.
struct MetricCategoryGroup: Identifiable {
    let id: String
    let label: String
    let items: [MetricGridItem]
}

/// Groups device-metrics cards by their FHIR `category` coding whose system
/// is b.well's display-group system - ported from ui-platform's mfe-devices
/// groupMetricsByDisplayGroup (lib/metrics/utils.ts). Anything with no
/// matching coding falls into a single "Other" bucket rather than being
/// dropped, mirroring that same fallback.
private let displayGroupSystem = "https://www.icanbwell.com/display-group"

func groupMetricsByCategory(_ groups: [BWell.DeviceMetricsGroup]) -> [MetricCategoryGroup] {
    var order: [String] = []
    var labels: [String: String] = [:]
    var items: [String: [MetricGridItem]] = [:]

    for (index, group) in groups.enumerated() {
        let coding = group.category?
            .flatMap { $0.coding ?? [] }
            .first { $0.system == displayGroupSystem }
        let code = coding?.code ?? "other"
        let item = MetricGridItem(id: "\(code)-\(index)", group: group)

        if items[code] != nil {
            items[code]?.append(item)
        } else {
            order.append(code)
            labels[code] = coding?.display ?? "Other"
            items[code] = [item]
        }
    }

    return order.map { code in
        MetricCategoryGroup(id: code, label: labels[code] ?? "Other", items: items[code] ?? [])
    }
}

@MainActor
final class HealthSyncDashboardViewModel: ObservableObject {
    @Published var metricsState: SyncGatedState<[BWell.DeviceMetricsGroup]> = .loading
    @Published var bodyScoreState: SyncGatedState<BWell.HealthScore> = .loading

    private var client: BWellClient?

    // How far back to sync when the user taps "Sync with b.well" - same
    // window as the Playground's own sync() card.
    private static let syncWindowDays = 30
    private static let secondsPerDay: TimeInterval = 86400

    // Server-side processing after sync() isn't instant (matches the Read
    // Data note elsewhere: "Data appears within 5 min") - poll instead of a
    // single re-fetch, but give up rather than polling forever.
    private static let pollInterval: Duration = .seconds(10)
    private static let maxPollAttempts = 30

    func attach(client: BWellClient) {
        guard self.client == nil else { return }
        self.client = client
        Task { await refreshMetrics() }
        Task { await refreshBodyScore() }
    }

    func refreshMetrics() async {
        do {
            let groups = try await fetchMetrics()
            metricsState = groups.isEmpty ? .empty() : .loaded(groups)
        } catch {
            metricsState = .empty()
        }
    }

    func refreshBodyScore() async {
        do {
            if let score = try await fetchBodyScore() {
                bodyScoreState = .loaded(score)
            } else {
                bodyScoreState = .empty()
            }
        } catch {
            bodyScoreState = .empty()
        }
    }

    func syncMetrics() {
        Task {
            let outcome = await triggerSyncThenPoll(
                fetch: fetchMetrics,
                isEmpty: \.isEmpty,
                onProgress: { self.metricsState = .syncing($0) }
            )
            switch outcome {
            case .found(let groups): metricsState = .loaded(groups)
            case .gaveUp(let message): metricsState = .empty(errorMessage: message)
            }
        }
    }

    func syncBodyScore() {
        Task {
            let outcome = await triggerSyncThenPoll(
                fetch: fetchBodyScore,
                isEmpty: { $0 == nil },
                onProgress: { self.bodyScoreState = .syncing($0) }
            )
            switch outcome {
            case .found(let score): if let score { bodyScoreState = .loaded(score) }
            case .gaveUp(let message): bodyScoreState = .empty(errorMessage: message)
            }
        }
    }

    private func fetchMetrics() async throws -> [BWell.DeviceMetricsGroup] {
        guard let client else { return [] }
        return try await client.health.getDeviceMetricsGroups(nil).resources ?? []
    }

    private func fetchBodyScore() async throws -> BWell.HealthScore? {
        guard let client else { return nil }
        return try await client.health.getHealthScore().resource
    }

    private enum PollOutcome<Value> {
        case found(Value)
        case gaveUp(errorMessage: String?)
    }

    /// Triggers a real sync() over the standard window, captures its actual
    /// per-type record counts as `ProcessingData` and reports it via
    /// `onProgress` immediately, then polls `fetch` - real completion
    /// detection, independent of the simulated processing clock the view
    /// ticks against - until it returns a non-empty value or
    /// `maxPollAttempts` is reached. A sync() failure is terminal (surfaced,
    /// not silently swallowed) rather than polling blind against a call that
    /// never ran.
    private func triggerSyncThenPoll<Value>(
        fetch: @escaping () async throws -> Value,
        isEmpty: @escaping (Value) -> Bool,
        onProgress: @escaping (SyncProgress) -> Void
    ) async -> PollOutcome<Value> {
        onProgress(SyncProgress())
        var recordsSynced = 0

        guard let client else { return .gaveUp(errorMessage: nil) }

        do {
            // "Sync with b.well" is meant to be a single self-contained
            // action from a completely fresh install (no prior connect() /
            // requestPermissions() from the Playground's Mobile Sync group) -
            // connect() is idempotent for the same user/session (see
            // AggregatorHealthSource.replaceOrKeepSession), so it's safe to
            // always run the full sequence here rather than assume it
            // already happened elsewhere.
            try await reconcileHealthSyncSessionIfNeeded(
                client: client,
                connectionId: HealthSyncPlaygroundViewModel.playgroundConnectionId
            )
            try await client.healthSync.connect()
            try await client.healthSync.requestPermissions(Set(HealthDataType.allCases))

            let now = Date()
            let windowStart = now.addingTimeInterval(-Double(Self.syncWindowDays) * Self.secondsPerDay)
            let window = try DateRange(from: windowStart, to: now)
            let counts = try await client.healthSync.sync(Set(HealthDataType.allCases), window: window)
            recordsSynced = counts.total
            onProgress(SyncProgress(processingData: Self.processingData(from: counts)))
        } catch {
            return .gaveUp(errorMessage: "Sync failed: \(Self.describe(error))")
        }

        for attempt in 1...Self.maxPollAttempts {
            if let value = try? await fetch(), !isEmpty(value) {
                return .found(value)
            }
            // Skip the pacing delay after the last attempt - there's no next
            // attempt to pace towards, so it was just a dead 10s wait
            // immediately before the give-up message.
            if attempt < Self.maxPollAttempts {
                try? await Task.sleep(for: Self.pollInterval)
            }
        }

        let stillEmptyMessage = recordsSynced == 0
            ? "Synced 0 records from your device - nothing to process. Check connect/requestPermissions ran first."
            : "Synced \(recordsSynced) records, but b.well hasn't finished processing them yet. Try again shortly."
        return .gaveUp(errorMessage: stillEmptyMessage)
    }

    /// Only resource types with records synced feed the processing animation.
    private static func processingData(from counts: SyncCounts) -> ProcessingData? {
        let resources = counts.perType
            .filter { $0.value > 0 }
            .map { ProcessingResource(label: HealthSyncProcessing.formatResourceLabel($0.key.rawValue), count: $0.value) }
        guard !resources.isEmpty else { return nil }
        return ProcessingData(total: resources.reduce(0) { $0 + $1.count }, resources: resources, startedAt: Date())
    }

    /// `.localizedDescription` on HealthSyncError's `.unknown`/`.network`/etc.
    /// cases just gives NSError's generic "The operation couldn't be
    /// completed" - useless for diagnosing what actually went wrong. Unwrap
    /// one level to the real underlying error instead.
    private static func describe(_ error: Error) -> String {
        switch error {
        case HealthSyncError.unknown(let underlying),
             HealthSyncError.network(let underlying),
             HealthSyncError.sourceUnavailable(let underlying),
             HealthSyncError.provisioningFailed(let underlying),
             HealthSyncError.permissionDenied(let underlying):
            return String(describing: underlying)
        default:
            return String(describing: error)
        }
    }
}
