//
//  HealthSyncSetupView.swift
//  bwell-swift-ios
//
//  Ported from bwell-sdk-swift/Examples/HealthSyncSampleApp/Sources/SetupView.swift.
//  First screen of this demo: client key + OAuth token.
//

import SwiftUI

struct HealthSyncSetupView: View {
    @ObservedObject var viewModel: HealthSyncPlaygroundViewModel
    let error: String?

    var body: some View {
        PlaygroundCenteredForm {
            Text("b.well Health Sync — iOS demo")
                .font(.title2.bold())
                .foregroundStyle(Color.playgroundHeading)
                .multilineTextAlignment(.center)

            TextField("Client key", text: $viewModel.clientKeyInput)
                .textFieldStyle(.plain)
                .playgroundFieldStyle()
                .autocorrectionDisabled().textInputAutocapitalization(.never)

            TextField("OAuth token", text: $viewModel.tokenInput)
                .textFieldStyle(.plain)
                .playgroundFieldStyle()
                .autocorrectionDisabled().textInputAutocapitalization(.never)

            if let error {
                Text(error).foregroundStyle(.red).font(.footnote)
                    .multilineTextAlignment(.center)
            }

            Button("Initialize + Authenticate") { viewModel.setup() }
                .buttonStyle(.playgroundPrimary)
        }
    }
}
