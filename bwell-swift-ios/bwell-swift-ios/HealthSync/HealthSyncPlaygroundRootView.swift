//
//  HealthSyncPlaygroundRootView.swift
//  bwell-swift-ios
//
//  Ported from bwell-sdk-swift/Examples/HealthSyncSampleApp/Sources/ContentView.swift.
//  Routes this demo's own state machine to its matching screen. This flow
//  owns a completely separate BWellClient from the main app's SDKManager —
//  opening it from the Developer section will prompt for a client key +
//  OAuth token even if you're already logged into the main app elsewhere.
//  That's expected: this is a faithful, self-contained port of the internal
//  standalone sample, not a merge into the main app's session. Confirmed
//  safe: BWellClient.initialize() is a plain instance method with no
//  process-wide singleton constraint (only BWellHealthSyncType.configure()
//  has one, and it's never called in this build — see
//  HealthSyncPlaygroundViewModel.swift).
//

import SwiftUI

struct HealthSyncPlaygroundRootView: View {
    @StateObject private var viewModel = HealthSyncPlaygroundViewModel()

    var body: some View {
        VStack(spacing: 0) {
            header

            Group {
                switch viewModel.state {
                case .setup(let error):
                    HealthSyncSetupView(viewModel: viewModel, error: error)
                case .playground:
                    HealthSyncPlaygroundView(viewModel: viewModel)
                case .reauthenticate(let error):
                    HealthSyncReauthenticateView(viewModel: viewModel, error: error)
                }
            }
        }
        .preferredColorScheme(.light)
    }

    private var header: some View {
        ZStack {
            Text("b.well Health Sync SDK")
                .font(.headline)
                .foregroundStyle(Color.playgroundHeading)

            if let backAction {
                HStack {
                    Button(action: backAction) {
                        Image(systemName: "chevron.left")
                            .font(.body.weight(.semibold))
                            .foregroundStyle(Color.accentColor)
                    }
                    .accessibilityLabel("Back")
                    Spacer()
                }
            }
        }
        .padding()
        .background(Color(.systemBackground))
    }

    private var backAction: (() -> Void)? {
        switch viewModel.state {
        case .reauthenticate:
            return viewModel.backToPlayground
        default:
            return nil
        }
    }
}
