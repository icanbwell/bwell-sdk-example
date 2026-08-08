//
//  HealthSyncPlaygroundView.swift
//  bwell-swift-ios
//
//  Ported from bwell-sdk-swift/Examples/HealthSyncSampleApp/Sources/PlaygroundView.swift.
//  This demo's primary screen — one card per SDK method, using the shared
//  PlaygroundCard (see Playground/PlaygroundCard.swift). Covers the
//  composition primitives (`connect`/`disconnect`), the HealthSync port's
//  own primitives (`requestPermissions`/`sync`), and the 9 DCON-4451
//  endpoints `ui-platform` uses; the 4 blocked ones render disabled rather
//  than being omitted, so the gaps stay visible instead of silently missing.
//

import BWellSDK
import SwiftUI

struct HealthSyncPlaygroundView: View {
    @ObservedObject var viewModel: HealthSyncPlaygroundViewModel

    var body: some View {
        VStack(spacing: 0) {
            Text("API Playground").font(.title2.bold())
                .foregroundStyle(Color.playgroundHeading)
                .frame(maxWidth: .infinity, alignment: .leading)
                .padding([.horizontal, .top])
            Text("Test bench — run any endpoint independently, raw response shown per card.")
                .font(.footnote).foregroundStyle(.secondary)
                .frame(maxWidth: .infinity, alignment: .leading)
                .padding(.horizontal)

            ScrollView {
                LazyVStack(spacing: 12) {
                    ForEach(HealthSyncPlaygroundEndpoint.allCases) { endpoint in
                        card(for: endpoint)
                    }
                }
                .padding()
            }

            accountActions
        }
        .background(Color(.systemGroupedBackground))
    }

    // MARK: - Account actions (no separate dashboard screen)

    private var accountActions: some View {
        HStack {
            Button("Switch account") { viewModel.openSwitchAccount() }
                .font(.footnote.weight(.medium))
            Spacer()
            Button("Log out") { viewModel.logOut() }
                .font(.footnote.weight(.medium))
                .foregroundStyle(.red)
        }
        .padding()
        .background(Color(.systemBackground))
    }

    // MARK: - Provider dropdown (embedded in the cards that need it)

    private var providerPicker: some View {
        Picker("Provider", selection: $viewModel.selectedDeviceProvider) {
            ForEach(viewModel.deviceProviderOptions, id: \.slug) { provider in
                Text(provider.name).tag(Optional(provider))
            }
        }
        .pickerStyle(.menu)
        .frame(maxWidth: .infinity, alignment: .leading)
        .playgroundFieldStyle()
    }

    // MARK: - Endpoint card

    @ViewBuilder
    private func card(for endpoint: HealthSyncPlaygroundEndpoint) -> some View {
        PlaygroundCard(
            endpoint: endpoint,
            state: viewModel.playgroundResults[endpoint] ?? .idle,
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
