//
//  HealthSyncPlaygroundViewModel.swift
//  bwell-swift-ios
//
//  Ported from bwell-sdk-swift/Examples/HealthSyncSampleApp/Sources/HealthSyncViewModel.swift.
//  Drives this demo's self-contained Setup/Playground/Reauthenticate flow —
//  intentionally independent of the main app's SDKManager/session (see
//  HealthSyncPlaygroundRootView.swift), a faithful port of a standalone tool.
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

@MainActor
final class HealthSyncPlaygroundViewModel: ObservableObject {
    @Published var state: SyncUiState = .setup()

    @Published var clientKeyInput = ""
    @Published var tokenInput = ""
    @Published var reauthTokenInput = ""

    @Published var playgroundResults: [HealthSyncPlaygroundEndpoint: PlaygroundCardState] = [:]
    @Published var deviceProviderOptions: [BWell.DeviceProvider] = []
    @Published var selectedDeviceProvider: BWell.DeviceProvider?
    @Published var connectionIdInput = playgroundConnectionId

    private var client: BWellClient?

    // Not SDK surface (sourceIdToConnectionSlug is internal) — hardcoded here
    // the same way a real integrator would, since the demo only ever
    // demonstrates this one source (AggregatorHealthSource.sourceId = "apple-health").
    static let playgroundConnectionId = "apple_health"

    // MARK: - Setup

    /// One-time base-SDK setup: the client key + OAuth token currently held in
    /// `clientKeyInput`/`tokenInput`. Owns its own `BWellClient`, independent
    /// of the main app's `SDKManager` — see HealthSyncPlaygroundRootView.swift
    /// for why that's intentional. Lands directly on the Playground - it's
    /// this demo's one primary screen.
    func setup() {
        let clientKey = clientKeyInput.trimmed
        let token = tokenInput.trimmed
        Task {
            do {
                let client = try BWellClient(config: BWell.SDKConfig(clientKey: clientKey))
                try await client.initialize()
                try await client.authenticate(credentials: .oauth(token: token))

                #if canImport(BWellHealthSyncAdapterAggregator)
                if !BWellHealthSyncType.isConfigured {
                    try BWellHealthSyncType.configure(AggregatorHealthSource())
                }
                #endif

                self.client = client
                state = .playground
                loadPlaygroundDropdownOptions()
            } catch {
                state = .setup(error: "\(error)")
            }
        }
    }

    // How far back to read on-device records for the demo `sync()` card — a
    // demo-app choice (not an SDK default), picked to reliably surface
    // something to show on real devices without a huge upload.
    private static let syncWindowDays = 30
    private static let secondsPerDay: TimeInterval = 86400

    // MARK: - Account switching

    /// The "Switch account" action (in Playground's own footer).
    func openSwitchAccount() {
        state = .reauthenticate()
    }

    /// The "Log Out" action — full logout, back to the Setup screen (client
    /// key + token), not just a new-token reauth. Different from "Switch
    /// account", which keeps the same client/init and only swaps the token.
    func logOut() {
        Task {
            do {
                try await client?.healthSync.disconnect()
            } catch {
                print("disconnect() during logOut() failed: \(error)")
            }
            client = nil
            clientKeyInput = ""
            tokenInput = ""
            resetPlaygroundState()
            state = .setup()
        }
    }

    /// Re-authenticates as a different b.well user. Calls only
    /// `authenticate()`, not `initialize()` — the latter is one-shot per
    /// client instance and the client key doesn't change when switching users.
    ///
    /// Ends the previous user's on-device session first (best-effort, same
    /// as `logOut()`'s teardown) — the on-device HealthSync session lives in
    /// a process-wide singleton independent of the base client's OAuth
    /// token, so re-authenticating alone leaves the old session active.
    /// `endSession()`, not the full `disconnect()`: switching accounts
    /// shouldn't delete the previous user's backend connection, only logOut()
    /// should do that.
    func reauthenticate() {
        let token = reauthTokenInput.trimmed
        Task {
            do {
                try await client?.healthSync.endSession()
            } catch {
                print("endSession() during reauthenticate() failed: \(error)")
            }
            do {
                try await client?.authenticate(credentials: .oauth(token: token))
                resetPlaygroundState()
                reauthTokenInput = ""
                state = .playground
                loadPlaygroundDropdownOptions()
            } catch {
                state = .reauthenticate(error: "\(error)")
            }
        }
    }

    /// The header back-arrow from Reauthenticate — Playground is this demo's
    /// only other screen, so it's the one place "back" can mean.
    func backToPlayground() {
        state = .playground
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
            } catch PlaygroundInputError.missingInput(let message) {
                playgroundResults[endpoint] = .error(message)
            } catch HealthSyncError.notConfigured {
                // No on-device health source is linked in this build (see
                // file header) - vendor-neutral by design, not a bug.
                playgroundResults[endpoint] = .error(
                    "Health Sync on-device sync: some endpoints require a third-party health-data provider integration. Credential provisioning for this integration is currently under consideration - these endpoints will show a 'Health Sync Credentials Required' state until that's finalized."
                )
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
            try await client.healthSync.connect()
            return "connect() completed successfully."

        case .disconnect:
            try await client.healthSync.disconnect()
            return "disconnect() completed successfully."

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

        case .getDeviceMetrics, .getDeviceMetricGroups, .getHealthScore, .getBodySystemScore:
            // Unreachable — guarded by endpoint.blockedReason above.
            throw PlaygroundInputError.missingInput(endpoint.blockedReason ?? "blocked")
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

    /// Clears every Playground result, dropdown option, and selection - all
    /// of it is scoped to whichever user was authenticated when it was
    /// fetched, so it goes stale the moment the account switches.
    private func resetPlaygroundState() {
        playgroundResults = [:]
        deviceProviderOptions = []
        selectedDeviceProvider = nil
    }
}

private extension String {
    var trimmed: String { trimmingCharacters(in: .whitespacesAndNewlines) }
}
