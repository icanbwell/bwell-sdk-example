//
//  HealthSyncPlaygroundView.swift
//  bwell-swift-ios
//
//  Ported from bwell-sdk-swift/Examples/HealthSyncSampleApp/Sources/PlaygroundView.swift.
//  This demo's primary screen — one card per SDK method, using the shared
//  PlaygroundCard (see Playground/PlaygroundCard.swift). Covers the
//  composition primitives (`connect`/`disconnect`), the HealthSync port's
//  own primitives (`requestPermissions`/`sync`), and the 9 DCON-4451
//  endpoints `ui-platform` uses; the 5 endpoints that need an on-device
//  adapter render disabled (not omitted) when none is linked, so the gaps
//  stay visible instead of silently missing (see
//  HealthSyncPlaygroundEndpoint.isBlocked).
//

import BWellSDK
import SwiftUI

struct HealthSyncPlaygroundView: View {
    @ObservedObject var viewModel: HealthSyncPlaygroundViewModel

    var body: some View {
        VStack(spacing: 0) {
            VStack(spacing: 6) {
                Text("API Playground").font(.title2.bold())
                    .foregroundStyle(Color.playgroundHeading)
                Text("Test bench — run any endpoint independently, raw response shown per card.")
                    .font(.footnote).foregroundStyle(.secondary)
                    .multilineTextAlignment(.center)
            }
            .frame(maxWidth: .infinity, alignment: .center)
            .padding(.horizontal, 24)
            .padding(.vertical, 20)

            ScrollView {
                LazyVStack(spacing: 20) {
                    ForEach(HealthSyncPlaygroundGroup.allCases, id: \.self) { group in
                        GroupBox(label: header(for: group)) {
                            VStack(spacing: 12) {
                                if group == .readData {
                                    PlaygroundNoteBanner(text: "Run connect, requestPermissions, and sync first (Mobile Sync group above) — Read Data reflects whatever was last synced. Data appears within 5 min.")
                                }
                                ForEach(HealthSyncPlaygroundEndpoint.allCases.filter { $0.group == group }) { endpoint in
                                    card(for: endpoint)
                                }
                            }
                            .padding(.top, 8)
                        }
                        .groupBoxStyle(.playground)
                    }
                }
                .padding()
            }
        }
        .background(Color(.systemGroupedBackground))
        // Re-checks for newly-synced data every time this screen (re-)appears -
        // e.g. jumping back to the Playground tab after syncing from the
        // Dashboard's Metrics/Body Score tabs, which this ViewModel has no
        // other way to observe. Mirrors Kotlin's HealthSyncPlaygroundScreen,
        // which calls the equivalent refresh from a screen-level
        // LaunchedEffect(Unit) rather than from the individual pickers.
        .onAppear { viewModel.refreshDeviceDataOptions() }
    }

    // MARK: - Group header

    private func header(for group: HealthSyncPlaygroundGroup) -> some View {
        Text(group.rawValue.uppercased())
            .font(.caption.weight(.semibold))
            .foregroundStyle(.secondary)
    }

    // MARK: - Provider dropdown (embedded in the cards that need it)

    @ViewBuilder
    private var providerPicker: some View {
        if !viewModel.deviceProviderOptions.isEmpty {
            Picker("Provider", selection: $viewModel.selectedDeviceProvider) {
                ForEach(viewModel.deviceProviderOptions, id: \.slug) { provider in
                    Text(provider.name).tag(Optional(provider))
                }
            }
            .pickerStyle(.menu)
            .frame(maxWidth: .infinity, alignment: .leading)
            .playgroundFieldStyle()
            .contentShape(Rectangle())
        }
    }

    // MARK: - Device metrics code dropdown

    /// Renders nothing at all while there are no options yet (no "All"
    /// placeholder) - matches Kotlin's LabeledDropdown, which only ever
    /// appears once there's real data to choose from. Fetching itself lives
    /// in HealthSyncPlaygroundView.body's screen-level `.onAppear`, not here.
    @ViewBuilder
    private var deviceMetricsCodePicker: some View {
        if !viewModel.deviceMetricsCodeOptions.isEmpty {
            Picker("Code", selection: $viewModel.selectedDeviceMetricsCode) {
                Text("All").tag(Optional<String>.none)
                ForEach(viewModel.deviceMetricsCodeOptions) { option in
                    Text(option.label).tag(Optional(option.code))
                }
            }
            .pickerStyle(.menu)
            .frame(maxWidth: .infinity, alignment: .leading)
            .playgroundFieldStyle()
            .contentShape(Rectangle())
        }
    }

    // MARK: - Body system dropdown

    /// Same "nothing until options exist" rule as deviceMetricsCodePicker above.
    @ViewBuilder
    private var bodySystemPicker: some View {
        if !viewModel.bodySystemOptions.isEmpty {
            Picker("Body system", selection: $viewModel.selectedBodySystemId) {
                // Just the human-readable title here - the code itself
                // (e.g. "cardiovascular") barely differs from its title
                // ("Cardiovascular"), so "code - title" reads as
                // redundant noise for this one, unlike the
                // device-metrics code picker.
                ForEach(viewModel.bodySystemOptions) { option in
                    Text(option.display ?? option.code).tag(Optional(option.code))
                }
            }
            .pickerStyle(.menu)
            .frame(maxWidth: .infinity, alignment: .leading)
            .playgroundFieldStyle()
            .contentShape(Rectangle())
        }
    }

    // MARK: - Endpoint card

    /// Endpoints whose only usable options come from another endpoint's live
    /// response (not hardcoded) - disabled rather than left to fail/confirm
    /// with nothing to select, until at least one option is available. The
    /// "why" lives in each endpoint's parameterInfo (the info sheet), not
    /// inline text here.
    private func isRunDisabled(for endpoint: HealthSyncPlaygroundEndpoint) -> Bool {
        switch endpoint {
        case .getDeviceMetrics: return viewModel.deviceMetricsCodeOptions.isEmpty
        case .getBodySystemScore: return viewModel.bodySystemOptions.isEmpty
        default: return false
        }
    }

    @ViewBuilder
    private func card(for endpoint: HealthSyncPlaygroundEndpoint) -> some View {
        PlaygroundCard(
            endpoint: endpoint,
            state: viewModel.playgroundResults[endpoint] ?? .idle,
            runDisabled: isRunDisabled(for: endpoint),
            onRun: { viewModel.runPlaygroundEndpoint(endpoint) }
        ) {
            // Each card embeds only the picker(s)/input it actually needs.
            switch endpoint {
            case .getOauthUrl, .getDeviceProviderStatus:
                providerPicker
            case .deleteConnection:
                providerPicker
                    .onChange(of: viewModel.selectedDeviceProvider) { _, provider in
                        if let provider { viewModel.connectionIdInput = provider.slug }
                    }
                TextField("connectionId", text: $viewModel.connectionIdInput)
                    .textFieldStyle(.plain)
                    .playgroundFieldStyle()
                    .autocorrectionDisabled().textInputAutocapitalization(.never)
            case .getBodySystemScore:
                bodySystemPicker
            case .getDeviceMetrics:
                deviceMetricsCodePicker
            default:
                EmptyView()
            }
        }
    }
}

extension BWell.DeviceProvider: @retroactive Equatable, @retroactive Hashable {
    public static func == (lhs: Self, rhs: Self) -> Bool { lhs.slug == rhs.slug }
    public func hash(into hasher: inout Hasher) { hasher.combine(slug) }
}
