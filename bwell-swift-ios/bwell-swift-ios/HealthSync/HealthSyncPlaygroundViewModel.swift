//
//  HealthSyncPlaygroundViewModel.swift
//  bwell-swift-ios
//
//  Adapted from bwell-sdk-swift/Examples/HealthSyncSampleApp/Sources/HealthSyncViewModel.swift.
//  Unlike the internal sample, this demo does not own its own BWellClient or
//  login flow - it attaches to the app's existing SDKManager session (see
//  HealthSyncPlaygroundRootView.swift). Whether the visitor is the current
//  logged-in user is the app's own concern, decided before this screen is
//  ever reachable, not something re-checked here.
//
//  Vendor-neutrality: `AggregatorHealthSource` (the on-device health source
//  implementation) lives in a package that pulls a third-party vendor SDK
//  through b.well's private Artifactory registry — this public repo cannot
//  resolve it without credentials, so it is never added as a dependency here.
//  The `#if canImport(...)` guard below means `BWellHealthSyncType.configure`
//  is simply never called in this build: every on-device card's
//  `client.healthSync` access throws the SDK's own vendor-neutral
//  `HealthSyncError.notConfigured` (see `runPlaygroundEndpoint`), which this
//  file maps to a plain-language fallback. A developer with real credentials
//  can add the adapter package locally via Xcode; this exact same source
//  then activates automatically, no further edits needed.
//

import BWellHealthSync
#if canImport(BWellHealthSyncAdapterAggregator)
import BWellHealthSyncAdapterAggregator
#endif
import BWellSDK
import Foundation

/// A dynamically-fetched dropdown option identified by a code, with an
/// optional human-readable meaning (e.g. code "vital-signs", display "Vital
/// Signs"). Shared shape for both the getDeviceMetrics code picker and the
/// getBodySystemScore body-system picker - both are "code - meaning" pairs
/// sourced live from another endpoint's response, not hardcoded.
struct PlaygroundCodeOption: Identifiable, Hashable {
    let code: String
    let display: String?
    var id: String { code }
    var label: String { display.map { "\(code) - \($0)" } ?? code }
}

@MainActor
final class HealthSyncPlaygroundViewModel: ObservableObject {
    @Published var playgroundResults: [HealthSyncPlaygroundEndpoint: PlaygroundCardState] = [:]
    @Published var deviceProviderOptions: [BWell.DeviceProvider] = []
    @Published var selectedDeviceProvider: BWell.DeviceProvider?
    @Published var connectionIdInput = playgroundConnectionId
    @Published var deviceMetricsCodeOptions: [PlaygroundCodeOption] = []
    @Published var selectedDeviceMetricsCode: String?
    @Published var bodySystemOptions: [PlaygroundCodeOption] = []
    @Published var selectedBodySystemId: String?

    private var client: BWellClient?

    // Not SDK surface (sourceIdToConnectionSlug is internal) — hardcoded here
    // the same way a real integrator would, since the demo only ever
    // demonstrates this one source (AggregatorHealthSource.sourceId = "apple-health").
    static let playgroundConnectionId = "apple_health"

    // How far back to read on-device records for the demo `sync()` card — a
    // demo-app choice (not an SDK default), picked to reliably surface
    // something to show on real devices without a huge upload.
    private static let syncWindowDays = 30
    private static let secondsPerDay: TimeInterval = 86400

    /// Attaches the app's already-authenticated client - called once from
    /// HealthSyncPlaygroundRootView, which owns the actual session. Also
    /// where the on-device health source gets configured, when the adapter
    /// package is present (see file header).
    func attach(client: BWellClient) {
        guard self.client == nil else { return }
        self.client = client

        #if canImport(BWellHealthSyncAdapterAggregator)
        if !BWellHealthSyncType.isConfigured {
            try? BWellHealthSyncType.configure(AggregatorHealthSource())
        }
        #endif

        loadPlaygroundDropdownOptions()
    }

    // MARK: - Playground

    /// Silently (re-)populates the provider dropdown from its natural parent
    /// endpoint. Failures are silent - this is a dev-tooling convenience,
    /// not the endpoint the user is actually testing.
    private func loadPlaygroundDropdownOptions() {
        Task {
            do {
                if let result = try await client?.device.getDeviceProviders() {
                    deviceProviderOptions = result.providers
                    if selectedDeviceProvider == nil {
                        selectedDeviceProvider = result.providers.first
                    }
                }
            } catch {
                print("getDeviceProviders() dropdown prefetch failed: \(error)")
            }
        }
    }

    /// Re-populates both dropdowns sourced from another endpoint's live
    /// response - called right after a successful `sync` (see
    /// runPlaygroundEndpoint) so newly-synced data shows up on this same
    /// screen without waiting for the picker's own `.task` to remount.
    /// Mirrors Kotlin's HealthSyncPlaygroundViewModel.refreshDeviceDataOptions().
    func refreshDeviceDataOptions() {
        loadDeviceMetricsCodeOptions()
        loadBodySystemOptions()
    }

    /// (Re-)populates the getDeviceMetrics code dropdown from
    /// getDeviceMetricsGroups() - called each time that card appears, since
    /// which codes exist depends on what's been synced (Mobile Sync group
    /// above), which can change after this screen's initial load.
    func loadDeviceMetricsCodeOptions() {
        Task {
            do {
                guard let result = try await client?.health.getDeviceMetricsGroups(nil) else { return }
                var seen = Set<String>()
                let options = (result.resources ?? []).compactMap { group -> PlaygroundCodeOption? in
                    guard let code = group.coding?.code, seen.insert(code).inserted else { return nil }
                    return PlaygroundCodeOption(code: code, display: group.coding?.display)
                }.sorted { $0.code < $1.code }
                deviceMetricsCodeOptions = options
                if let selectedDeviceMetricsCode, !options.contains(where: { $0.code == selectedDeviceMetricsCode }) {
                    self.selectedDeviceMetricsCode = nil
                }
            } catch {
                print("getDeviceMetricsGroups() dropdown prefetch failed: \(error)")
            }
        }
    }

    /// (Re-)populates the getBodySystemScore body-system dropdown from
    /// getHealthScore()'s contributing body systems - called each time that
    /// card appears, since which body systems have a score depends on how
    /// much has been synced, which can change after this screen's initial load.
    func loadBodySystemOptions() {
        Task {
            do {
                guard let result = try await client?.health.getHealthScore() else { return }
                var seen = Set<String>()
                let options = (result.resource?.bodySystems ?? []).compactMap { summary -> PlaygroundCodeOption? in
                    guard let id = summary.bodySystemId, seen.insert(id).inserted else { return nil }
                    return PlaygroundCodeOption(code: id, display: summary.title)
                }.sorted { $0.code < $1.code }
                bodySystemOptions = options
                if let selectedBodySystemId, !options.contains(where: { $0.code == selectedBodySystemId }) {
                    self.selectedBodySystemId = nil
                }
                // Unlike the device-metrics code picker, there's no "All"
                // option here - getBodySystemScore always needs exactly one
                // id, so default to the first one once options load.
                if selectedBodySystemId == nil {
                    selectedBodySystemId = options.first?.code
                }
            } catch {
                print("getHealthScore() dropdown prefetch failed: \(error)")
            }
        }
    }

    /// Runs one playground endpoint independently — each card has its own
    /// loading/result state.
    func runPlaygroundEndpoint(_ endpoint: HealthSyncPlaygroundEndpoint) {
        if let reason = endpoint.blockedReason {
            playgroundResults[endpoint] = .error(reason)
            return
        }
        playgroundResults[endpoint] = .loading
        Task {
            guard let client else {
                playgroundResults[endpoint] = .error("SDK not initialized")
                return
            }
            do {
                let description = try await run(endpoint, client: client)
                playgroundResults[endpoint] = .success(description)
                if endpoint == .sync {
                    refreshDeviceDataOptions()
                }
            } catch PlaygroundInputError.missingInput(let message) {
                playgroundResults[endpoint] = .error(message)
            } catch HealthSyncError.notConfigured {
                // Shouldn't be reachable - the 5 gated endpoints' Run
                // buttons are pre-emptively disabled via isBlocked/
                // blockedReason above. Kept as a defensive fallback, same
                // copy as that locked-state message.
                playgroundResults[endpoint] = .error(HealthSyncPlaygroundEndpoint.notConfiguredMessage)
            } catch {
                playgroundResults[endpoint] = .error("\(error)")
            }
        }
    }

    private enum PlaygroundInputError: Error {
        case missingInput(String)
    }

    private func run(_ endpoint: HealthSyncPlaygroundEndpoint, client: BWellClient) async throws -> String {
        switch endpoint {
        case .connect:
            try await reconcileHealthSyncSessionIfNeeded(client: client, connectionId: Self.playgroundConnectionId)
            try await client.healthSync.connect()
            return "connect() completed successfully."

        case .disconnect:
            try await client.healthSync.disconnect()
            return "disconnect() completed successfully."

        case .getCurrentUser:
            guard let user = try await client.healthSync.currentUser() else {
                return "No active session."
            }
            return "userId: \(user.userId)\norganizationId: \(user.organizationId)"

        case .requestPermissions:
            try await client.healthSync.requestPermissions(Set(HealthDataType.allCases))
            return "requestPermissions() completed successfully for \(HealthDataType.allCases.count) types."

        case .sync:
            let now = Date()
            let windowStart = now.addingTimeInterval(-Double(Self.syncWindowDays) * Self.secondsPerDay)
            let window = try DateRange(from: windowStart, to: now)
            let counts = try await client.healthSync.sync(Set(HealthDataType.allCases), window: window)
            let perTypeSummary = counts.perType
                .map { "\($0.key.rawValue): \($0.value)" }
                .sorted()
                .joined(separator: ", ")
            return "Synced \(counts.total) records.\nPer type: \(perTypeSummary)"

        case .getOauthUrl:
            guard let slug = selectedDeviceProvider?.slug else {
                throw PlaygroundInputError.missingInput("Select a provider first")
            }
            let result = try await client.connection.getOAuthURL(.init(connectionId: slug))
            return prettyJSON(result)

        case .setupMobileSync:
            let result = try await client.device.setupMobileSync(.init(connectionId: Self.playgroundConnectionId))
            return prettyJSON(result)

        case .getDeviceUserStatus:
            let result = try await client.device.getDeviceUserStatus(.init(connectionId: Self.playgroundConnectionId))
            return prettyJSON(result)

        case .getDeviceProviderStatus:
            guard let slug = selectedDeviceProvider?.slug else {
                throw PlaygroundInputError.missingInput("Select a provider first")
            }
            let result = try await client.device.getDeviceProviderStatus(.init(connectionId: slug))
            return prettyJSON(result)

        case .getDeviceProviders:
            let result = try await client.device.getDeviceProviders()
            return prettyJSON(result)

        case .deleteConnection:
            // Exercises the raw `connection.deleteConnection` ATS call only -
            // not the full `client.disconnect()` composition (session
            // teardown + connection delete), which has its own card above.
            let connectionId = connectionIdInput.trimmed
            guard !connectionId.isEmpty else {
                throw PlaygroundInputError.missingInput("Enter a connection id first")
            }
            let result = try await client.connection.deleteConnection(.init(connectionId: connectionId))
            return prettyJSON(result)

        case .getDeviceMetrics:
            let groupCode = selectedDeviceMetricsCode.map { BWell.SearchToken(value: .init(code: $0)) }
            let result = try await client.health.getDeviceMetrics(.init(page: 0, groupCode: groupCode))
            return prettyJSON(result)

        case .getHealthScore:
            let result = try await client.health.getHealthScore()
            return prettyJSON(result)

        case .getBodySystemScore:
            guard let bodySystemId = selectedBodySystemId else {
                throw PlaygroundInputError.missingInput("Select a body system first")
            }
            let result = try await client.health.getBodySystemScore(.init(bodySystemId: bodySystemId))
            return prettyJSON(result)

        case .getDeviceMetricsGroups:
            let result = try await client.health.getDeviceMetricsGroups(.init(page: 0))
            return prettyJSON(result)
        }
    }

    // Playground-only, for visibility while testing - pretty-printed JSON of
    // whatever a card's response holds, not an SDK-wide serialization format.
    private static let playgroundJSONEncoder: JSONEncoder = {
        let encoder = JSONEncoder()
        encoder.outputFormatting = [.prettyPrinted, .sortedKeys]
        return encoder
    }()

    private func prettyJSON(_ value: some Encodable) -> String {
        guard let data = try? Self.playgroundJSONEncoder.encode(value),
              let json = String(data: data, encoding: .utf8) else {
            return String(describing: value)
        }
        // Some URL-shaped fields (e.g. Coding.system) come through already
        // containing literal backslash-escaped slashes - purely cosmetic for
        // this display-only view, so unescape for readability.
        return json.replacingOccurrences(of: "\\/", with: "/")
    }
}

private extension String {
    var trimmed: String { trimmingCharacters(in: .whitespacesAndNewlines) }
}
